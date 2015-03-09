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
