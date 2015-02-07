package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "RampTest")
public class RampTest extends AbstractTest
{
	private final int DEFAULT_MIN_EVENTS = 0;
	private final int DEFAULT_MAX_EVENTS = 0;
	
	private int minEvents = DEFAULT_MIN_EVENTS;
	private int maxEvents = DEFAULT_MAX_EVENTS;
	private int eventsToAddPerTest = 0;
	private int expectedResultCountPerTest = -1;
	
	public RampTest()
	{
		setType(TestType.RAMP);
	}
	
	@XmlAttribute
	public int getMinEvents()
	{
		return minEvents;
	}
	public void setMinEvents(int minEvents)
	{
		this.minEvents = minEvents;
	}
	
	@XmlAttribute
	public int getMaxEvents()
	{
		return maxEvents;
	}
	public void setMaxEvents(int maxEvents)
	{
		this.maxEvents = maxEvents;
	}
	
	@XmlAttribute
	public int getEventsToAddPerTest()
	{
		return eventsToAddPerTest;
	}
	public void setEventsToAddPerTest(int eventsToAddPerTest)
	{
		this.eventsToAddPerTest = eventsToAddPerTest;
	}
	
	@XmlAttribute
	public int getExpectedResultCountPerTest()
	{
		return expectedResultCountPerTest;
	}
	public void setExpectedResultCountPerTest(int expectedResultCountPerTest)
	{
		this.expectedResultCountPerTest = expectedResultCountPerTest;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
