package com.esri.geoevent.test.performance;

public interface Producer
{
	int sendEvents(int eventIndex, int numEventsToSend);
}
