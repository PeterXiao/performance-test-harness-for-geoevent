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

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.jaxb.TestType;

public abstract class ProducerBase extends PerformanceCollectorBase implements Producer
{
	private int				eventsPerSec				= -1;
	private int				staggeringInterval	= 10;
	private TestType	testType						= TestType.UNKNOWN;
	private String 		simulationFile			= null;
	private boolean		addUniqueId					= false;
	private AtomicInteger	uniqueIdIndex		= new AtomicInteger(0);
	
	public ProducerBase()
	{
		super(Mode.Producer);
	}

	@Override
	public synchronized void init(Config config) throws TestException
	{
		try
		{
			simulationFile = config.getPropertyValue("simulationFile", "");
			loadEvents(new File(simulationFile));
			eventsPerSec = Integer.parseInt(config.getPropertyValue("eventsPerSec", "-1"));
			staggeringInterval = Integer.parseInt(config.getPropertyValue("staggeringInterval", "1"));
			testType = TestType.fromValue(config.getPropertyValue("testType"));
			addUniqueId = Boolean.parseBoolean(config.getPropertyValue("addUniqueId", "false"));
			uniqueIdIndex		= new AtomicInteger(0);
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
				System.out.println( ImplMessages.getMessage("PRODUCER_TIMED_FINISH_MSG", successfulEvents.get(), String.valueOf(totalTime), String.valueOf(((double) numberOfEvents / (double) totalTime))) );
			else
				System.out.println( ImplMessages.getMessage("PRODUCER_FINISH_MSG", successfulEvents.get()) );
			
			if (runningStateListener != null)
				runningStateListener.onStateChange(new RunningState(RunningStateType.STOPPED));
		}
	}
	
	@Override
	public void messageSent(String message)
	{
		successfulEvents.incrementAndGet();
		if( message != null )
			successfulEventBytes.addAndGet(message.getBytes().length);
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		events.clear();
	}

	public String getSimulationFile()
	{
		return simulationFile;
	}
	
	protected String augmentMessage(String message)
	{
		String newMessage = message;
		if( getEventFileType() == FileType.TEXT )
		{
			if( addUniqueId )
				newMessage = uniqueIdIndex.incrementAndGet() + "," + newMessage;
		}
		return newMessage;
	}
}
