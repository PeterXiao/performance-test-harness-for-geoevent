package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "GeoEventConfiguration")
public class GeoEventConfiguration
{
	private String configurationFile;
	private String username;
	private String password;
	private String hostname;
	
	@XmlElement(name = "ConfigurationFile")
	public String getConfigurationFile()
	{
		return configurationFile;
	}
	public void setConfigurationFile(String configurationFile)
	{
		this.configurationFile = configurationFile;
	}
	
	@XmlElement(name = "Hostname")
	public String getHostname()
	{
		return hostname;
	}
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	@XmlElement(name = "Username")
	public String getUsername()
	{
		return username;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	@XmlElement(name = "Password")
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
