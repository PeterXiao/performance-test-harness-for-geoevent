package com.esri.ges.test.performance.tcp;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;
import com.esri.ges.test.performance.jaxb.ConsumerConfig;

public class ClusterableTcpEventConsumer extends DiagnosticsCollectorBase implements MessageListener
{
	private int port = 5775;
	private TcpSocketServer socketServer;
	private AtomicInteger eventIx = new AtomicInteger(0);
	private int expectedResultCount = 0;
	private Long[] timeStamp = null;
			
	public ClusterableTcpEventConsumer(int port)
	{
		super(Mode.CONSUMER);
		this.port = port;
	}
	
	@Override
	public synchronized void init(Config config) throws TestException
	{
		try
		{
			port = Integer.valueOf(config.getPropertyValue("port", String.valueOf(port)));
			if( socketServer != null )
			{
				socketServer.setPort(port);
			}
			
			setTimeOutInSec(((ConsumerConfig)config).getTimeoutInSec());
			
			// init
			eventIx = new AtomicInteger(0);
			timeStamp = null;
		}
		catch (Throwable e)
		{
			throw new TestException(e.getMessage());
		}
	}

	@Override
	public void listenOnCommandPort(int commandPort, boolean isLocal)
	{
		super.listenOnCommandPort(commandPort, isLocal);
		
		socketServer = new TcpSocketServer(this);
		socketServer.setPort(port);
		socketServer.start();
	}
	
	@Override
	public void validate() throws TestException
	{
		if (socketServer == null)
			throw new TestException("Socket connection is not established. Please initialize TcpEventConsumer before it starts collecting diagnostics.");
	}
	
	@Override
	public void run(AtomicBoolean running) 
	{
		expectedResultCount = numberOfEvents;//(this.numberOfExpectedResults > -1) ? this.numberOfExpectedResults : numberOfEvents;
		if (expectedResultCount > 0)
		{
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STARTED);
		}
	}

	@Override
	public void receive(String message)
	{
		if( message == null && running.get() )
			return;
		
		if (eventIx.get() == 0)
		{
			timeStamp = new Long[2];
			timeStamp[0] = System.currentTimeMillis();
		}
		
		eventIx.incrementAndGet();
		successfulEvents.incrementAndGet();
		
		if (eventIx.get() == expectedResultCount)
		{
			timeStamp[1] = System.currentTimeMillis();
			synchronized(timeStamps)
			{
				timeStamps.put(timeStamps.size(), timeStamp);
			}
			long totalTime = 0;
			if(timeStamp != null && timeStamp[0] != null && timeStamp[1] != null )
			{ 
				totalTime = (timeStamp[1] - timeStamp[0]) / 1000;
				System.out.println("Consumed a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double)successfulEvents.get() / (double)totalTime) + " e/s).");
			}
			else if( successfulEvents.get() > 0 )
				System.out.println("Consumed a total of: " + successfulEvents.get() + " events.");
			running.set(false);
			
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STOPPED);
		}
	}
	
	public void shutdown()
	{
		if( socketServer != null )
		{
			socketServer.destroy();
			socketServer = null;
		}
	}
}
