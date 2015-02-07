package com.esri.ges.test.performance;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esri.ges.test.performance.activemq.ActiveMQEventConsumer;
import com.esri.ges.test.performance.activemq.ActiveMQEventProducer;
import com.esri.ges.test.performance.jaxb.Fixture;
import com.esri.ges.test.performance.kafka.KafkaEventProducer;
import com.esri.ges.test.performance.rabbitmq.RabbitMQEventConsumer;
import com.esri.ges.test.performance.rabbitmq.RabbitMQEventProducer;
import com.esri.ges.test.performance.statistics.FixtureStatistic;
import com.esri.ges.test.performance.statistics.FixturesStatistics;
import com.esri.ges.test.performance.statistics.Statistics;
import com.esri.ges.test.performance.streamservice.StreamServiceEventConsumer;
import com.esri.ges.test.performance.streamservice.StreamServiceEventProducer;
import com.esri.ges.test.performance.tcp.ClusterableTcpEventConsumer;
import com.esri.ges.test.performance.tcp.TcpEventConsumer;
import com.esri.ges.test.performance.tcp.TcpEventProducer;
import com.esri.ges.test.performance.websocket.WebsocketEventConsumer;
import com.esri.ges.test.performance.websocket.WebsocketEventProducer;

/**
 * 
 * <b>NOTE:</b> This class is not Thread-safe
 * 
 * @author rica4208
 *
 */
public class ThroughputPerformanceTestHarness implements TestHarness, RunningStateListener
{
	private DiagnosticsCollector	eventProducer;
	private DiagnosticsCollector	eventConsumer;

	private int										numberOfIterations;
	private int										eventsPerIteration;
	private int										expectedResultCount;
	private int										minEventsPerIteration;
	private int										maxEventsPerIteration;
	private int										eventsPerIterationStep;
	private int										producerCount					= 1;
	private int										producerConnections		= 1;
	private int										consumerConnections		= 1;
	private int										expectedResultCountPerIteration;
	private int										eventsPerSec = -1;
	private int										staggeringInterval = 10;
	private int										currentIteration	= 0;
	private String								testName = "";
	
	public static Protocol				protocol							= Protocol.TCP;
	private AtomicBoolean					running								= new AtomicBoolean(false);
	private Fixture fixture;
	private String myNull;
	
	public ThroughputPerformanceTestHarness(Fixture fixture)
	{
		this.fixture = fixture;
	}

