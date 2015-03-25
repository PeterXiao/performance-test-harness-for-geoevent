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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.io.IOUtils;

import com.esri.geoevent.test.performance.ImplMessages;

public class ProducerTcpSocketServer
{
	private int									port;
	private ConnectionListener	listenerThread;
	private Deque<Socket>				clients	= new ConcurrentLinkedDeque<>();

	public ProducerTcpSocketServer()
	{
		port = -1;
	}

	public synchronized void setPort(int port)
	{
		if (this.port == port)
			return;

		this.port = port;

		if (listenerThread != null)
		{
			stop();
			start();
		}
	}

	public synchronized void start()
	{
		if (listenerThread != null) // the server is already running.
			return;

		listenerThread = new ConnectionListener();
		listenerThread.start();
	}

	public synchronized void stop()
	{
		if (listenerThread == null)
			return;

		listenerThread.interrupt();
	}

	public void clearClientList()
	{
		if( ! clients.isEmpty() )
		{
			clients.stream().forEach(client->IOUtils.closeQuietly(client));
		}
		clients.clear();
	}
	
	/**
	 * Send the event in a round robin fashion to distribute the load
	 * @param message to send
	 */
	public void sendEvent(String message)
	{
		Socket socket = clients.pop();
		try
		{
			OutputStream out = socket.getOutputStream();
			out.write(message.getBytes());
			out.flush();
		}
		catch( Exception error )
		{
			error.printStackTrace();
		}
		clients.push(socket);
	}

	/**
	 * Clean up method used when we are finished
	 */
	public void destroy()
	{
		stop();
		clearClientList();
	}
	
	/**
	 * Helper class used to listen to connections
	 *
	 */
	private class ConnectionListener extends Thread
	{
		private volatile boolean	running	= true;

		public ConnectionListener()
		{
			setName("SimpleSocketServer Connection Listener");
		}

		@Override
		public void interrupt()
		{
			running = false;
			super.interrupt();
		}

		public void run()
		{
			ServerSocket serverSocket = null;
			try
			{
				serverSocket = new ServerSocket(port);
				System.out.println( ImplMessages.getMessage("TCP_SERVER_START_MSG", String.valueOf(port)) );
			}
			catch (IOException e1)
			{
				System.err.println("Error while trying to bind to port " + port + " : " + e1.getMessage());
				return;
			}
			while (running)
			{
				try
				{
					Socket client = serverSocket.accept();
					System.out.println("New client connected.");
					clients.add(client);
				}
				catch (IOException e)
				{
				}
			}
			try
			{
				serverSocket.close();
			}
			catch (IOException e)
			{
				System.err.println("Error while trying to close the server bound to port " + port + " : " + e.getMessage());
				return;
			}
		}
	}
}
