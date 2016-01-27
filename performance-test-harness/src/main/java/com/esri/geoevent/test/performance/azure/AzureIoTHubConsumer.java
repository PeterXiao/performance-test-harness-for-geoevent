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
import java.util.function.Consumer;

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
	private String						eventHubNamespace						= "";
	private String						eventHubName								= "";
	private String						eventHubSharedAccessKeyName	= "";
	private String						eventHubSharedAccessKey			= "";
	private int								eventHubNumberOfPartitions	= 4;

	private PartitionReceiver receiver = null;
	
	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);

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
			ConnectionStringBuilder connStr = new ConnectionStringBuilder(eventHubNamespace, eventHubName, eventHubSharedAccessKeyName, eventHubSharedAccessKey);
			EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString(), true).get();  // the true boolean is temp
			String partitionId = "0"; // API to get PartitionIds will be released as part of V0.2
			receiver = ehClient.createReceiver(EventHubClient.DefaultConsumerGroupName, partitionId, Instant.now()).get();
			System.out.println("R receiver created...");
		}
		catch (Exception error)
		{
			throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
		}
	}

	@Override
	public void validate() throws TestException
	{
		if (receiver == null)
			throw new TestException("Azure Iot Hub event consumer could not be created.");
	}

	@Override
	public String pullMessage()
	{
		String message = null;
		try
		{
			receiver.receive().thenAccept(new Consumer<Iterable<EventData>>()
			{
				public void accept(Iterable<EventData> receivedEvents)
				{
					for (EventData receivedEvent: receivedEvents)
					{
						String messageAsString = new String(receivedEvent.getBody(), Charset.defaultCharset());

						//String offset = receivedEvent.getSystemProperties().getOffset();
						//long seqNumber = receivedEvent.getSystemProperties().getSequenceNumber();
						//Instant enqueuedTime = receivedEvent.getSystemProperties().getEnqueuedTime();
						//String partitionKey = receivedEvent.getSystemProperties().getPartitionKey();
						//System.out.println(String.format("R Message Payload: %s", messageAsString));

						System.out.println("R message received - " + messageAsString.trim());
						receive(messageAsString);
					}
				}
			}).get();
		}
		catch (Exception ignored)
		{
		}
		return message;
	}

	@Override
	public void destroy()
	{
		super.destroy();

		//TODO ...

		receiver = null;
	}

}
