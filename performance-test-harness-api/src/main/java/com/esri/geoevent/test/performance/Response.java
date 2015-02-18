package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Response
{	
	private ResponseType type;
	private String data;
	
	public Response()
	{
	}
	
	public Response(ResponseType type)
	{
		this.type = type;
	}
	
	public Response(ResponseType type, String data)
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
	
	public ResponseType getType()
	{
		return type;
	}
	
	public void setType(ResponseType type)
	{
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Response))
      return false;
		
		Response response = (Response) obj;
    if (!ObjectUtils.equals(getType(), response.getType()))
      return false;
    if (!ObjectUtils.equals(getData(), response.getData()))
      return false;
    
		return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
