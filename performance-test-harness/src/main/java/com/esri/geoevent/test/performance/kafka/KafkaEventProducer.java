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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class KafkaEventProducer extends ProducerBase
{
  private String brokerList;
  private String topic;
  private String acks;
  private Producer<String, String> producer;
    
  @Override
  public synchronized void init(Config config) throws TestException
  {
  	super.init(config);
    try
    {      
      brokerList = config.getPropertyValue("brokerList", "localhost:9092");
      topic = config.getPropertyValue("topic", "default-topic");
      acks = config.getPropertyValue("requiredAcks", "1");
      
      Properties kprops = new Properties();
      kprops.put("metadata.broker.list", brokerList);
      kprops.put("serializer.class", "kafka.serializer.StringEncoder");
      kprops.put("request.required.acks", acks);
      kprops.put("partitioner.class", SimplePartitioner.class.getCanonicalName());
      kprops.put("producer.type", "async");
      kprops.put("queue.buffering.max.ms", "1000");
      kprops.put("batch.num.messages", "2000");
       
      ProducerConfig producerConfig = new ProducerConfig(kprops);
      
      producer = new Producer<String, String>(producerConfig);
    }
    catch (Throwable error)
    {
			throw new TestException( ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error );
    }
  } 

  @Override
  public void validate() throws TestException
  {
  	super.validate();
    if (producer == null)
      throw new TestException("Kafka producer is not created.");
    if (events.isEmpty())
      throw new TestException("KafkaEventProducer is missing events to produce.");
  }

  public int sendEvents(int index, int numEventsToSend)
  {
    List<KeyedMessage<String, String>> messages = new ArrayList<KeyedMessage<String, String>>();
    int eventIndex = index;
    try
    {
      for (int i=0; i < numEventsToSend; i++)
      {
        if (eventIndex == events.size())
          eventIndex = 0;
        String message = events.get(eventIndex++);
        messages.add(new KeyedMessage<String, String>(topic, message.substring(0, message.indexOf(",")), message));
//        messages.add(new KeyedMessage<String, String>(topic, thisEvent.split(",")[0], thisEvent));
//        messages.add(new KeyedMessage(topic, events.get(eventIndex++)));
        messageSent(message);
        if (running.get() == false)
          break;
      }
      producer.send(messages);
    }
    catch(Exception error)
    {
      error.printStackTrace();
    }
    return eventIndex;
  }

  @Override
  public void destroy()
  {
    super.destroy();
    events.clear();
    try
    {
      producer.close();
    }
    catch (Exception e)
    {
      ;
    }
    producer = null;
  }
}
