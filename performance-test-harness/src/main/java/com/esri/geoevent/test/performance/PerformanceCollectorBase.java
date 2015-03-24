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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.jaxb.ConsumerConfig;
import com.esri.geoevent.test.performance.jaxb.ProducerConfig;
import com.esri.geoevent.test.performance.utils.KryoUtils;
import com.esri.geoevent.test.performance.utils.MessageUtils;

public abstract class PerformanceCollectorBase implements PerformanceCollector, Runnable
{
	private static final String			REQUEST_SEPERATOR	= "::::";

	protected int										numberOfEvents;
	protected int										numberOfExpectedResults;
	protected Map<Integer, Long[]>	timeStamps				= new ConcurrentHashMap<Integer, Long[]>();
	protected AtomicBoolean					running						= new AtomicBoolean(false);
	protected RunningStateListener	runningStateListener;
	protected List<String>					events						= new ArrayList<String>();
	private CommandInterpreter			commandInterpreter;
	private Thread									commandInterpreterThread;
	protected AtomicLong						successfulEvents	= new AtomicLong();
	protected AtomicLong						successfulEventBytes	= new AtomicLong();
	protected final Mode						mode;
	private Integer									commandPort;
	private Boolean									isLocal;
	private ClockSync 							clockSync = null;
	
	public PerformanceCollectorBase(Mode mode)
	{
		this.mode = mode;
	}

	public int getNumberOfEvents()
	{
		return numberOfEvents;
	}

	public void setNumberOfEvents(int numberOfEvents)
	{
		this.numberOfEvents = numberOfEvents;
	}

	public int getNumberOfExpectedResults()
	{
		return numberOfExpectedResults;
	}

	public void setNumberOfExpectedResults(int numberOfExpectedResults)
	{
		this.numberOfExpectedResults = numberOfExpectedResults;
	}

	public long getSuccessfulEvents()
	{
		return successfulEvents.get();
	}

	public long getSuccessfulEventBytes()
	{
		return successfulEventBytes.get();
	}
	
	public synchronized Map<Integer, Long[]> getTimeStamps()
	{
		return timeStamps;
	}

