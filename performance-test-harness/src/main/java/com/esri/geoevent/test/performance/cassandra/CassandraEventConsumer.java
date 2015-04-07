package com.esri.geoevent.test.performance.cassandra;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class CassandraEventConsumer extends ConsumerBase
{
	private static final int DEFAULT_TOTAL_TIME_IN_SEC = 20;  // default to 20 secs
	
	// member vars
	private String nodeName;
	private String keyspace;
	private String tableName;
	private String columnName;
	private long startTime = 0L;
	private int timeToWaitInSec = DEFAULT_TOTAL_TIME_IN_SEC;
	private int retries = 0;
	
	@Override
	public void init(Config config) throws TestException 
	{
		super.init(config);
		
		this.nodeName = config.getPropertyValue("nodeName");
		this.keyspace = config.getPropertyValue("keyspace");
		this.tableName = config.getPropertyValue("tableName");
		this.columnName = config.getPropertyValue("columnName");
		
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
	public void validate() throws TestException
	{
		if(StringUtils.isEmpty(nodeName))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "nodeName") );
		if(StringUtils.isEmpty(keyspace))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "keyspace") );
		if(StringUtils.isEmpty(tableName))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "tableName") );
		if(StringUtils.isEmpty(columnName))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "columnName") );

		// check if we can connect
		try( CassandraClient client = new DefaultCassandraClient(nodeName) )
		{
			;
		} 
		catch( Exception error)
		{
			throw new TestException( ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName") );
		}
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
		try( CassandraClient client = new DefaultCassandraClient(nodeName) )
		{
			ResultSet results = client.queryForLastWriteTimes(keyspace, tableName, columnName);
	    List<Row> allRows = results.all();
	    
	    //sort all of the rows accordingly
      allRows.sort(new RowComparator());
      
      // set the start and end times
      long previousSuccessFulCount = getSuccessfulEvents();
      setStartTime(allRows.get(0).getLong(0)/1000);
      setSuccessfulEvents(allRows.size());
      
      // we are done
	    if( allRows.size() == getNumberOfExpectedResults() )
      {
	    	finishConsuming(allRows.get(allRows.size()-1).getLong(0)/1000);
      }
	    else
	    {
	    	// keep track of the previous counts - check if they are changing -if the are then reset the retries counter
	    	if( previousSuccessFulCount != getSuccessfulEvents() )
	    		retries = 0;
	    	// check the number of retries if we reached our max, then finish
	    	if( retries >= 3 )
	    		finishConsuming(allRows.get(allRows.size()-1).getLong(0)/1000);
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
