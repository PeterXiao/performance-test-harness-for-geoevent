package com.esri.ges.test.performance.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Producers")
public class Producers
{
	private final int DEFAULT_NUM_OF_CONNECTIONS = 1;
	
	private int numOfConnections = DEFAULT_NUM_OF_CONNECTIONS;
	private List<String> producers;
	
	@XmlAttribute
	public int getNumOfConnections()
	{
		return numOfConnections;
	}
	public void setNumOfConnections(int numOfConnections)
	{
		this.numOfConnections = numOfConnections;
	}
	
	@XmlElement(name = "Producer")
	public List<String> getProducers()
	{
		return producers;
	}
	public void setProducers(List<String> producers)
	{
		this.producers = producers;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
