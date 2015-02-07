package com.esri.ges.test.performance;

import java.util.concurrent.atomic.AtomicBoolean;

public interface InterruptableRunnable 
{
	public void run( AtomicBoolean alive );
}
