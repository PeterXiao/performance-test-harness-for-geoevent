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
package com.esri.geoevent.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "TimeTest")
public class TimeTest extends AbstractTest
{
	protected final int DEFAULT_EVENTS_PER_SEC = 0;
	protected final int DEFAULT_TOTAL_TIME_IN_SEC = 0;
	protected final int DEFAULT_EXPECTED_RESULT_COUNT_PER_SEC = -1;
	protected final int DEFAULT_STAGGERING_INTERVAL = 10;
	
	private int eventsPerSec = DEFAULT_EVENTS_PER_SEC;
	private int totalTimeInSec = DEFAULT_TOTAL_TIME_IN_SEC;
	private int expectedResultCountPerSec = DEFAULT_EXPECTED_RESULT_COUNT_PER_SEC;
	private int staggeringInterval = DEFAULT_STAGGERING_INTERVAL;
	
	public TimeTest()
	{
		setType(TestType.TIME);
	}
	
	@XmlAttribute
	public int getEventsPerSec()
	{
		return eventsPerSec;
	}
	public void setEventsPerSec(int eventsPerSec)
	{
		this.eventsPerSec = eventsPerSec;
	}
	
	@XmlAttribute
	public int getTotalTimeInSec()
	{
		return totalTimeInSec;
	}
	public void setTotalTimeInSec(int totalTimeInSec)
	{
		this.totalTimeInSec = totalTimeInSec;
	}
	
	@XmlAttribute
	public int getExpectedResultCountPerSec()
	{
		return expectedResultCountPerSec;
	}
	public void setExpectedResultCountPerSec(int expectedResultCountPerSec)
	{
		this.expectedResultCountPerSec = expectedResultCountPerSec;
	}
	
	@XmlAttribute
	public int getStaggeringInterval()
	{
		return staggeringInterval;
	}
	public void setStaggeringInterval(int staggeringInterval)
	{
		this.staggeringInterval = staggeringInterval;
	}
	
	@Override
	public void apply(Test test)
	{
		if( test == null )
			return;
		
		if( test instanceof TimeTest )
		{
			TimeTest timeTest = (TimeTest) test;
			if( getEventsPerSec() == DEFAULT_EVENTS_PER_SEC )
				setEventsPerSec( timeTest.getEventsPerSec() );
			if( getExpectedResultCountPerSec() == DEFAULT_EXPECTED_RESULT_COUNT_PER_SEC )
				setExpectedResultCountPerSec( timeTest.getExpectedResultCountPerSec() );
			if( getStaggeringInterval() == DEFAULT_STAGGERING_INTERVAL )
				setStaggeringInterval( timeTest.getStaggeringInterval() );
			if( getTotalTimeInSec() == DEFAULT_TOTAL_TIME_IN_SEC )
				setTotalTimeInSec( timeTest.getTotalTimeInSec() );
		}
	}
	
	@Override
	public Test copy()
	{
		TimeTest copy = new TimeTest();
		copy.setEventsPerSec(getEventsPerSec());
		copy.setExpectedResultCountPerSec(getExpectedResultCountPerSec());
		copy.setStaggeringInterval(getStaggeringInterval());
		copy.setTotalTimeInSec(getTotalTimeInSec());
		copy.setType(getType());
		return copy;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof TimeTest))
      return false;
		
		TimeTest test = (TimeTest) obj;
    if (!ObjectUtils.equals(getEventsPerSec(), test.getEventsPerSec()))
      return false;
    if (!ObjectUtils.equals(getExpectedResultCountPerSec(), test.getExpectedResultCountPerSec()))
      return false;
    if (!ObjectUtils.equals(getStaggeringInterval(), test.getStaggeringInterval()))
      return false;
    if (!ObjectUtils.equals(getTotalTimeInSec(), test.getTotalTimeInSec()))
      return false;
    
    return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
