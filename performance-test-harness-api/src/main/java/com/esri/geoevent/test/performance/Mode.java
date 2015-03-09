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
