package com.esri.geoevent.test.performance;

import javax.xml.bind.annotation.XmlEnum;

import org.apache.commons.lang3.StringUtils;

@XmlEnum
public enum Protocol 
{
	TCP, WEBSOCKETS, ACTIVE_MQ, RABBIT_MQ, STREAM_SERVICE, KAFKA, WEBSOCKET_SERVER, UNKNOWN;
	
	public static Protocol fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		if( TCP.toString().equalsIgnoreCase(valueStr))
			return TCP;
		else if( WEBSOCKETS.toString().equalsIgnoreCase(valueStr))
			return WEBSOCKETS;
		else if( ACTIVE_MQ.toString().equalsIgnoreCase(valueStr))
			return ACTIVE_MQ;
		else if( RABBIT_MQ.toString().equalsIgnoreCase(valueStr))
			return RABBIT_MQ;
		else if( STREAM_SERVICE.toString().equalsIgnoreCase(valueStr))
			return STREAM_SERVICE;
		else if( KAFKA.toString().equalsIgnoreCase(valueStr))
		  return KAFKA;
		else if( WEBSOCKET_SERVER.toString().equalsIgnoreCase(valueStr))
		  return WEBSOCKET_SERVER;
		else 
			return UNKNOWN;
	}
}