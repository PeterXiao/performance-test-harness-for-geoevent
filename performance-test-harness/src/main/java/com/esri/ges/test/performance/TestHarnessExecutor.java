package com.esri.ges.test.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.esri.ges.test.performance.activemq.ActiveMQEventConsumer;
import com.esri.ges.test.performance.activemq.ActiveMQEventProducer;
import com.esri.ges.test.performance.jaxb.Fixture;
import com.esri.ges.test.performance.jaxb.Fixtures;
import com.esri.ges.test.performance.jaxb.GeoEventConfiguration;
import com.esri.ges.test.performance.kafka.KafkaEventProducer;
import com.esri.ges.test.performance.rabbitmq.RabbitMQEventConsumer;
import com.esri.ges.test.performance.rabbitmq.RabbitMQEventProducer;
import com.esri.ges.test.performance.report.CSVReportWriter;
import com.esri.ges.test.performance.report.ReportType;
import com.esri.ges.test.performance.report.ReportWriter;
import com.esri.ges.test.performance.report.XLSXReportWriter;
import com.esri.ges.test.performance.statistics.FixturesStatistics;
import com.esri.ges.test.performance.streamservice.StreamServiceEventConsumer;
import com.esri.ges.test.performance.streamservice.StreamServiceEventProducer;
import com.esri.ges.test.performance.tcp.ClusterableTcpEventConsumer;
import com.esri.ges.test.performance.tcp.ClusterableTcpEventProducer;
import com.esri.ges.test.performance.tcp.TcpEventConsumer;
import com.esri.ges.test.performance.tcp.TcpEventProducer;
import com.esri.ges.test.performance.websocket.WebsocketEventConsumer;
import com.esri.ges.test.performance.websocket.WebsocketEventProducer;
import com.esri.ges.test.performance.websocket.server.WebsocketServerEverntProducer;

public class TestHarnessExecutor
{
	private static Fixtures fixtures;
	private static List<String> testNames;
	private static boolean reportComplete;
	private static long startTime;
	
