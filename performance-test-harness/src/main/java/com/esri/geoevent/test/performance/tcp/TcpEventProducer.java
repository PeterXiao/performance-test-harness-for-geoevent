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

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.esri.geoevent.test.performance.ImplMessages;
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
			host = config.getPropertyValue("hosts", "localhost");
			port = Integer.parseInt(config.getPropertyValue("port", "5565"));
			socket = new Socket(host, port);
			os = socket.getOutputStream();
		}
		catch (Throwable error)
		{
			throw new TestException( ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error );
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
				String message = events.get(eventIndex++);
				os.write(message.getBytes());
				os.flush();
				messageSent(message);
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
