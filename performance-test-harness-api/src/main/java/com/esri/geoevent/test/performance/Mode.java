package com.esri.geoevent.test.performance;

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
	
	public static String getAllowableValues()
	{
		StringBuilder allowableValues = new StringBuilder("");
		int index = 0;
		for (Mode mode : Mode.values()) 
		{
			if( mode != UNKNOWN )
			{
				if( index != 0 )
					allowableValues.append(",");
				allowableValues.append("\"").append(mode.toString()).append("\"");
				index++;
			}
		}
		return allowableValues.toString();
	}
}
