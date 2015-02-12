package com.esri.ges.test.performance.streamservice;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;

public class StreamServiceEventConsumer extends DiagnosticsCollectorBase
{
	//private static final String STREAM_SERVICE = "/streamservice";
	private static final String SUBSCRIBE = "/subscribe";

	private String host;
	private int port;
	private static final int MAX_IDLE_TIME = 30000;
	//private WebSocket.Connection connection;
	private WebSocketClientFactory factory;
	private WebSocketClient client;
	private Long[] timeStamp = null;
	private int connectionCount;
	private MyConnection[] connections;
	private StreamMetadata metaData;
	private String serviceName;

	public StreamServiceEventConsumer()
	{
		super(Mode.CONSUMER);
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
			if( currentCount % 1000 == 0 )
				System.out.println("received "+currentCount+" messages.");
			if (currentCount == numberOfEvents * connectionCount)
			{
				System.out.println("Reached the max event count.");
				timeStamp[1] = System.currentTimeMillis();
				timeStamps.put(timeStamps.size(), timeStamp);
				running.set(false);
				if (runningStateListener != null)
					runningStateListener.onStateChange(RunningState.STOPPED);
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
			
			host = config.getPropertyValue("host", "localhost");
			port = Integer.parseInt(config.getPropertyValue("port","6180"));
			serviceName = config.getPropertyValue("serviceName", "vehicles");
			connectionCount = Integer.parseInt(config.getPropertyValue("connectionCount","1"));
			
			String serviceMetadataUrl = "http://"+host+":"+port+"/arcgis/rest/services/"+serviceName+"/StreamServer?f=json";
			metaData = new StreamMetadata( serviceMetadataUrl );
			List<String> wsUrls = metaData.gerUrls();

			System.out.println("Creating "+connectionCount+" client connections.");
			connections = new MyConnection[connectionCount];
			for( int i = 0; i < connectionCount; i++ )
			{
				String wsUrl = wsUrls.get( i % wsUrls.size() ) + SUBSCRIBE;
				URI uri = new URI( wsUrl );
				connections[i] = new MyConnection();
				connections[i].setConnection( client.open( uri, connections[i], 10, TimeUnit.SECONDS) );
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
				throw new TestException("Socket connection is not established. Please initialize "+StreamServiceEventConsumer.class.getName()+" before it starts collecting diagnostics.");
		}
	}

	@Override
	public void run( AtomicBoolean running )
	{
		if (numberOfEvents > 0)
		{
			System.out.println("Starting to listen for " + numberOfEvents + " events.");
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STARTED);
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