	@Override
	public void init() throws TestException
	{
		this.numberOfIterations = fixture.getSimulation().getIterations();
		this.minEventsPerIteration = fixture.getSimulation().getMinEvents();
		this.maxEventsPerIteration = fixture.getSimulation().getMaxEvents();
		this.eventsPerIterationStep = fixture.getSimulation().getEventsToAddPerIteration();
		this.producerConnections = (fixture.getProducers() != null ) ? fixture.getProducers().getNumOfConnections() : 1;
		this.consumerConnections = (fixture.getConsumers() != null ) ? fixture.getConsumers().getNumOfConnections() : 1;
		this.expectedResultCountPerIteration = fixture.getSimulation().getExpectedResultCount();
		this.eventsPerIteration = this.minEventsPerIteration;
		this.expectedResultCount = this.expectedResultCountPerIteration;
		this.eventsPerSec = fixture.getSimulation().getEventsPerSec();
		this.staggeringInterval = fixture.getSimulation().getStaggeringInterval();
		this.testName = fixture.getName();
		
		System.out.println("-----------------------------------------------");
		System.out.println(" Runnning Performance test \"" + testName + "\" ");
		System.out.println("-----------------------------------------------");

		Properties producerProperties = new Properties();
		Properties consumerProperties = new Properties();

		String simulationFilePath = fixture.getSimulation().getSourceFile();
		String host = fixture.getGeoEventHost().getHostName();
		int producerPort = fixture.getGeoEventHost().getProducerPort();
		int consumerPort = fixture.getGeoEventHost().getConsumerPort();
		
		this.protocol = Protocol.fromValue(fixture.getProtocol());
		switch (protocol)
		{
			case TCP:
			{
				eventProducer = new TcpEventProducer();
				producerProperties = socketProducerProps(simulationFilePath, host, String.valueOf(producerPort), String.valueOf(producerConnections), String.valueOf(eventsPerSec), String.valueOf(staggeringInterval));
				eventConsumer = new TcpEventConsumer();
				consumerProperties = socketConsumerProps(host, String.valueOf(consumerPort), String.valueOf(consumerConnections), String.valueOf(fixture.getSimulation().getConsumerTimeOutInSec()));
				break;
			}
			case WEBSOCKETS:
			{
				eventProducer = new WebsocketEventProducer();
				producerProperties = socketProducerProps(simulationFilePath, host, String.valueOf(producerPort), String.valueOf(producerConnections), String.valueOf(eventsPerSec), String.valueOf(staggeringInterval));
				eventConsumer = new WebsocketEventConsumer();
				consumerProperties = socketConsumerProps(host, String.valueOf(consumerPort), String.valueOf(consumerConnections), String.valueOf(fixture.getSimulation().getConsumerTimeOutInSec()));
				break;
			}
			case ACTIVE_MQ:
			{
				eventProducer = new ActiveMQEventProducer();
				producerProperties = activeMQProducerProps(simulationFilePath, host);
				eventConsumer = new ActiveMQEventConsumer();
				consumerProperties = activeMQConsumerProps(host);
				// eventConsumer = new TcpEventConsumer();
				// consumerProperties = socketConsumerProps(host, consumerPort,
				// String.valueOf(consumerConnections));
				break;
			}
			case RABBIT_MQ:
			{
				String uri = fixture.getRabbitMQHost().getUri();
				String exchange = fixture.getRabbitMQHost().getExchange();
				String queue = fixture.getRabbitMQHost().getQueue();
				String routingKey = fixture.getRabbitMQHost().getRoutingKey();
				
				eventProducer = new RabbitMQEventProducer();
				producerProperties = rabbitMQProducerProps(simulationFilePath, uri, exchange, queue, routingKey, String.valueOf(producerConnections), String.valueOf(eventsPerSec), String.valueOf(staggeringInterval));
				eventConsumer = new RabbitMQEventConsumer();
				consumerProperties = rabbitMQConsumerProps(uri, exchange, queue, routingKey, String.valueOf(producerConnections), String.valueOf(eventsPerSec), String.valueOf(staggeringInterval));
//				eventConsumer = new ClusterableTcpEventConsumer(consumerPort);
//			  consumerProperties = socketConsumerProps(host, String.valueOf(consumerPort), String.valueOf(consumerConnections), String.valueOf(fixture.getSimulation().getConsumerTimeOutInSec()));
				break;
			}
			case STREAM_SERVICE:
			{
				eventProducer = new StreamServiceEventProducer();
				producerProperties = streamProducerProps(simulationFilePath, host, String.valueOf(producerPort), String.valueOf(producerConnections));
				eventConsumer = new StreamServiceEventConsumer();
				consumerProperties = streamConsumerProps(host, String.valueOf(consumerPort), String.valueOf(consumerConnections));
				break;
			}
			case KAFKA:
			{
			  String brokerlist = fixture.getKafkaHost().getBrokerList();
			  String topic = fixture.getKafkaHost().getTopic();
			  String acks = fixture.getKafkaHost().getAcks();
			  eventProducer = new KafkaEventProducer();
			  producerProperties = kafkaProducerProps(simulationFilePath, brokerlist, topic, acks, String.valueOf(eventsPerSec), String.valueOf(staggeringInterval));
			  eventConsumer = new ClusterableTcpEventConsumer(consumerPort);
			  consumerProperties = socketConsumerProps(host, String.valueOf(consumerPort), String.valueOf(consumerConnections), String.valueOf(fixture.getSimulation().getConsumerTimeOutInSec()));
			  break;
			}
			case WEBSOCKET_SERVER:
      {
        //eventProducer = new WebsocketServerEverntProducer();
        producerProperties = socketProducerProps(simulationFilePath, host, String.valueOf(producerPort), String.valueOf(producerConnections), String.valueOf(eventsPerSec), String.valueOf(staggeringInterval));
        eventConsumer = new ClusterableTcpEventConsumer(consumerPort);
        consumerProperties = socketConsumerProps(host, String.valueOf(consumerPort), String.valueOf(consumerConnections), String.valueOf(fixture.getSimulation().getConsumerTimeOutInSec()));
        break;
      }
			default:
				break;
		}

		int producerCommandPort = fixture.isLocal() ? 5010 : fixture.getCommandPort();
		int consumerCommandPort = fixture.isLocal() ? 5020 : fixture.getCommandPort();
		String[] producers = null;
		String[] consumers = null;
		if( fixture.isLocal() )
		{
			producers = new String[]{"localhost"};
			consumers = new String[]{"localhost"};
		}
		else if( fixture.getProducers() != null && fixture.getConsumers() != null )
		{
			producers = fixture.getProducers().getProducers().toArray(new String[fixture.getProducers().getProducers().size()]);
			consumers = fixture.getConsumers().getConsumers().toArray(new String[fixture.getConsumers().getConsumers().size()]);
		}
		
		producerCount = producers.length;
		eventProducer = new RemoteDiagnosticsCollectorBaseClass(producers, producerCommandPort, fixture.isLocal());
		eventConsumer = new RemoteDiagnosticsCollectorBaseClass(consumers, consumerCommandPort, fixture.isLocal());
	
		eventProducer.init(producerProperties);
		eventProducer.setRunningStateListener(this);
		eventProducer.validate();

		eventConsumer.init(consumerProperties);
		eventConsumer.setRunningStateListener(this);
		eventConsumer.validate();
	}
	
