package com.esri.geoevent.test.performance.rabbitmq;

import java.io.IOException;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitMQEventConsumer extends ConsumerBase
{
	private Connection				connection					= null;
	private Channel						channel							= null;
	private QueueingConsumer	consumer						= null;
	private String						uri;
	private String						exchangeName;
	private String						queueName;
	private String 						routingKey 					= null;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		
		uri = config.getPropertyValue("uri");
		exchangeName = config.getPropertyValue("exchangeName");
		queueName = config.getPropertyValue("queueName");
		routingKey = config.getPropertyValue("routingKey");
	
		if (uri == null)
			throw new TestException("RabbitMQ event consumer ERROR: 'uri' property must be specified");
		if (exchangeName == null)
			throw new TestException("RabbitMQ event consumer ERROR: 'exchangeName' property must be specified");
		if (queueName == null)
			throw new TestException("RabbitMQ event consumer ERROR: 'queueName' property must be specified");

		try
		{
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUri(uri);
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(queueName, false, false, true, null);
			channel.queueBind(queueName, exchangeName, routingKey);
			// channel.basicQos(1);
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
		}
		catch (Exception e)
		{
			throw new TestException("RabbitMQ event consumer initialization: problem setting up connection to RabbitMQ: " + e.getMessage());
		}
	}

	@Override
	public void validate() throws TestException
	{
		if (consumer == null)
			throw new TestException("RabbitMQ event consumer could not be created.");
	}

	
	@Override
	public String pullMessage()
	{
		String message = null;
		try
		{
			byte[] bytes = consumer.nextDelivery().getBody();
			message = new String(bytes);
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
		
		try
		{
			consumer.getChannel().basicCancel(consumer.getConsumerTag());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException ignored)
		{
		}
		
		try
		{
			channel.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		try
		{
			connection.close(1000);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		consumer = null;
		channel = null;
		connection = null;
	}
}
