package com.esri.geoevent.test.performance;

public class TestException extends Exception
{
  private static final long serialVersionUID = 2969976566823134019L;

  public TestException(String message)
  {
    super(message);
  }
  
  public TestException(String message, Throwable error)
  {
    super(message, error);
  }
  
  public TestException(Throwable error)
  {
    super(error);
  }
}