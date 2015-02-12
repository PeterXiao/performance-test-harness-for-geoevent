package com.esri.ges.test.performance.rabbitmq;

import java.io.File;
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

public class RabbitMQEventProducer extends DiagnosticsCollectorBase
{
	private Connection	connection	= null;
	private Channel			channel			= null;
	private String			uri;
	private String			exchangeName;
	private String			queueName;
	private String 			routingKey;
	//private byte[]			bytes				= new byte[] { -84, -19, 0, 5, 115, 114, 0, 26, 99, 111, 109, 46, 101, 115, 114, 105, 46, 103, 101, 111, 101, 118, 101, 110, 116, 46, 71, 101, 111, 69, 118, 101, 110, 116, 69, -91, 109, 38, -28, 62, -73, 79, 2, 0, 6, 76, 0, 7, 103, 101, 100, 78, 97, 109, 101, 116, 0, 18, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 76, 0, 5, 111, 119, 110, 101, 114, 113, 0, 126, 0, 1, 76, 0, 5, 115, 112, 101, 101, 100, 116, 0, 19, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 73, 110, 116, 101, 103, 101, 114, 59, 76, 0, 9, 116, 105, 109, 101, 115, 116, 97, 109, 112, 116, 0, 24, 76, 111, 114, 103, 47, 106, 111, 100, 97, 47, 116, 105, 109, 101, 47, 68, 97, 116, 101, 84, 105, 109, 101, 59, 76, 0, 11, 118, 101, 104, 105, 99, 108, 101, 78, 97, 109, 101, 113, 0, 126, 0, 1, 76, 0, 11, 118, 101, 104, 105, 99, 108, 101, 84, 121, 112, 101, 113, 0, 126, 0, 1, 120, 112, 116, 0, 5, 84, 114, 117, 99, 107, 116, 0, 3, 105, 110, 49, 115, 114, 0, 17, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 73, 110, 116, 101, 103, 101, 114, 18, -30, -96, -92, -9, -127, -121, 56, 2, 0, 1, 73, 0, 5, 118, 97, 108, 117, 101, 120, 114, 0, 16, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 78, 117, 109, 98, 101, 114, -122, -84, -107, 29, 11, -108, -32, -117, 2, 0, 0, 120, 112, 0, 0, 0, 55, 115, 114, 0, 22, 111, 114, 103, 46, 106, 111, 100, 97, 46, 116, 105, 109, 101, 46, 68, 97, 116, 101, 84, 105, 109, 101, -72, 60, 120, 100, 106, 91, -35, -7, 2, 0, 0, 120, 114, 0, 31, 111, 114, 103, 46, 106, 111, 100, 97, 46, 116, 105, 109, 101, 46, 98, 97, 115, 101, 46, 66, 97, 115, 101, 68, 97, 116, 101, 84, 105, 109, 101, -1, -1, -7, -31, 79, 93, 46, -93, 2, 0, 2, 74, 0, 7, 105, 77, 105, 108, 108, 105, 115, 76, 0, 11, 105, 67, 104, 114, 111, 110, 111, 108, 111, 103, 121, 116, 0, 26, 76, 111, 114, 103, 47, 106, 111, 100, 97, 47, 116, 105, 109, 101, 47, 67, 104, 114, 111, 110, 111, 108, 111, 103, 121, 59, 120, 112, 0, 0, 1, 69, -113, -43, -57, -67, 115, 114, 0, 39, 111, 114, 103, 46, 106, 111, 100, 97, 46, 116, 105, 109, 101, 46, 99, 104, 114, 111, 110, 111, 46, 73, 83, 79, 67, 104, 114, 111, 110, 111, 108, 111, 103, 121, 36, 83, 116, 117, 98, -87, -56, 17, 102, 113, 55, 80, 39, 3, 0, 0, 120, 112, 115, 114, 0, 31, 111, 114, 103, 46, 106, 111, 100, 97, 46, 116, 105, 109, 101, 46, 68, 97, 116, 101, 84, 105, 109, 101, 90, 111, 110, 101, 36, 83, 116, 117, 98, -90, 47, 1, -102, 124, 50, 26, -29, 3, 0, 0, 120, 112, 119, 21, 0, 19, 65, 109, 101, 114, 105, 99, 97, 47, 76, 111, 115, 95, 65, 110, 103, 101, 108, 101, 115, 120, 120, 116, 0, 1, 49, 116, 0, 5, 116, 114, 117, 99, 107, 121 };

