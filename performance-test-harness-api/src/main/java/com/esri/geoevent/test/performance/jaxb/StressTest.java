package com.esri.geoevent.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "StressTest")
public class StressTest extends AbstractTest 
{
	protected final int DEFAULT_ITERATIONS = 1;
	protected final int DEFAULT_NUM_OF_EVENTS = 0;
	protected final int DEFAULT_EXPECTED_RESULT_COUNT = -1;
	
	private int iterations = DEFAULT_ITERATIONS;
	private int numOfEvents = DEFAULT_NUM_OF_EVENTS;
	private int expectedResultCount = DEFAULT_EXPECTED_RESULT_COUNT;
	
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
	public int getNumOfEvents()
	{
		return numOfEvents;
	}
	public void setNumOfEvents(int minEvents)
	{
		this.numOfEvents = minEvents;
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
	public void apply(Test test)
	{
		if( test == null )
			return;
		
		if( test instanceof StressTest )
		{
			StressTest stressTest = (StressTest) test;
			if( getExpectedResultCount() == DEFAULT_EXPECTED_RESULT_COUNT )
				setExpectedResultCount( stressTest.getExpectedResultCount() );
			if( getIterations() == DEFAULT_ITERATIONS )
				setIterations( stressTest.getIterations() );
			if( getNumOfEvents() == DEFAULT_NUM_OF_EVENTS )
				setNumOfEvents( stressTest.getNumOfEvents() );
		}
	}
	
	@Override
	public Test copy()
	{
		StressTest copy = new StressTest();
		copy.setExpectedResultCount(getExpectedResultCount());
		copy.setIterations(getIterations());
		copy.setNumOfEvents(getNumOfEvents());
		copy.setType(getType());
		return copy;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof StressTest))
      return false;
		
		StressTest test = (StressTest) obj;
    if (!ObjectUtils.equals(getExpectedResultCount(), test.getExpectedResultCount()))
      return false;
    if (!ObjectUtils.equals(getIterations(), test.getIterations()))
      return false;
    if (!ObjectUtils.equals(getNumOfEvents(), test.getNumOfEvents()))
      return false;
    
    return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
