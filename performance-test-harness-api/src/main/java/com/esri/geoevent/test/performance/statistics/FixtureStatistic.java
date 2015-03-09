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

import java.util.HashMap;
import java.util.Map;

public class FixtureStatistic implements Comparable<FixtureStatistic>
{
	private int iteration;
	private Map<String, Object> stats;
	
	//----------------------------------------------------------
	//  Constructor Methods
	//----------------------------------------------------------
	
	public FixtureStatistic(int iteration)
	{
		this.iteration = iteration;
	}
	
	//----------------------------------------------------------
	//  Public Methods
	//----------------------------------------------------------
	
	@Override
	public int compareTo(FixtureStatistic obj)
	{
		if( obj != null )
		{
			return Integer.compare(getIteration(), obj.getIteration());
		}
		return 0;
	}
	
	//----------------------------------------------------------
	//  Getters/ Setters Methods
	//----------------------------------------------------------
	
	public int getIteration()
	{
		return iteration;
	}
	
	public Map<String, Object> getStats()
	{
		return stats;
	}
	public void setStats(Map<String, Object> stats)
	{
		this.stats = stats;
	}
	
	public void addStat(String columnName, Object value)
	{
		if( stats == null )
		{
			stats = new HashMap<String, Object>();
		}
		stats.put(columnName, value);
	}
	
	public Object getStat(String columnName)
	{
		if( stats == null || ! stats.containsKey(columnName) )
			return null;
		return stats.get( columnName );
	}
	
	public String getStatAsString(String columnName)
	{
		if( stats == null || ! stats.containsKey(columnName) )
			return "";
		Object value = stats.get( columnName );
		return value.toString();
	}
}
