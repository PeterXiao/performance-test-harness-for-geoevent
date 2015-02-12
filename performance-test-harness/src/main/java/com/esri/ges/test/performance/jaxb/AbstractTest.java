package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;

public abstract class AbstractTest implements Test
{
	private TestType type;
	
	@XmlTransient
	public TestType getType()
	{
		return type;
	}
	public void setType(TestType type)
	{
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof AbstractTest))
      return false;
		
		AbstractTest test = (AbstractTest) obj;
    if (!ObjectUtils.equals(getType(), test.getType()))
      return false;
    
    return true;
	}
	
	@XmlEnum
	enum TestType
	{
		RAMP, STRESS, TIME, UNKNOWN;
	}
	
}
