package com.esri.ges.test.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;

public abstract class DiagnosticsCollectorBase implements DiagnosticsCollector, InterruptableRunnable
{
	private static final String ERROR = "ERROR:";
	private static final String STATE_LABEL = "STATE:";
	private static final String OK = "OK";

	protected int numberOfEvents;
	protected int numberOfExpectedResults;
	protected Map<Integer, Long[]> timeStamps = new ConcurrentHashMap<Integer, Long[]>();
	protected AtomicBoolean running = new AtomicBoolean(false);
	protected RunningStateListener runningStateListener;
	protected List<String> events = new ArrayList<String>();
	private CommandInterpreter commandInterpreter;
	protected AtomicLong successfulEvents = new AtomicLong();
	protected Mode mode = Mode.UNKNOWN;
	protected long timeOutInSec = 10;
	
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

	public void setTimeOutInSec(long timeOutInSec)
	{
		this.timeOutInSec = timeOutInSec;
	}
	
	public synchronized Map<Integer, Long[]> getTimeStamps()
	{
		return timeStamps;
	}
	
	class InterruptableThread extends Thread
	{
		AtomicBoolean running;
		InterruptableRunnable worker;
		
		public InterruptableThread( AtomicBoolean running, InterruptableRunnable worker )
		{
			this.running = running;
			this.worker = worker;
		}
		
		public void run()
		{
			worker.run(running);
		}
	}

