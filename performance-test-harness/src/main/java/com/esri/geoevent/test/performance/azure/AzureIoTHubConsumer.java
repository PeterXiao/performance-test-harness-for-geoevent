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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.qpid.amqp_1_0.jms.BytesMessage;
import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class AzureIoTHubConsumer extends ConsumerBase implements MessageListener
{
	// connection properties
	private String	connectionUri				= "";
	private String	eventHubName				= "";
	private int			numberOfPartitions	= 4;

	private Connection						connection							= null;
	private Session								session									= null;
	private List<MessageConsumer>	consumers								= new ArrayList<MessageConsumer>();

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);

		connectionUri = config.getPropertyValue("consumerConnectionUri");
		eventHubName = config.getPropertyValue("consumerEventHubName");
		numberOfPartitions = Integer.parseInt(config.getPropertyValue("consumerNumberOfPartitions", String.valueOf(4)));
		
		if (connectionUri == null)
			throw new TestException("Azure Iot Hub event consumer ERROR: 'connectionUri' property must be specified");
		if (eventHubName == null)
			throw new TestException("Azure Iot Hub event consumer ERROR: 'eventHubName' property must be specified");

		ConnectionFactory factory = null;
		try
		{
			factory = ConnectionFactoryImpl.createFromURL(connectionUri);
			connection = factory.createConnection();
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			for (int i = 0; i < numberOfPartitions; i++)
			{
				String queueName = eventHubName + i;
				Destination destination = session.createQueue(queueName);
				MessageConsumer consumer = session.createConsumer(destination);
				consumer.setMessageListener(this);
				consumers.add(consumer);
			}
		}
		catch (Exception error)
		{
			throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
		}
	}

	@Override
	public void validate() throws TestException
	{
		if (session == null)
			throw new TestException("Azure Iot Hub event consumer could not be created.");
	}

	@Override
	public void destroy()
	{
		super.destroy();

		//TODO ...

		connection = null;
	}

	@Override
	public void onMessage(Message message)
	{
		try
		{
			if (message instanceof BytesMessage)
			{
				// read the message body
				BytesMessage byteMessage = (BytesMessage) message;
				byte[] data = new byte[(int) byteMessage.getBodyLength()];
				byteMessage.readBytes(data);
				byteMessage.reset();

				//String deviceId = byteMessage.getStringProperty("iothub-connection-device-id");

				// parse out the message to string
				String messageAsString = new String(data, StandardCharsets.UTF_8);
				//LOGGER.debug("MSG_RECEIVED_DEBUG", messageAsString);

				super.receive(messageAsString);
			}
		}
		catch (Exception error)
		{
			//LOGGER.error("ERROR_READING_MSG", error);
		}
	}}
