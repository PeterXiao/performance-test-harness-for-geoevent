package com.esri.ges.test.performance.es;

import org.junit.Test;

import com.esri.geoevent.test.performance.TestHarnessExecutor;

public class ElasticSearchTestHarnessTest
{
	// ---------------------------------------------------------
	// Tests
	// ---------------------------------------------------------
	
	@Test
	public void testPerformanceTestHarnessMode_ES()
	{		
		// setup consumer
		String args = "-m consumer -t es -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t kafka -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_es.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
}
