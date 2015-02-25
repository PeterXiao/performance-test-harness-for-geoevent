package com.esri.geoevent.test.performance;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esri.geoevent.test.performance.jaxb.ConsumerConfig;
import com.esri.geoevent.test.performance.jaxb.Fixture;
import com.esri.geoevent.test.performance.jaxb.ProducerConfig;
import com.esri.geoevent.test.performance.jaxb.Property;
import com.esri.geoevent.test.performance.jaxb.RemoteHost;
import com.esri.geoevent.test.performance.jaxb.TestType;
import com.esri.geoevent.test.performance.statistics.FixtureStatistic;
import com.esri.geoevent.test.performance.statistics.FixturesStatistics;
import com.esri.geoevent.test.performance.statistics.Statistics;

/**
 * <b>NOTE:</b> This class is not Thread-safe
 */
public class ThroughputPerformanceTestHarness implements TestHarness, RunningStateListener
{
	private PerformanceCollector					eventProducer;
	private PerformanceCollector					eventConsumer;

	private int														numberOfIterations;
	private int														eventsPerIteration;
	private int														expectedResultCount;
	private int														minEventsPerIteration;
	private int														maxEventsPerIteration;
	private int														eventsPerIterationStep;
	private int														producerCount				= 1;
	private int														producerConnections	= 1;
	private int														consumerConnections	= 1;
	private int														expectedResultCountPerIteration;
	private int														eventsPerSec				= -1;
	private int														staggeringInterval	= 10;
	private int														currentIteration		= 0;
	private String												testName						= "";
	private TestType 											testType						= null;
	
	public Protocol												protocol						= Protocol.TCP;
	private AtomicBoolean									running							= new AtomicBoolean(false);
	private Fixture												fixture;
	private Map<String, RunningState>	runningStateMap			= new ConcurrentHashMap<String, RunningState>();

	public ThroughputPerformanceTestHarness(Fixture fixture)
	{
		this.fixture = fixture;
		runningStateMap = new ConcurrentHashMap<String, RunningState>();
	}

	@Override
	public void init() throws TestException
	{
		this.numberOfIterations = fixture.getSimulation().getIterations();
		this.minEventsPerIteration = fixture.getSimulation().getMinEvents();
		this.maxEventsPerIteration = fixture.getSimulation().getMaxEvents();
		this.eventsPerIterationStep = fixture.getSimulation().getEventsToAddPerIteration();
		this.producerConnections = fixture.getProducerConfig().getNumOfConnections();
		this.consumerConnections = fixture.getConsumerConfig().getNumOfConnections();
		this.expectedResultCountPerIteration = fixture.getSimulation().getExpectedResultCount();
		this.eventsPerIteration = this.minEventsPerIteration;
		this.expectedResultCount = this.expectedResultCountPerIteration;
		this.eventsPerSec = fixture.getSimulation().getEventsPerSec();
		this.staggeringInterval = fixture.getSimulation().getStaggeringInterval();
		this.testName = fixture.getName();
		this.testType = fixture.getSimulation().getTest().getType();
		
		System.out.println("-------------------------------------------------------");
		System.out.println( Messages.getMessage("TEST_HARNESS_START_MSG", testName ) );
		System.out.println("-------------------------------------------------------");

		ProducerConfig producerConfig = fixture.getProducerConfig();
		ConsumerConfig consumerConfig = fixture.getConsumerConfig();
		// apply any shared properties and settings that may not have been applied
		if (fixture.getDefaultConfig() != null)
		{
			producerConfig.apply(fixture.getDefaultConfig());
			consumerConfig.apply(fixture.getDefaultConfig());
		}

		// add the test properties
		producerConfig.getProperties().add(new Property("eventsPerSec", String.valueOf(eventsPerSec)));
		producerConfig.getProperties().add(new Property("staggeringInterval", String.valueOf(staggeringInterval)));
		producerConfig.getProperties().add(new Property("testType", testType.toString()));
		
		consumerConfig.getProperties().add(new Property("testType", testType.toString()));
		
		// get the list of producers
		List<RemoteHost> producers = producerConfig.getProducers();
		if (producers.isEmpty())
			producers.add(producerConfig.getDefaultRemoteHost());

		// init the remote producer(s) proxy
		eventProducer = new RemotePerformanceCollectorBase(producers);
		eventProducer.init(producerConfig);
		eventProducer.setRunningStateListener(this);
		eventProducer.validate();

		// get the list of consumers
		List<RemoteHost> consumers = consumerConfig.getConsumers();
		if (consumers.isEmpty())
			consumers.add(consumerConfig.getDefaultRemoteHost());

		// init the remote consumer(s) proxy
		eventConsumer = new RemotePerformanceCollectorBase(consumers);
		eventConsumer.init(consumerConfig);
		eventConsumer.setRunningStateListener(this);
		eventConsumer.validate();
	}