	@Override
	public void start() throws RunningException
	{
		running = new AtomicBoolean(true);
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void stop()
	{
		running.set(false);
	}

	@Override
	public boolean isRunning()
	{
		return running.get();
	}

	@Override
	public void destroy()
	{
		reset();
	}

	@Override
	public void reset()
	{
		successfulEvents.set(0);
		successfulEventBytes.set(0);
		synchronized (timeStamps)
		{
			timeStamps.clear();
		}
	}

	@Override
	public RunningStateType getRunningState()
	{
		return running.get() ? RunningStateType.STARTED : RunningStateType.STOPPED;
	}

	@Override
	public void setRunningStateListener(RunningStateListener listener)
	{
		this.runningStateListener = listener;
	}

	protected void loadEvents(File file) throws TestException
	{
		if (!events.isEmpty())
			events.clear();

		BufferedReader input = null;
		try
		{
			input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while ((line = input.readLine()) != null)
				events.add(line + "\n");
		}
		catch (Exception e)
		{
			throw new TestException(e.getMessage());
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
					input = null;
				}
				catch (IOException e)
				{
					;
				}
			}
		}
	}

	@Override
	public void listenOnCommandPort(int commandPort, boolean isLocal)
	{
		// keep for re-initialization
		if( this.commandPort == null )
			this.commandPort = commandPort;
		if( this.isLocal == null )
			this.isLocal = isLocal;
		
		System.out.println(ImplMessages.getMessage("PERFORMANCE_COLLECTOR_LISTENING_CMD_PORT_MSG", String.valueOf(commandPort)));
		if( commandInterpreter != null )
		{
			commandInterpreter.stop();
			commandInterpreter.destroy();
			commandInterpreter = null;
		}
		commandInterpreter = new CommandInterpreter(commandPort);
		setRunningStateListener(commandInterpreter);
		if( commandInterpreterThread != null )
		{
			commandInterpreterThread.interrupt();
			commandInterpreterThread = null;
		}
		commandInterpreterThread = new Thread(commandInterpreter);
		commandInterpreterThread.start();
		
		if (!isLocal)
		{
			if( clockSync != null )
			{
				clockSync.stop();
				clockSync = null;
			}
			clockSync = new ClockSync();
			Thread thread = new Thread(clockSync);
			thread.start();
		}
	}

	@Override
	public void disconnectCommandPort()
	{
		commandInterpreter.stop();
		commandInterpreter.destroy();
		commandInterpreter = null;
		
		commandInterpreterThread.interrupt();
		commandInterpreterThread = null;
		
		if( clockSync != null )
		{
			clockSync.stop();
			clockSync = null;
		}
		
		System.out.println(ImplMessages.getMessage("PERFORMANCE_COLLECTOR_DISCONNECTED_CMD_PORT_MSG"));
	}

	private class CommandInterpreter implements Runnable, RunningStateListener
	{
		int											port;
		private BufferedReader	in;
		private PrintWriter			out;
		private ServerSocket		server;
		private AtomicBoolean 	isRunning = new AtomicBoolean(false);
		
		public CommandInterpreter(int commandPort)
		{
			this.port = commandPort;
			isRunning = new AtomicBoolean(true);
		}

		public void run()
		{
			try
			{
				server = new ServerSocket(port);
				while (isRunning.get())
				{
					// System.out.println("Listening for a connection from the orchestrator.");
					Socket commandSocket = server.accept();
					try
					{
						commandSocket.setSoTimeout(50);
						in = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
						out = new PrintWriter(commandSocket.getOutputStream());
						while (isRunning.get() && in != null)
						{
							String command = null;
							try
							{
								command = in.readLine();
							}
							catch (Exception ignored)
							{
							}
							if (StringUtils.isEmpty(command))
								continue;

							// System.out.println("Received request (raw) \"" + command +"\"");
							command = MessageUtils.unescapeNewLineCharacters(command);

							String requestStr = command;
							String additionalDataStr = null;
							if (command.contains(REQUEST_SEPERATOR))
							{
								String[] requestSplitter = command.split(REQUEST_SEPERATOR);
								if (requestSplitter == null || requestSplitter.length < 2)
									continue;

								requestStr = requestSplitter[0];
								additionalDataStr = requestSplitter[1];
							}

							// parse out the request object
							Request request = KryoUtils.fromString(requestStr, Request.class);
							if (TestHarnessExecutor.DEBUG)
								System.out.println("Received request \"" + request + "\"");
							if (request == null)
							{
								System.err.println(ImplMessages.getMessage("PERFORMANCE_COLLECTOR_REQUEST_PARSE_ERROR"));
								continue;
							}

							// request action switch
							Response response = null;
							switch (request.getType())
							{
								case INIT:
									response = new Response(ResponseType.OK);
									try
									{
										// parse out the init data
										if (additionalDataStr == null)
										{
											String errorMsg = ImplMessages.getMessage("PERFORMANCE_COLLECTOR_INIT_PARSE_ERROR");
											response = new Response(ResponseType.ERROR, errorMsg);
											System.err.println(errorMsg);
											respond(response);
											continue;
										}
										Config config = null;
										if (mode == Mode.Consumer)
											config = KryoUtils.fromString(additionalDataStr, ConsumerConfig.class);
										else if (mode == Mode.Producer)
											config = KryoUtils.fromString(additionalDataStr, ProducerConfig.class);
										if (TestHarnessExecutor.DEBUG)
											System.out.println("Received additional config " + config);
										init(config);
									}
									catch (Exception ex)
									{
										response = new Response(ResponseType.ERROR, ex.getMessage());
									}
									finally
									{
										respond(response);
									}
									break;

								case START:
									response = new Response(ResponseType.OK);
									try
									{
										start();
									}
									catch (Exception ex)
									{
										response = new Response(ResponseType.ERROR, ex.getMessage());
									}
									finally
									{
										respond(response);
									}
									break;

								case STOP:
									response = new Response(ResponseType.OK);
									try
									{
										stop();
									}
									catch (Exception ex)
									{
										response = new Response(ResponseType.ERROR, ex.getMessage());
									}
									finally
									{
										respond(response);
									}
									break;

								case IS_RUNNING:
									response = new Response(ResponseType.OK);
									if (isRunning())
										response.setData("true");
									else
										response.setData("false");
									respond(response);
									break;

								case GET_RUNNING_STATE:
									RunningStateType st = getRunningState();
									response = new Response(ResponseType.OK, st.toString());
									respond(response);
									break;

								case VALIDATE:
									response = new Response(ResponseType.OK);
									try
									{
										validate();
									}
									catch (Exception ex)
									{
										response = new Response(ResponseType.ERROR, ex.getMessage());
									}
									finally
									{
										respond(response);
									}
									break;

								case DESTROY:
									reset();
									response = new Response(ResponseType.OK);
									respond(response);
									destroy();
									listenOnCommandPort(commandPort, isLocal);
									break;

								case RESET:
									reset();
									response = new Response(ResponseType.OK);
									respond(response);
									break;

								case GET_NUMBER_OF_EVENTS:
									response = new Response(ResponseType.OK, String.valueOf(getNumberOfEvents()));
									respond(response);
									break;

								case GET_SUCCESSFUL_EVENTS:
									response = new Response(ResponseType.OK, String.valueOf(getSuccessfulEvents()));
									respond(response);
									break;
								
								case GET_SUCCESSFUL_EVENT_BYTES:
									response = new Response(ResponseType.OK, String.valueOf(getSuccessfulEventBytes()));
									respond(response);
									break;

								case SET_NUMBER_OF_EVENTS:
									setNumberOfEvents(Integer.parseInt(request.getData()));
									response = new Response(ResponseType.OK);
									respond(response);
									break;

								case SET_NUMBER_OF_EXPECTED_RESULTS:
									setNumberOfExpectedResults(Integer.parseInt(request.getData()));
									response = new Response(ResponseType.OK);
									respond(response);
									break;

								case GET_TIMESTAMPS:
									// TODO: We need a better way to send this across the wire
									// build the time stamps string
									Map<Integer, Long[]> values = getTimeStamps();
									StringBuilder stringBuilder = new StringBuilder();
									for (Integer key : values.keySet())
									{
										stringBuilder.append("__" + key);
										Long[] valueArray = values.get(key);
										for (Long l : valueArray)
											stringBuilder.append("::" + l);
									}

									String timeStampsStr = null;
									if (stringBuilder.length() > 2)
										timeStampsStr = stringBuilder.substring(2);

									response = new Response(ResponseType.OK, timeStampsStr);
									respond(response);
									break;

								case UNKNOWN:
									String erroMsg = ImplMessages.getMessage("PERFORMANCE_COLLECTOR_UNKNOWN_REQUEST_ERROR", RequestType.UNKNOWN, command);
									response = new Response(ResponseType.ERROR, erroMsg);
									System.err.println(erroMsg);
									respond(response);
									continue;
							}
						}
					}
					catch (Exception ex)
					{
						if (ex.getMessage().equals("Connection reset"))
						{
							System.out.println(ImplMessages.getMessage("PERFORMANCE_COLLECTOR_DISCONNECTED_MSG"));
							reset();
						}
						else
							ex.printStackTrace();
					}
				}
			}
			catch (SocketException error)
			{
				// ignored
			}
			catch (Exception error)
			{
				error.printStackTrace();
			}
		}

		@Override
		public void onStateChange(RunningState newState)
		{
			if (out != null)
			{
				Response response = new Response(ResponseType.STATE_CHANGED, newState.getType().toString());
				respond(response);
				if (TestHarnessExecutor.DEBUG)
					System.out.println("@@@ State change event(" + newState + ")  sent!");
			}
		}

		private void respond(Response response)
		{
			if (out != null)
			{
				if (TestHarnessExecutor.DEBUG)
					System.out.println("Sending response: \"" + response + "\"");
				String responseStr = KryoUtils.toString(response, Response.class);
				responseStr = MessageUtils.escapeNewLineCharacters(responseStr);
				// if( TestHarnessExecutor.DEBUG )
				// System.out.println("Sending response (raw): \"" + responseStr + "\"");
				out.println(responseStr);
				out.flush();
			}
			else
				System.err.println("Failed to send the response: \"" + response + "\". The Output stream is NULL!");
		}
		
		public void stop()
		{
			isRunning.set(false);
		}
		
		private void destroy()
		{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(server);
		}
	}
}
