/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.test.performance.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

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
			
			host = config.getPropertyValue("hosts", "localhost");
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
