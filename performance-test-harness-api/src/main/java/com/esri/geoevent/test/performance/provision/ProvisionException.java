package com.esri.geoevent.test.performance.provision;

public class ProvisionException extends Exception
{
	private static final long	serialVersionUID	= -976827787329437490L;

	public ProvisionException(String message)
  {
    super(message);
  }
  
  public ProvisionException(String message, Throwable error)
  {
    super(message, error);
  }
  
  public ProvisionException(Throwable error)
  {
    super(error);
  }
}
