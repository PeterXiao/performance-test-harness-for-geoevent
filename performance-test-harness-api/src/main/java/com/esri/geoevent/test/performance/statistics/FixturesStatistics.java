package com.esri.geoevent.test.performance.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixturesStatistics
{
	//private vars
	private Map<String, List<FixtureStatistic>> stats = new HashMap<String, List<FixtureStatistic>>();
	
	// ----------------------------------------------------------
	// Constructor - Singleton
	// ----------------------------------------------------------

	// Private constructor prevents instantiation from other classes
	private FixturesStatistics()
	{
		;
	}

	/**
	 * FixturesStatisticsHolder is loaded on the first execution of
	 * FixturesStatisticsHolder.getInstance() or the first access to
	 * FixturesStatisticsHolder.INSTANCE, not before.
	 */
	private static class FixturesStatisticsHolder
	{
		public static final FixturesStatistics	INSTANCE	= new FixturesStatistics();
	}

	public static FixturesStatistics getInstance()
	{
		return FixturesStatisticsHolder.INSTANCE;
	}
	
	//----------------------------------------------------------
	//  Public Methods
	//----------------------------------------------------------
	
	public void addFixtureStatistic(String testName, FixtureStatistic statistic)
	{
		if( ! stats.containsKey(testName) )
		{
			stats.put(testName, new ArrayList<FixtureStatistic>());
		}
		List<FixtureStatistic> fixtureStats = stats.get(testName);
		fixtureStats.add( statistic );
	}
	
	public Map<String, List<FixtureStatistic>> getStats()
	{
		return stats;
	}
}
