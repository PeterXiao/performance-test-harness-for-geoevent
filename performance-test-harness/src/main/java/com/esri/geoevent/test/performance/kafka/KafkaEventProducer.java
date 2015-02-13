package com.esri.geoevent.test.performance.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class KafkaEventProducer extends ProducerBase
{
  private String brokerList;
  private String topic;
  private String acks;
  private Producer<String, String> producer;
    
  @Override
  public synchronized void init(Config config) throws TestException
  {
  	super.init(config);
    try
    {      
      brokerList = config.getPropertyValue("brokerList", "localhost:9092");
      topic = config.getPropertyValue("topic", "default-topic");
      acks = config.getPropertyValue("requiredAcks", "1");
      
      Properties kprops = new Properties();
      kprops.put("metadata.broker.list", brokerList);
      kprops.put("serializer.class", "kafka.serializer.StringEncoder");
      kprops.put("request.required.acks", acks);
      kprops.put("partitioner.class", "com.esri.ges.test.performance.kafka.SimplePartitioner");
      kprops.put("producer.type", "async");
      kprops.put("queue.buffering.max.ms", "1000");
      kprops.put("batch.num.messages", "2000");
       
      ProducerConfig producerConfig = new ProducerConfig(kprops);
      
      producer = new Producer<String, String>(producerConfig);
    }
    catch (Throwable e)
    {
      e.printStackTrace();
      throw new TestException(e.getMessage());
    }
  } 

  @Override
  public void validate() throws TestException
  {
  	super.validate();
    if (producer == null)
      throw new TestException("Kafka producer is not created.");
    if (events.isEmpty())
      throw new TestException("KafkaEventProducer is missing events to produce.");
  }

  public int sendEvents(int index, int numEventsToSend)
  {
    List<KeyedMessage<String, String>> messages = new ArrayList<KeyedMessage<String, String>>();
    int eventIndex = index;
    try
    {
      for (int i=0; i < numEventsToSend; i++)
      {
        if (eventIndex == events.size())
          eventIndex = 0;
        String thisEvent = events.get(eventIndex++);
        messages.add(new KeyedMessage<String, String>(topic, thisEvent.substring(0, thisEvent.indexOf(",")), thisEvent));
//        messages.add(new KeyedMessage<String, String>(topic, thisEvent.split(",")[0], thisEvent));
//        messages.add(new KeyedMessage(topic, events.get(eventIndex++)));
        successfulEvents.incrementAndGet();
        if (running.get() == false)
          break;
      }
      producer.send(messages);
    }
    catch(Exception error)
    {
      error.printStackTrace();
    }
    return eventIndex;
  }

  @Override
  public void destroy()
  {
    super.destroy();
    events.clear();
    try
    {
      producer.close();
    }
    catch (Exception e)
    {
      ;
    }
    producer = null;
  }
}
