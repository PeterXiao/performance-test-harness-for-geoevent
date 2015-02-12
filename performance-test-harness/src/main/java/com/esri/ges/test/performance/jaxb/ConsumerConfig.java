package com.esri.ges.test.performance.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@XmlRootElement(name = "ConsumerConfig")
public class ConsumerConfig extends AbstractConfig
{
	protected final int DEFAULT_TIMEOUT_IN_SEC = 10;
	protected final int DEFAULT_NUM_OF_CONNECTIONS = 1;
	
	private List<RemoteHost> consumers = new ArrayList<RemoteHost>();
	private long timeoutInSec = DEFAULT_TIMEOUT_IN_SEC;
	
	@XmlElementWrapper(name = "Consumers", required=false)
	@XmlElement(name = "Consumer", required=false)
	public List<RemoteHost> getConsumers()
	{
		return consumers;
	}
	public void setConsumers(List<RemoteHost> consumers)
	{
		this.consumers = consumers;
	}

	@XmlAttribute
	public long getTimeoutInSec()
	{
		return timeoutInSec;
	}

	public void setTimeoutInSec(long timeoutInSec)
	{
		this.timeoutInSec = timeoutInSec;
	}
	
	@XmlTransient
	public int getNumOfConnections()
	{
		return (getConsumers() != null) ? getConsumers().size() : DEFAULT_NUM_OF_CONNECTIONS;
	}
	
	@Override
	public void apply(AbstractConfig config)
	{
		if( config == null)
			return;
		
		// apply ConsumerConfig related
		if( config instanceof ConsumerConfig )
		{
			ConsumerConfig consumerConfig = (ConsumerConfig) config;
			if( getTimeoutInSec() == DEFAULT_TIMEOUT_IN_SEC )
				setTimeoutInSec( consumerConfig.getTimeoutInSec() );
			
			if( consumerConfig.getConsumers() != null && ! consumerConfig.getConsumers().isEmpty() )
			{
				if( getConsumers().isEmpty() )
					getConsumers().addAll(consumerConfig.getConsumers());
				else
				{
					for(RemoteHost consumer : consumerConfig.getConsumers() )
					{
						// check if we don't have the property - if we do not then add it
						RemoteHost existingConsumer = getConsumerByName( consumer.getHost() );
						if( existingConsumer == null )
						{
							getConsumers().add( consumer );
						}
					}
				}
			}
		}
		super.apply(config);
	}
	
	/**
	 * Fetches the consumer within the {@link ConsumerConfig#getConsumers()} list by host name.
	 * 
	 * @param hostName of the consumer to fetch
	 * @return the {@link RemoteHost} found or <code>null</code> otherwise.
	 */
	public RemoteHost getConsumerByName(String hostName)
	{
		if( getConsumers() == null || StringUtils.isEmpty(hostName) )
			return null;
		
		for( RemoteHost consumer : getConsumers() )
		{
			if( hostName.equalsIgnoreCase(consumer.getHost() ) )
				return consumer;
		}
		return null;
	}
	
	@Override
	public Config copy()
	{
		ConsumerConfig copy = new ConsumerConfig();
		copy.setCommandPort(getCommandPort());
		copy.setConsumers(new ArrayList<RemoteHost>(getConsumers()));
		copy.setHost(getHost());
		copy.setProperties(new ArrayList<Property>(getProperties()));
		copy.setProtocol(getProtocol());
		copy.setTimeoutInSec(getTimeoutInSec());
		return copy;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof ConsumerConfig))
      return false;
		
		ConsumerConfig consumerConfig = (ConsumerConfig) obj;
    if (!ObjectUtils.equals(getConsumers(), consumerConfig.getConsumers()))
      return false;
    if (!ObjectUtils.equals(getTimeoutInSec(), consumerConfig.getTimeoutInSec()))
      return false;
    
    return super.equals(obj);
	}
}
