package com.esri.ges.test.performance.cassandra;

import org.junit.Test;

import com.esri.geoevent.test.performance.TestHarnessExecutor;

public class CassandraTestHarnessTest
{
	// ---------------------------------------------------------
	// Tests
	// ---------------------------------------------------------
	
	@Test
	public void testPerformanceTestHarnessMode_CASSANDRA() throws InterruptedException
	{		
		// setup consumer
		String args = "-m consumer -t cassandra -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t kafka -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_cassandra.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
}
