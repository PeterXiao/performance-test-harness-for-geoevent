package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
