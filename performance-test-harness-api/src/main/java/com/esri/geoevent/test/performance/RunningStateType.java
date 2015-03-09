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