	private Properties activeMQProducerProps(String simulationFilePath, String host)
	{
		Properties props = new Properties();
		props.put("simulationFilePath", simulationFilePath);
		props.put("providerUrl", "tcp://adamm:61617");
		props.put("destinationType", "Queue");
		props.put("destinationName", "InputQueue");
		return props;
	}

	private Properties activeMQConsumerProps(String host)
	{
		Properties props = new Properties();
		props.put("providerUrl", "tcp://adamm:61617");
		props.put("destinationType", "Queue");
		props.put("destinationName", "OutputQueue");
		return props;
	}

	private Properties rabbitMQProducerProps(String simulationFilePath, String uri, String exchangeName, String queueName, String routingKey, String connectionCount, String eventsPerSec, String staggeringInterval)
	{
		Properties producerProps = new Properties();
		producerProps.put("simulationFilePath", simulationFilePath);
		producerProps.put("uri", uri);
		producerProps.put("exchangeName", exchangeName);
		producerProps.put("queueName", queueName);
		if( routingKey != null )
			producerProps.put("routingKey", routingKey);
		producerProps.put("connectionCount", connectionCount);
		producerProps.put("eventsPerSec", eventsPerSec);
		producerProps.put("staggeringInterval", staggeringInterval);
		return producerProps;
	}

	private Properties rabbitMQConsumerProps(String uri, String exchangeName, String queueName, String routingKey, String connectionCount, String eventsPerSec, String staggeringInterval)
	{
		Properties consumerProps = new Properties();
		consumerProps.put("uri", uri);
		consumerProps.put("exchangeName", exchangeName);
		consumerProps.put("queueName", queueName);
		if( routingKey != null )
			consumerProps.put("routingKey", routingKey);
		consumerProps.put("connectionCount", connectionCount);
		consumerProps.put("eventsPerSec", eventsPerSec);
		consumerProps.put("staggeringInterval", staggeringInterval);
		return consumerProps;
	}

