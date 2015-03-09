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
package com.esri.geoevent.test.performance.streamservice;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class StreamServiceEventProducer extends ProducerBase
{
	// private static final String STREAM_SERVICE = "/streamservice";
	private static final String			BROADCAST	= "/broadcast";

	private ConnectionHandler[]					connections;
	private String									host;
	private int											port;
	private WebSocketClientFactory	factory;
	private WebSocketClient					client;
	private int											connectionCount;
	private String									serviceName;
	private StreamMetadata					metaData;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		try
		{
			if (factory == null)
			{
				factory = new WebSocketClientFactory();
				factory.start();
			}

			if (client == null)
			{
				client = factory.newWebSocketClient();
				client.setMaxIdleTime(30000);
				client.setProtocol("input");
			}

			host = config.getPropertyValue("hosts", "localhost");
			port = Integer.parseInt(config.getPropertyValue("port", "6180"));
			serviceName = config.getPropertyValue("serviceName", "vehicles");
			connectionCount = Integer.parseInt(config.getPropertyValue("connectionCount", "1"));

			String serviceMetadataUrl = "http://" + host + ":" + port + "/arcgis/rest/services/" + serviceName + "/StreamServer?f=json";
			metaData = new StreamMetadata(serviceMetadataUrl);
			List<String> wsUrls = metaData.gerUrls();

			connections = new ConnectionHandler[connectionCount];
			for (int i = 0; i < connectionCount; i++)
			{
				String wsUrl = wsUrls.get(i % wsUrls.size()) + BROADCAST;
				URI uri = new URI(wsUrl);
				connections[i] = new ConnectionHandler();
				connections[i].setConnection(client.open(uri, connections[i], 10, TimeUnit.SECONDS));
			}
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
		for (ConnectionHandler connection : connections)
		{
			if (connection.getConnection() == null)
				throw new TestException("Socket connection is not established. Please initialize " + StreamServiceEventProducer.class.getName() + " before it starts collecting diagnostics.");
		}
	}

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		int eventIndex = index;
		for (int i = 0; i < numberOfEvents; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;
			try
			{
				String message = events.get(eventIndex++);
				for (ConnectionHandler conn : connections)
					conn.getConnection().sendMessage(message);
				successfulEvents.incrementAndGet();
				successfulEventBytes.addAndGet(message.getBytes().length);
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
		for (ConnectionHandler conn : connections)
		{
			if (conn.getConnection() != null)
				conn.getConnection().close();
		}
		try
		{
			factory.stop();
		}
		catch (Exception ignored)
		{
		}
		
		factory.destroy();
		factory = null;
		client = null;
		connections = null;
	}
	
	class ConnectionHandler implements WebSocket.OnTextMessage
	{
		WebSocket.Connection	connection;

		public void setConnection(WebSocket.Connection connection)
		{
			this.connection = connection;
		}

		public WebSocket.Connection getConnection()
		{
			return connection;
		}

		/* ------------------------------------------------------------ */
		/**
		 * Callback on close of the WebSocket connection
		 */
		@Override
		public void onClose(int closeCode, String message)
		{
			System.out.println("The connection was closed by the remote host.  (this should not happen)");
			connection = null;
		}

		/* ------------------------------------------------------------ */
		/**
		 * Callback on receiving a message
		 */
		@Override
		public void onMessage(String data)
		{

		}

		/* ------------------------------------------------------------ */
		/**
		 * Callback on receiving a connection
		 */
		@Override
		public void onOpen(Connection connection)
		{
			this.connection = connection;
		}
	}
}
