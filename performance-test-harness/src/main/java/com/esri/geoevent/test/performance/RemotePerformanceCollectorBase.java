package com.esri.geoevent.test.performance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;

import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.jaxb.RemoteHost;
import com.esri.geoevent.test.performance.utils.KryoUtils;
import com.esri.geoevent.test.performance.utils.NetworkUtils;

public class RemotePerformanceCollectorBase implements PerformanceCollector 
{
	private static final String ERROR = "ERROR:";
	protected static final String STATE_LABEL = "STATE:";
	private ArrayList<Connection> clients = new ArrayList<Connection>();
	private RunningStateListener listener;
	private volatile boolean alive = true;

	private class StateDelivery extends Thread
	{
		private RunningStateListener listener;
		private RunningState state;

		public StateDelivery(RunningStateListener listener,	RunningState state)
		{
			this.listener = listener;
			this.state = state;
		}

		public void run()
		{
			listener.onStateChange(state);
		}
	}

	private class Connection
	{
		private BufferedReader in;
		private PrintWriter out;
		private String host;
		private int port;
		private Socket socket;
		private long clockOffset = 0;

		public Connection (String host, int commandPort, boolean isLocal) throws UnknownHostException, IOException
		{
			this.host = host;
			this.port = commandPort;
			System.out.println("Creating remote connection to " + host + ":"+port);
			socket = new Socket( host, port);
			in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			out = new PrintWriter( socket.getOutputStream() );
			Thread t = new Thread()
			{
				public void run()
				{
					try
					{
						while(alive)
						{
							RunningState newState = null;
							synchronized(in)
							{
								if( in.ready() )
								{
									String msg = in.readLine();
									if( msg != null && msg.startsWith(STATE_LABEL) )
									{
										//System.out.println("Received state msg: " + msg);
										newState = RunningState.valueOf(msg.substring(STATE_LABEL.length()));
									}
								}
							}
							if( newState != null )
								listener.onStateChange(newState);
							else
							{
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) 
								{
									break;
								}
							}
						}
					}catch(IOException ex)
					{
					}
				}
			};
			t.start();
			
			if (!isLocal)
			{
				//System.out.println("getting remote clock time.");
				DatagramPacket timeSocket = new DatagramPacket( new byte[8], 8, InetAddress.getByName(host), 7720);
				DatagramSocket sock = new DatagramSocket(7720);
				while(true)
				{
					long millisStart = System.currentTimeMillis();
					sock.send(timeSocket);
					sock.receive(timeSocket);
					long millisEnd = System.currentTimeMillis();
					byte[] buf = timeSocket.getData();
					ByteBuffer byteBuffer = ByteBuffer.wrap(buf,0,buf.length);
					long remoteTime = byteBuffer.getLong();
					long roundTripTime = millisEnd - millisStart;
					if( roundTripTime < 2 )
					{
						clockOffset = millisEnd - remoteTime;
						sock.close();
						break;
					}
					else
						System.out.println("While getting the remote clock time, round trip was " + roundTripTime + ", trying again.");
				}
			}
			//System.out.println("clock offset is " + clockOffset);
		}

		public String send( String command )
		{
			synchronized(in)
			{
				//System.out.println("Sending command " + command + " to " + host);
				out.println(command);
				out.flush();
				//System.out.println("The command was sent.");
				String response;
				try
				{
					//System.out.println("Looking for the response . . .");
					response = in.readLine();
					//System.out.println("Response was " + response);
					while( response == null || response.startsWith(STATE_LABEL) )
					{
						RunningState newState = RunningState.valueOf(response.substring(STATE_LABEL.length()));
						if( newState != null )
						{
							StateDelivery sd = new StateDelivery(listener,newState);
							sd.start();
						}
						//System.out.println("That's a state change message.  Let's listen some more.");
						response = in.readLine();
						//System.out.println("Response was " + response);
					}
				} catch (IOException e)
				{
					e.printStackTrace();
					return ERROR+"IOException:"+e.getMessage();
				}
				//System.out.println("Received response " + response + " from " + host);
				return response;
			}
		}

		private void destroy()
		{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(socket);
			
			in = null;
			out = null;
			socket = null;
		}
		
