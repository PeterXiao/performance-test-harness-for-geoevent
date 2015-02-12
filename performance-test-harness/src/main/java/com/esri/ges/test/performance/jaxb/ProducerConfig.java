package com.esri.ges.test.performance.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@XmlRootElement(name = "ProducerConfig")
public class ProducerConfig extends AbstractConfig
{
	protected final int DEFAULT_NUM_OF_CONNECTIONS = 1;	
	
	private List<RemoteHost> producers = new ArrayList<RemoteHost>();

	@XmlElementWrapper(name = "Producers", required=false)
	@XmlElement(name = "Producer", required=false)
	public List<RemoteHost> getProducers()
	{
		return producers;
	}
	public void setProducers(List<RemoteHost> producers)
	{
		this.producers = producers;
	}
	
	@XmlTransient
	public int getNumOfConnections()
	{
		return (getProducers() != null) ? getProducers().size() : DEFAULT_NUM_OF_CONNECTIONS;
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
		
		// apply ProducerConfig related
		if( config instanceof ProducerConfig )
		{
			ProducerConfig producerConfig = (ProducerConfig) config;
			if( producerConfig.getProducers() != null && ! producerConfig.getProducers().isEmpty() )
			{
				if( getProducers().isEmpty() )
					getProducers().addAll(producerConfig.getProducers());
				else
				{
					for(RemoteHost producer : producerConfig.getProducers() )
					{
						// check if we don't have the property - if we do not then add it
						RemoteHost existingProducer = getProducerByName( producer.getHost() );
						if( existingProducer == null )
						{
							getProducers().add( producer );
						}
					}
				}
			}
		}
		super.apply(config);
	}
	
	/**
	 * Fetches the producer within the {@link ProducerConfig#getProducers()} list by host name.
	 * 
	 * @param hostName of the producer to fetch
	 * @return the {@link RemoteHost} found or <code>null</code> otherwise.
	 */
	public RemoteHost getProducerByName(String hostName)
	{
		if( getProducers() == null || StringUtils.isEmpty(hostName) )
			return null;
		
		for( RemoteHost producer : getProducers() )
		{
			if( hostName.equalsIgnoreCase( producer.getHost() ) )
				return producer;
		}
		return null;
	}
	
	@Override
	public Config copy()
	{
		ProducerConfig copy = new ProducerConfig();
		copy.setCommandPort(getCommandPort());
		copy.setProducers(new ArrayList<RemoteHost>(getProducers()));
		copy.setHost(getHost());
		copy.setProperties(new ArrayList<Property>(getProperties()));
		copy.setProtocol(getProtocol());
		return copy;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof ProducerConfig))
      return false;
		
		ProducerConfig producerConfig = (ProducerConfig) obj;
    if (!ObjectUtils.equals(getProducers(), producerConfig.getProducers()))
      return false;
    
    return super.equals(obj);
	}
}
