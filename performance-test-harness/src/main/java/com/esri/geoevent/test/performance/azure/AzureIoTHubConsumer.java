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

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;

public class AzureIoTHubConsumer extends ConsumerBase
{
	// connection properties
	private String									eventHubNamespace						= "";
	private String									eventHubName								= "";
	private String									eventHubSharedAccessKeyName	= "";
	private String									eventHubSharedAccessKey			= "";
	private int											eventHubNumberOfPartitions	= 4;
	private ReceiverThread					receiverThread;

	private List<PartitionReceiver>	receivers										= null;
	private EventHubClient					ehClient										= null;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);

		System.out.println("Initializing Consumer...");

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
			receivers = new ArrayList<PartitionReceiver>();
			ConnectionStringBuilder connStr = new ConnectionStringBuilder(eventHubNamespace, eventHubName, eventHubSharedAccessKeyName, eventHubSharedAccessKey);
			ehClient = EventHubClient.createFromConnectionString(connStr.toString(), true).get(); // the true boolean is temp

			// receiver = ehClient.createReceiver(EventHubClient.DefaultConsumerGroupName, partitionId,
			// Instant.now().minus(12, ChronoUnit.HOURS)).get();
			for (int i = 0; i < eventHubNumberOfPartitions; i++)
			{
				PartitionReceiver receiver = ehClient.createReceiver(EventHubClient.DefaultConsumerGroupName, Integer.toString(i), Instant.now()).get();
				receivers.add(receiver);
			}
			startReceiverThread();
			System.out.println("Consumer receiver created...");
		}
		catch (Exception error)
		{
			cleanup();
			throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
		}
	}

	private void cleanup()
	{
		System.out.println("Closing receivers ...");
		for (PartitionReceiver receiver : receivers)
		{
			if (receiver != null)
			{
				System.out.println("Closing receiver " + receiver.getPartitionId());
				receiver.close();
			}
		}
		if (ehClient != null)
		{
			System.out.println("Closing Event Hub Client.");
			ehClient.close();
		}
	}

	@Override
	public void validate() throws TestException
	{
		if (receivers == null || receivers.size() == 0)
			throw new TestException("Azure Iot Hub event consumer could not be created.");
	}

	@Override
	public void destroy()
	{
		super.destroy();
		System.out.println("Destroy called");
		if (receiverThread != null)
		{
			receiverThread.dismiss();
			receiverThread = null;
		}
		cleanup();
	}

	private void startReceiverThread() throws InterruptedException
	{
		if (receiverThread != null)
		{
			receiverThread.dismiss();
			receiverThread = null;
		}
		receiverThread = new ReceiverThread();
		receiverThread.setName("AzureIotHubConsumer-receiverThread");
		receiverThread.setDaemon(true);
		receiverThread.start();
	}

	class ReceiverThread extends Thread
	{
		private volatile boolean running = true;

		private void dismiss()
		{
			running = false;
		}

		public void run()
		{
			Consumer<Iterable<EventData>> receiveAndPrint = new Consumer<Iterable<EventData>>()
				{
					public void accept(Iterable<EventData> receivedEvents)
					{
						if (receivedEvents != null)
							for (EventData receivedEvent : receivedEvents)
							{
								String message = new String(receivedEvent.getBody(), Charset.defaultCharset());
								receive(message);
							}
					}
				};

			try
			{
				while (running)
				{
					CompletableFuture<Void>[] futures = new CompletableFuture[receivers.size()];
					int i = 0;
					for (PartitionReceiver receiver : receivers)
					{
						futures[i] = receiver.receive().thenAccept(receiveAndPrint);
						i++;
					}
					CompletableFuture.anyOf(futures).get();
				}
				System.out.println("Receiver Thread existing...");
			}
			catch (Throwable t)
			{
				cleanup();
				System.out.println("Receiver Thread Exception.");
				t.printStackTrace();
			}

		}
	}
}
