package com.esri.geoevent.test.performance;

public interface Action
{
	ActionType getType();
	void setType(ActionType type);
	
	String getData();
	void setData(String data);
}
