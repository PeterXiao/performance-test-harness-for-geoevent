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
package com.esri.ges.test.performance;

import java.util.Comparator;

import junit.framework.Assert;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import com.esri.geoevent.test.performance.TestHarnessExecutor;

public class TestHarnessExecutorTest
{

	// ---------------------------------------------------------
	// Tests
	// ---------------------------------------------------------

	@Test
	public void testConsumerMode()
	{
		String args = "-m consumer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testProducerMode()
	{
		String args = "-m producer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_TCP_SANITY() throws InterruptedException
	{
		// setup consumer
		String args = "-m consumer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_simple_tcp.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_MINIMAL() throws InterruptedException
	{
		// setup consumer
		String args = "-m consumer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_minimal.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_TCP_RAMP_UP() throws InterruptedException
	{
		// setup consumer
		String args = "-m consumer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_ramp_tcp.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_TCP_STRESS() throws InterruptedException
	{
		// setup consumer
		String args = "-m consumer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_stress_tcp.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_PROVISIONING_TCP() throws InterruptedException
	{
		// setup consumer
		String args = "-m consumer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_provisioning_tcp.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_PROVISIONING_PER_FIXTURE_TCP() throws InterruptedException
	{
		// setup consumer
		String args = "-m consumer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t tcp -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_provisioning_per_fixture_tcp.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_WS_SANITY() throws InterruptedException
	{
		// setup consumer
		String args = "-m consumer -t websockets -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t websockets -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_simple_ws.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_RABBITMQ() throws InterruptedException
	{	  
	  // setup consumer
	  String args = "-m consumer -t rabbit_mq -p local";
	  TestHarnessExecutor.main(args.split(" "));
	  
	  // setup producer
	  args = "-m producer -t rabbit_mq -p local";
	  TestHarnessExecutor.main(args.split(" "));
	  
	  // run the test
	  args = "-f src/test/resources/fixtures_rabbitmq.xml";
	  TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
	public void testPerformanceTestHarnessMode_KAFKA() throws InterruptedException
	{
	  String args;
	  
	  // setup consumer
//	  args = "-m consumer -t tcp -p local -cp 5665 -c yes";
//	  TestHarnessExecutor.main(args.split(" "));
	  
	  // setup producer
	  args = "-m producer -t kafka -p local";
	  TestHarnessExecutor.main(args.split(" "));
	  
	  // run the test
	  args = "-f src/test/resources/fixtures_kafka.xml";
	  TestHarnessExecutor.main(args.split(" "));
	}
	
	@Test
  public void testPerformanceTestHarnessMode_WSServer() throws InterruptedException
  {
    String args;
    
    // setup consumer
    args = "-m consumer -t tcp -p local -cp 5665 -c yes";
    TestHarnessExecutor.main(args.split(" "));
    
    // setup producer
//    args = "-m producer -t websocket_server -p local";
//    TestHarnessExecutor.main(args.split(" "));
//    int numOfThreadsAtStartTime = Thread.activeCount();
    
    // run the test
    args = "-f src/test/resources/fixtures_ws_server.xml";
    TestHarnessExecutor.main(args.split(" "));
    
//    int numOfThreads = Thread.activeCount();
//    while( numOfThreads > numOfThreadsAtStartTime )
//    {
//      Thread.sleep(100);
//      numOfThreads = Thread.activeCount();
//    }
  }
	
	@Test
	public void testCMDLineParameters_HELP()
	{
		String cmdLineParameters = "-h";
		CommandLine cmd = parseCMDLineParameters(cmdLineParameters.split(" "));
		Assert.assertNull(cmd);
		
		cmdLineParameters = "-help";
		cmd = parseCMDLineParameters(cmdLineParameters.split(" "));
		Assert.assertNull(cmd);
	}
	
	@Test
	public void testCMDLineParameters_TH_BASIC()
	{
		String cmdLineParameters = "-f fixtures.xml";
		CommandLine cmd = parseCMDLineParameters(cmdLineParameters.split(" "));
		Assert.assertNotNull(cmd);
		Assert.assertNotNull(cmd.getOptionValue("f"));
		
		cmdLineParameters = "-fixtures fixtures.xml";
		cmd = parseCMDLineParameters(cmdLineParameters.split(" "));
		Assert.assertNotNull(cmd);
		Assert.assertNotNull(cmd.getOptionValue("f"));
	}

	// ---------------------------------------------------------
	// Helper Methods
	// ---------------------------------------------------------

	@SuppressWarnings("static-access")
	private CommandLine parseCMDLineParameters(String[] args)
	{
		// test harness options
		Options testHarnessOptions = new Options();
		testHarnessOptions.addOption( OptionBuilder.withLongOpt("fixtures").withDescription("The fixtures xml file to load and configure the performance test harness.").hasArg().isRequired().create("f"));
		testHarnessOptions.addOption("h", "help", false, "print the help message");

		// performer options
		Options performerOptions = new Options();
		performerOptions.addOption( OptionBuilder.withLongOpt("protocol").withDescription("[TCP(default) | WEBSOCKETS | ACTIVE_MQ | RABBIT_MQ]").hasArg().isRequired().create("p") );
		performerOptions.addOption( OptionBuilder.withLongOpt("type").withDescription("[producer|consumer]").hasArg().isRequired().create("t") );
		performerOptions.addOption( OptionBuilder.withLongOpt("commandListenerPort").withDescription("The TCP Port where this diagnostic tool will listen for commands from the orchestrator, e.g. 5010").hasArg().isRequired().create("c"));
		performerOptions.addOption("h", "help", false, "print the help message");

		// parse the command line
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try
		{
			cmd = parser.parse(testHarnessOptions, args, false);
		}
		catch (ParseException ignore)
		{
			//ignore
			;
		}
		
		// do we have options for the test harness or help
		if (cmd != null && cmd.getOptions().length > 0)
		{
			if (cmd.hasOption("h") || !cmd.hasOption("f"))
			{
				printHelp(testHarnessOptions, performerOptions);
				return null;
			}

			String fixturesFile = cmd.getOptionValue("f");
			System.out.println("Found the fixtures file: " + fixturesFile);
		}
		// check if we have performer options
		else
		{
			// parse
			try
			{
				cmd = parser.parse(performerOptions, args, false);
			}
			catch (ParseException error)
			{
				printHelp(testHarnessOptions, performerOptions);
				return null;
			}

			// no valid options were found
			if (cmd.getOptions().length == 0 || cmd.hasOption("h"))
			{
				printHelp(testHarnessOptions, performerOptions);
				return null;
			}

			// parse out the performer options
			String protocol = cmd.getOptionValue("p");
			String type = cmd.getOptionValue("p");
			String commandListenerPort = cmd.getOptionValue("c");

			System.out.println("Found the protocol: " + protocol);
			System.out.println("Found the type: " + type);
			System.out.println("Found the commandListenerPort: " + commandListenerPort);
		}
		return cmd;
	}

	private void printHelp(Options testHarnessOptions, Options performerOptions)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.setLongOptPrefix("-");
		formatter.setArgName("value");
		formatter.setWidth(100);
		//do not sort the options in any order
		formatter.setOptionComparator(new Comparator<Option>()
		{
			@Override
			public int compare(Option o1, Option o2)
			{
				return 0;
			}
		});
		
		formatter.printHelp("java ThroughputPerformanceTestHarness", testHarnessOptions, true);
		System.out.println("");
		System.out.println("or simply invoke as a remotely controlled process like this:");
		formatter.printHelp("java ThroughputPerformanceTestHarness", performerOptions, true);
		System.out.println("");
	}
}