	private int eventsPerSec = -1;
	private int staggeringInterval = 10;
	  
	public RabbitMQEventProducer()
	{
		super(Mode.PRODUCER);
	}
	
	@Override
	public void init(Config config) throws TestException
	{
		loadEvents(new File( config.getPropertyValue("simulationFile", "")));
		uri = config.getPropertyValue("uri");
		exchangeName = config.getPropertyValue("exchangeName");
		queueName = config.getPropertyValue("queueName");
		routingKey = config.getPropertyValue("routingKey");
		eventsPerSec = Integer.parseInt(config.getPropertyValue("eventsPerSec","-1"));
    staggeringInterval = Integer.parseInt(config.getPropertyValue("staggeringInterval","1"));
     
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
		if (channel == null)
			throw new TestException("RabbitMQ Event Producer could not be created.");
		if (events.isEmpty())
			throw new TestException("RabbitMQ Event Producer has no events to simulate.");
	}

	@Override
	public void run(AtomicBoolean alive)
	{
		if (numberOfEvents > 0)
		{
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STARTED);
			int eventIx = 0;
			Long[] timeStamp = new Long[2];
			timeStamp[0] = System.currentTimeMillis();

			// send out events with delay
			if (eventsPerSec > -1)
			{
				// determine the events to send and delay
				// use a staggering approach to
				int staggeringInterval = (this.staggeringInterval > 0) ? this.staggeringInterval : 10;
				int eventsToSend = eventsPerSec / staggeringInterval;
				long delay = 1000 / staggeringInterval;
				long targetTimeStamp = timeStamp[0];
				long sleepTime = 0;

				// loop through all events until we are finished
				while (successfulEvents.get() < numberOfEvents)
				{
					targetTimeStamp = targetTimeStamp + delay;
					// send the events
					sendEvents(eventIx, eventsToSend);

					sleepTime = targetTimeStamp - System.currentTimeMillis();
					// add the delay
					if (sleepTime > 0)
					{
						try
						{
							Thread.sleep(sleepTime);
						}
						catch (InterruptedException ignored)
						{
							;
						}
					}

					// check if we need to break
					if (running.get() == false)
						break;
				}
			}
			// no delays just send out as fast as possible
			else
			{
				sendEvents(eventIx, numberOfEvents);
			}
			timeStamp[1] = System.currentTimeMillis();
			synchronized (timeStamps)
			{
				timeStamps.put(timeStamps.size(), timeStamp);
			}
			running.set(false);
			long totalTime = (timeStamp[1] - timeStamp[0]) / 1000;
			System.out.println("Produced a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double) numberOfEvents / (double) totalTime) + " e/s).");
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STOPPED);
		}
	}

	private void sendEvents(int eventIndex, int numEventsToSend)
	{
		try
		{
			for (int i = 0; i < numEventsToSend; i++)
			{
				if (eventIndex == events.size())
					eventIndex = 0;
				String thisEvent = events.get(eventIndex++);
				try
				{
					byte[] bytes = thisEvent.getBytes();
//					System.out.println("Publishing GeoEvent(" + successfulEvents + ", " + bytes.length + " bytes)...");
					channel.basicPublish(exchangeName, routingKey, null, bytes);
					successfulEvents.incrementAndGet();
					if (running.get() == false)
						break;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		events.clear();
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
