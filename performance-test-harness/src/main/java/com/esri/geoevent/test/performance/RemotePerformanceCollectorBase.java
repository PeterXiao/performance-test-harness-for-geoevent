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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.jaxb.RemoteHost;
import com.esri.geoevent.test.performance.utils.KryoUtils;
import com.esri.geoevent.test.performance.utils.MessageUtils;
import com.esri.geoevent.test.performance.utils.NetworkUtils;

public class RemotePerformanceCollectorBase implements PerformanceCollector
{
	private static final String		REQUEST_SEPERATOR						= "::::";
	
	private ArrayList<Connection>	clients											= new ArrayList<Connection>();
	private RunningStateListener	listener;
	private volatile boolean			alive												= true;
	private volatile boolean			sending											= false;
	
	public RemotePerformanceCollectorBase(List<RemoteHost> hosts)
	{
		for (RemoteHost host : hosts)
		{
			try
			{
				synchronized (clients)
				{
					clients.add(new Connection(host.getHost(), host.getCommandPort(), NetworkUtils.isLocal(host.getHost())));
				}
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setRunningStateListener(RunningStateListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void listenOnCommandPort(int port, boolean isLocal)
	{
		// This should never be called on the Remote Diagnostics Collectors
		System.err.println("listenOnCommandPort() was called on the " + this.getClass().getName() + " class, which should never happen.  It commands other machines, not the other way around.");
	}

	@Override
	public void disconnectCommandPort()
	{
		// This should never be called on the Remote Diagnostics Collectors
		System.err.println("disconnectCommandPort() was called on the " + this.getClass().getName() + " class, which should never happen.  It commands other machines, not the other way around.");
	}

	// -------------------------------------------------------------
	// Start Request Commands
	// -------------------------------------------------------------

	@Override
	public synchronized void start() throws RunningException
	{
		Request request = new Request(RequestType.START);
		send(request);
	}

	@Override
	public synchronized void stop()
	{
		Request request = new Request(RequestType.STOP);
		send(request);
	}

	@Override
	public synchronized boolean isRunning()
	{
		Request request = new Request(RequestType.IS_RUNNING);
		List<Response> responses = send(request);
		boolean isRunning = true;
		for (Response response : responses)
		{
			if( response == null )
				isRunning = false;
			else
				isRunning = isRunning & BooleanUtils.toBoolean(response.getData());
		}
		return isRunning;
	}

	@Override
	public synchronized RunningStateType getRunningState()
	{
		Request request = new Request(RequestType.GET_RUNNING_STATE);
		List<Response> responses = send(request);

		// read the responses
		RunningStateType state = RunningStateType.UNAVAILABLE;
		for (Response response : responses)
		{
			RunningStateType clientState = RunningStateType.valueOf(response.getData());
			if (clientState == RunningStateType.STARTING || clientState == RunningStateType.STOPPING || clientState == RunningStateType.ERROR)
				return clientState;
			state = clientState;
		}
		return state;
	}

	@Override
	public synchronized void init(Config config) throws TestException
	{
		String requestStr = KryoUtils.toString(new Request(RequestType.INIT), Request.class);
		String dataStr = KryoUtils.toString(config, Config.class);
		List<Response> responses = send(requestStr + REQUEST_SEPERATOR + dataStr);
		checkResponseForErrors(responses);
	}

	@Override
	public synchronized void validate() throws TestException
	{
		Request request = new Request(RequestType.VALIDATE);
		List<Response> responses = send(request);
		checkResponseForErrors(responses);
	}

	@Override
	public synchronized void destroy()
	{
		Request request = new Request(RequestType.DESTROY);
		send(request);
		synchronized (clients)
		{
			alive = false;

			// cleanup
			while (clients.size() > 0)
			{
				Connection connection = clients.remove(0);
				connection.destroy();
				connection = null;
			}
		}
	}

	@Override
	public synchronized void reset()
	{
		Request request = new Request(RequestType.RESET);
		send(request);
	}

	@Override
	public synchronized int getNumberOfEvents()
	{
		Request request = new Request(RequestType.GET_NUMBER_OF_EVENTS);
		List<Response> responses = send(request);
		if (responses != null)
		{
			Response response = responses.get(0);
			return Integer.parseInt(response.getData());
		}
		return -1;
	}

	@Override
	public synchronized void setNumberOfEvents(int numberOfEvents)
	{
		Request request = new Request(RequestType.SET_NUMBER_OF_EVENTS, String.valueOf(numberOfEvents));
		send(request);
	}

	@Override
	public synchronized void setNumberOfExpectedResults(int numberOfExpectedResults)
	{
		Request request = new Request(RequestType.SET_NUMBER_OF_EXPECTED_RESULTS, String.valueOf(numberOfExpectedResults));
		send(request);
	}

	@Override
	public synchronized long getSuccessfulEvents()
	{
		Request request = new Request(RequestType.GET_SUCCESSFUL_EVENTS);
		List<Response> responses = send(request);
		if (responses != null)
		{
			Response response = responses.get(0);
			return Long.parseLong(response.getData());
		}
		return 0;
	}
	
	@Override
	public synchronized long getSuccessfulEventBytes()
	{
		Request request = new Request(RequestType.GET_SUCCESSFUL_EVENT_BYTES);
		List<Response> responses = send(request);
		if (responses != null)
		{
			Response response = responses.get(0);
			return Long.parseLong(response.getData());
		}
		return 0;
	}

	@Override
	public synchronized Map<Integer, Long[]> getTimeStamps()
	{
		Map<Integer, Long[]> timeStamps = new ConcurrentHashMap<Integer, Long[]>();
		synchronized (clients)
		{
			String requestStr = KryoUtils.toString(new Request(RequestType.GET_TIMESTAMPS), Request.class);
			Response response = null;
			String timeStampStr = null;
			for (Connection connection : clients)
			{
				response = connection.send(requestStr);
				if (response == null)
					continue;
				timeStampStr = response.getData();
				if (StringUtils.isNotEmpty(timeStampStr) && timeStampStr.length() > 2)
				{
					String[] entries = timeStampStr.split("__");
					for (String entry : entries)
					{
						String[] values = entry.split("::");
						Long[] v = new Long[values.length - 1];
						for (int i = 1; i < values.length; i++)
							v[i - 1] = Long.valueOf(values[i]) + connection.clockOffset;
						synchronized (timeStamps)
						{
							timeStamps.put(Integer.valueOf(values[0]), v);
						}
					}
				}
			}
		}
		return timeStamps;
	}

	// -------------------------------------------------------------
	// Helper Methods
	// -------------------------------------------------------------

	private synchronized List<Response> send(Request request)
	{
		if( TestHarnessExecutor.DEBUG )
			System.out.println("Sending request: " + request);
		String requestStr = KryoUtils.toString(request, Request.class);
		return send(requestStr);
	}

	private synchronized List<Response> send(String requestStr)
	{
		List<Response> responses = new ArrayList<Response>();
		synchronized (clients)
		{
			Response response = null;
			for (Connection connection : clients)
			{
				response = connection.send(requestStr);
				responses.add(response);
			}
		}
		return responses;
	}

	private void checkResponseForErrors(List<Response> responses) throws TestException
	{
		// check the response and throw exception accordingly
		if( responses != null )
		{
			for( Response reponse : responses )
			{
				if( reponse == null || reponse.getType() == ResponseType.ERROR )
					throw new TestException( (reponse != null) ? reponse.getData() : "Empty Response" );
			}
		}
	}
	
	// -------------------------------------------------------------
	// Inner Classes
	// -------------------------------------------------------------

	/**
	 * Takes care of state changes
	 * 
	 */
	private class StateDelivery extends Thread
	{
		private RunningStateListener	listener;
		private RunningState					state;

		public StateDelivery(RunningStateListener listener, RunningState state)
		{
			this.listener = listener;
			this.state = state;
		}

		public void run()
		{
			listener.onStateChange(state);
		}
	}

	/**
	 * This class deals with connectivity between all of the remote performance collectors (consumers and producers)
	 */
	private class Connection
	{
		private BufferedReader	in;
		private PrintWriter			out;
		private String					host;
		private int							port;
		private Socket					socket;
		private long						clockOffset	= 0;

		public Connection(String host, int commandPort, boolean isLocal) throws UnknownHostException, IOException
		{
			this.host = host;
			this.port = commandPort;
			System.out.println(ImplMessages.getMessage("REMOTE_COLLECTOR_CONNECTION_CREATED", host, String.valueOf(port)));
			socket = new Socket(host, port);
			socket.setSoTimeout(100);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());

			Thread thread = new Thread()
				{
					public void run()
					{
						try
						{
							while (alive)
							{
								if( ! sending )
								{
									RunningState newState = null;
									synchronized (in)
									{
										if (alive)
										{
											String responseStr = readResponse();
											if (StringUtils.isEmpty(responseStr))
												continue;
											
											Response response = KryoUtils.fromString(responseStr, Response.class);
											if( TestHarnessExecutor.DEBUG ) 
												System.out.println("Received response: \"" + response + "\"");
											if (response != null && response.getType() == ResponseType.STATE_CHANGED)
											{
												if( TestHarnessExecutor.DEBUG ) 
													System.out.println("@@@ Found a state change response: \"" + response + "\"");
												RunningStateType type = RunningStateType.fromValue(response.getData());
												if (type != RunningStateType.UNKNOWN)
													newState = new RunningState(type, getConnectionString());
											}
										}
									}
									if (newState != null)
										listener.onStateChange(newState);
									else
									{
										try
										{
											Thread.sleep(50);
										}
										catch (InterruptedException e)
										{
											break;
										}
									}
								}
								else
								{
									try
									{
										Thread.sleep(50);
									}
									catch (InterruptedException e)
									{
										break;
									}
								}
							}
						}
						catch (Exception ignored)
						{
						}
					}
				};
			thread.start();

			if (!isLocal)
			{
				DatagramPacket timeSocket = new DatagramPacket(new byte[8], 8, InetAddress.getByName(host), 7720);
				DatagramSocket sock = new DatagramSocket(7720);
				while (true)
				{
					long millisStart = System.currentTimeMillis();
					sock.send(timeSocket);
					sock.receive(timeSocket);
					long millisEnd = System.currentTimeMillis();
					byte[] buf = timeSocket.getData();
					ByteBuffer byteBuffer = ByteBuffer.wrap(buf, 0, buf.length);
					long remoteTime = byteBuffer.getLong();
					long roundTripTime = millisEnd - millisStart;
					if (roundTripTime < 2)
					{
						clockOffset = millisEnd - remoteTime;
						sock.close();
						break;
					}
					else
						System.out.println(ImplMessages.getMessage("REMOTE_COLLECTOR_CLOCK_SYNC_MSG", String.valueOf(roundTripTime)));
				}
			}
		}

		public Response send(String command)
		{
			Response response = null;
			try
			{
				sending = true;
				synchronized (in)
				{
					String messageToSend = MessageUtils.escapeNewLineCharacters(command);
					//System.out.println( "Sending request (raw): \"" + messageToSend + "\"");
					
					// send
					out.println(messageToSend);
					out.flush();
	
					String responseStr = null;
					while( responseStr == null )
					{
						// read the response
						responseStr = readResponse();
						if (responseStr == null)
							continue;
						
						response = KryoUtils.fromString(responseStr, Response.class);
						if (response == null)
							return null;
		
						if( TestHarnessExecutor.DEBUG ) 
							System.out.println("Received response: \"" + response + "\"");
						while (response.getType() == ResponseType.STATE_CHANGED)
						{
							if( TestHarnessExecutor.DEBUG ) 
								System.out.println("@@@ Found a state change response: \"" + response + "\"");
							RunningStateType type = RunningStateType.fromValue(response.getData());
							RunningState newState = new RunningState(type, getConnectionString());
							if (type != RunningStateType.UNKNOWN)
							{
								StateDelivery sd = new StateDelivery(listener, newState);
								sd.start();
							}
		
							// read some more
							responseStr = readResponse();
							response = KryoUtils.fromString(responseStr, Response.class);
							if( response == null )
								break;
						}
					}
				}
			}
			catch (Exception error)
			{
				error.printStackTrace();
				String errorMsg = error.getMessage();
				response = new Response(ResponseType.ERROR, errorMsg);
			}
			sending = false;
			return response;
		}

		private String readResponse()
		{
			String response = null;
			try
			{
				if (in != null)
				{
					response = in.readLine();
					if (response != null)
					{
						//System.out.println("Received response (raw): \"" + response + "\"");
						response = MessageUtils.unescapeNewLineCharacters(response);
					}
				}
			}
			catch (IOException error)
			{
				//error.printStackTrace();
			}
			return response;
		}

		private void destroy()
		{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(socket);

			in = null;
			out = null;
			socket = null;
			System.out.println(ImplMessages.getMessage("REMOTE_COLLECTOR_DISCONNECTION", host, String.valueOf(port)));
		}

		public String getConnectionString()
		{
			return host + ":" + port;
		}
	}
}
