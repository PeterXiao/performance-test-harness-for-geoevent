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

@XmlRootElement(name = "RampTest")
public class RampTest extends AbstractTest
{
	protected final int DEFAULT_MIN_EVENTS = 0;
	protected final int DEFAULT_MAX_EVENTS = 0;
	protected final int DEFAULT_EVENTS_TO_ADD_PER_TEST = 0;
	protected final int DEFAULT_EXPECTED_RESULT_COUNT_PER_TEST = -1;
	
	private int minEvents = DEFAULT_MIN_EVENTS;
	private int maxEvents = DEFAULT_MAX_EVENTS;
	private int eventsToAddPerTest = DEFAULT_EVENTS_TO_ADD_PER_TEST;
	private int expectedResultCountPerTest = DEFAULT_EXPECTED_RESULT_COUNT_PER_TEST;
	
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
	public void apply(Test test)
	{
		if( test == null )
			return;
		
		if( test instanceof RampTest )
		{
			RampTest rampTest = (RampTest) test;
			if( getEventsToAddPerTest() == DEFAULT_EVENTS_TO_ADD_PER_TEST )
				setEventsToAddPerTest( rampTest.getEventsToAddPerTest() );
			if( getExpectedResultCountPerTest() == DEFAULT_EXPECTED_RESULT_COUNT_PER_TEST )
				setExpectedResultCountPerTest( rampTest.getExpectedResultCountPerTest() );
			if( getMaxEvents() == DEFAULT_MAX_EVENTS )
				setMaxEvents( rampTest.getMaxEvents() );
			if( getMinEvents() == DEFAULT_MIN_EVENTS )
				setMinEvents( rampTest.getMinEvents() );
		}
	}
	
	@Override
	public Test copy()
	{
		RampTest copy = new RampTest();
		copy.setEventsToAddPerTest(getEventsToAddPerTest());
		copy.setExpectedResultCountPerTest(getExpectedResultCountPerTest());
		copy.setMaxEvents(getMaxEvents());
		copy.setMinEvents(getMinEvents());
		copy.setType(getType());
		return copy;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof RampTest))
      return false;
		
		RampTest test = (RampTest) obj;
    if (!ObjectUtils.equals(getEventsToAddPerTest(), test.getEventsToAddPerTest()))
      return false;
    if (!ObjectUtils.equals(getExpectedResultCountPerTest(), test.getExpectedResultCountPerTest()))
      return false;
    if (!ObjectUtils.equals(getMaxEvents(), test.getMaxEvents()))
      return false;
    if (!ObjectUtils.equals(getMinEvents(), test.getMinEvents()))
      return false;
    
    return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
