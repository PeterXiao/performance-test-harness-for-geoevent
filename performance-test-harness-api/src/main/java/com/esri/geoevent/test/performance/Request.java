package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.ObjectUtils;

public class Request
{
	private RequestType type;
	private String data;

	public Request()
	{
	}
	
	public Request(RequestType type)
	{
		this.type = type;
	}
	
	public Request(RequestType type, String data)
	{
		this.type = type;
		this.data = data;
	}
	
	public String getData()
	{
		return data;
	}
	
	public void setData(String data)
	{
		this.data = data;
	}
	
	public RequestType getType()
	{
		return type;
	}
	
	public void setType(RequestType type)
	{
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Request))
      return false;
		
		Request request = (Request) obj;
    if (!ObjectUtils.equals(getType(), request.getType()))
      return false;
    if (!ObjectUtils.equals(getData(), request.getData()))
      return false;
    
		return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( getClass().getSimpleName() );
		builder.append( "[");
		builder.append( "type=");
		builder.append( getType() );
		if( getData() != null )
		{
			builder.append( ",data=");
			builder.append( getData() );
		}
		builder.append( "]");
		return builder.toString();
	}
}
