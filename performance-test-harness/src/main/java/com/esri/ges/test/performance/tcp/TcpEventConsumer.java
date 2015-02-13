package com.esri.ges.test.performance.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.esri.ges.test.performance.ConsumerBase;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;

public class TcpEventConsumer extends ConsumerBase
{
	private String host;
	private int port;
	private Socket socket;
	private BufferedReader is;
	
	@Override
	public synchronized void init(Config config) throws TestException
	{
		try
		{
			super.init(config);
			
			host = config.getPropertyValue("host", "localhost");
			//if we have list, then grab the first one
			if( host.indexOf(",") != -1 )
			{
				host = host.split(",")[0];
			}
			port = Integer.parseInt(config.getPropertyValue("port", "5570"));
			socket = new Socket(host, port);
			socket.setSoTimeout(50);
			is = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );			
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
	public String pullMessage()
	{
		String message = null;
		try
		{
			message = is.readLine();
		}
		catch(Exception error)
		{
		}
		return message;
	}

	@Override
	public void destroy()
	{
		super.destroy();
		IOUtils.closeQuietly(is);
		IOUtils.closeQuietly(socket);
		is = null;
		socket = null;
	}
}
