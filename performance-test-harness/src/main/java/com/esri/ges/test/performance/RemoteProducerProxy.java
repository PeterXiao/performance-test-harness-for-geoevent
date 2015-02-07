package com.esri.ges.test.performance;

public class RemoteProducerProxy extends RemoteDiagnosticsCollectorBaseClass implements DiagnosticsCollector {

	public RemoteProducerProxy(String[] hosts, int commandPort, boolean isLocal)
	{
		super(hosts, commandPort, isLocal);
	}
}