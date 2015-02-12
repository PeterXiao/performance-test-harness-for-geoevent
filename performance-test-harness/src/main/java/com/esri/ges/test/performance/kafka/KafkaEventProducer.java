package com.esri.ges.test.performance.kafka;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;

public class KafkaEventProducer extends DiagnosticsCollectorBase
{
  private String brokerList;
  private String topic;
  private String acks;
  private Producer<String, String> producer;
  
  private int eventsPerSec = -1;
  private int staggeringInterval = 10;
  
  public KafkaEventProducer()
	{
  	super(Mode.PRODUCER);
	}
  
  @Override
  public synchronized void init(Config config) throws TestException
  {
    try
    {
      String path = config.getPropertyValue("simulationFile", "");
      loadEvents(new File(path));
      
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
      
      eventsPerSec = Integer.parseInt(config.getPropertyValue("eventsPerSec","-1"));
      staggeringInterval = Integer.parseInt(config.getPropertyValue("staggeringInterval","1"));
    }
    catch (Throwable e)
    {
      e.printStackTrace();
      throw new TestException(e.getMessage());
    }
  }
  
  public String getBrokerList()
  {
    return brokerList;
  }
  
  public String getTopic()
  {
    return topic;
  }
  
  public String getAcks()
  {
    return acks;
  }
 

  @Override
  public void validate() throws TestException
  {
    if (producer == null)
      throw new TestException("Kafka producer is not created.");
    if (events.isEmpty())
      throw new TestException("KafkaEventProducer is missing events to produce.");
  }

  @Override
  public void run(AtomicBoolean alive)
  {
    if (numberOfEvents > 0)
    {
      if (runningStateListener != null)
        runningStateListener.onStateChange(RunningState.STARTED);
      int eventIx = 0;
      Long[] timeStamp = new Long[2];
      timeStamp[0] = System.currentTimeMillis();
      
      // send out events with delay
      if( eventsPerSec > -1 )
      {
        // determine the events to send and delay
        // use a staggering approach to
        int staggeringInterval = (this.staggeringInterval > 0) ? this.staggeringInterval : 10;
        int eventsToSend = eventsPerSec / staggeringInterval;
        long delay = 1000 / staggeringInterval;
        long targetTimeStamp = timeStamp[0];
        long sleepTime = 0;
        
        // loop through all events until we are finished
        while( successfulEvents.get() < numberOfEvents )
        {
          targetTimeStamp = targetTimeStamp + delay;
          // send the events
          sendEvents(eventIx, eventsToSend);
          
          sleepTime =  targetTimeStamp - System.currentTimeMillis();
          
          // add the delay
          if( sleepTime > 0 )
          {
            try
            {
              Thread.sleep(sleepTime);
            }
            catch( InterruptedException ignored )
            {
              ;
            }
          }
          
          //check if we need to break
          if (running.get() == false)
            break;
        }
      }
      // no delays just send out as fast as possible
      else
      {
        sendEvents(eventIx, numberOfEvents);
      }
      timeStamp[1] = System.currentTimeMillis();
      synchronized(timeStamps)
      {
        timeStamps.put(timeStamps.size(), timeStamp);
      }
      running.set(false);
      long totalTime = (timeStamp[1] - timeStamp[0]) / 1000;
      System.out.println("Produced a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double)numberOfEvents / (double)totalTime) + " e/s).");
      if (runningStateListener != null)
        runningStateListener.onStateChange(RunningState.STOPPED);
    }
  }
  
  private void sendEvents(int eventIndex, int numEventsToSend)
  {
    List<KeyedMessage<String, String>> messages = new ArrayList<KeyedMessage<String, String>>();
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
    catch(Exception e)
    {
      e.printStackTrace();
    }
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
