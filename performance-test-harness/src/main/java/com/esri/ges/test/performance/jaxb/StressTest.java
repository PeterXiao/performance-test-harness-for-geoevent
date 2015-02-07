package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "StressTest")
public class StressTest extends AbstractTest
{
	private int iterations = 1;
	private int minEvents = 0;
	private int expectedResultCount = -1;
	
	public StressTest()
	{
		setType(TestType.STRESS);
	}
	
	@XmlAttribute
	public int getIterations()
	{
		return iterations;
	}
	public void setIterations(int iterations)
	{
		this.iterations = iterations;
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
	public int getExpectedResultCount()
	{
		return expectedResultCount;
	}
	public void setExpectedResultCount(int expectedResultCount)
	{
		this.expectedResultCount = expectedResultCount;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
