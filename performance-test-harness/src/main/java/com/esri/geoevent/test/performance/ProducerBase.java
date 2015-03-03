package com.esri.geoevent.test.performance;

import java.io.File;

import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.jaxb.TestType;

public abstract class ProducerBase extends PerformanceCollectorBase implements Producer
{
	private int				eventsPerSec				= -1;
	private int				staggeringInterval	= 10;
	private TestType	testType						= TestType.UNKNOWN;

	public ProducerBase()
	{
		super(Mode.Producer);
	}

	@Override
	public synchronized void init(Config config) throws TestException
	{
		try
		{
			String path = config.getPropertyValue("simulationFile", "");
			loadEvents(new File(path));
			eventsPerSec = Integer.parseInt(config.getPropertyValue("eventsPerSec", "-1"));
			staggeringInterval = Integer.parseInt(config.getPropertyValue("staggeringInterval", "1"));
			testType = TestType.fromValue(config.getPropertyValue("testType"));
		}
		catch( Exception error )
		{
			error.printStackTrace();
			throw new TestException(error.getMessage());
		}
	}

	@Override
	public void validate() throws TestException
	{
		if (events.isEmpty())
			throw new TestException("TcpEventProducer is missing events to produce.");
	}

	@Override
	public void run()
	{
		if (numberOfEvents > 0)
		{
			if (runningStateListener != null)
				runningStateListener.onStateChange(new RunningState(RunningStateType.STARTED));

			int eventIx = 0;
			Long[] timeStamp = new Long[2];
			timeStamp[0] = System.currentTimeMillis();

			// send out events with delay
			if (eventsPerSec > -1)
			{
				// determine the events to send and delay
				// use a staggering approach to
				int staggeringInterval = (this.staggeringInterval > 0) ? this.staggeringInterval : 10;
				int eventsToSend = eventsPerSec / staggeringInterval;
				long delay = 1000 / staggeringInterval;
				long targetTimeStamp = timeStamp[0];
				long sleepTime = 0;

				// loop through all events until we are finished
				while (successfulEvents.get() < numberOfEvents)
				{
					targetTimeStamp = targetTimeStamp + delay;

					// send the events
					eventIx = sendEvents(eventIx, eventsToSend);

					sleepTime = targetTimeStamp - System.currentTimeMillis();
					// add the delay - if necessary
					if (sleepTime > 0)
					{
						try
						{
							Thread.sleep(sleepTime);
						}
						catch (InterruptedException ignored)
						{
							;
						}
					}

					// check if we need to break
					if (running.get() == false)
						break;
				}
			}
			// no delays just send out as fast as possible
			else
			{
				sendEvents(eventIx, numberOfEvents);
			}
			timeStamp[1] = System.currentTimeMillis();
			synchronized (timeStamps)
			{
				timeStamps.put(timeStamps.size(), timeStamp);
			}
			running.set(false);
			double totalTime = (timeStamp[1] - timeStamp[0]) / 1000;
			if( testType == TestType.TIME )
				System.out.println( Messages.getMessage("PRODUCER_TIMED_FINISH_MSG", successfulEvents.get(), String.valueOf(totalTime), String.valueOf(((double) numberOfEvents / (double) totalTime))) );
			else
				System.out.println( Messages.getMessage("PRODUCER_FINISH_MSG", successfulEvents.get()) );
			
			if (runningStateListener != null)
				runningStateListener.onStateChange(new RunningState(RunningStateType.STOPPED));
		}
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		events.clear();
	}

}
