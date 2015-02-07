package com.esri.ges.test.performance;

import org.apache.commons.lang3.StringUtils;

public enum Mode
{
	CONSUMER, PRODUCER, UNKNOWN;
	
	public static Mode fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		if( CONSUMER.toString().equalsIgnoreCase(valueStr))
			return CONSUMER;
		else if( PRODUCER.toString().equalsIgnoreCase(valueStr))
			return PRODUCER;
		else 
			return UNKNOWN;
	}
}