	private Properties socketConsumerProps(String targetHost, String targetPort, String connectionCount, String timeOutInSec)
	{
		Properties consumerProps = new Properties();
		consumerProps.put("host", targetHost);
		consumerProps.put("port", targetPort);
		consumerProps.put("connectionCount", connectionCount);
		consumerProps.put("timeOutInSec", timeOutInSec);
		return consumerProps;
	}

	private Properties socketProducerProps(String simulationFilePath, String targetHost, String targetPort, String connectionCount, String eventsPerSec, String staggeringInterval)
	{
		Properties producerProps = new Properties();
		producerProps.put("simulationFilePath", simulationFilePath);
		producerProps.put("host", targetHost);
		producerProps.put("port", targetPort);
		producerProps.put("connectionCount", connectionCount);
		producerProps.put("eventsPerSec", eventsPerSec);
		producerProps.put("staggeringInterval", staggeringInterval);
		return producerProps;
	}

	private Properties streamConsumerProps(String targetHost, String targetPort, String connectionCount)
	{
		Properties consumerProps = new Properties();
		consumerProps.put("host", targetHost);
		consumerProps.put("port", targetPort);
		consumerProps.put("connectionCount", connectionCount);
		consumerProps.put("serviceName", "asdi");
		return consumerProps;
	}

	private Properties streamProducerProps(String simulationFilePath, String targetHost, String targetPort, String connectionCount)
	{
		Properties producerProps = new Properties();
		producerProps.put("simulationFilePath", simulationFilePath);
		producerProps.put("host", targetHost);
		producerProps.put("port", targetPort);
		producerProps.put("connectionCount", connectionCount);
		producerProps.put("serviceName", "asdi");
		return producerProps;
	}
	
	private Properties kafkaProducerProps(String simulationFilePath, String brokerList, String topic, String acks, String eventsPerSec, String staggeringInterval)
	{
	  Properties producerProps = new Properties();
	  producerProps.put("simulationFilePath", simulationFilePath);
	  producerProps.put("brokerlist", brokerList);
	  producerProps.put("topic", topic);
	  producerProps.put("requiredacks", acks);
	  producerProps.put("eventsPerSec", eventsPerSec);
    producerProps.put("staggeringInterval", staggeringInterval);
	  return producerProps;
	}
	
