package com.esri.geoevent.test.performance.rabbitmq;

import java.io.IOException;

import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQEventProducer extends ProducerBase
{
	private Connection	connection	= null;
	private Channel			channel			= null;
	private String			uri;
	private String			exchangeName;
	private String			queueName;
	private String 			routingKey;
	
	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		
		uri = config.getPropertyValue("uri");
		exchangeName = config.getPropertyValue("exchangeName");
		queueName = config.getPropertyValue("queueName");
		routingKey = config.getPropertyValue("routingKey");
		 
		if (uri == null)
			throw new TestException("RabbitMQ event producer ERROR: 'uri' property must be specified");
		if (exchangeName == null)
			throw new TestException("RabbitMQ event producer ERROR: 'exchangeName' property must be specified");
		if (queueName == null)
			throw new TestException("RabbitMQ event producer ERROR: 'queueName' property must be specified");

		try
		{
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUri(uri);
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(queueName, false, false, true, null);
			channel.queueBind(queueName, exchangeName, routingKey);
		}
		catch (Exception e)
		{
			throw new TestException("RabbitMQ event producer initialization: problem setting up connection to RabbitMQ: " + e.getMessage());
		}
	}

	@Override
	public void validate() throws TestException
	{
		super.validate();
		if (channel == null)
			throw new TestException("RabbitMQ Event Producer could not be created.");
		if (events.isEmpty())
			throw new TestException("RabbitMQ Event Producer has no events to simulate.");
	}

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		int eventIndex = index;
		for (int i = 0; i < numEventsToSend; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;
			String thisEvent = events.get(eventIndex++);
			try
			{
				byte[] bytes = thisEvent.getBytes();
				channel.basicPublish(exchangeName, routingKey, null, bytes);
				successfulEvents.incrementAndGet();
				successfulEventBytes.addAndGet(thisEvent.getBytes().length);
				if (running.get() == false)
					break;
			}
			catch (Exception error)
			{
				error.printStackTrace();
			}
		}
		return eventIndex;
	}

	@Override
	public void destroy()
	{
		super.destroy();
		try
		{
			channel.close();
		}
		catch (IOException e)
		{
			;
		}
		finally
		{
			channel = null;
		}
		try
		{
			connection.close();
		}
		catch (IOException e)
		{
			;
		}
		finally
		{
			connection = null;
		}
	}
}
