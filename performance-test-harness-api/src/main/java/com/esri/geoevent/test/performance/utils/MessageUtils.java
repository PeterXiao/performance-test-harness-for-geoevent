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
package com.esri.geoevent.test.performance.utils;

import org.apache.commons.lang3.StringUtils;

public class MessageUtils
{
	private static final String		NL_SEPERATOR								= ";NL;";
	private static final String		CR_SEPERATOR								= ";CR;";
	private static final String		CRNL_SEPERATOR								= ";CRNL;";
	
	private static final String		DEFAULT_NL_SEPERATOR				= "\n";
	private static final String		DEFAULT_CR_SEPERATOR				= "\r";
	private static final String		DEFAULT_CRNL_SEPERATOR			= "\r\n";
	
	public static String escapeNewLineCharacters(String data)
	{
		if( StringUtils.isEmpty(data) )
			return null;
		
		String replacedData = data.replace(DEFAULT_CRNL_SEPERATOR, CRNL_SEPERATOR);
		replacedData = replacedData.replace(DEFAULT_CR_SEPERATOR, CR_SEPERATOR);
		replacedData = replacedData.replace(DEFAULT_NL_SEPERATOR, NL_SEPERATOR);
		
		return replacedData;
	}
	
	public static String unescapeNewLineCharacters(String data)
	{
		if( StringUtils.isEmpty(data) )
			return null;
		
		String replacedData = data.replace(CRNL_SEPERATOR, DEFAULT_CRNL_SEPERATOR);
		replacedData = replacedData.replace(CR_SEPERATOR, DEFAULT_CR_SEPERATOR);
		replacedData = replacedData.replace(NL_SEPERATOR, DEFAULT_NL_SEPERATOR);
		
		return replacedData;
	}
}
