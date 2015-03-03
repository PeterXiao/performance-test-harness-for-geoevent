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
		if( TCP_SERVER.toString().equalsIgnoreCase(valueStr) || TCP_SERVER.name().equalsIgnoreCase(valueStr) )
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