	@Override
	public void runTest() throws TestException
	{
		if (eventsPerIteration < 1)
			throw new TestException( Messages.getMessage("TEST_HARNESS_NUM_OF_EVENTS_VALIDATION") );
		if (!eventConsumer.isRunning() && !eventProducer.isRunning())
		{
			// set the running flag
			if (!running.get())
				running.set(true);

			currentIteration++;
			if( testType != TestType.TIME)
				System.out.println( Messages.getMessage("TEST_HARNESS_ITERATION_MSG", currentIteration) );
			
			try
			{
				eventConsumer.setNumberOfEvents(producerCount * eventsPerIteration);
				if( expectedResultCount != -1 )
				{
					eventConsumer.setNumberOfExpectedResults(producerCount * expectedResultCount);
				}
				else
				{
					expectedResultCount = eventsPerIteration;
					eventConsumer.setNumberOfExpectedResults(producerCount * eventsPerIteration);
				}
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
				throw new TestException( Messages.getMessage("TEST_HARNESS_CONSUMER_START_ERROR") );
			}
			try
			{
				eventProducer.setNumberOfEvents(eventsPerIteration);
				eventProducer.start();
			}
			catch (RunningException e)
			{
				throw new TestException( Messages.getMessage("TEST_HARNESS_PRODUCER_START_ERROR") );
			}
		}
	}

	@Override
	public void destroy()
	{
		if (running.get())
		{
			try
			{
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
		// add the state of the component to our cache
		runningStateMap.put(state.getConnectionString(), state);
		checkIfTestIsComplete();
	}

	private void checkIfTestIsComplete()
	{
		Set<String> connections = runningStateMap.keySet();
		for( String connection : connections )
		{
			// check if all connections are done - if not we wait
			RunningState state = runningStateMap.get(connection);
			if( state.getType() != RunningStateType.STOPPED )
				return;
		}
		
		// all connections are done - lets finish up
		Map<Integer, Long[]> producerTimestamps = eventProducer.getTimeStamps();
		Map<Integer, Long[]> consumerTimestamps = eventConsumer.getTimeStamps();
		
		if (producerTimestamps.size() == numberOfIterations && consumerTimestamps.size() == numberOfIterations)
		{
			createStatistics(eventProducer.getSuccessfulEvents(), eventConsumer.getSuccessfulEvents(), producerTimestamps, consumerTimestamps, expectedResultCount);
			eventProducer.reset();
			eventConsumer.reset();
			if (eventsPerIteration < maxEventsPerIteration)
			{
				eventsPerIteration += eventsPerIterationStep;
				if (expectedResultCountPerIteration != -1)
					expectedResultCount += expectedResultCountPerIteration;
				
				// re-run the tests - next iteration
				try
				{
					runTest();
				} 
				catch( Exception error )
				{
					error.printStackTrace();
				}
			}
			// the tests are done
			else
			{
				destroy();
			}
		}
		else
		{
			if (expectedResultCountPerIteration != -1)
				expectedResultCount += (producerCount * expectedResultCountPerIteration);
			else
				expectedResultCount += (producerCount * eventsPerIteration);
			
			// re-run the tests - next iteration
			try
			{
				runTest();
			} 
			catch( Exception error )
			{
				error.printStackTrace();
			}
		}
	}
	
	private void createStatistics(long totalEvents, long successes, Map<Integer, Long[]> producerDiagnostics, Map<Integer, Long[]> consumerDiagnostics, int expectedResultCount)
	{
		System.out.print( Messages.getMessage("TEST_HARNESS_REPORT_STATS_MSG") );
		int size = producerDiagnostics.size();

		// check if we have a sync issue / bad events / did not receive the final event(s) on consumer side
		// we do this to capture the statistics within the report
		if (size == (consumerDiagnostics.size() + 1))
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
			}
			try
			{
				long actualTotalEvents = totalEvents * producerConnections / numberOfIterations;
				FixtureStatistic fixtureStat = new FixtureStatistic(currentIteration);
				fixtureStat.addStat("totalEvents", actualTotalEvents);
				fixtureStat.addStat("successes", (successes / numberOfIterations));
				int actualExpectedResultCount = (expectedResultCount != -1) ? expectedResultCount : (int) actualTotalEvents;
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

				double minEventsPerSec = ((double) (actualTotalEvents - failures) / (double) totalTimesStats.getMinimum()) * 1000.0d;
				fixtureStat.addStat("minEventsPerSec", minEventsPerSec);
				double maxEventsPerSec = ((double) (actualTotalEvents - failures) / (double) totalTimesStats.getMaximum()) * 1000.0d;
				fixtureStat.addStat("maxEventsPerSec", maxEventsPerSec);
				double avgEventsPerSec = ((double) (actualTotalEvents - failures) / totalTimesStats.getMean()) * 1000.0d;
				fixtureStat.addStat("avgEventsPerSec", avgEventsPerSec);
				if (eventsPerSec > 0)
				{
					fixtureStat.addStat("Rate", eventsPerSec);
					fixtureStat.addStat("%", avgEventsPerSec / (double) eventsPerSec);
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

				// add the statistics
				FixturesStatistics.getInstance().addFixtureStatistic(testName, fixtureStat);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println( Messages.getMessage("DONE") );
		}
		else
			System.out.println( Messages.getMessage("TEST_HARNESS_REPORT_STATS_ERROR", size, consumerDiagnostics.size() ) );
	}

	@Override
	public synchronized boolean isRunning()
	{
		return running.get();
	}
}
