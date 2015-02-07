package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Simulation")
public class Simulation
{
	private final int DEFAULT_CONSUMER_TIMEOUT_IN_SEC = 10;

	private int consumerTimeOutInSec = DEFAULT_CONSUMER_TIMEOUT_IN_SEC;
	private String sourceFile;
	private Test test;
	
	@XmlAttribute
	public int getConsumerTimeOutInSec()
	{
		return consumerTimeOutInSec;
	}
	public void setConsumerTimeOutInSec(int consumerTimeOutInSec)
	{
		this.consumerTimeOutInSec = consumerTimeOutInSec;
	}
	
	@XmlElement(name = "SourceFile")
	public String getSourceFile()
	{
		return sourceFile;
	}
	public void setSourceFile(String sourceFile)
	{
		this.sourceFile = sourceFile;
	}
	
	@XmlElements( { 
		@XmlElement( name="RampTest", type = RampTest.class ),
		@XmlElement( name="StressTest", type = StressTest.class),
		@XmlElement( name="TimeTest", type = TimeTest.class ) } )
	public Test getTest()
	{
		return test;
	}
	public void setTest(Test test)
	{
		this.test = test;
	}
	
	//---------------------------------------------------------------
	// Helper Methods
	//---------------------------------------------------------------
	
	@XmlTransient
	public int getEventsPerSec()
	{
		if( getTest() instanceof TimeTest )
		{
			return ((TimeTest)getTest()).getEventsPerSec();
		}
		return -1;
	}
	
	@XmlTransient
	public int getIterations()
	{
		switch( getTest().getType() )
		{
			case STRESS:
				return ((StressTest)getTest()).getIterations();
				
			case TIME:
			case RAMP:
			default:
				return 1;
		}
	}
	
	@XmlTransient
	public int getMinEvents()
	{
		switch( getTest().getType() )
		{
			case RAMP:
				return ((RampTest)getTest()).getMinEvents();
			case TIME:
				return ((TimeTest)getTest()).getEventsPerSec() * ((TimeTest)getTest()).getTotalTimeInSec();
			case STRESS:
				return ((StressTest)getTest()).getMinEvents();
			default:
				return 1;
		}
	}
	
	@XmlTransient
	public int getMaxEvents()
	{
		switch( getTest().getType() )
		{
			case RAMP:
				return ((RampTest)getTest()).getMaxEvents();
			case TIME:
				return ((TimeTest)getTest()).getEventsPerSec() * ((TimeTest)getTest()).getTotalTimeInSec();
			case STRESS:
				return ((StressTest)getTest()).getMinEvents();
			default:
				return 1;
		}
	}
		
	@XmlTransient
	public int getExpectedResultCount()
	{
		switch( getTest().getType() )
		{
			case RAMP:
				return ((RampTest)getTest()).getExpectedResultCountPerTest();
			case TIME:
				TimeTest timeTest = (TimeTest)getTest();
				if( timeTest.getExpectedResultCountPerSec() == -1 )
					return timeTest.getEventsPerSec() * timeTest.getTotalTimeInSec();
				else
					return timeTest.getExpectedResultCountPerSec() * timeTest.getTotalTimeInSec();
			case STRESS:
				return ((StressTest)getTest()).getExpectedResultCount();
			default:
				return 1;
		}
	}
	
	@XmlTransient
	public int getEventsToAddPerIteration()
	{
		switch( getTest().getType() )
		{
			case RAMP:
				return ((RampTest)getTest()).getEventsToAddPerTest();
			case TIME:
				return ((TimeTest)getTest()).getEventsPerSec();
			case STRESS:
				return ((StressTest)getTest()).getMinEvents();
			default:
				return 1;
		}
	}
	
	@XmlTransient
	public int getStaggeringInterval()
	{
		if( getTest() instanceof TimeTest )
		{
			return ((TimeTest)getTest()).getStaggeringInterval();
		}
		return 10;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
