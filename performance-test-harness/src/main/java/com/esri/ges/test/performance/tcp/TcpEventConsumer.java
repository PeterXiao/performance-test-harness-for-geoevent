package com.esri.ges.test.performance.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;
import com.esri.ges.test.performance.jaxb.ConsumerConfig;

public class TcpEventConsumer extends DiagnosticsCollectorBase
{
	private String host;
	private int port;
	private Socket socket;
	private BufferedReader is;
	
	public TcpEventConsumer()
	{
		super(Mode.CONSUMER);
	}
	
	@Override
	public synchronized void init(Config config) throws TestException
	{
		try
		{
			host = config.getPropertyValue("host", "localhost");
			//if we have list, then grab the first one
			if( host.indexOf(",")  != -1 )
			{
				host = host.split(",")[0];
			}
			port = Integer.parseInt(config.getPropertyValue("port", "5570"));
			socket = new Socket(host, port);
			socket.setSoTimeout(100);
			is = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			
			setTimeOutInSec(((ConsumerConfig)config).getTimeoutInSec());
		}
		catch (Throwable e)
		{
			throw new TestException(e.getMessage());
		}
	}

	@Override
	public void validate() throws TestException
	{
		if (is == null)
			throw new TestException("Socket connection is not established. Please initialize TcpEventConsumer before it starts collecting diagnostics.");
	}

	@Override
	public void run(AtomicBoolean running) 
	{
		int expectedResultCount = numberOfEvents;//(this.numberOfExpectedResults > -1) ? this.numberOfExpectedResults : numberOfEvents;
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
						String line = is.readLine();
						if( line == null )
							continue;
					}catch(SocketTimeoutException ex)
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
		try
		{
			is.close();
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
		is = null;
		socket = null;
	}
}
