/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.test.performance.ui;

import org.apache.commons.lang3.ObjectUtils;

import com.esri.geoevent.test.performance.jaxb.RemoteHost;

public class ConnectableRemoteHost extends RemoteHost
{
	private boolean connected;

	public ConnectableRemoteHost()
	{
	}
	
	public ConnectableRemoteHost(RemoteHost remoteHost)
	{
		if( remoteHost == null )
			return;
		setHost(remoteHost.getHost());
		setCommandPort(remoteHost.getCommandPort());
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
