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
package com.esri.geoevent.test.performance.db;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public abstract class DBConsumerBase extends ConsumerBase
{
	private static final int	DEFAULT_TOTAL_TIME_IN_SEC	= 20;												// default to 20 secs

	private long							startTime									= 0L;
	private int								timeToWaitInSec						= DEFAULT_TOTAL_TIME_IN_SEC;
	private int								retries										= 0;

	@Override
	public synchronized void init(Config config) throws TestException
	{
		super.init(config);

		int totalTimeInSec = getTotalTimeInSec();
		if (totalTimeInSec <= 0)
			totalTimeInSec = DEFAULT_TOTAL_TIME_IN_SEC;

		// set our time to wait and timeout
		this.startTime = 0L;
		this.retries = 0;
		this.timeToWaitInSec = totalTimeInSec + 2;
		setTimeOutInSec(Math.max(totalTimeInSec * 2, timeToWaitInSec));
	}

	@Override
	public String pullMessage()
	{
		long now = System.currentTimeMillis();
		if (startTime == 0)
			startTime = now;

		// check if we need to start to query the Cassandra
		if (now - startTime > timeToWaitInSec * 1000)
		{
			queryForAllWriteTimes();
		}
		return null;
	}

	/**
	 * Query Cassandra for all of the write times
	 */
	private void queryForAllWriteTimes()
	{
		// check if we can connect
		try (DBClient client = getDBClient())
		{
			DBResult results = client.queryForLastWriteTimes();

			// set the start and end times
			long previousSuccessFulCount = getSuccessfulEvents();
			setStartTime(results.getStartTime());
			setSuccessfulEvents(results.getTotalCount());

			// we are done
			if (results.getTotalCount() == getNumberOfExpectedResults())
			{
				finishConsuming(results.getEndTime());
			}
			else
			{
				// keep track of the previous counts - check if they are changing -if the are then reset the retries counter
				if (previousSuccessFulCount != getSuccessfulEvents())
					retries = 0;
				// check the number of retries if we reached our max, then finish
				if (retries >= 3)
					finishConsuming(results.getEndTime());
				else
				{
					// inc and wait a sec
					retries++;
					Thread.sleep(1000);
				}
			}
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}

	/**
	 * Abstract method to be implemented by the sub class
	 * 
	 * @return
	 */
	public abstract DBClient getDBClient();
}
