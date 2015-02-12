package com.esri.ges.test.performance.rabbitmq;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitMQEventConsumer extends DiagnosticsCollectorBase
{
	private Connection				connection					= null;
	private Channel						channel							= null;
	private QueueingConsumer	consumer						= null;
	private String						uri;
	private String						exchangeName;
	private String						queueName;
	private String 						routingKey 					= null;

	public RabbitMQEventConsumer()
	{
		super(Mode.CONSUMER);
	}
	
	@Override
	public void init(Config config) throws TestException
	{
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
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
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
	public void run(AtomicBoolean alive)
	{
		System.out.println("------> Running RabbitMQ event consumer");
		int expectedResultCount = numberOfEvents;
		if (expectedResultCount > 0)
		{
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STARTED);
			try
			{
				int eventIx = 0;
				Long[] timeStamp = null;
				while (running.get())
				{
					try
					{
						byte[] bytes = consumer.nextDelivery().getBody();
//						System.out.println("Consuming GeoEvent(" + successfulEvents + ", " + bytes.length + " bytes)...");
						// channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					}
					catch (Exception ex)
					{
						continue;
					}
					if (eventIx == 0)
					{
						timeStamp = new Long[2];
						timeStamp[0] = System.currentTimeMillis();
					}
					eventIx++;
					successfulEvents.incrementAndGet();
					if (eventIx == expectedResultCount)
					{
						timeStamp[1] = System.currentTimeMillis();
						synchronized(timeStamps)
						{
							timeStamps.put(timeStamps.size(), timeStamp);
						}
						running.set(false);
					}
				}
				long totalTime = 0;
				if(timeStamp != null && timeStamp[0] != null && timeStamp[1] != null )
				{ 
					totalTime = (timeStamp[1] - timeStamp[0]) / 1000L;
					System.out.println("Consumed a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double)successfulEvents.get() / (double)totalTime) + " e/s).");
				}
				else if( successfulEvents.get() > 0 )
					System.out.println("Consumed a total of: " + successfulEvents.get() + " events.");
			}
			catch (Exception e)
			{
				running.set(false);
				e.printStackTrace();
			}
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STOPPED);
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		events.clear();
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
		catch (InterruptedException e2)
		{
			e2.printStackTrace();
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
