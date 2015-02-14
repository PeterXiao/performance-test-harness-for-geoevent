package com.esri.geoevent.test.performance;

public class Request implements Action
{
	private ActionType type;
	private String data;
	
	public Request()
	{
	}
	
	public Request(ActionType type)
	{
		this.type = type;
	}
	
	public Request(ActionType type, String data)
	{
		this.type = type;
		this.data = data;
	}
	
	@Override
	public String getData()
	{
		return data;
	}
	
	@Override
	public void setData(String data)
	{
		this.data = data;
	}
	
	@Override
	public ActionType getType()
	{
		return type;
	}
	
	@Override
	public void setType(ActionType type)
	{
		this.type = type;
	}
}
