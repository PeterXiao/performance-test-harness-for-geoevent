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
package com.esri.geoevent.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "RemoteHost")
public class RemoteHost
{
	private String host;
	private int commandPort;
	
	public RemoteHost()
	{
	}
	
	public RemoteHost(String host, int commandPort)
	{
		this.host = host;
		this.commandPort = commandPort;
	}
	
	@XmlValue
	public String getHost()
	{
		return host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	
	@XmlAttribute
	public int getCommandPort()
	{
		return commandPort;
	}
	public void setCommandPort(int commandPort)
	{
		this.commandPort = commandPort;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof RemoteHost))
      return false;
		
		RemoteHost remoteHost = (RemoteHost) obj;
    if (!ObjectUtils.equals(getHost(), remoteHost.getHost()))
      return false;
    if (!ObjectUtils.equals(getCommandPort(), remoteHost.getCommandPort()))
      return false;
    
    return true;
	}
}
