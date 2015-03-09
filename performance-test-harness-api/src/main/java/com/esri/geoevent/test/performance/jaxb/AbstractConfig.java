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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.esri.geoevent.test.performance.Protocol;

public abstract class AbstractConfig implements Config, Applicable<AbstractConfig>
{
	protected final Protocol DEFAULT_PROTOCOL = Protocol.UNKNOWN;
	protected final String DEFAULT_HOST = "localhost";
	protected final int DEFAULT_COMMAND_PORT = -1;
	protected final int DEFAULT_NUM_OF_CONNECTIONS = 1;
	
	private Protocol protocol = DEFAULT_PROTOCOL;
	private String host = DEFAULT_HOST;
	private int commandPort = DEFAULT_COMMAND_PORT;
	private List<Property> properties = new ArrayList<Property>();
	
	@XmlAttribute
	public Protocol getProtocol()
	{
		return protocol;
	}
	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}
	
	@XmlAttribute(required=false)
	public String getHost()
	{
		return host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	
	@XmlAttribute(required=false)
	public int getCommandPort()
	{
		return commandPort;
	}
	public void setCommandPort(int commandPort)
	{
		this.commandPort = commandPort;
	}
	
	@XmlElementWrapper(name = "Properties", required=false)
	@XmlElement(name = "Property", required=false)
	public List<Property> getProperties()
	{
		return properties;
	}
	public void setProperties(List<Property> properties)
	{
		this.properties = properties;
	}
	
	/**
	 * This method applies all of the parameter's properties if and only if the
	 * existing object's properties are null or are using the default values.
	 * 
	 * @param config of type {@link AbstractConfig}
	 */
	@Override
	public void apply(AbstractConfig config)
	{
		if( config == null)
			return;
		
		if( getHost() == DEFAULT_HOST )
			setHost( config.getHost() );
		if( getProtocol() == DEFAULT_PROTOCOL )
			setProtocol( config.getProtocol() );
		
		// check the properties
		if( config.getProperties() != null && ! config.getProperties().isEmpty() )
		{
			if( getProperties().isEmpty() )
				getProperties().addAll( config.getProperties() );
			else
			{
				for(Property property : config.getProperties() )
				{
					// check if we don't have the property - if we do not then add it
					Property existingProperty = getPropertyByName( property.getName() );
					if( existingProperty == null )
					{
						getProperties().add( property );
					}
				}
			}
		}
	}
	
	/**
	 * Fetches the property within the {@link AbstractConfig#getProperties()} list by name.
	 * 
	 * @param name of the property to fetch
	 * @return the {@link Property} found or <code>null</code> otherwise.
	 */
	public Property getPropertyByName(String name)
	{
		if( getProperties() == null || StringUtils.isEmpty(name) )
			return null;
		
		for( Property property : getProperties() )
		{
			if( name.equalsIgnoreCase(property.getName() ) )
				return property;
		}
		return null;
	}
	
	/**
	 * Retrieves the value of the property by looking up if the property exists first and then
	 * returning its value only. If the property doesn't exists then <code>null</code> will be
	 * returned.
	 * 
	 * @param name of the property to retrieve its value
	 * @return the value of the property found or <code>null</code> if the property was not found.
	 */
	public String getPropertyValue(String name)
	{
		return getPropertyValue(name, null);
	}
	
	/**
	 * Retrieves the value of the property by looking up if the property exists first and then
	 * returning its value only. If the property doesn't exists then the <code>defaultValue</code> will be
	 * returned.
	 * 
	 * @param name of the property to retrieve its value
	 * @param defaultValue to return instead of null if the property was not found
	 * @return the value of the property found or <code>defaultValue</code> if the property was not found.
	 */
	public String getPropertyValue(String name, String defaultValue)
	{
		Property property = getPropertyByName(name);
		if( property == null )
			return defaultValue;
		
		return property.getValue();
	}
	
	/**
	 * This method will create a {@link RemoteHost} from the {@link #getHost()} and {@link #getCommandPort()} attributes.
	 * @return {@link RemoteHost}
	 */
	@XmlTransient
	public RemoteHost getDefaultRemoteHost()
	{
		return new RemoteHost(getHost(), getCommandPort());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof AbstractConfig))
      return false;
		
		AbstractConfig config = (AbstractConfig) obj;
    if (!ObjectUtils.equals(getCommandPort(), config.getCommandPort()))
      return false;
    if (!ObjectUtils.equals(getHost(), config.getHost()))
      return false;
    if (!ObjectUtils.equals(getProperties(), config.getProperties()))
      return false;
    if (!ObjectUtils.equals(getProtocol(), config.getProtocol()))
      return false;
    
    return true;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