	@Override
	public void runTest() throws TestException
	{
		if (eventsPerIteration < 1)
			throw new TestException("Number of events to be produced/consumed per iteration cannot be less than 1.");
		if (!eventConsumer.isRunning() && !eventProducer.isRunning())
		{
			// set the running flag
			if( ! running.get() )
				running.set(true);
			
			// System.out.println("Next iteration of "+eventsPerIteration+".");
			Map<Integer, Long[]> producerTimestamps = eventProducer.getTimeStamps();
			Map<Integer, Long[]> consumerTimestamps = eventConsumer.getTimeStamps();
			//System.out.println("Producer Size: " + producerTimestamps.size() + ", Consumer Size: " + consumerTimestamps.size());
			if (producerTimestamps.size() == numberOfIterations && consumerTimestamps.size() == numberOfIterations)
			{
				createStatistics(eventProducer.getSuccessfulEvents(), eventConsumer.getSuccessfulEvents(), producerTimestamps, consumerTimestamps, expectedResultCount);
				eventProducer.reset();
				eventConsumer.reset();
				if (eventsPerIteration < maxEventsPerIteration)
				{
					eventsPerIteration += eventsPerIterationStep;
					if( expectedResultCountPerIteration != -1 )
						expectedResultCount += expectedResultCountPerIteration;
				}
				else
				{
					destroy();
					return;
				}
			}
			currentIteration++;
			if( currentIteration > 1 )
			{
				try
				{
					myNull.length();
				}catch(NullPointerException ex )
				{
					System.err.println("Iteration count is off, how did we get here?");
					ex.printStackTrace();
				}
				
				return;
			}
			System.out.println("Running iteration: " + currentIteration);
			try
			{
				eventConsumer.setNumberOfEvents(producerCount * eventsPerIteration);
				eventConsumer.setNumberOfExpectedResults(producerCount * expectedResultCount);
				eventConsumer.start();
				// Sleep for a second to let the consumer get started.
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException ex)
				{
				}
			}
			catch (RunningException e)
			{
				throw new TestException("Event Consumer failed to start.");
			}
			try
			{
				eventProducer.setNumberOfEvents(eventsPerIteration);
				eventProducer.start();
			}
			catch (RunningException e)
			{
				throw new TestException("Event Producer failed to start.");
			}
		}
	}
	
	@Override
	public void destroy()
	{
		if( running.get() )
		{
			try{
				eventProducer.destroy();
				eventConsumer.destroy();
			}
			finally
			{
				running.set(false);
			}
		}
	}

	@Override
	public synchronized void onStateChange(RunningState state)
	{
		if (RunningState.STOPPED.equals(state) && isRunning())
		{
			try
			{
				runTest();
			}
			catch (TestException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void createStatistics(long totalEvents, long successes, Map<Integer, Long[]> producerDiagnostics, Map<Integer, Long[]> consumerDiagnostics, int expectedResultCount)
	{
		System.out.println("Trying to create the report statistics ...");
		int size = producerDiagnostics.size();
		
		// check if we have a sync issue / bad events / did not receive the final event(s) on consumer side
		// we do this to capture the statistics within the report
		if( size == (consumerDiagnostics.size() + 1) )
		{
			consumerDiagnostics.put(consumerDiagnostics.size(), new Long[] { System.currentTimeMillis(), System.currentTimeMillis() });
		}
		if (size > 0 && size == consumerDiagnostics.size())
		{
			long[] productionTimes = new long[size];
			long[] consumptionTimes = new long[size];
			long[] firstReceivedLatencies = new long[size];
			long[] lastReceivedLatencies = new long[size];
			long[] totalTimes = new long[size];
			
			for (int i = 0; i < size; i++)
			{
				Long[] pdIx = producerDiagnostics.get(i);
				Long[] cdIx = consumerDiagnostics.get(i);
				productionTimes[i] = pdIx[1] - pdIx[0];
				consumptionTimes[i] = cdIx[1] - cdIx[0];
				firstReceivedLatencies[i] = cdIx[0] - pdIx[0];
				lastReceivedLatencies[i] = cdIx[1] - pdIx[1];
				totalTimes[i] = cdIx[1] - pdIx[0];
				//System.out.println( "cdIx[0] = " + cdIx[0] + ", cdIx[1] = " + cdIx[1] );
				//System.out.println( "pdIx[0] = " + pdIx[0] + ", pdIx[1] = " + pdIx[1] );
				//System.out.println("Total times[" + i + "] = (" + cdIx[1] + ") - (" + pdIx[0] + "): " + totalTimes[i]);
			}
			try
			{
				long actualTotalEvents = totalEvents * producerConnections / numberOfIterations;
				FixtureStatistic fixtureStat = new FixtureStatistic(currentIteration);
				fixtureStat.addStat("totalEvents", actualTotalEvents);
				fixtureStat.addStat("successes", (successes / numberOfIterations));
				int actualExpectedResultCount =  (expectedResultCount != -1) ? expectedResultCount : (int)actualTotalEvents;
				long expectedFailures = actualTotalEvents - actualExpectedResultCount;
				long failures = (totalEvents * producerConnections * consumerConnections - successes - expectedFailures);
				fixtureStat.addStat("expectedResultCount", (actualExpectedResultCount / numberOfIterations));
				fixtureStat.addStat("failures", (failures / numberOfIterations));
				fixtureStat.addStat("producerConnections", producerConnections);
				fixtureStat.addStat("consumerConnections", consumerConnections);
				
				Statistics prodTimesStats = new Statistics(productionTimes);
				fixtureStat.addStat("minProductionTime", prodTimesStats.getMinimum());
				fixtureStat.addStat("maxProductionTime", prodTimesStats.getMaximum());
				fixtureStat.addStat("avgProductionTime", prodTimesStats.getMean());
				fixtureStat.addStat("medProductionTime", prodTimesStats.getMedian());
				fixtureStat.addStat("devProductionTime", prodTimesStats.getStdDev());
				
				Statistics consumptionTimesStats = new Statistics(consumptionTimes);
				fixtureStat.addStat("minConsumptionTime", consumptionTimesStats.getMinimum());
				fixtureStat.addStat("maxConsumptionTime", consumptionTimesStats.getMaximum());
				fixtureStat.addStat("avgConsumptionTime", consumptionTimesStats.getMean());
				fixtureStat.addStat("medConsumptionTime", consumptionTimesStats.getMedian());
				fixtureStat.addStat("devConsumptionTime", consumptionTimesStats.getStdDev());
				
				Statistics totalTimesStats = new Statistics(totalTimes);
				fixtureStat.addStat("minTotalTime", totalTimesStats.getMinimum());
				fixtureStat.addStat("maxTotalTime", totalTimesStats.getMaximum());
				fixtureStat.addStat("avgTotalTime", totalTimesStats.getMean());
				fixtureStat.addStat("medTotalTime", totalTimesStats.getMedian());
				fixtureStat.addStat("devTotalTime", totalTimesStats.getStdDev());
				
				double minEventsPerSec = ((double)(actualTotalEvents-failures)/(double)totalTimesStats.getMinimum()) * 1000.0d;
				fixtureStat.addStat("minEventsPerSec", minEventsPerSec);
				double maxEventsPerSec = ((double)(actualTotalEvents-failures)/(double)totalTimesStats.getMaximum()) * 1000.0d;
				fixtureStat.addStat("maxEventsPerSec", maxEventsPerSec);
				double avgEventsPerSec = ((double)(actualTotalEvents-failures)/totalTimesStats.getMean()) * 1000.0d;
				fixtureStat.addStat("avgEventsPerSec", avgEventsPerSec);
				//System.out.println("avgEventsPerSec = " + (actualTotalEvents-failures) + " / "  + totalTimesStats.getMean() + " *  1000: " +  avgEventsPerSec);
				if( eventsPerSec > 0 )
				{
					fixtureStat.addStat("Rate", eventsPerSec);
					fixtureStat.addStat("%", avgEventsPerSec/(double)eventsPerSec);
				}
				Statistics firstReceivedLatenciesStats = new Statistics(firstReceivedLatencies);
				fixtureStat.addStat("minFirstReceivedLatency", firstReceivedLatenciesStats.getMinimum());
				fixtureStat.addStat("maxFirstReceivedLatency", firstReceivedLatenciesStats.getMaximum());
				fixtureStat.addStat("avgFirstReceivedLatency", firstReceivedLatenciesStats.getMean());
				fixtureStat.addStat("medFirstReceivedLatency", firstReceivedLatenciesStats.getMedian());
				fixtureStat.addStat("devFirstReceivedLatency", firstReceivedLatenciesStats.getStdDev());
				
				Statistics lastReceivedLatenciesStats = new Statistics(lastReceivedLatencies);
				fixtureStat.addStat("minLastReceivedLatency", lastReceivedLatenciesStats.getMinimum());
				fixtureStat.addStat("maxLastReceivedLatency", lastReceivedLatenciesStats.getMaximum());
				fixtureStat.addStat("avgLastReceivedLatency", lastReceivedLatenciesStats.getMean());
				fixtureStat.addStat("medLastReceivedLatency", lastReceivedLatenciesStats.getMedian());
				fixtureStat.addStat("devLastReceivedLatency", lastReceivedLatenciesStats.getStdDev());
				
				//add the statistics
				FixturesStatistics.getInstance().addFixtureStatistic(testName, fixtureStat);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
			System.out.println("Failed to create the statistics, sizes(producer=" + size + ", consumer=" + consumerDiagnostics.size() +") do not match!");
	}

	@Override
	public synchronized boolean isRunning()
	{
		return running.get();
	}
}
