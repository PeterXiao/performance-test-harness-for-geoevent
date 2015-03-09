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
package com.esri.geoevent.test.performance.websocket;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.esri.geoevent.test.performance.PerformanceCollectorBase;
import com.esri.geoevent.test.performance.Mode;
import com.esri.geoevent.test.performance.RunningState;
import com.esri.geoevent.test.performance.RunningStateType;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class WebsocketEventConsumer extends PerformanceCollectorBase
{
	private static final String STREAM_SERVICE = "/streamservice";
	private static final String OUTBOUND = "/outbound";

	private String host;
	private int port;
	private static final int MAX_IDLE_TIME = 30000;
	//private WebSocket.Connection connection;
	private WebSocketClientFactory factory;
	private WebSocketClient client;
	private Long[] timeStamp = null;
	private int connectionCount;
	private MyConnection[] connections;

	public WebsocketEventConsumer()
	{
		super(Mode.Consumer);
	}
	
	class MyConnection  implements WebSocket.OnTextMessage
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
		public void onMessage(String data)
		{
			if( !running.get() )
			{
				System.out.println("received a websocket packet, but I'm not running.");
				return;
			}

			long currentCount = successfulEvents.getAndIncrement();
			if (currentCount == 0)
			{
				timeStamp = new Long[2];
				timeStamp[0] = System.currentTimeMillis();
			}
			currentCount++;
			if( currentCount % 100000 == 0 )
				System.out.println("received "+currentCount+" messages.");
			if (currentCount == numberOfEvents * connectionCount)
			{
				System.out.println("Reached the max event count.");
				timeStamp[1] = System.currentTimeMillis();
				timeStamps.put(timeStamps.size(), timeStamp);
				running.set(false);
				if (runningStateListener != null)
					runningStateListener.onStateChange(new RunningState(RunningStateType.STOPPED));
			}
		}
	}

	@Override
	public void init(Config config) throws TestException
	{
		System.out.println("init:");
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
			port = Integer.parseInt(config.getPropertyValue("port", "5570"));
			connectionCount = Integer.parseInt(config.getPropertyValue("connectionCount", "1"));
			
			String url = "ws://"+host+":"+port+STREAM_SERVICE+OUTBOUND;
			URI uri = new URI(url);

			System.out.println("Creating "+connectionCount+" client connections.");
			connections = new MyConnection[connectionCount];
			for( int i = 0; i < connectionCount; i++ )
			{
				connections[i] = new MyConnection();
				connections[i].setConnection( client.open(uri, connections[i], 10, TimeUnit.SECONDS) );
			}
			System.out.println("Successfully connected to the Websocket Server.");
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
		for( MyConnection connection : connections )
		{
			if (connection.getConnection() == null)
				throw new TestException("Socket connection is not established. Please initialize "+WebsocketEventConsumer.class.getName()+" before it starts collecting diagnostics.");
		}
	}

	@Override
	public void run()
	{
		if (numberOfEvents > 0)
		{
			System.out.println("Starting to listen for " + numberOfEvents + " events.");
			if (runningStateListener != null)
				runningStateListener.onStateChange(new RunningState(RunningStateType.STARTED));
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		for( MyConnection conn : connections )
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
}
