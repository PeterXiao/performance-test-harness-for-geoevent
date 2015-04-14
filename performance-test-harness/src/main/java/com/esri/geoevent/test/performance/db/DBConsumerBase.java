package com.esri.geoevent.test.performance.db;

import java.util.Comparator;

import com.datastax.driver.core.Row;
import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public abstract class DBConsumerBase extends ConsumerBase
{
	private static final int DEFAULT_TOTAL_TIME_IN_SEC = 20;  // default to 20 secs
	
	private long startTime = 0L;
	private int timeToWaitInSec = DEFAULT_TOTAL_TIME_IN_SEC;
	private int retries = 0;
	
	@Override
	public synchronized void init(Config config) throws TestException
	{
		super.init(config);
		
		int totalTimeInSec = getTotalTimeInSec();
		if( totalTimeInSec <= 0 )
			totalTimeInSec = DEFAULT_TOTAL_TIME_IN_SEC;
		
		// set our time to wait and timeout
		this.startTime= 0L;
		this.retries = 0;
		this.timeToWaitInSec = totalTimeInSec + 2;
		setTimeOutInSec( Math.max(totalTimeInSec*2, timeToWaitInSec) );
	}
		
	@Override
	public String pullMessage()
	{
		long now = System.currentTimeMillis();
	  if( startTime == 0 )
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
		try( DBClient client = getDBClient() )
		{
			DBResult results = client.queryForLastWriteTimes();
			
      // set the start and end times
      long previousSuccessFulCount = getSuccessfulEvents();
      setStartTime(results.getStartTime());
      setSuccessfulEvents(results.getTotalCount());
      
      // we are done
	    if( results.getTotalCount() == getNumberOfExpectedResults() )
      {
	    	finishConsuming(results.getEndTime());
      }
	    else
	    {
	    	// keep track of the previous counts - check if they are changing -if the are then reset the retries counter
	    	if( previousSuccessFulCount != getSuccessfulEvents() )
	    		retries = 0;
	    	// check the number of retries if we reached our max, then finish
	    	if( retries >= 3 )
	    		finishConsuming(results.getEndTime());
	    	else
	    	{
	    		// inc and wait a sec
	    		retries++;
	    		Thread.sleep(1000);
	    	}
	    }
		} 
		catch( Exception error)
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
	
	/**
	 * Inner class used to sort the incoming Cassandra rows by last updated (long)
	 */
	class RowComparator implements Comparator<Row>
	{
		@Override
		public int compare(Row row1, Row row2)
		{
			if( row1 == null || row2 == null )
				return 0;
			
			return Long.compare(row1.getLong(0), row2.getLong(0));
		}
	}

}
