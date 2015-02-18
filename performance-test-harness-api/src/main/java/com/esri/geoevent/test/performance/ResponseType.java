package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.StringUtils;

public enum ResponseType
{
	OK, 
	STATE_CHANGED, 
	ERROR,
	UNKNOWN;
	
	public static ResponseType fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		else if( OK.toString().equalsIgnoreCase(valueStr))
			return OK;
		else if( STATE_CHANGED.toString().equalsIgnoreCase(valueStr))
			return STATE_CHANGED;
		else if( ERROR.toString().equalsIgnoreCase(valueStr))
			return ERROR;
		else 
			return UNKNOWN;
	}
}
