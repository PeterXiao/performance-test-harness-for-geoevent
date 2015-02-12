package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Fixture")
public class Fixture implements Appliable<Fixture>
{
	private String name;
	private DefaultConfig defaultConfig;
	private ProducerConfig producerConfig;
	private ConsumerConfig consumerConfig;
	private Simulation simulation;
	
	@XmlAttribute
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
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
	
	@XmlElement(name = "DefaultSharedConfig", required = false)
	public DefaultConfig getDefaultConfig()
	{
		return defaultConfig;
	}
	public void setDefaultConfig(DefaultConfig defaultConfig)
	{
		this.defaultConfig = defaultConfig;
	}
	
	@XmlElement(name = "ProducerConfig")
	public ProducerConfig getProducerConfig()
	{
		return producerConfig;
	}
	public void setProducerConfig(ProducerConfig producerConfig)
	{
		this.producerConfig = producerConfig;
	}
	
	@XmlElement(name = "ConsumerConfig")
	public ConsumerConfig getConsumerConfig()
	{
		return consumerConfig;
	}
	public void setConsumerConfig(ConsumerConfig consumerConfig)
	{
		this.consumerConfig = consumerConfig;
	}
	
	@Override
	public void apply(Fixture fixture)
	{
		if( fixture == null )
			return;
		
		if( StringUtils.isEmpty( getName() ) )
			setName( fixture.getName() );
		if( fixture.getConsumerConfig() != null )
		{
			if( getConsumerConfig() == null )
				setConsumerConfig((ConsumerConfig)fixture.getConsumerConfig().copy());
			else
				getConsumerConfig().apply(fixture.getConsumerConfig());
		}
		if( fixture.getDefaultConfig() != null )
		{
			if( getDefaultConfig() == null )
				setDefaultConfig((DefaultConfig)fixture.getDefaultConfig().copy());
			else
				getDefaultConfig().apply(fixture.getDefaultConfig());
		}
		if( fixture.getProducerConfig() != null )
		{
			if( getProducerConfig() == null )
				setProducerConfig((ProducerConfig)fixture.getProducerConfig().copy());
			else
				getProducerConfig().apply(fixture.getProducerConfig());
		}
		if( fixture.getSimulation() != null )
		{
			if( getSimulation() == null )
				setSimulation(fixture.getSimulation().copy());
			else
				getSimulation().apply(fixture.getSimulation());
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Fixture))
      return false;
		
		Fixture fixture = (Fixture) obj;
    if (!ObjectUtils.equals(getConsumerConfig(), fixture.getConsumerConfig()))
      return false;
    if (!ObjectUtils.equals(getDefaultConfig(), fixture.getDefaultConfig()))
      return false;
    if (!ObjectUtils.equals(getName(), fixture.getName()))
      return false;
    if (!ObjectUtils.equals(getProducerConfig(), fixture.getProducerConfig()))
      return false;
    if (!ObjectUtils.equals(getSimulation(), fixture.getSimulation()))
      return false;
    
    return true;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
