package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "GeoEventHost")
public class GeoEventHost
{
	private String hostName;
	private int consumerPort;
	private int producerPort;
	
	@XmlValue
	public String getHostName()
	{
		return hostName;
	}
	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}
	
	@XmlAttribute
	public int getConsumerPort()
	{
		return consumerPort;
	}
	public void setConsumerPort(int consumerPort)
	{
		this.consumerPort = consumerPort;
	}
	
	@XmlAttribute
	public int getProducerPort()
	{
		return producerPort;
	}
	public void setProducerPort(int producerPort)
	{
		this.producerPort = producerPort;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