	@SuppressWarnings("static-access")
	public static void main(String[] args)
	{
		// test harness options
		Options testHarnessOptions = new Options();
		testHarnessOptions.addOption(OptionBuilder.withLongOpt("fixtures").withDescription("The fixtures xml file to load and configure the performance test harness.").hasArg().isRequired().create("f"));
		testHarnessOptions.addOption("h", "help", false, "print the help message");

		// performer options
		Options performerOptions = new Options();
		performerOptions.addOption(OptionBuilder.withLongOpt("type").withDescription("[TCP(default) | WEBSOCKETS | ACTIVE_MQ | RABBIT_MQ]").hasArg().isRequired().create("t"));
		performerOptions.addOption(OptionBuilder.withLongOpt("clusterable").withDescription("[yes|no]").hasArg().create("c"));
		performerOptions.addOption(OptionBuilder.withLongOpt("mode").withDescription("[producer|consumer]").hasArg().isRequired().create("m"));
		performerOptions.addOption(OptionBuilder.withLongOpt("commandListenerPort").withDescription("The TCP Port where this diagnostic tool will listen for commands from the orchestrator, e.g. 5010. It can be set to \"local\" to be run locally (port 5010 for \"producer\" and port 5020 for \"consumer\"").hasArg().isRequired().create("p"));
		performerOptions.addOption(OptionBuilder.withLongOpt("consumerServerPort").withDescription("The TCP Port where the consumer will listen for TCP connections to consume events, e.g. 5775. (Default value is 5775").hasArg().create("cp"));
		performerOptions.addOption("s", "server", false, "Indicates that the producer/consumer should be running as a server, listening for connections from the GEP connector.");
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
			testNames = new ArrayList<String>();
			try
			{
				fixtures = fromXML(fixturesFilePath);
			}
			catch (JAXBException error)
			{
				System.err.println("There was a problem parsing the \"" + fixturesFilePath + "\". Cannot continue, exiting.");
				error.printStackTrace();
				return;
			}
			
			// add this runtime hook to write out whatever results we have to a report in case of exit or failures
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run()
				{
					long totalTime = System.currentTimeMillis() - startTime;
					writeReport(fixtures,testNames,totalTime);
				}
			});
			
			// Configure the GeoEvent Processor with the configuration specified in the fixture description
			GeoEventConfiguration geoEventConfiguration = fixtures.getGeoEventConfiguration();
			if( geoEventConfiguration != null )
			{
  			ClusterConfigurator configurator = new ClusterConfigurator( geoEventConfiguration.getConfigurationFile() );
  			configurator.setHostname(geoEventConfiguration.getHostname());
  			configurator.setUserName(geoEventConfiguration.getUsername());
  			configurator.setPassword(geoEventConfiguration.getPassword());
  			configurator.applyConfiguration();
			}

			
			startTime = System.currentTimeMillis();
			//process all fixtures in sequence/series
			Queue<Fixture> processingQueue = new ConcurrentLinkedQueue<Fixture>(fixtures.getFixtures());
			while( ! processingQueue.isEmpty() )
			{
				Fixture fixture = processingQueue.remove();
				testNames.add( fixture.getName() );
				TestHarness testHarness = new ThroughputPerformanceTestHarness(fixture);
				try
				{
					testHarness.init();
					testHarness.runTest();
				}
				catch (Exception error)
				{
					error.printStackTrace();
				}
				// check if we are running and sleep accordingly
				while( testHarness.isRunning() )
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
				testHarness = null;
				
				//pause for 1/2 second before continuing with the next test
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
			writeReport(fixtures, testNames,totalTime);
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
			boolean isClusterable = BooleanUtils.toBoolean(cmd.getOptionValue("c"));

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
			if (mode == Mode.PRODUCER)
			{
				DiagnosticsCollector producer = null;
				switch (protocol)
				{
					case TCP:
						if(isClusterable)
						{
							int connectionPort = NumberUtils.toInt(cmd.getOptionValue("cp"), 5775);
							producer = new ClusterableTcpEventProducer(connectionPort);
						}
						else 
							producer = new TcpEventProducer();
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
            final WebsocketServerEverntProducer wsProducer = new WebsocketServerEverntProducer();
            producer = wsProducer;
            Runtime.getRuntime().addShutdownHook(new Thread() {
              @Override
              public void run()
              {
                wsProducer.shutdown();
              }
            });
            break;
					default:
						return;
				}
				producer.listenOnCommandPort((isLocal ? 5010 : commandListenerPort), isLocal);
			}
			else if (mode == Mode.CONSUMER)
			{
				DiagnosticsCollector consumer = null;
				switch (protocol)
				{
					case TCP:
						if( isClusterable )
						{
							int consumerServerPort = NumberUtils.toInt(cmd.getOptionValue("cp"), 5775);
							final ClusterableTcpEventConsumer clusterableTcpEventConsumer = new ClusterableTcpEventConsumer(consumerServerPort);
							consumer = clusterableTcpEventConsumer;
							// add this runtime hook - cleanup after we shutdown
							Runtime.getRuntime().addShutdownHook(new Thread() {
								@Override
								public void run()
								{
									clusterableTcpEventConsumer.shutdown();
								}
							});
						}
						else
							consumer = new TcpEventConsumer();
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
				consumer.listenOnCommandPort((isLocal ? 5020 : commandListenerPort), isLocal);
			}
		}
	}
	
	private static void writeReport(final Fixtures fixtures, final List<String> testNames, long totalTestingTime)
	{
		if( fixtures == null || testNames.size() == 0 || reportComplete)
			return;

		// write out the report
		ReportWriter reportWriter = null;
		ReportType type = ReportType.fromValue(fixtures.getReport().getType());
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
			List<String> columnNames = (fixtures.getReport().isSimpleColumnNames()) ? reportWriter.getSimpleColumnNames() : reportWriter.getDetailedColumnNames();
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

		formatter.printHelp("java ThroughputPerformanceTestHarness", testHarnessOptions, true);
		System.out.println("");
		System.out.println("or simply invoke as a remotely controlled process like this:");
		formatter.printHelp("java ThroughputPerformanceTestHarness", performerOptions, true);
		System.out.println("");
	}

	private static boolean validateFixturesFile(String fixturesFilePath)
	{
		if (StringUtils.isEmpty(fixturesFilePath))
		{
			System.err.println("Invalid \"fixtures\" value \"" + fixturesFilePath + "\". The fixtures must be a valid file.");
			return false;
		}
		File fixturesFile = new File(fixturesFilePath);
		if (!fixturesFile.exists() || fixturesFile.isDirectory())
		{
			System.err.println("Invalid \"fixtures\" value \"" + fixturesFilePath + "\". The fixtures must be a valid file and not a directory. Check the file path to see if it exist.");
			return false;
		}
		return true;
	}

	private static boolean validateTestHarnessOptions(String protocolStr, String modeStr, String commandListenerPort)
	{
		Protocol protocol = Protocol.fromValue(protocolStr);
		if (protocol == Protocol.UNKNOWN)
		{
			System.err.println("Invalid \"protocol\" value \"" + protocolStr + "\". The mode must be set to one of these values: \"TCP\", \"WEBSOCKETS\", \"ACTIVE_MQ\", \"RABBIT_MQ\".");
			return false;
		}
		Mode mode = Mode.fromValue(modeStr);
		if (mode == Mode.UNKNOWN)
		{
			System.err.println("Invalid \"mode\" value \"" + modeStr + "\". The mode must be set to one of these values: \"producer\" or \"consumer\".");
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
				System.err.println("Invalid \"commandListenerPort\" value \"" + commandListenerPort + "\". The mode must be a number or set to \"local\" to run locally.");
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
