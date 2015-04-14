package com.esri.geoevent.test.performance.db;

public class DBResult
{
	private long startTime;
	private long endTime;
	private int totalCount;
	
	public DBResult()
	{
	}
	
	public DBResult(long startTime, long endTime, int totalCount)
	{
		this.startTime = startTime;
		this.endTime = endTime;
		this.totalCount = totalCount;
	}
	
	public long getStartTime()
	{
		return startTime;
	}
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}
	
	public long getEndTime()
	{
		return endTime;
	}
	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}
	
	public int getTotalCount()
	{
		return totalCount;
	}
	public void setTotalCount(int totalCount)
	{
		this.totalCount = totalCount;
	}
}