	@Override
	public void start() throws RunningException
	{
		running = new AtomicBoolean(true);
		Thread t = new InterruptableThread(running,this);
		t.start();
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
	public RunningState getRunningState()
	{
		return running.get() ? RunningState.STARTED : RunningState.STOPPED;
	}

	@Override
	public String getStatusDetails()
	{
		return null;
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
		//System.out.println("Listening on port " + commandPort);
		commandInterpreter = new CommandInterpreter( commandPort );
		setRunningStateListener(commandInterpreter);
		Thread t = new Thread(commandInterpreter);
		t.start();
		if (!isLocal)
		{
			t = new Thread(new ClockSync());
			t.start();
		}
	}

	private class CommandInterpreter implements Runnable, RunningStateListener
	{
		int port;
		private BufferedReader in;
		private PrintWriter out;
		private long lastSuccessCount = 0;
		private long lastSuccessIncrement = 0;

		public CommandInterpreter( int commandPort )
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
						commandSocket.setSoTimeout(100);
						in = new BufferedReader( new InputStreamReader( commandSocket.getInputStream() ) );
						out = new PrintWriter( commandSocket.getOutputStream() );
						while(in != null)
						{
							if( running.get() )
							{
								long currentValue = successfulEvents.get();
								long now = System.currentTimeMillis();
									
								//System.out.println("Current count = " + currentValue + " last timestamp = " + lastSuccessIncrement + ", which is " + (now-lastSuccessIncrement)/1000 + " seconds.");
								if( lastSuccessCount == currentValue )
								{
									if( lastSuccessIncrement == 0 )
										lastSuccessIncrement = now;
									if( now - lastSuccessIncrement > timeOutInSec*1000 && mode == Mode.CONSUMER)
									{
										System.out.println("Timeout reached.  Stopping test because data apparently stopped flowing.");
										running.set(false);
										Long[] timeStamp = { lastSuccessIncrement, lastSuccessIncrement };
										synchronized (timeStamps)
										{
											timeStamps.put(timeStamps.size(), timeStamp);
										}
										lastSuccessIncrement = 0;
										System.out.println("Checking the consumed a total of: " + successfulEvents.get() + " events.");
										runningStateListener.onStateChange(RunningState.STOPPED);
									}
//									else
//									{
//										long seconds = (now - lastSuccessIncrement) / 1000;
//										if( seconds > 0 )
//											System.out.println("It's been " + seconds + " seconds since the last message.");
//									}
								}
								else
								{
									lastSuccessIncrement = now;
									lastSuccessCount = currentValue;
									//System.out.println("Updating values to successtimestamp="+lastSuccessIncrement+", and lastCount="+lastSuccessCount);
								}
							}
							String command = null;
							try
							{
								command = in.readLine();
							}catch(SocketTimeoutException ex)
							{
								//continue;
							}
							if( command == null )
								continue;
							//System.out.println("Received command \"" + command + "\"");
							synchronized(out)
							{
								if( command.equals("start") )
								{
									try
									{
										start();
										out.println(OK);
									}catch(RunningException ex)
									{
										out.println(ERROR+ex.getMessage());
									}
								}
								else if( command.equals("stop"))
								{
									stop();
									out.println(OK);
								}
								else if( command.equals("isRunning"))
								{
									boolean b = isRunning();
									if(b)
										out.println("TRUE");
									else
										out.println("FALSE");
								}
								else if( command.equals("getRunningState"))
								{
									RunningState st = getRunningState();
									out.println(st.toString());
								}
								else if( command.equals("getStatusDetails"))
								{
									String details = getStatusDetails();
									out.println(details);
								}
								else if( command.startsWith("init:"))
								{
									command = command.substring("init:".length());
									String[] propertyStrings = command.split("__");
									Properties props = new Properties();
									for( String propertyString : propertyStrings )
									{
										String[] pair = propertyString.split("::");
										if( pair.length == 2 )
											props.setProperty(pair[0], pair[1]);
									}
									try
									{
										init(props);
										out.println(OK);
									}catch(TestException ex)
									{
										out.println(ERROR+ex.getMessage());
									}
								}
								else if( command.equals("validate"))
								{
									try
									{
										validate();
										out.println(OK);
									}catch(TestException ex)
									{
										out.println(ERROR+ex.getMessage());
									}
								}
								else if( command.equals("destroy"))
								{
									destroy();
									out.println(OK);
									out.flush();
									
									//close the input and output stream
									IOUtils.closeQuietly(in);
									in = null;
									IOUtils.closeQuietly(out);
									out= null;
									
									//continue to the top of the while loop
									continue;
								}
								else if( command.equals("reset"))
								{
									reset();
									out.println(OK);
								}
								else if( command.equals("getNumberOfEvents"))
								{
									int num = getNumberOfEvents();
									out.println(String.valueOf(num));
								}
								else if( command.equals("getSuccessfulEvents"))
								{
									long num = getSuccessfulEvents();
									out.println(String.valueOf(num));
								}
								else if( command.startsWith("setNumberOfEvents"))
								{
									if( command.length() > "setNumberOfEvents:".length() )
									{
										String param = command.substring("setNumberOfEvents:".length());
										setNumberOfEvents(Integer.parseInt(param));
									}
									out.println(OK);
								}
								else if( command.startsWith("setNumberOfExpectedResults"))
								{
									if( command.length() > "setNumberOfExpectedResults:".length() )
									{
										String param = command.substring("setNumberOfExpectedResults:".length());
										setNumberOfExpectedResults(Integer.parseInt(param));
									}
									out.println(OK);
								}
								else if( command.startsWith("setTimeOutInSec"))
								{
									if( command.length() > "setTimeOutInSec:".length() )
									{
										String param = command.substring("setTimeOutInSec:".length());
										setTimeOutInSec(Long.parseLong(param));
									}
									out.println(OK);
								}
								else if( command.equals("getTimeStamps"))
								{
									Map<Integer, Long[]> values = getTimeStamps();
									StringBuilder b = new StringBuilder();
									for( Integer key : values.keySet() )
									{
										b.append("__"+key);
										Long[] valueArray = values.get(key);
										for( Long l : valueArray )
											b.append("::"+l);
									}
									if(b.length()>2)
										out.println(b.substring(2));
									else
										out.println();
								}
								out.flush();
							}
						}
					}catch(IOException ex)
					{
						if( ex.getMessage().equals("Connection reset") )
						{
							System.out.println("The orchestrator disconnected.");
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
			if( out != null )
			{
				synchronized (out)
				{
					//System.out.println("Sending state change command: " + STATE_LABEL+newState);
					if( out != null && newState != null)
					{
						out.println(STATE_LABEL+newState);
						out.flush();
					}
				}
			}
		}
	}
}