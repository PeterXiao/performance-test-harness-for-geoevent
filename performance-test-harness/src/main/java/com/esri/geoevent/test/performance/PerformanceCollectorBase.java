package com.esri.geoevent.test.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

public abstract class PerformanceCollectorBase implements PerformanceCollector, Runnable
{
	private static final String 		REQUEST_SEPERATOR = "::::";

	protected int										numberOfEvents;
	protected int										numberOfExpectedResults;
	protected Map<Integer, Long[]>	timeStamps				= new ConcurrentHashMap<Integer, Long[]>();
	protected AtomicBoolean					running						= new AtomicBoolean(false);
	protected RunningStateListener	runningStateListener;
	protected List<String>					events						= new ArrayList<String>();
	private CommandInterpreter			commandInterpreter;
	protected AtomicLong						successfulEvents	= new AtomicLong();
	protected final Mode						mode;

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

	public void listenOnCommandPort(int commandPort, boolean isLocal)
	{
		// System.out.println("Listening on port " + commandPort);
		commandInterpreter = new CommandInterpreter(commandPort);
		setRunningStateListener(commandInterpreter);
		Thread thread = new Thread(commandInterpreter);
		thread.start();
		if (!isLocal)
		{
			thread = new Thread(new ClockSync());
			thread.start();
		}
	}

	private class CommandInterpreter implements Runnable, RunningStateListener
	{
		int											port;
		private BufferedReader	in;
		private PrintWriter			out;

		public CommandInterpreter(int commandPort)
		{
			this.port = commandPort;
		}

		public void run()
		{
			ServerSocket s;
			try 
			{
				s = new ServerSocket( port );
				while(true)
				{
					//System.out.println("Listening for a connection from the orchestrator.");
					Socket commandSocket = s.accept();
					try
					{
						commandSocket.setSoTimeout(50);
						in = new BufferedReader( new InputStreamReader( commandSocket.getInputStream() ) );
						out = new PrintWriter( commandSocket.getOutputStream() );
						while(in != null)
						{
							//we need to read multiple lines efficiently
							StringWriter sw = new StringWriter();
							String command = null;
							try
							{
								char[] buffer = new char[1024 * 4];
								int n = 0;
								while (-1 != (n = in.read(buffer))) {
								    sw.write(buffer, 0, n);
								}
							}
							catch(SocketTimeoutException ex)
							{
								//ignore
							}
							finally
							{
								command = sw.toString();
							}
							
							if( StringUtils.isEmpty(command))
								continue;
							
							//System.out.println("Received command \"" + command + "\"");
							synchronized(out)
							{
								String requestStr = command;
								String additionalDataStr = null;
								if( command.contains(REQUEST_SEPERATOR) )
								{
									String[] requestSplitter = command.split(REQUEST_SEPERATOR);
									if( requestSplitter == null || requestSplitter.length < 2 )
										continue;
									
									requestStr = requestSplitter[0];
									additionalDataStr = requestSplitter[1];
								}
								
								// parse out the request object
								Request request = KryoUtils.fromString(requestStr, Request.class);
								if( request == null )
								{
									System.err.println( "Failed to parse out the Request object!" );
									continue;
								}
								
								// request action switch
								Response response = null;
								switch( request.getType() )
								{
									case INIT:
										
										response = new Response(ResponseType.OK);
										try
										{
											// parse out the init data
											if( additionalDataStr == null )
											{
												String errorMsg = "Failed to parse out the Additional Data for INIT!";
												response = new Response(ResponseType.ERROR, errorMsg);
												System.err.println( errorMsg );
												respond(response);
												continue;
											}
											Config config = null;
											if( mode == Mode.CONSUMER)
												config = KryoUtils.fromString(additionalDataStr, ConsumerConfig.class);
											else if( mode == Mode.PRODUCER)
												config = KryoUtils.fromString(additionalDataStr, ProducerConfig.class);
											
											init(config);
										}
										catch(Exception ex)
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
										catch(Exception ex)
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
										catch(Exception ex)
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
										if(isRunning())
											response.setData( "true" );
										else
											response.setData( "false" );
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
										catch(Exception ex)
										{
											response = new Response(ResponseType.ERROR, ex.getMessage());
										}
										finally
										{
											respond(response);
										}
										break;
									
									case DESTROY:
										destroy();
										
										response = new Response(ResponseType.OK);
										respond(response);
										
										//close the input and output stream
										IOUtils.closeQuietly(in);
										in = null;
										IOUtils.closeQuietly(out);
										out= null;
										
										//continue to the top of the while loop
										continue;
										
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
										for( Integer key : values.keySet() )
										{
											stringBuilder.append("__"+key);
											Long[] valueArray = values.get(key);
											for( Long l : valueArray )
												stringBuilder.append("::"+l);
										}
										
										String timeStampsStr = null;
										if(stringBuilder.length()>2)
											timeStampsStr = stringBuilder.substring(2);
										
										response = new Response(ResponseType.OK, timeStampsStr);
										respond(response);
										break;
										
									case UNKNOWN:
										String erroMsg = "Could not recognize the current Request type \"" + RequestType.UNKNOWN + "\". Discarding the current request: " + command;
										response = new Response(ResponseType.ERROR, erroMsg);
										System.err.println(erroMsg);
										respond(response);
										continue;
								}
							}
						}
					}
					catch(IOException ex)
					{
						if( ex.getMessage().equals("Connection reset") )
						{
							System.out.println( Messages.getMessage("ORCHESTRATOR_DISCONNECTED_MSG") );
							reset();
						}
						else
							ex.printStackTrace();
					}
				}
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		@Override
		public void onStateChange(RunningState newState)
		{
			if (out != null)
			{
				Response response = new Response(ResponseType.STATE_CHANGED, newState.getType().toString());
				respond(response);
			}
		}
		
		private void respond(Response response)
		{
			if( out != null )
			{
				synchronized (out)
				{
					String responseStr = KryoUtils.toString(response, Response.class);
					out.println(responseStr);
					out.flush();
				}
			}
		}
	}
}
