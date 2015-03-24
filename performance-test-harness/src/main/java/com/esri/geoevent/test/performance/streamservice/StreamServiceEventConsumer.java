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

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class StreamServiceEventConsumer extends ConsumerBase
{
	//private static final String STREAM_SERVICE = "/streamservice";
	private static final String SUBSCRIBE = "/subscribe";

	private String host;
	private int port;
	private static final int MAX_IDLE_TIME = 30000;
	//private WebSocket.Connection connection;
	private WebSocketClientFactory factory;
	private WebSocketClient client;
	private int connectionCount;
	private ConnectionHandler[] connections;
	private StreamMetadata metaData;
	private String serviceName;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		try
		{
			if( factory == null )
			{
				System.out.println("factory being constructed.");
				factory = new WebSocketClientFactory();
				factory.start();
			}
			else
			{
				System.out.println("Factory exists.");
			}

			if( client == null )
			{
				System.out.println("Creating client.");
				client = factory.newWebSocketClient();
				client.setMaxIdleTime(MAX_IDLE_TIME);
				client.setProtocol("output");
			}
			
			host = config.getPropertyValue("hosts", "localhost");
			port = Integer.parseInt(config.getPropertyValue("port","6180"));
			serviceName = config.getPropertyValue("serviceName", "vehicles");
			connectionCount = Integer.parseInt(config.getPropertyValue("connectionCount","1"));
			
			String serviceMetadataUrl = "http://"+host+":"+port+"/arcgis/rest/services/"+serviceName+"/StreamServer?f=json";
			metaData = new StreamMetadata( serviceMetadataUrl );
			List<String> wsUrls = metaData.gerUrls();

			System.out.println("Creating "+connectionCount+" client connections.");
			connections = new ConnectionHandler[connectionCount];
			for( int i = 0; i < connectionCount; i++ )
			{
				String wsUrl = wsUrls.get( i % wsUrls.size() ) + SUBSCRIBE;
				URI uri = new URI( wsUrl );
				connections[i] = new ConnectionHandler();
				connections[i].setConnection( client.open( uri, connections[i], 10, TimeUnit.SECONDS) );
			}
			System.out.println("Successfully connected to the Websocket Server.");
		}
		catch (Throwable error)
		{
			throw new TestException( ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error );
		}
	}

	@Override
	public void validate() throws TestException 
	{
		for( ConnectionHandler connection : connections )
		{
			if (connection.getConnection() == null)
				throw new TestException("Socket connection is not established. Please initialize "+StreamServiceEventConsumer.class.getName()+" before it starts collecting diagnostics.");
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		for( ConnectionHandler conn : connections )
		{
			if( conn.getConnection() != null )
				conn.getConnection().close();
		}
		try {
			factory.stop();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		factory.destroy();
		factory = null;
		connections = null;
		client = null;
	}
	
	class ConnectionHandler  implements WebSocket.OnTextMessage
	{
		WebSocket.Connection connection;
		
		public void setConnection( WebSocket.Connection connection )
		{
			this.connection = connection;
		}

		public WebSocket.Connection getConnection()
		{
			return connection;
		}
		/* ------------------------------------------------------------ */
		/** Callback on successful open of the websocket connection.
		 */
		public void onOpen(Connection connection)
		{
		}

		/* ------------------------------------------------------------ */
		/** Callback on close of the WebSocket connection
		 */
		public void onClose(int closeCode, String message)
		{
			System.out.println("Lost the connection to the websocket server.");
		}

		/* ------------------------------------------------------------ */
		/** Callback on receiving a message
		 */
		public void onMessage(String message)
		{
			if( !running.get() )
			{
				System.out.println("received a websocket packet, but I'm not running.");
				return;
			}
			receive( message );
		}
	}
}
