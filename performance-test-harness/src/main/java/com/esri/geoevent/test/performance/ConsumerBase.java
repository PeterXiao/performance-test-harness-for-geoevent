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

import java.util.concurrent.atomic.AtomicInteger;

import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.jaxb.ConsumerConfig;
import com.esri.geoevent.test.performance.jaxb.TestType;

public abstract class ConsumerBase extends PerformanceCollectorBase implements Consumer
{
	private AtomicInteger	eventIx								= new AtomicInteger(0);
	private Long[]				timeStamp							= null;
	private long					lastSuccessCount			= 0;
	private long					lastSuccessIncrement	= 0;
	private long					timeOutInSec					= 10;
	private TestType			testType							= TestType.UNKNOWN;
	private int 					totalTimeInSec 				= -1;
	
	public ConsumerBase()
	{
		super(Mode.Consumer);
	}

	@Override
	public synchronized void init(Config config) throws TestException
	{
		eventIx = new AtomicInteger(0);
		timeStamp = null;
		lastSuccessCount = 0;
		lastSuccessIncrement = 0;

		testType = TestType.fromValue(config.getPropertyValue("testType"));
		timeOutInSec = ((ConsumerConfig) config).getTimeoutInSec();
		totalTimeInSec = Integer.parseInt(config.getPropertyValue("totalTimeInSec", "-1"));
	}

	/**
	 * This method is used to accumulate events we are consuming.
	 */
	@Override
	public void receive(String message)
	{
		if (message == null && running.get())
			return;

		if (eventIx.get() == 0)
		{
			timeStamp = new Long[2];
			timeStamp[0] = System.currentTimeMillis();
		}

		eventIx.incrementAndGet();
		long successEvents = successfulEvents.incrementAndGet();
		successfulEventBytes.addAndGet(message.getBytes().length);
		// check if we are done - received all expected results
		//System.out.println( "Current Count[" + currentCount + "] =? Expected Count[" + expectedResultCount +"] =? Success Events["  + successEvents + "]");
		if (successEvents == getNumberOfExpectedResults())
		{
			finishConsuming(System.currentTimeMillis());
		}
	}

	/**
	 * NOTE: this method must be overwritten if you would like to use the "pull" method of consuming data. Otherwise you
	 * can use the {@link #receive(String)} method
	 */
	@Override
	public String pullMessage()
	{
		return null;
	}

	@Override
	public void run()
	{
		int expectedResultCount = numberOfEvents;// (this.numberOfExpectedResults > -1) ? this.numberOfExpectedResults : numberOfEvents;
		if (expectedResultCount > 0)
		{
			if (runningStateListener != null)
				runningStateListener.onStateChange(new RunningState(RunningStateType.STARTED));

			try
			{
				while (running.get())
				{
					// check if we have timed out
					checkTimeout();

					// pull message (otherwise we might receive them using the <code>receive(String message)</code> method
					try
					{
						String message = pullMessage();
						if (message == null)
							continue;
						receive(message);
					}
					catch (Exception error)
					{
						continue;
					}
				}
			}
			catch (Exception e)
			{
				running.set(false);
				e.printStackTrace();
			}
		}
	}

	private void checkTimeout()
	{
		if (running.get())
		{
			long currentValue = successfulEvents.get();
			long now = System.currentTimeMillis();

			// System.out.println("Current count = " + currentValue + " last timestamp = " + lastSuccessIncrement +
			// ", which is " + (now-lastSuccessIncrement)/1000 + " seconds.");
			if (lastSuccessCount == currentValue)
			{
				if (lastSuccessIncrement == 0)
					lastSuccessIncrement = now;
				if (now - lastSuccessIncrement > timeOutInSec * 1000)
				{
					System.out.println( ImplMessages.getMessage("CONSUMER_TIMEOUT_MSG") );
					finishConsuming(lastSuccessIncrement);
				}
			}
			else
			{
				lastSuccessIncrement = now;
				lastSuccessCount = currentValue;
			}
		}
	}

	private void finishConsuming(long finalTimeStamp)
	{
		// check if we init the time stamps
		if (timeStamp == null)
		{
			timeStamp = new Long[2];
			timeStamp[0] = finalTimeStamp;
		}
		timeStamp[1] = finalTimeStamp;
		timeStamps.put(timeStamps.size(), timeStamp);

		double totalTime = 0;
		lastSuccessIncrement = 0;
		if (timeStamp != null && timeStamp[0] != null && timeStamp[1] != null)
		{
			totalTime = ((double) timeStamp[1] - (double) timeStamp[0]) / 1000d;
			
			// for tests of type TIME - lets get the greatest of the two times for display purposes (actual time versus total time)
			if( testType == TestType.TIME )
			{
				if( totalTime < new Integer(totalTimeInSec).doubleValue() )
				{
					// sleep to sync up the displays - this does not effect the calculations
					double timeToSleep = new Integer(totalTimeInSec).doubleValue() - (totalTime * 1000.0d);
					try {
						Thread.sleep( new Double(timeToSleep).longValue() );
					} catch ( Exception ignored )
					{
					}
					totalTime = totalTimeInSec;
				}
				System.out.println( ImplMessages.getMessage("CONSUMER_TIMED_FINISH_MSG", successfulEvents.get(), String.valueOf(totalTime), String.valueOf(((double) successfulEvents.get() / totalTime))) );
			}
			else
				System.out.println( ImplMessages.getMessage("CONSUMER_FINISH_MSG", successfulEvents.get()) );
		}
		else if (successfulEvents.get() > 0)
			System.out.println( ImplMessages.getMessage("CONSUMER_FINISH_MSG", successfulEvents.get()) );

		// send the stop status
		running.set(false);
		if (runningStateListener != null)
			runningStateListener.onStateChange(new RunningState(RunningStateType.STOPPED));
	}
}
