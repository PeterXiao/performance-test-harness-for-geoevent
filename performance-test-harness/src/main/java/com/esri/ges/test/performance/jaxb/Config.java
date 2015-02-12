package com.esri.ges.test.performance.jaxb;

import java.util.List;

import com.esri.ges.test.performance.Protocol;

public interface Config
{
	Protocol getProtocol();
	void setProtocol(Protocol protocol);
	
	String getHost();
	void setHost(String host);
	
	int getCommandPort();
	void setCommandPort(int commandPort);

	List<Property> getProperties();
	void setProperties(List<Property> properties);
	
	/**
	 * Fetches the property within the {@link #getProperties()} list by name.
	 * 
	 * @param name of the property to fetch
	 * @return the {@link Property} found or <code>null</code> otherwise.
	 */
	Property getPropertyByName(String name);
	
	/**
	 * Retrieves the value of the property by looking up if the property exists first and then
	 * returning its value only. If the property doesn't exists then <code>null</code> will be
	 * returned.
	 * 
	 * @param name of the property to retrieve its value
	 * @return the value of the property found or <code>null</code> if the property was not found.
	 */
	String getPropertyValue(String name);
	
	/**
	 * Retrieves the value of the property by looking up if the property exists first and then
	 * returning its value only. If the property doesn't exists then the <code>defaultValue</code> will be
	 * returned.
	 * 
	 * @param name of the property to retrieve its value
	 * @param defaultValue to return instead of null if the property was not found
	 * @return the value of the property found or <code>defaultValue</code> if the property was not found.
	 */
	String getPropertyValue(String name, String defaultValue);
	
	/**
	 * This method will create a {@link RemoteHost} from the {@link #getHost()} and {@link #getCommandPort()} attributes.
	 * @return {@link RemoteHost}
	 */
	RemoteHost getDefaultRemoteHost();
	
	/**
	 * Perform a deep copy of this object.
	 * 
	 * @return
	 */
	Config copy();
}
