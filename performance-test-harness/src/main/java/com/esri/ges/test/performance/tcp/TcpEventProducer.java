package com.esri.ges.test.performance.tcp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;

public class TcpEventProducer extends DiagnosticsCollectorBase
{
	private String				host;
	private int						port;
	private Socket				socket;
	private OutputStream	os;
	private int						eventsPerSec				= -1;
	private int						staggeringInterval	= 10;

	@Override
	public synchronized void init(Properties props) throws TestException
	{
		try
		{
			String path = props.containsKey("simulationFilePath") ? props.getProperty("simulationFilePath").trim() : "";
			loadEvents(new File(path));

			host = props.containsKey("host") ? props.getProperty("host").trim() : "localhost";
			// if we have list, then grab the first one
			if (host.indexOf(",") != -1)
			{
				host = host.split(",")[0];
			}
			port = props.containsKey("port") ? Integer.parseInt(props.getProperty("port")) : 5565;
			socket = new Socket(host, port);
			eventsPerSec = props.containsKey("eventsPerSec") ? Integer.parseInt(props.getProperty("eventsPerSec")) : -1;
			staggeringInterval = props.containsKey("staggeringInterval") ? Integer.parseInt(props.getProperty("staggeringInterval")) : 10;
			os = socket.getOutputStream();
			mode = Mode.PRODUCER;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new TestException(e.getMessage());
		}
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	@Override
	public void validate() throws TestException
	{
		if (os == null)
			throw new TestException("Socket connection is not established. Please initialize TcpEventProducer before it starts collecting diagnostics.");
		if (events.isEmpty())
			throw new TestException("TcpEventProducer is missing events to produce.");
	}

	@Override
	public void run(AtomicBoolean running)
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
					// add the delay - if necessary
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
		for (int i = 0; i < numEventsToSend; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;
			try
			{
				os.write(events.get(eventIndex++).getBytes());
				os.flush();
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

	@Override
	public void destroy()
	{
		super.destroy();
		events.clear();
		try
		{
			os.close();
		}
		catch (IOException e)
		{
			;
		}
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			;
		}
		os = null;
		socket = null;
	}
}