		public String getHost()
		{
			return host;
		}
	}

	public RemotePerformanceCollectorBase(List<RemoteHost> hosts)
	{
		for( RemoteHost host : hosts )
		{
			try {
				synchronized(clients)
				{
					clients.add( new Connection( host.getHost(), host.getCommandPort(), NetworkUtils.isLocal(host.getHost()) ) );
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start() throws RunningException 
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				String result = connection.send("start");
				if( result.startsWith(ERROR) )
					throw new RunningException(result.substring(ERROR.length()));
			}
		}
	}

	@Override
	public long getSuccessfulEvents()
	{
		long value = 0;
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				String result = connection.send("getSuccessfulEvents");
				if( result != null )
					return Long.valueOf(result);
			}
		}
		return value;
	}

	@Override
	public void stop() 
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				connection.send("stop");
			}
		}
	}

	@Override
	public boolean isRunning() 
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				String result = connection.send("isRunning");
				if( result.equalsIgnoreCase("TRUE") )
					return true;
			}
		}
		return false;
	}

	@Override
	public RunningState getRunningState() 
	{
		RunningState state = RunningState.UNAVAILABLE;
		synchronized(clients)
		{
			try
			{
				for( Connection connection: clients )
				{
					String result = connection.send("getRunningState");
					RunningState clientState = RunningState.valueOf(result);
					if( clientState == RunningState.STARTING || clientState == RunningState.STOPPING || clientState == RunningState.ERROR )
						return clientState;
					state = clientState;
				}
			}catch(IllegalArgumentException ex)
			{
				return RunningState.UNAVAILABLE;
			}
		}
		return state;
	}

	@Override
	public String getStatusDetails()
	{
		String details = "";
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				String result = connection.send("getStatusDetails");
				if( details.length() > 0 )
					details += ",";
				details += connection.getHost() + ":" + result;
			}
		}
		return details;
	}

	@Override
	public void setRunningStateListener(RunningStateListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void init(Config config) throws TestException 
	{
		String data = KryoUtils.toString(config, Config.class);
		//System.out.println( "Sending Data: " + data);
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				connection.send("init:"+data);
			}
		}
	}

	@Override
	public void validate() throws TestException 
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				connection.send("validate");
			}
		}
	}

	@Override
	public void destroy() 
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				connection.send("destroy");
			}
			
			alive = false;
			
			// cleanup
			while( clients.size() > 0 )
			{
				Connection connection = clients.remove(0);
				connection.destroy();
				connection = null;
			}
		}
	}

	@Override
	public void reset() 
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				connection.send("reset");
			}
		}
	}

	@Override
	public int getNumberOfEvents() 
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				String result = connection.send("getNumberOfEvents");
				int intResult = Integer.parseInt(result);
				return intResult;
			}
		}
		return -1;
	}

	@Override
	public void setNumberOfEvents(int numberOfEvents)
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				connection.send("setNumberOfEvents:"+numberOfEvents);
			}
		}
	}

	@Override
	public void setNumberOfExpectedResults(int numberOfExpectedResults)
	{
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				connection.send("setNumberOfExpectedResults:"+numberOfExpectedResults);
			}
		}
	}
	
	@Override
	public Map<Integer, Long[]> getTimeStamps() 
	{
		Map<Integer,Long[]> timeStamps = new ConcurrentHashMap<Integer, Long[]>();
		synchronized(clients)
		{
			for( Connection connection: clients )
			{
				String result = connection.send("getTimeStamps");
				if( result.length() > 2 )
				{
					String[] entries = result.split("__");
					for( String entry : entries )
					{
						String[] values = entry.split("::");
						Long[] v = new Long[values.length-1];
						for( int i = 1; i < values.length; i++ )
							v[i-1] = Long.valueOf(values[i]) + connection.clockOffset;
						synchronized (timeStamps)
						{
							timeStamps.put( Integer.valueOf(values[0]), v);
						}
					}
				}
			}
		}
		return timeStamps;
	}

	@Override
	public void listenOnCommandPort(int port, boolean isLocal)
	{
		System.err.println("listenOnCommandPort() was called on the " + this.getClass().getName() + " class, which should never happen.  It commands other machines, not the other way around.");
		// This should never be called on the Remote Diagnostics Collectors
	}
}