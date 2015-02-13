package com.esri.geoevent.test.performance;

public interface RunnableComponent
{
	public void start() throws RunningException;
	public void stop();
	public boolean isRunning();

	public RunningState getRunningState();
	public String getStatusDetails();
	public void setRunningStateListener(RunningStateListener listener);
}