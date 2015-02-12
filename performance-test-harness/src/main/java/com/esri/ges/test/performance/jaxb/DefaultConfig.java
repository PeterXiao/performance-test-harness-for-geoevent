package com.esri.ges.test.performance.jaxb;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DefaultSharedConfig")
public class DefaultConfig extends AbstractConfig
{
	@Override
	public Config copy()
	{
		DefaultConfig copy = new DefaultConfig();
		copy.setCommandPort(getCommandPort());
		copy.setHost(getHost());
		copy.setProperties(new ArrayList<Property>(getProperties()));
		copy.setProtocol(getProtocol());
		return copy;
	}
}
