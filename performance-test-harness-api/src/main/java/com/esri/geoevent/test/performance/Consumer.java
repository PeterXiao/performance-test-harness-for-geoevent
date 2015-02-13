package com.esri.geoevent.test.performance;

public interface Consumer
{
	void receive(String message);
	
	String pullMessage();
	
}
