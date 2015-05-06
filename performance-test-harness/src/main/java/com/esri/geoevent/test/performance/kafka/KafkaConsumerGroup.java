/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.test.performance.kafka;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.esri.geoevent.test.performance.MessageListener;
import com.esri.geoevent.test.performance.provision.GeoEventProvisioner;
import com.esri.ges.core.geoevent.GeoEvent;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumerGroup
{
	private final ConsumerConnector	consumer;
	private final String						topic;
	private ExecutorService					executor;
	private MessageListener														messageListener;
	private boolean receiveGeoEvent;

	public KafkaConsumerGroup(String a_zookeeper, String a_groupId, String a_topic, boolean receiveGeoEvent, MessageListener messageListener)
	{
		consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig(a_zookeeper, a_groupId));
		this.topic = a_topic;
		this.messageListener = messageListener;
		this.receiveGeoEvent = receiveGeoEvent;
	}

	public void setMessageListener(MessageListener messageListener)
	{
		this.messageListener = messageListener;
	}

	public void run(int numThreads)
	{
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, new Integer(numThreads));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

		// now launch all the threads
		//
		executor = Executors.newFixedThreadPool(numThreads);

		// now create an object to consume the messages
		//
		int threadNumber = 0;
		for (final KafkaStream stream : streams)
		{
			executor.submit(new ConsumerThread(stream, receiveGeoEvent, threadNumber));
			threadNumber++;
		}
	}

	private ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId)
	{
		Properties props = new Properties();
		props.put("zookeeper.connect", a_zookeeper);
		props.put("group.id", a_groupId);
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");

		return new ConsumerConfig(props);
	}

	public void shutdown()
	{
		if (consumer != null)
			consumer.shutdown();
		if (executor != null)
			executor.shutdown();
		try
		{
			if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS))
			{
				System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
			}
		}
		catch (InterruptedException e)
		{
			System.out.println("Interrupted during shutdown, exiting uncleanly");
		}
	}

	private class ConsumerThread implements Runnable
	{
		private KafkaStream	m_stream;
		private int					m_threadNumber;
		private boolean m_receiveGeoEvent;

		public ConsumerThread(KafkaStream a_stream, boolean receiveGeoEvent, int a_threadNumber)
		{
			m_threadNumber = a_threadNumber;
			m_stream = a_stream;
			m_receiveGeoEvent = receiveGeoEvent;
		}

		public void run()
		{
			//System.out.print("Consumer thread started");
			ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
			while (it.hasNext())
			{
				
				String message = null;
				if(m_receiveGeoEvent)
				{
					byte[] bytes = it.next().message();
					ByteArrayInputStream bis = null;
		      ObjectInputStream ois = null;
		      try
		      {
		        bis = new ByteArrayInputStream(bytes);
		        ois = new ObjectInputStream (bis);
		        Object obj = ois.readObject();
		        
		        if (obj instanceof GeoEvent) 
		        {
		        	message = (String)((GeoEvent)obj).getField(0);
		        	//System.out.println("received geoevent - " + message);
		        }
		        if(obj instanceof String)
		        {
		        	message = new String(bytes);
		        	//System.out.println("received String");
		        }
		      }
		      catch (Exception e)
		      {
		      	//System.out.println("didn't received geoevent");
		      	e.printStackTrace();
		        message = null;
		      }
		      finally
		      {
		      	IOUtils.closeQuietly(ois);
	          IOUtils.closeQuietly(bis);
		      }
				}
				else
					 message =  new String(it.next().message());
				
				if (message != null && messageListener != null)
					messageListener.handleMessage(message);
				//System.out.println(message);
			}
		}
	}

}
