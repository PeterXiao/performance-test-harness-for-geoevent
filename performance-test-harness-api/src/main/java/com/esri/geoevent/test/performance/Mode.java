package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.StringUtils;

public enum Mode
{
	Consumer, Producer, Orchestrator, Unknown;
	
	public static Mode fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return Unknown;
		if( Consumer.toString().equalsIgnoreCase(valueStr))
			return Consumer;
		else if( Producer.toString().equalsIgnoreCase(valueStr))
			return Producer;
		else if( Orchestrator.toString().equalsIgnoreCase(valueStr))
			return Orchestrator;
		else 
			return Unknown;
	}
	
	public static String getAllowableValues()
	{
		StringBuilder allowableValues = new StringBuilder("");
		int index = 0;
		for (Mode mode : Mode.values()) 
		{
			if( mode != Unknown )
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
