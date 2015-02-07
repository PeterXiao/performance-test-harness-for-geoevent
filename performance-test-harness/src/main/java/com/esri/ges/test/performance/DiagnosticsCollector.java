package com.esri.ges.test.performance;

import java.util.Map;
import java.util.Properties;

public interface DiagnosticsCollector extends RunnableComponent
{
  public void init(Properties props) throws TestException;
  public void validate() throws TestException;
  public void destroy();
  public void reset();
  public int getNumberOfEvents();
  public void setNumberOfEvents(int numberOfEvents);
  public void setNumberOfExpectedResults(int numberOfExpectedResults);
  public Map<Integer, Long[]> getTimeStamps();
  public void listenOnCommandPort(int port, boolean isLocal);
  public long getSuccessfulEvents();
}