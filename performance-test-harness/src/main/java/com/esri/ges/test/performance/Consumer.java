package com.esri.ges.test.performance;

public interface Consumer
{
	void receive(String message);
	
	String pullMessage();
	
}
