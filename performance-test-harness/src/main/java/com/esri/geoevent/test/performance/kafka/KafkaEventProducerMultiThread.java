package com.esri.geoevent.test.performance.kafka;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.esri.geoevent.test.performance.PerformanceCollectorBase;
import com.esri.geoevent.test.performance.Mode;
import com.esri.geoevent.test.performance.RunningState;
import com.esri.geoevent.test.performance.RunningStateType;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

/**
 * This class creates multiple threads to process incoming data and uses multiple Kafka producer to send message to kafka.
 * Currently the max number of threads and producers are set to 4.
 *
 */
@Deprecated
public class KafkaEventProducerMultiThread extends PerformanceCollectorBase
{
  private String brokerList;
  private String topic;
  private String acks;
//  private List<Producer<String, String>> producers;
//  private Producer<String, String> producer;
  
  private int eventsPerSec = -1;
  private int staggeringInterval = 10;
  
  private ExecutorService executor;
  private int numThreads = 4;
  private BlockingQueue<Producer<String, String>> producers;
  private List<String> sync_events = Collections.synchronizedList(events);
  
  private volatile boolean more = true;
  
  public KafkaEventProducerMultiThread()
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
      
//      producer = new Producer<String, String>(config);
      
      eventsPerSec = Integer.parseInt(config.getPropertyValue("eventsPerSec","-1"));
      staggeringInterval = Integer.parseInt(config.getPropertyValue("staggeringInterval","1"));
      
      executor = Executors.newFixedThreadPool(numThreads);
//      producers = new ArrayList<Producer<String, String>>();
      producers = new ArrayBlockingQueue<Producer<String, String>>(numThreads);
      for(int i=0; i<numThreads; i++)
      {
        producers.add(new Producer<String, String>(producerConfig) );
      }
      
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
    if (producers == null)
      throw new TestException("Kafka producers is not created.");
    if (events.isEmpty())
      throw new TestException("KafkaEventProducer is missing events to produce.");
  }

  @Override
  public void run()
  {
    if (numberOfEvents > 0)
    {
      if (runningStateListener != null)
      	runningStateListener.onStateChange(new RunningState(RunningStateType.STARTED));
      
      int eventIx = 0;
      Long[] timeStamp = new Long[2];
      timeStamp[0] = System.currentTimeMillis();
      
//      ExecutorService executor = Executors.newFixedThreadPool(5);
      
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
        
        more = true;

        // loop through all events until we are finished
//        while( successfulEvents.get() < numberOfEvents )
        while( more )
        {
          //System.out.println("successful events is " + successfulEvents.get() + ".  number of events is " + numberOfEvents);
          targetTimeStamp = targetTimeStamp + delay;
          // send the events
          //sendEvents(eventIx, eventsToSend);
          Runnable worker = new WorkerThread(eventIx, eventsToSend);
          executor.execute(worker);

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
//        sendEvents(eventIx, numberOfEvents);
        Runnable worker = new WorkerThread(eventIx, numberOfEvents);
        executor.execute(worker);
      }
      timeStamp[1] = System.currentTimeMillis();
//      executor.shutdown();
      synchronized(timeStamps)
      {
        timeStamps.put(timeStamps.size(), timeStamp);
      }
      running.set(false);
      long totalTime = (timeStamp[1] - timeStamp[0]) / 1000;
      System.out.println("Produced a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double)numberOfEvents / (double)totalTime) + " e/s).");
      if (runningStateListener != null)
      	runningStateListener.onStateChange(new RunningState(RunningStateType.STOPPED));
    }
  }
  
  @Override
  public void destroy()
  {
    super.destroy();
    events.clear();

    executor.shutdown();
    executor = null;
    
    producers.clear();
    producers = null;

  }
  
  public class WorkerThread implements Runnable {

    private int eventIndex;
    private int numEventsToSend;

    public WorkerThread(int eventIndex, int numEventsToSend){
        this.eventIndex = eventIndex;
        this.numEventsToSend = numEventsToSend;
    }

    @Override
    public void run() {
      //System.out.println("worker thread " + Thread.currentThread().getId());
        sendEvents(eventIndex, numEventsToSend);
    }

    private void sendEvents(int eventIndex, int numEventsToSend)
    {
      if(successfulEvents.get()>= numberOfEvents)
      {
        more = false;
        return;
      }
      //System.out.println("sendEvents is called.  eventIndex is " + eventIndex + "  numeventstosend is " + numEventsToSend);
      List<KeyedMessage<String, String>> messages = new ArrayList<KeyedMessage<String, String>>();
      try
      {
        for (int i=0; i < numEventsToSend; i++)
        {
          if (eventIndex == events.size())
            eventIndex = 0;
          String thisEvent = sync_events.get(eventIndex++);
          messages.add(new KeyedMessage<String, String>(topic, thisEvent.substring(0, thisEvent.indexOf(",")), thisEvent));
          successfulEvents.incrementAndGet();
          if (running.get() == false)
            break;
        }
        Producer<String, String> producer = producers.take();
        //System.out.println("producer is " + producer.toString());
        //System.out.println("Sending message.  Thread is " + Thread.currentThread().getId() + ".  Producer is " + producer.toString() + ".  Successful events is " + successfulEvents.get());
        producer.send(messages);
        producers.put(producer);
        
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    @Override
    public String toString(){
        return "";
    }
  }

}
