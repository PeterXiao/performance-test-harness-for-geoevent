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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.esri.geoevent.test.performance.activemq.ActiveMQEventConsumer;
import com.esri.geoevent.test.performance.activemq.ActiveMQEventProducer;
import com.esri.geoevent.test.performance.jaxb.Fixture;
import com.esri.geoevent.test.performance.jaxb.Fixtures;
import com.esri.geoevent.test.performance.jaxb.ProvisionerConfig;
import com.esri.geoevent.test.performance.kafka.KafkaEventProducer;
import com.esri.geoevent.test.performance.provision.DefaultProvisionerFactory;
import com.esri.geoevent.test.performance.provision.ProvisionException;
import com.esri.geoevent.test.performance.provision.Provisioner;
import com.esri.geoevent.test.performance.provision.ProvisionerFactory;
import com.esri.geoevent.test.performance.rabbitmq.RabbitMQEventConsumer;
import com.esri.geoevent.test.performance.rabbitmq.RabbitMQEventProducer;
import com.esri.geoevent.test.performance.report.CSVReportWriter;
import com.esri.geoevent.test.performance.report.ReportType;
import com.esri.geoevent.test.performance.report.ReportWriter;
import com.esri.geoevent.test.performance.report.XLSXReportWriter;
import com.esri.geoevent.test.performance.statistics.FixturesStatistics;
import com.esri.geoevent.test.performance.streamservice.StreamServiceEventConsumer;
import com.esri.geoevent.test.performance.streamservice.StreamServiceEventProducer;
import com.esri.geoevent.test.performance.tcp.TcpServerEventConsumer;
import com.esri.geoevent.test.performance.tcp.TcpServerEventProducer;
import com.esri.geoevent.test.performance.tcp.TcpEventConsumer;
import com.esri.geoevent.test.performance.tcp.TcpEventProducer;
import com.esri.geoevent.test.performance.websocket.WebsocketEventConsumer;
import com.esri.geoevent.test.performance.websocket.WebsocketEventProducer;
import com.esri.geoevent.test.performance.websocket.server.WebsocketServerEventProducer;

public class TestHarnessExecutor implements RunnableComponent
{
	public static boolean DEBUG = false;
	
	// member vars
	private List<String> testNames;
	private boolean reportComplete;
	private long startTime;	
	private Fixtures fixtures;
	
	// Runnable
	private RunningStateListener	listener;
	protected AtomicBoolean					running						= new AtomicBoolean(false);
	
	//-------------------------------------------------------
	// Constructor
	// ------------------------------------------------------
	
	public TestHarnessExecutor(Fixtures fixtures)
	{
		this.fixtures = fixtures;
	}
	
	//-------------------------------------------------------
	// Public methods
	// ------------------------------------------------------
	
	@Override
	public void start() throws RunningException
	{
		running = new AtomicBoolean(true);
		run();
		if( listener != null )
			listener.onStateChange(new RunningState(RunningStateType.STARTED));
	}

	@Override
	public void stop()
	{
		running.set(false);
		if( listener != null )
			listener.onStateChange(new RunningState(RunningStateType.STOPPED));
	}
	
	@Override
	public boolean isRunning()
	{
		return running.get();
	}
	
	@Override
	public RunningStateType getRunningState()
	{
		return running.get() ? RunningStateType.STARTED : RunningStateType.STOPPED;
	}

	@Override
	public void setRunningStateListener(RunningStateListener listener)
	{
		this.listener = listener;
	}
	
