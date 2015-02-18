package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.StringUtils;

public enum RunningStateType
{
  STARTING, STARTED, STOPPING, STOPPED, UNAVAILABLE, ERROR, UNKNOWN;
  
  public static RunningStateType fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		else if( STARTING.toString().equalsIgnoreCase(valueStr))
			return STARTING;
		else if( STARTED.toString().equalsIgnoreCase(valueStr))
			return STARTED;
		else if( STOPPING.toString().equalsIgnoreCase(valueStr))
			return STOPPING;
		else if( STOPPED.toString().equalsIgnoreCase(valueStr))
			return STOPPED;
		else if( UNAVAILABLE.toString().equalsIgnoreCase(valueStr))
			return UNAVAILABLE;
		else if( ERROR.toString().equalsIgnoreCase(valueStr))
			return ERROR;
		else 
			return UNKNOWN;
	}
}