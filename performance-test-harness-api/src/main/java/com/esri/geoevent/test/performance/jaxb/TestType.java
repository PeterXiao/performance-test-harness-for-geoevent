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
package com.esri.geoevent.test.performance.jaxb;

import javax.xml.bind.annotation.XmlEnum;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ApiMessages;

@XmlEnum
public enum TestType
{
	RAMP(ApiMessages.getMessage("TEST_TYPE_RAMP")), 
	STRESS(ApiMessages.getMessage("TEST_TYPE_STRESS")), 
	TIME(ApiMessages.getMessage("TEST_TYPE_TIME")), 
	UNKNOWN(ApiMessages.getMessage("TEST_TYPE_UNKNOWN"));
	
	private String label;
	
	private TestType(String label)
	{
		this.label = label;
	}
	
	private String getLabel()
	{
		return label;
	}
	
	@Override
	public String toString()
	{
		return getLabel();
	}
	
	public static TestType fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		if( RAMP.toString().equalsIgnoreCase(valueStr) || RAMP.name().equalsIgnoreCase(valueStr) )
			return RAMP;
		else if( STRESS.toString().equalsIgnoreCase(valueStr) || STRESS.name().equalsIgnoreCase(valueStr) )
			return STRESS;
		else if( TIME.toString().equalsIgnoreCase(valueStr) || TIME.name().equalsIgnoreCase(valueStr) )
			return TIME;
		else 
			return UNKNOWN;
	}
}
