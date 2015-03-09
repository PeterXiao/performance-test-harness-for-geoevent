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
package com.esri.geoevent.test.performance.report;

import javax.xml.bind.annotation.XmlEnum;

import org.apache.commons.lang3.StringUtils;

@XmlEnum
public enum ReportType
{
	CSV, XLSX, XML, UNKNOWN;
	
	public static ReportType fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		if( CSV.toString().equalsIgnoreCase(valueStr))
			return CSV;
		else if( XLSX.toString().equalsIgnoreCase(valueStr))
			return XLSX;
		else if( XML.toString().equalsIgnoreCase(valueStr))
			return XML;
		else 
			return UNKNOWN;
	}
}
