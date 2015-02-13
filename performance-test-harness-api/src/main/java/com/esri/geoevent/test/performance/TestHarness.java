package com.esri.geoevent.test.performance;

public interface TestHarness
{
	public void init() throws TestException;
  public void runTest() throws TestException;
  public void destroy();
  public boolean isRunning();
  
}