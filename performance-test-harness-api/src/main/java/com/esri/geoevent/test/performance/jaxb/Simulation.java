package com.esri.geoevent.test.performance.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Simulation")
public class Simulation implements Appliable<Simulation>
{
	private Test test;
	
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
	public void apply(Simulation simulation)
	{
		if( simulation == null )
			return;
		
		// apply the test
		if( simulation.getTest() != null )
		{
			if( getTest() != null )
			{
				getTest().apply( simulation.getTest() );
			}
		}
	}
	
	public Simulation copy()
	{
		Simulation copy = new Simulation();
		if( getTest() != null )
			copy.setTest(getTest().copy());
		return copy;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Simulation))
      return false;
		
		Simulation simulation = (Simulation) obj;
    if (!ObjectUtils.equals(getTest(), simulation.getTest()))
      return false;
    
    return true;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
