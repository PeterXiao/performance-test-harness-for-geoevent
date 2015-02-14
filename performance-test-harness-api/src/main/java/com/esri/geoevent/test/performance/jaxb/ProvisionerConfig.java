package com.esri.geoevent.test.performance.jaxb;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ProvisionerConfig")
public class ProvisionerConfig extends AbstractConfig
{
	private String className;
	
	@XmlAttribute(name = "className")
	public String getClassName()
	{
		return className;
	}
	public void setClassName(String className)
	{
		this.className = className;
	}
	
	@Override
	public Config copy()
	{
		ProvisionerConfig copy = new ProvisionerConfig();
		copy.setCommandPort(getCommandPort());
		copy.setHost(getHost());
		copy.setProperties(new ArrayList<Property>(getProperties()));
		copy.setProtocol(getProtocol());
		copy.setClassName(getClassName());
		return copy;
	}

}
