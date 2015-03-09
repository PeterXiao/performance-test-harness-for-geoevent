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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Simulation")
public class Simulation implements Applicable<Simulation>
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
				return ((StressTest)getTest()).getNumOfEvents();
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
				return ((StressTest)getTest()).getNumOfEvents();
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
				return 0;
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
		return 1;
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
