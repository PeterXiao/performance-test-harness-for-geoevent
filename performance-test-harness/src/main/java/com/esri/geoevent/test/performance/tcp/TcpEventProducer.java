package com.esri.geoevent.test.performance.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class TcpEventProducer extends ProducerBase
{
	private String				host;
	private int						port;
	private Socket				socket;
	private OutputStream	os;
	
	@Override
	public synchronized void init(Config config) throws TestException
	{
		super.init(config);
		try
		{
			host = config.getPropertyValue("host", "localhost");
			port = Integer.parseInt(config.getPropertyValue("port", "5565"));
			socket = new Socket(host, port);
			os = socket.getOutputStream();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new TestException(e.getMessage());
		}
	}

	@Override
	public void validate() throws TestException
	{
		super.validate();
		if (os == null)
			throw new TestException("Socket connection is not established. Please initialize TcpEventProducer before it starts collecting diagnostics.");
	}

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		int eventIndex = index;
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
		return eventIndex;
	}

	@Override
	public void destroy()
	{
		super.destroy();
		
		IOUtils.closeQuietly(os);
		IOUtils.closeQuietly(socket);
		os = null;
		socket = null;
	}
}
