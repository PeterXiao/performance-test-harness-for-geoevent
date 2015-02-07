package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "TimeTest")
public class TimeTest extends AbstractTest
{
	private int eventsPerSec = 0;
	private int totalTimeInSec = 0;
	private int expectedResultCountPerSec = -1;
	private int staggeringInterval = 10;
	
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
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
