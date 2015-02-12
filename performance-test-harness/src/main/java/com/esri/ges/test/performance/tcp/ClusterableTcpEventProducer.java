package com.esri.ges.test.performance.tcp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;

public class ClusterableTcpEventProducer extends DiagnosticsCollectorBase
{
	private String[]									hosts;
	private String										host;
	private int												port								= 5565;
	private int												eventsPerSec				= -1;
	private int												staggeringInterval	= 10;
	private List<EventProducerWorker>	workers;
	private Long[]										localTimeStamp			= new Long[2];
	private boolean										serverMode					= true;				// Not sure why we have this flag ?
	private SimpleSocketServer				socketServer;

	public ClusterableTcpEventProducer(int communicationPort)
	{
		super(Mode.PRODUCER);
		port = communicationPort;
		if (serverMode)
		{
			socketServer = new SimpleSocketServer();
			socketServer.setPort(port);
			socketServer.start();
		}
	}

	@Override
	public void listenOnCommandPort(int commandPort, boolean isLocal)
	{
		super.listenOnCommandPort(commandPort, isLocal);
		if (serverMode)
			socketServer.clearClientList();
	}

	@Override
	public synchronized void init(Config config) throws TestException
	{
		try
		{
			String path = config.getPropertyValue("simulationFile", "");
			loadEvents(new File(path));

			host = config.getPropertyValue("host", "localhost");
			// do we have a list of hosts
			if (host.indexOf(",") != -1)
			{
				hosts = host.split(",");
			}
			else
			{
				hosts = new String[] { host };
			}

			port = Integer.parseInt(config.getPropertyValue("port", "5565"));
			eventsPerSec = Integer.parseInt(config.getPropertyValue("eventsPerSec", "-1"));
			staggeringInterval = Integer.parseInt(config.getPropertyValue("staggeringInterval", "1"));

			// init the workers
			initWorkers();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new TestException(e.getMessage());
		}
	}

	private void initWorkers() throws TestException
	{
		workers = new ArrayList<EventProducerWorker>();

		// create workers and work
		for (int i = 0; i < hosts.length; i++)
		{
			EventProducerWorker worker = new EventProducerWorker(hosts[i], staggeringInterval);
			worker.init();
			workers.add(worker);
		}
	}

	public int getPort()
	{
		return port;
	}

	@Override
	public void validate() throws TestException
	{
		if (workers == null || workers.size() == 0)
			throw new TestException("Socket connection is not established. Please initialize TcpEventProducer before it starts collecting diagnostics.");
		if (events.isEmpty())
			throw new TestException("TcpEventProducer is missing events to produce.");
	}

	private synchronized void startTimer()
	{
		if (localTimeStamp[0] == null)
			localTimeStamp[0] = System.currentTimeMillis();
	}

	private void stopTimer()
	{
		localTimeStamp[1] = System.currentTimeMillis();
	}