	/**
	 * Main Test Harness Orchestrator Method
	 */
	public void run()
	{
		// parse the xml file
		testNames = new ArrayList<String>();

		// add this runtime hook to write out whatever results we have to a report in case of exit or failures
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			long totalTime = System.currentTimeMillis() - startTime;
			writeReport(fixtures, testNames, totalTime);
		}));

		// Check the master fixtures configuration to see if we need to provision all of the test
		ProvisionerFactory provisionerFactory = new DefaultProvisionerFactory();
		try
		{
			ProvisionerConfig masterProvisionerConfig = fixtures.getProvisionerConfig();
			if (masterProvisionerConfig != null)
			{
				Provisioner provisioner = provisionerFactory.createProvisioner(masterProvisionerConfig);
				if (provisioner != null)
					provisioner.provision();
			}
		}
		catch (ProvisionException error)
		{
			System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_PROVISIONING_ERROR"));
			error.printStackTrace();
			return;
		}

		// start
		startTime = System.currentTimeMillis();

		// process all fixtures in sequence/series
		final Fixture defaultFixture = fixtures.getDefaultFixture();
		Queue<Fixture> processingQueue = new ConcurrentLinkedQueue<Fixture>(fixtures.getFixtures());
		while (!processingQueue.isEmpty() && isRunning())
		{
			Fixture fixture = processingQueue.remove();
			fixture.apply(defaultFixture);
			try
			{
				ProvisionerConfig fixtureProvisionerConfig = fixture.getProvisionerConfig();
				if (fixtureProvisionerConfig != null)
				{
					Provisioner provisioner = provisionerFactory.createProvisioner(fixtureProvisionerConfig);
					if (provisioner != null)
						provisioner.provision();
				}
			}
			catch (Exception error)
			{
				System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_FIXTURE_PROVISIONING_ERROR", fixture.getName()));
				error.printStackTrace();
				continue;
			}

			testNames.add(fixture.getName());
			Orchestrator orchestrator = new PerformanceTestHarness(fixture);
			try
			{
				orchestrator.init();
				orchestrator.runTest();
			}
			catch (Exception error)
			{
				error.printStackTrace();
				orchestrator.destroy();
				orchestrator = null;
				continue;
			}

			// check if we are running and sleep accordingly
			while (orchestrator.isRunning() && isRunning())
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			orchestrator = null;

			// pause for 1/2 second before continuing with the next test
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		long totalTime = System.currentTimeMillis() - startTime;
		// write out the report
		writeReport(fixtures, testNames, totalTime);
		//notify
		stop();
	}
	
	//-------------------------------------------------------
	// Statics Methods
	// ------------------------------------------------------
	
	/**
	 * Main method - this is used to when running from command line
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args)
	{
		// TODO: Localize the messages
		// test harness options
		Options testHarnessOptions = new Options();
		testHarnessOptions.addOption(OptionBuilder.withLongOpt("fixtures").withDescription("The fixtures xml file to load and configure the performance test harness.").hasArg().isRequired().create("f"));
		testHarnessOptions.addOption("h", "help", false, "print the help message");

		// performer options
		Options performerOptions = new Options();
		performerOptions.addOption(OptionBuilder.withLongOpt("type").withDescription("[TCP(default) | WEBSOCKETS | ACTIVE_MQ | RABBIT_MQ]").hasArg().isRequired().create("t"));
		performerOptions.addOption(OptionBuilder.withLongOpt("mode").withDescription("[producer|consumer]").hasArg().isRequired().create("m"));
		performerOptions.addOption(OptionBuilder.withLongOpt("commandListenerPort").withDescription("The TCP Port where this diagnostic tool will listen for commands from the orchestrator, e.g. 5010. It can be set to \"local\" to be run locally (port 5010 for \"producer\" and port 5020 for \"consumer\"").hasArg().isRequired().create("p"));
		performerOptions.addOption(OptionBuilder.withLongOpt("serverPort").withDescription("The TCP Port where the server will listen for TCP connections to produce/consume events, e.g. 5665. (Default value is 5665 for producer and 5775 for consumer").hasArg().create("sp"));
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
			// ignore
			;
		}

		// do we have options for the test harness or help
		if (cmd != null && cmd.getOptions().length > 0)
		{
			if (cmd.hasOption("h") || !cmd.hasOption("f"))
			{
				printHelp(testHarnessOptions, performerOptions);
				return;
			}

			String fixturesFilePath = cmd.getOptionValue("f");
			// validate
			if (!validateFixturesFile(fixturesFilePath))
			{
				printHelp(testHarnessOptions, performerOptions);
				return;
			}

			// parse the xml file
			final Fixtures fixtures;
			try
			{
				fixtures = fromXML(fixturesFilePath);
			}
			catch (JAXBException error)
			{
				System.err.println( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_CONFIG_ERROR", fixturesFilePath ) );
				error.printStackTrace();
				return;
			}
			
			// run the test harness
			try
			{
				TestHarnessExecutor executor = new TestHarnessExecutor(fixtures);
				executor.start();
			} 
			catch( RunningException error )
			{
				error.printStackTrace();
			}
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
				return;
			}

			// no valid options were found
			if (cmd.getOptions().length == 0 || cmd.hasOption("h"))
			{
				printHelp(testHarnessOptions, performerOptions);
				return;
			}

			// parse out the performer options
			String protocolValue = cmd.getOptionValue("t");
			String modeValue = cmd.getOptionValue("m");
			String commandListenerPortValue = cmd.getOptionValue("p");

			// validate
			if (!validateTestHarnessOptions(protocolValue, modeValue, commandListenerPortValue))
			{
				printHelp(testHarnessOptions, performerOptions);
				return;
			}

			// parse the values
			Protocol protocol = Protocol.fromValue(protocolValue);
			Mode mode = Mode.fromValue(modeValue);
			boolean isLocal = "local".equalsIgnoreCase(commandListenerPortValue);
			int commandListenerPort = -1;
			if (!isLocal)
			{
				commandListenerPort = Integer.parseInt(commandListenerPortValue);
			}

			// activate
			if (mode == Mode.Producer)
			{
				PerformanceCollector producer = null;
				switch (protocol)
				{
					case TCP:
						producer = new TcpEventProducer();
						break;
					case TCP_SERVER:
						int connectionPort = NumberUtils.toInt(cmd.getOptionValue("sp"), 5665);
						producer = new TcpServerEventProducer(connectionPort);
						break;
					case WEBSOCKETS:
						producer = new WebsocketEventProducer();
						break;
					case ACTIVE_MQ:
						producer = new ActiveMQEventProducer();
						break;
					case RABBIT_MQ:
						producer = new RabbitMQEventProducer();
						break;
					case STREAM_SERVICE:
						producer = new StreamServiceEventProducer();
						break;
					case KAFKA:
					  producer = new KafkaEventProducer();
					  break;
					case WEBSOCKET_SERVER:
            final WebsocketServerEventProducer wsProducer = new WebsocketServerEventProducer();
            producer = wsProducer;
            break;
					default:
						return;
				}
				producer.listenOnCommandPort((isLocal ? 5010 : commandListenerPort), true);
			}
			else if (mode == Mode.Consumer)
			{
				PerformanceCollector consumer = null;
				switch (protocol)
				{
					case TCP:
						consumer = new TcpEventConsumer();
						break;
					case TCP_SERVER:
						int consumerServerPort = NumberUtils.toInt(cmd.getOptionValue("sp"), 5775);
						TcpServerEventConsumer clusterableTcpEventConsumer = new TcpServerEventConsumer(consumerServerPort);
						consumer = clusterableTcpEventConsumer;
						break;
					case WEBSOCKETS:
						consumer = new WebsocketEventConsumer();
						break;
					case ACTIVE_MQ:
						consumer = new ActiveMQEventConsumer();
						break;
					case RABBIT_MQ:
						consumer = new RabbitMQEventConsumer();
						break;
					case STREAM_SERVICE:
						consumer = new StreamServiceEventConsumer();
						break;
					default:
						return;
				}
				consumer.listenOnCommandPort((isLocal ? 5020 : commandListenerPort), true);
			}
		}
	}
	
	private void writeReport(final Fixtures fixtures, final List<String> testNames, long totalTestingTime)
	{
		if( fixtures == null || testNames.size() == 0 || reportComplete)
			return;

		// write out the report
		ReportWriter reportWriter = null;
		ReportType type = fixtures.getReport().getType();
		switch( type )
		{
			case XLSX:
				reportWriter = new XLSXReportWriter();
				break;
			case CSV:
			default:
				reportWriter = new CSVReportWriter();
				break;
		}
		
		//write the report			
		try
		{
			List<String> columnNames = reportWriter.getReportColumnNames(fixtures.getReport().getReportColumns(),  fixtures.getReport().getAdditionalReportColumns());
			reportWriter.setMaxNumberOfReportFiles(fixtures.getReport().getMaxNumberOfReportFiles());
			reportWriter.setTotalTestingTime(totalTestingTime);
			reportWriter.writeReport(fixtures.getReport().getReportFile(), testNames, columnNames, FixturesStatistics.getInstance().getStats());
		}
		catch( Exception error)
		{
			error.printStackTrace();
		}
		finally
		{
			reportComplete = true;
		}
	}
	
	// -------------------------------------------------------------------------------
	// Main Helper Methods
	// -------------------------------------------------------------------------------

	private static void printHelp(Options testHarnessOptions, Options performerOptions)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.setLongOptPrefix("-");
		formatter.setArgName("value");
		formatter.setWidth(100);
		// do not sort the options in any order
		formatter.setOptionComparator(new Comparator<Option>()
		{
			@Override
			public int compare(Option o1, Option o2)
			{
				return 0;
			}
		});

		formatter.printHelp( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_HELP_TITLE_MSG"), testHarnessOptions, true);
		System.out.println("");
		System.out.println( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_HELP_SUBTITLE_MSG") );
		formatter.printHelp( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_HELP_TITLE_MSG"), performerOptions, true);
		System.out.println("");
	}

	private static boolean validateFixturesFile(String fixturesFilePath)
	{
		if (StringUtils.isEmpty(fixturesFilePath))
		{
			System.err.println( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_FIXTURE_VALIDATION", fixturesFilePath) );
			return false;
		}
		File fixturesFile = new File(fixturesFilePath);
		if (!fixturesFile.exists() || fixturesFile.isDirectory())
		{
			System.err.println( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_FIXTURE_FILE_VALIDATION", fixturesFilePath) );
			return false;
		}
		return true;
	}

	private static boolean validateTestHarnessOptions(String protocolStr, String modeStr, String commandListenerPort)
	{
		Protocol protocol = Protocol.fromValue(protocolStr);
		if (protocol == Protocol.UNKNOWN)
		{
			System.err.println( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_PROTOCOL_VALIDATION", protocolStr, Protocol.getAllowableValues()) );
			return false;
		}
		Mode mode = Mode.fromValue(modeStr);
		if (mode == Mode.Unknown)
		{
			System.err.println( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_MODE_VALIDATION", modeStr, Mode.getAllowableValues() ) );
			return false;
		}

		// validate the port - it could be set to local
		if (!"local".equalsIgnoreCase(commandListenerPort))
		{
			try
			{
				Integer.parseInt(commandListenerPort);
			}
			catch (NumberFormatException error)
			{
				System.err.println( ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_COMMAND_PORT_VALIDATION", String.valueOf(commandListenerPort)) );
				return false;
			}
		}
		return true;
	}

	// -------------------------------------------------------
	// Conversion Methods
	// ------------------------------------------------------

	private static Fixtures fromXML(String xmlLocation) throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(Fixtures.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource xml = new StreamSource(xmlLocation);
		return (Fixtures) unmarshaller.unmarshal(xml);
	}
}
