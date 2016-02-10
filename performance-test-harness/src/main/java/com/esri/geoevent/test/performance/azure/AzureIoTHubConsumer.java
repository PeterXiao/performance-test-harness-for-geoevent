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
package com.esri.geoevent.test.performance.azure;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class AzureIoTHubConsumer extends ConsumerBase
{
	// connection properties
	private String															eventHubNamespace						= "";
	private String															eventHubName								= "";
	private String															eventHubSharedAccessKeyName	= "";
	private String															eventHubSharedAccessKey			= "";
	private int																	eventHubNumberOfPartitions	= 4;
	private PartitionReceiver[]									receivers;
	private EventHubClient[]										ehClients;
	private ConcurrentHashMap<String, Integer>	receivedEventsByPartition;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		logAmqp();
//		if (receivers == null)
//		{
//		Runtime.getRuntime().addShutdownHook(new Thread(()->shutdown()));
			System.out.println("Initializing Consumer...");
		
			receivedEventsByPartition = new ConcurrentHashMap<>();

			eventHubNamespace = config.getPropertyValue("eventHubNamespace");
			eventHubName = config.getPropertyValue("eventHubName");
			eventHubSharedAccessKeyName = config.getPropertyValue("eventHubSharedAccessKeyName");
			eventHubSharedAccessKey = config.getPropertyValue("eventHubSharedAccessKey");
			eventHubNumberOfPartitions = Integer.parseInt(config.getPropertyValue("eventHubNumberOfPartitions", String.valueOf(4)));

			if (eventHubNamespace == null)
				throw new TestException("Azure Iot Hub event consumer ERROR: 'eventHubNamespace' property must be specified");
			if (eventHubName == null)
				throw new TestException("Azure Iot Hub event consumer ERROR: 'eventHubName' property must be specified");
			if (eventHubSharedAccessKeyName == null)
				throw new TestException("Azure Iot Hub event consumer ERROR: 'eventHubSharedAccessKeyName' property must be specified");
			if (eventHubSharedAccessKey == null)
				throw new TestException("Azure Iot Hub event consumer ERROR: 'eventHubSharedAccessKey' property must be specified");

			try
			{
				receivers = new PartitionReceiver[eventHubNumberOfPartitions];
				ehClients = new EventHubClient[eventHubNumberOfPartitions];
				ConnectionStringBuilder connStr = new ConnectionStringBuilder(eventHubNamespace, eventHubName, eventHubSharedAccessKeyName, eventHubSharedAccessKey);

				for (int i = 0; i < eventHubNumberOfPartitions; i++)
				{
					receivedEventsByPartition.put(Integer.toString(i), 0);
					EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString(), true).get();
					if (ehClient != null)
					{
						ehClients[i] = ehClient;
						System.out.println("Created Event Hub Client for " + i);
					}
					PartitionReceiver receiver = ehClient.createReceiver(EventHubClient.DefaultConsumerGroupName, Integer.toString(i), Instant.now()).get();
					if (receiver != null)
					{
						receiver.setReceiveHandler(new EventHandler(Integer.toString(i)));
						receivers[i] = receiver;
						System.out.println("Got Receiver for " + i);
					}
					else
						throw new TestException("Unable to get Receiver for " + i);
				}
				System.out.println("Consumer receiver created...");
			}
			catch (Exception error)
			{
				cleanup();
				throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
			}
//		}
	}

	private void cleanup()
	{
		System.out.println("Closing receivers ...");
		for (PartitionReceiver receiver : receivers)
		{
			if (receiver != null)
			{
				System.out.println("Closing receiver " + receiver.getPartitionId());
				try
				{
					receiver.close();
				}
				catch (ServiceBusException e)
				{
					System.out.println("Error when closing receiver ...");
					e.printStackTrace();
				}
			}
		}
		System.out.println("Closing clients ...");
		for (EventHubClient ehClient : ehClients)
		{
			if (ehClient != null)
			{
				System.out.println("Closing Event Hub Client.");
				ehClient.close();
			}
		}
	}

	@Override
	public void validate() throws TestException
	{
		for (PartitionReceiver receiver : receivers)
		{
			if (receiver == null)
				throw new TestException("Azure Iot Hub event consumer could not be created.");
		}
	}

	@Override
	public void destroy()
	{
		Iterator it = receivedEventsByPartition.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pair = (Map.Entry) it.next();
			System.out.println("Partition " + pair.getKey() + " received " + pair.getValue() + " events.");
			it.remove();
		}
		receivedEventsByPartition.clear();
		cleanup();
		super.destroy();
	}

	// sample implementation of the Handler
	public final class EventHandler extends PartitionReceiveHandler
	{
		private long		count;
		private String	partitionId;

		public EventHandler(final String partitionId)
		{
			count = 0;
			this.partitionId = partitionId;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			for (EventData event : events)
			{
				receivedEventsByPartition.put(partitionId, receivedEventsByPartition.get(partitionId) + 1);
				String message = new String(event.getBody(), Charset.defaultCharset());
				receive(message);
			}

			count++;
		}

		@Override
		public void onError(Exception exception)
		{
			System.out.println("Receive Error - " + exception.getMessage());
		}
	}
	
	public void shutdown()
	{
		cleanup();
	}

	private void logAmqp()
	{
		FileHandler fhc = null;
		try
		{
			fhc = new FileHandler("c:\\amqpClientframes.log", false);
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger lc = Logger.getLogger("servicebus.trace");
		fhc.setFormatter(new SimpleFormatter());
		lc.addHandler(fhc);
		lc.setLevel(Level.ALL);
	}
}