	@Override
	public void run(AtomicBoolean running)
	{
		if (numberOfEvents > 0)
		{
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STARTED);

			localTimeStamp = new Long[2];

			// make some calculations
			int numOfEventsToSendPerProducer = numberOfEvents / hosts.length;
			int numOfEventsLeftOvers = numberOfEvents % hosts.length;
			int eventsPerSecPerProducer = -1;
			int eventsPerSecLeftOvers = -1;
			if (eventsPerSec > -1)
			{
				eventsPerSecPerProducer = eventsPerSec / hosts.length;
				eventsPerSecLeftOvers = eventsPerSec % hosts.length;
			}

			// execute - determine the correct number of events to send per worker
			int numOfEventsToSendForWorker = numOfEventsToSendPerProducer;
			int eventsPerSecForWorker = eventsPerSecPerProducer;
			for (EventProducerWorker worker : workers)
			{
				numOfEventsToSendForWorker = numOfEventsToSendPerProducer;
				eventsPerSecForWorker = eventsPerSecPerProducer;
				if (numOfEventsLeftOvers > 0)
				{
					numOfEventsToSendForWorker++;
					numOfEventsLeftOvers--;
				}
				if (eventsPerSecLeftOvers > 0)
				{
					eventsPerSecForWorker++;
					eventsPerSecLeftOvers--;
				}
				worker.setWorkerEventsPerSec(eventsPerSecForWorker);
				worker.setWorkerNumberOfEventsToSend(numOfEventsToSendForWorker);
				worker.start();
			}
		}
	}

	private void onNotifyComplete(EventProducerWorker worker)
	{
		if (worker == null)
			return;

		// remove from our list
		workers.remove(worker);
		worker.destroy();

		// we are all done
		if (workers.size() == 0)
		{
			synchronized (timeStamps)
			{
				timeStamps.put(timeStamps.size(), localTimeStamp);
			}
			running.set(false);
			long totalTime = (localTimeStamp[1] - localTimeStamp[0]) / 1000;
			System.out.println("Produced a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double) numberOfEvents / (double) totalTime) + " e/s).");
			if (runningStateListener != null)
				runningStateListener.onStateChange(RunningState.STOPPED);
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		events.clear();

		// cleanup
		if (workers != null)
		{
			for (EventProducerWorker worker : workers)
				worker.destroy();
			workers.clear();
			workers = null;
		}
	}

	class EventProducerWorker extends Thread
	{
		private int						staggeringInterval					= 10;
		private int						workerEventsPerSec					= -1;
		private int						workerNumberOfEventsToSend	= 0;
		private AtomicLong		workerSuccessfulEvents			= new AtomicLong(0);
		private String				host;
		private Socket				socket;
		private OutputStream	os;
		private AtomicBoolean	isRunning										= new AtomicBoolean(false);

		public EventProducerWorker(String host, int staggeringInterval)
		{
			this.host = host;
			this.staggeringInterval = staggeringInterval;
		}

		public void init() throws TestException
		{
			if (serverMode)
			{
				if (socket == null)
				{
					try
					{
						socket = socketServer.peekClient();
						if (socket == null)
							throw new TestException("Not enough tcp clients connected to the producer.");
						os = socket.getOutputStream();
					}
					catch (Throwable e)
					{
						e.printStackTrace();
						throw new TestException(e.getMessage());
					}
				}
			}
			else
			{

				try
				{
					socket = new Socket(host, port);
					os = socket.getOutputStream();
				}
				catch (Throwable e)
				{
					e.printStackTrace();
					throw new TestException(e.getMessage());
				}
			}
		}

		public void run()
		{
			int eventIx = 0;

			// send out events with delay
			if (workerEventsPerSec > -1)
			{
				// determine the events to send and delay
				// use a staggering approach to
				int staggeringInterval = (this.staggeringInterval > 0) ? this.staggeringInterval : 10;
				int eventsToSend = this.workerEventsPerSec / staggeringInterval;
				long delay = 1000 / staggeringInterval;
				// loop through all events until we are finished
				while (workerSuccessfulEvents.get() < this.workerNumberOfEventsToSend && successfulEvents.get() < numberOfEvents)
				{
					if (!isRunning.get())
					{
						isRunning.set(true);
						startTimer();
						// System.out.println( "Started the timer: [" + localTimeStamp[0] + "] NOW: [" + System.currentTimeMillis() + "]");
					}
					// add the delay
					if (delay > 0)
					{
						try
						{
							Thread.sleep(delay);
						}
						catch (InterruptedException ignored)
						{
							;
						}
					}
					if ((workerNumberOfEventsToSend - workerSuccessfulEvents.get()) < eventsToSend)
						eventsToSend += (workerNumberOfEventsToSend - workerSuccessfulEvents.get());
					// send the events
					sendEvents(eventIx, eventsToSend);

					// check if we need to break
					if (running.get() == false)
						break;
				}
			}
			// no delays just send out as fast as possible
			else
			{
				sendEvents(eventIx, this.workerNumberOfEventsToSend);
			}
			stopTimer();
			onNotifyComplete(this);
		}

		public void destroy()
		{
			if (!serverMode)
			{
				try
				{
					os.close();
				}
				catch (IOException e)
				{
					;
				}
				try
				{
					socket.close();
				}
				catch (IOException e)
				{
					;
				}
				os = null;
				socket = null;
			}
		}

		private void sendEvents(int eventIndex, int numEventsToSend)
		{
			for (int i = 0; i < numEventsToSend; i++)
			{
				// make sure we break out of the loop if we are done sending the total number
				if (workerSuccessfulEvents.get() >= this.workerNumberOfEventsToSend || successfulEvents.get() >= numberOfEvents)
					break;

				if (eventIndex == events.size())
					eventIndex = 0;
				try
				{
					byte[] data = events.get(eventIndex++).getBytes();
					if (os != null)
					{
						os.write(data);
						os.flush();
						workerSuccessfulEvents.incrementAndGet();
						successfulEvents.incrementAndGet();
					}
					if (running.get() == false)
						break;
				}
				catch (IOException e)
				{
					System.err.println(e.getMessage());
					e.printStackTrace();
					os = null;
				}
			}
		}

		public void setWorkerEventsPerSec(int workerEventsPerSec)
		{
			this.workerEventsPerSec = workerEventsPerSec;
		}

		public void setWorkerNumberOfEventsToSend(int workerNumberOfEventsToSend)
		{
			this.workerNumberOfEventsToSend = workerNumberOfEventsToSend;
		}
	}
}
