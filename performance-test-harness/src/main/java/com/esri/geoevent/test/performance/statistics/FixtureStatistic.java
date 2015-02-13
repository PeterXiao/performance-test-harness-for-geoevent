package com.esri.ges.test.performance.statistics;

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
