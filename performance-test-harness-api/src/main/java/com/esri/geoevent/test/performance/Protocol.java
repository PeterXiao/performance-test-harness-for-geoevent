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

import javax.xml.bind.annotation.XmlEnum;

import org.apache.commons.lang3.StringUtils;

@XmlEnum
public enum Protocol 
{
	TCP(ApiMessages.getMessage("PROTOCOL_TCP")), 
	TCP_SERVER(ApiMessages.getMessage("PROTOCOL_TCP_SERVER")), 
	WEBSOCKETS(ApiMessages.getMessage("PROTOCOL_WEBSOCKETS")), 
	ACTIVE_MQ (ApiMessages.getMessage("PROTOCOL_ACTIVE_MQ")), 
	RABBIT_MQ(ApiMessages.getMessage("PROTOCOL_RABBIT_MQ")), 
	STREAM_SERVICE(ApiMessages.getMessage("PROTOCOL_STREAM_SERVICE")), 
	KAFKA(ApiMessages.getMessage("PROTOCOL_KAFKA")), 
	WEBSOCKET_SERVER(ApiMessages.getMessage("PROTOCOL_WEBSOCKET_SERVER")), 
	UNKNOWN(ApiMessages.getMessage("PROTOCOL_UNKNOWN"));

	private String label;
	
	private Protocol(String label)
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
	
	public static Protocol fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		if( TCP.toString().equalsIgnoreCase(valueStr) || TCP.name().equalsIgnoreCase(valueStr) )
			return TCP;
		else if( TCP_SERVER.toString().equalsIgnoreCase(valueStr) || TCP_SERVER.name().equalsIgnoreCase(valueStr) )
			return TCP_SERVER;
		else if( WEBSOCKETS.toString().equalsIgnoreCase(valueStr) || WEBSOCKETS.name().equalsIgnoreCase(valueStr))
			return WEBSOCKETS;
		else if( ACTIVE_MQ.toString().equalsIgnoreCase(valueStr) || ACTIVE_MQ.name().equalsIgnoreCase(valueStr))
			return ACTIVE_MQ;
		else if( RABBIT_MQ.toString().equalsIgnoreCase(valueStr) || RABBIT_MQ.name().equalsIgnoreCase(valueStr))
			return RABBIT_MQ;
		else if( STREAM_SERVICE.toString().equalsIgnoreCase(valueStr) || STREAM_SERVICE.name().equalsIgnoreCase(valueStr))
			return STREAM_SERVICE;
		else if( KAFKA.toString().equalsIgnoreCase(valueStr) || KAFKA.name().equalsIgnoreCase(valueStr))
		  return KAFKA;
		else if( WEBSOCKET_SERVER.toString().equalsIgnoreCase(valueStr) || WEBSOCKET_SERVER.name().equalsIgnoreCase(valueStr))
		  return WEBSOCKET_SERVER;
		else 
			return UNKNOWN;
	}
	
	public static String getAllowableValues()
	{
		StringBuilder allowableValues = new StringBuilder("");
		int index = 0;
		for (Protocol protocol : Protocol.values()) 
		{
			if( protocol != UNKNOWN )
			{
				if( index != 0 )
					allowableValues.append(",");
				allowableValues.append("\"").append(protocol.toString()).append("\"");
				index++;
			}
		}
		return allowableValues.toString();
	}
}