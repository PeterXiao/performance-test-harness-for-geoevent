package com.esri.geoevent.test.performance;

import java.util.concurrent.atomic.AtomicInteger;

import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.jaxb.ConsumerConfig;
import com.esri.geoevent.test.performance.jaxb.TestType;

public abstract class ConsumerBase extends PerformanceCollectorBase implements Consumer
{
	private AtomicInteger	eventIx								= new AtomicInteger(0);
	private int						expectedResultCount		= 0;
	private Long[]				timeStamp							= null;
	private long					lastSuccessCount			= 0;
	private long					lastSuccessIncrement	= 0;
	private long					timeOutInSec					= 10;
	private TestType			testType							= TestType.UNKNOWN;
	
	public ConsumerBase()
	{
		super(Mode.CONSUMER);
	}

	@Override
	public void setNumberOfExpectedResults(int numberOfExpectedResults)
	{
		super.setNumberOfExpectedResults(numberOfExpectedResults);
		expectedResultCount = numberOfExpectedResults;
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
	}

	@Override
	public void reset()
	{
		super.reset();

		eventIx = new AtomicInteger(0);
		timeStamp = null;
		lastSuccessCount = 0;
		lastSuccessIncrement = 0;
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

		int currentCount = eventIx.incrementAndGet();
		successfulEvents.incrementAndGet();

		// check if we are done - received all expected results
		//System.out.println( "Current Count[" + currentCount + "] =? Expected Count[" + expectedResultCount +"]" );
		if (currentCount == expectedResultCount)
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
					System.out.println("Timeout reached.  Stopping test because data apparently stopped flowing.");
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
			if( testType == TestType.TIME )
				System.out.println("Consumed a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double) successfulEvents.get() / totalTime) + " e/s).");
			else
				System.out.println("Consumed a total of: " + successfulEvents.get() + " events.");
		}
		else if (successfulEvents.get() > 0)
			System.out.println("Consumed a total of: " + successfulEvents.get() + " events.");

		// send the stop status
		running.set(false);
		if (runningStateListener != null)
			runningStateListener.onStateChange(new RunningState(RunningStateType.STOPPED));
	}
}
