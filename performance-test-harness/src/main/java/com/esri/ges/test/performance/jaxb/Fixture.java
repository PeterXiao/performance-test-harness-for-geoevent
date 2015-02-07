package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Fixture")
public class Fixture
{
	private String name;
	private String protocol;
	private int commandPort;
	private boolean isLocal;
	private GeoEventHost geoEventHost;
	private Simulation simulation;
	private Producers producers;
	private Consumers consumers;
	private KafkaHost kafkaHost;
	private RabbitMQHost rabbitMQHost;
	
	@XmlAttribute
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	@XmlAttribute
	public String getProtocol()
	{
		return protocol;
	}
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
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
	
	@XmlAttribute(name="isLocal", required=false)
	public boolean isLocal()
	{
		return isLocal;
	}
	public void setLocal(boolean isLocal)
	{
		this.isLocal = isLocal;
	}
	
	@XmlElement(name = "GeoEventHost")
	public GeoEventHost getGeoEventHost()
	{
		return geoEventHost;
	}
	public void setGeoEventHost(GeoEventHost geoEventHost)
	{
		this.geoEventHost = geoEventHost;
	}
	
	@XmlElement(name = "KafkaHost", required=false)
	public KafkaHost getKafkaHost()
	{
	  return kafkaHost;
	}
	public void setKafkaHost(KafkaHost kafkaHost)
	{
	  this.kafkaHost = kafkaHost;
	}
	
	@XmlElement(name = "RabbitMQHost", required=false)
	public RabbitMQHost getRabbitMQHost()
	{
		return rabbitMQHost;
	}
	public void setRabbitMQHost(RabbitMQHost rabbitMQHost)
	{
		this.rabbitMQHost = rabbitMQHost;
	}
	
	@XmlElement(name = "Simulation")
	public Simulation getSimulation()
	{
		return simulation;
	}
	public void setSimulation(Simulation simulation)
	{
		this.simulation = simulation;
	}
	
	@XmlElement(name = "Producers", required=false)
	public Producers getProducers()
	{
		return producers;
	}
	public void setProducers(Producers producers)
	{
		this.producers = producers;
	}
	
	@XmlElement(name = "Consumers", required=false)
	public Consumers getConsumers()
	{
		return consumers;
	}
	public void setConsumers(Consumers consumers)
	{
		this.consumers = consumers;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
