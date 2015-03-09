package com.esri.geoevent.test.performance.ui;

import org.apache.commons.lang3.ObjectUtils;

import com.esri.geoevent.test.performance.jaxb.RemoteHost;

public class ConnectableRemoteHost extends RemoteHost
{
	private boolean connected;

	public ConnectableRemoteHost()
	{
	}
	
	public ConnectableRemoteHost(String host, int commandPort)
	{
		setHost(host);
		setCommandPort(commandPort);
	}
	
	public ConnectableRemoteHost(String host, int commandPort, boolean connected)
	{
		setHost(host);
		setCommandPort(commandPort);
		setConnected(connected);
	}
	
	public boolean isConnected()
	{
		return connected;
	}

	public void setConnected(boolean connected)
	{
		this.connected = connected;
	}
	
	// ui reasons
	public String getLabel()
	{
		return getHost() + ":" + getCommandPort();
	}
	public void setLabel()
	{
		// do nothing;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof ConnectableRemoteHost))
      return false;
		
		ConnectableRemoteHost connectableRemoteHost = (ConnectableRemoteHost) obj;
    if (!ObjectUtils.equals(getHost(), connectableRemoteHost.getHost()))
      return false;
    
		return super.equals(obj);
	}
}
