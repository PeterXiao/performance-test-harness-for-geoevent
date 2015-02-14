package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.StringUtils;

public enum ActionType
{
	INIT, 
	VALIDATE, 
	START, 
	STOP, 
	RESET, 
	DESTROY, 
	IS_RUNNING, 
	GET_RUNNING_STATE, 
	GET_STATUS_DETAILS, 
	GET_TIMESTAMPS,
	GET_NUMBER_OF_EVENTS, 
	SET_NUMBER_OF_EVENTS, 
	SET_NUMBER_OF_EXPECTED_RESULTS, 
	GET_SUCCESSFUL_EVENTS,
	UNKNOWN;
	
	public static ActionType fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		else if( INIT.toString().equalsIgnoreCase(valueStr))
			return INIT;
		else if( VALIDATE.toString().equalsIgnoreCase(valueStr))
			return VALIDATE;
		else if( START.toString().equalsIgnoreCase(valueStr))
			return START;
		else if( STOP.toString().equalsIgnoreCase(valueStr))
			return STOP;
		else if( RESET.toString().equalsIgnoreCase(valueStr))
			return RESET;
		else if( RESET.toString().equalsIgnoreCase(valueStr))
			return RESET;
		else if( DESTROY.toString().equalsIgnoreCase(valueStr))
			return DESTROY;
		else if( IS_RUNNING.toString().equalsIgnoreCase(valueStr))
			return IS_RUNNING;
		else if( GET_RUNNING_STATE.toString().equalsIgnoreCase(valueStr))
			return GET_RUNNING_STATE;
		else if( GET_STATUS_DETAILS.toString().equalsIgnoreCase(valueStr))
			return GET_STATUS_DETAILS;
		else if( GET_TIMESTAMPS.toString().equalsIgnoreCase(valueStr))
			return GET_TIMESTAMPS;
		else if( GET_NUMBER_OF_EVENTS.toString().equalsIgnoreCase(valueStr))
			return GET_NUMBER_OF_EVENTS;
		else if( SET_NUMBER_OF_EVENTS.toString().equalsIgnoreCase(valueStr))
			return SET_NUMBER_OF_EVENTS;
		else if( SET_NUMBER_OF_EXPECTED_RESULTS.toString().equalsIgnoreCase(valueStr))
			return SET_NUMBER_OF_EXPECTED_RESULTS;
		else if( GET_SUCCESSFUL_EVENTS.toString().equalsIgnoreCase(valueStr))
			return GET_SUCCESSFUL_EVENTS;
		else 
			return UNKNOWN;
	}
}
