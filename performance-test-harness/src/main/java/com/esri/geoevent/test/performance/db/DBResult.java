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
package com.esri.geoevent.test.performance.db;

public class DBResult
{
	private long	startTime;
	private long	endTime;
	private int		totalCount;

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

	public void addStartTime(long startTime)
	{
		if (startTime < this.startTime) 
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

	public void addEndTime(long endTime)
	{
		if (endTime > this.endTime) 
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

	public void addTotalCount(int totalCount)
	{
		this.totalCount += totalCount;
	}
}
