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
