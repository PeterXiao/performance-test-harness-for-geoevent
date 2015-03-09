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
package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.StringUtils;

public enum RequestType
{
	INIT, 
	VALIDATE, 
	START, 
	STOP, 
	RESET, 
	DESTROY, 
	IS_RUNNING, 
	GET_RUNNING_STATE, 
	GET_TIMESTAMPS,
	GET_NUMBER_OF_EVENTS, 
	SET_NUMBER_OF_EVENTS, 
	SET_NUMBER_OF_EXPECTED_RESULTS, 
	GET_SUCCESSFUL_EVENTS,
	GET_SUCCESSFUL_EVENT_BYTES,
	UNKNOWN;
	
	public static RequestType fromValue(String valueStr)
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
		else if( GET_SUCCESSFUL_EVENT_BYTES.toString().equalsIgnoreCase(valueStr))
			return GET_SUCCESSFUL_EVENT_BYTES;
		else 
			return UNKNOWN;
	}
}
