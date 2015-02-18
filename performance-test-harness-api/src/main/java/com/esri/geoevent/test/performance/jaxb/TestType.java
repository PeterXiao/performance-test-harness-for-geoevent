package com.esri.geoevent.test.performance.jaxb;

import javax.xml.bind.annotation.XmlEnum;

import org.apache.commons.lang3.StringUtils;

@XmlEnum
public enum TestType
{
	RAMP, STRESS, TIME, UNKNOWN;
	
	public static TestType fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		if( RAMP.toString().equalsIgnoreCase(valueStr))
			return RAMP;
		else if( STRESS.toString().equalsIgnoreCase(valueStr))
			return STRESS;
		else if( TIME.toString().equalsIgnoreCase(valueStr))
			return TIME;
		else 
			return UNKNOWN;
	}
}
