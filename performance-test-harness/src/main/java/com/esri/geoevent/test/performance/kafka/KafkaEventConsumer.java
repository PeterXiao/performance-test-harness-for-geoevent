package com.esri.geoevent.test.performance.kafka;

import java.util.UUID;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class KafkaEventConsumer extends ConsumerBase
{
	private KafkaConsumerGroup kafkaConsumerGroup;
	private String zookeeperString = "localhost:2181";
	private String consumerGroupId = UUID.randomUUID().toString();
	private String topic;
	private int numThreads;
	private boolean receiveGeoEvent;
	
	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		try
		{
			zookeeperString = config.getPropertyValue("zookeeper", String.valueOf(zookeeperString));
			consumerGroupId = config.getPropertyValue("consumergroup", String.valueOf(consumerGroupId));
			topic = config.getPropertyValue("topic");
			numThreads = Integer.parseInt(config.getPropertyValue("numthreads", String.valueOf(1)));
			receiveGeoEvent = Boolean.getBoolean(config.getPropertyValue("receiveGeoEvent", "true"));

			kafkaConsumerGroup = new KafkaConsumerGroup(zookeeperString, consumerGroupId, topic, receiveGeoEvent, message->receive(message));
			kafkaConsumerGroup.run(numThreads);
		}
		catch (Throwable error)
		{
			throw new TestException( ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error );
		}
	}

	@Override
	public void validate() throws TestException
	{
		if (kafkaConsumerGroup == null)
			throw new TestException("Kafka connection is not established. Please initialize KafkaEventConsumer before it starts collecting diagnostics.");
	}

	@Override
	public void destroy()
	{
		if( kafkaConsumerGroup != null )
		{
			kafkaConsumerGroup.shutdown();
			kafkaConsumerGroup = null;
		}
	}
}
