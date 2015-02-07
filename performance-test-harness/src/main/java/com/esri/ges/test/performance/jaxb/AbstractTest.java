package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlTransient;

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
	
	@XmlEnum
	enum TestType
	{
		RAMP, STRESS, TIME, UNKNOWN;
	}
	
}
