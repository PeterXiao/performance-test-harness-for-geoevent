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
import com.esri.geoevent.test.performance.jaxb.RampTest;
import com.esri.geoevent.test.performance.jaxb.RemoteHost;
import com.esri.geoevent.test.performance.jaxb.StressTest;
import com.esri.geoevent.test.performance.jaxb.TestType;
import com.esri.geoevent.test.performance.jaxb.TimeTest;
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
		this.producerConnections = Math.max(fixture.getProducerConfig().getNumOfConnections(), 1);
		this.consumerConnections = Math.max(fixture.getConsumerConfig().getNumOfConnections(), 1);
		this.expectedResultCountPerIteration = fixture.getSimulation().getExpectedResultCount();
		this.eventsPerIteration = this.minEventsPerIteration;
		this.expectedResultCount = this.expectedResultCountPerIteration;
		this.eventsPerSec = fixture.getSimulation().getEventsPerSec();
		this.staggeringInterval = fixture.getSimulation().getStaggeringInterval();
		this.testName = fixture.getName();
		this.testType = fixture.getSimulation().getTest().getType();
				
		System.out.println("-------------------------------------------------------");
		System.out.println( ImplMessages.getMessage("TEST_HARNESS_START_MSG", testName ) );
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
		if( testType == TestType.TIME )
		{
			TimeTest timeTest = (TimeTest) fixture.getSimulation().getTest();
			int expectedEventsPerSec = (timeTest.getExpectedResultCountPerSec() != -1) ? timeTest.getExpectedResultCountPerSec() : eventsPerSec;
			consumerConfig.getProperties().add(new Property("eventsPerSec", String.valueOf(expectedEventsPerSec)));
			consumerConfig.getProperties().add(new Property("totalTimeInSec", String.valueOf(timeTest.getTotalTimeInSec())));
		}
		
		// get the list of producers
		List<RemoteHost> producers = producerConfig.getProducers();
		if (producers.isEmpty())
			producers.add(producerConfig.getDefaultRemoteHost());

		// init the remote producer(s) proxy
		eventProducer = new RemotePerformanceCollectorBase(producers);
		eventProducer.setRunningStateListener(this);
		eventProducer.init(producerConfig);
		eventProducer.validate();

		// get the list of consumers
		List<RemoteHost> consumers = consumerConfig.getConsumers();
		if (consumers.isEmpty())
			consumers.add(consumerConfig.getDefaultRemoteHost());

		// init the remote consumer(s) proxy
		eventConsumer = new RemotePerformanceCollectorBase(consumers);
		eventConsumer.setRunningStateListener(this);
		eventConsumer.init(consumerConfig);
		eventConsumer.validate();
	}

	@Override
	public void runTest() throws TestException
	{
		if (eventsPerIteration < 1)
			throw new TestException( ImplMessages.getMessage("TEST_HARNESS_NUM_OF_EVENTS_VALIDATION") );
		if (!eventConsumer.isRunning() && !eventProducer.isRunning())
		{
			// set the running flag
			if (!running.get())
				running.set(true);

			currentIteration++;
			if( testType != TestType.TIME)
				System.out.println( ImplMessages.getMessage("TEST_HARNESS_ITERATION_MSG", currentIteration) );
			
			// calculate two things 
			// 1.) the number of events to produce for this iteration
			// 2.) the number of expected events to consumer for this iteration
			
			int numOfEventsToProduce = 0;
			int expectedResultCountToConsume = 0;
			switch( fixture.getSimulation().getTest().getType() )
			{	
				case RAMP:
					RampTest rampTest = (RampTest) fixture.getSimulation().getTest();
					numOfEventsToProduce = (producerCount * rampTest.getEventsToAddPerTest() * currentIteration); 
					expectedResultCountToConsume = (rampTest.getExpectedResultCountPerTest() != -1) ? (producerCount * rampTest.getExpectedResultCountPerTest() * currentIteration) : numOfEventsToProduce; 
					break;
				case STRESS:
					StressTest stressTest = (StressTest) fixture.getSimulation().getTest();
					numOfEventsToProduce = (producerCount * stressTest.getNumOfEvents()); 
					expectedResultCountToConsume = (stressTest.getExpectedResultCount() != -1) ? (producerCount * stressTest.getExpectedResultCount() * currentIteration) : numOfEventsToProduce * currentIteration;
					break;
				case TIME:
					TimeTest timeTest = (TimeTest) fixture.getSimulation().getTest();
					numOfEventsToProduce = timeTest.getEventsPerSec() * timeTest.getTotalTimeInSec();
					expectedResultCountToConsume = (timeTest.getExpectedResultCountPerSec() != -1) ? (timeTest.getExpectedResultCountPerSec() * timeTest.getTotalTimeInSec()) : numOfEventsToProduce;
					break;
				default:
					break;
			}
			
			try
			{
				eventConsumer.setNumberOfEvents(numOfEventsToProduce);
				eventConsumer.setNumberOfExpectedResults(expectedResultCountToConsume);
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
				throw new TestException( ImplMessages.getMessage("TEST_HARNESS_CONSUMER_START_ERROR") );
			}
			try
			{
				eventProducer.setNumberOfEvents(numOfEventsToProduce);
				eventProducer.start();
			}
			catch (RunningException e)
			{
				throw new TestException( ImplMessages.getMessage("TEST_HARNESS_PRODUCER_START_ERROR") );
			}
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			if( eventProducer != null )
				eventProducer.destroy();
			if( eventConsumer != null )
				eventConsumer.destroy();
		}
		catch( Exception ignored )
		{
		}
		finally
		{
			running.set(false);
		}
	}

	@Override
	public synchronized void onStateChange(RunningState state)
	{
		// add the state of the component to our cache
		runningStateMap.put(state.getConnectionString(), state);
		//System.out.println( "Adding connection string: " + state.getConnectionString() + " with state: " + state);
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
			long bytesConsumed = eventConsumer.getSuccessfulEventBytes();
			createStatistics(eventProducer.getSuccessfulEvents(), eventConsumer.getSuccessfulEvents(), producerTimestamps, consumerTimestamps, expectedResultCount, bytesConsumed);
			eventProducer.reset();
			eventConsumer.reset();
			
			if (eventsPerIteration < maxEventsPerIteration)
			{
				eventsPerIteration += eventsPerIterationStep;				
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
	
	private void createStatistics(long totalEvents, long successes, Map<Integer, Long[]> producerDiagnostics, Map<Integer, Long[]> consumerDiagnostics, int expectedResultCount, long bytesConsumed)
	{
		System.out.print( ImplMessages.getMessage("TEST_HARNESS_REPORT_STATS_MSG") );
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

				fixtureStat.addStat("totalBytesConsumed", bytesConsumed);
				fixtureStat.addStat("avgBytesConsumedPerMessage", new Long(bytesConsumed).doubleValue() / ( new Long(successes).doubleValue() / new Integer(numberOfIterations).doubleValue() ) );
				
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

				double latency = Math.max( firstReceivedLatenciesStats.getMinimum(), 0);
				double minEventsPerSec = ((double) (actualTotalEvents - failures) / ((double) totalTimesStats.getMinimum() - latency)) * 1000.0d;
				fixtureStat.addStat("minEventsPerSec", minEventsPerSec);
				
				latency = Math.max( firstReceivedLatenciesStats.getMaximum(), 0);
				double maxEventsPerSec = ((double) (actualTotalEvents - failures) / ((double) totalTimesStats.getMaximum() - latency)) * 1000.0d;
				fixtureStat.addStat("maxEventsPerSec", maxEventsPerSec);
				
				latency = Math.max( firstReceivedLatenciesStats.getMean(), 0);
				double avgEventsPerSec = ((double) (actualTotalEvents - failures) / (totalTimesStats.getMean() - latency)) * 1000.0d;
				fixtureStat.addStat("avgEventsPerSec", avgEventsPerSec);
				if (eventsPerSec > 0)
				{
					fixtureStat.addStat("Rate", eventsPerSec);
					fixtureStat.addStat("%", avgEventsPerSec / (double) eventsPerSec);
				}
				
				// add the statistics
				FixturesStatistics.getInstance().addFixtureStatistic(testName, fixtureStat);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println( ImplMessages.getMessage("DONE") );
		}
		else
			System.out.println( ImplMessages.getMessage("TEST_HARNESS_REPORT_STATS_ERROR", size, consumerDiagnostics.size() ) );
	}

	@Override
	public synchronized boolean isRunning()
	{
		return running.get();
	}
}
