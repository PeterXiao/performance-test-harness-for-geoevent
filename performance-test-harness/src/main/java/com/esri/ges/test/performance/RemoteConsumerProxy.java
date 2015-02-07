package com.esri.ges.test.performance;

public class RemoteConsumerProxy extends RemoteDiagnosticsCollectorBaseClass implements DiagnosticsCollector {

	public RemoteConsumerProxy(String[] hosts, int commandPort, boolean isLocal) 
	{
		super(hosts, commandPort, isLocal);
	}
}