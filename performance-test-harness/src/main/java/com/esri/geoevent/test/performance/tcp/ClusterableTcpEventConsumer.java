package com.esri.geoevent.test.performance.tcp;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class ClusterableTcpEventConsumer extends ConsumerBase implements MessageListener
{
	private int port = 5775;
	private TcpSocketServer socketServer;
			
	public ClusterableTcpEventConsumer(int port)
	{
		super();
		this.port = port;
	}
	
	@Override
	public synchronized void init(Config config) throws TestException
	{
		super.init(config);
		try
		{
			port = Integer.valueOf(config.getPropertyValue("port", String.valueOf(port)));
			if( socketServer != null )
			{
				socketServer.setPort(port);
			}
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
	
	public void shutdown()
	{
		if( socketServer != null )
		{
			socketServer.destroy();
			socketServer = null;
		}
	}
}
