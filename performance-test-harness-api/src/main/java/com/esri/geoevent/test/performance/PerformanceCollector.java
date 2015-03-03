package com.esri.geoevent.test.performance;

import java.util.Map;

import com.esri.geoevent.test.performance.jaxb.Config;

public interface PerformanceCollector extends RunnableComponent
{
  public void init(Config config) throws TestException;
  public void validate() throws TestException;
  public void destroy();
  public void reset();
  public int getNumberOfEvents();
  public void setNumberOfEvents(int numberOfEvents);
  public void setNumberOfExpectedResults(int numberOfExpectedResults);
  public Map<Integer, Long[]> getTimeStamps();
  public void listenOnCommandPort(int port, boolean isLocal);
  public void disconnectCommandPort();
  public long getSuccessfulEvents();
  public long getSuccessfulEventBytes();
}