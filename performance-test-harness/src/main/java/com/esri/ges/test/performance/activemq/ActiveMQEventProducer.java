package com.esri.ges.test.performance.activemq;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;

public class ActiveMQEventProducer extends DiagnosticsCollectorBase
{
  protected Connection connection;
  protected Session session;
  protected MessageProducer producer;
  
  @Override
  public void init(Properties props) throws TestException
  {
    loadEvents(new File(props.containsKey("simulationFilePath") ? props.getProperty("simulationFilePath").trim() : ""));
    String providerUrl = props.containsKey("providerUrl") ? props.getProperty("providerUrl").trim() : null;
    String destinationType = props.containsKey("destinationType") ? props.getProperty("destinationType").trim() : null;
    String destinationName = props.containsKey("destinationName") ? props.getProperty("destinationName").trim() : null;
    if (providerUrl == null)
      throw new TestException("providerUrl property must be specified");
    if (destinationType == null)
      throw new TestException("destinationType property must be specified");
    if (!(destinationType.equals("Queue") || destinationType.equals("Topic")))
      throw new TestException("destinationType property must be 'Queue' or 'Topic'");
    if (destinationName == null)
      throw new TestException("destinationName property must be specified");
    try
    {
      String userName = ActiveMQConnection.DEFAULT_USER;
      String password = ActiveMQConnection.DEFAULT_PASSWORD;
      String tuning = "?tcpNoDelay=true&socketBufferSize=65536&wireFormat.tcpNoDelayEnabled=true&wireFormat.cacheEnabled=true&wireFormat.cacheSize=1024&wireFormat.tightEncodingEnabled=true";
      ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(userName, password, providerUrl+tuning);
      connection = factory.createConnection();
      connection.start();
      boolean transacted = false;
      session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
      Destination destination = (destinationType.equals("Queue")) ? session.createQueue(destinationName) : session.createTopic(destinationName);
      producer = session.createProducer(destination);
    }
    catch (JMSException e)
    {
      throw new TestException("Problem Setting up JMS Connection " + e.getMessage());
    }
  }

  @Override
  public void validate() throws TestException
  {
    if (producer == null)
      throw new TestException("JMS Producer could not be created.");
    if (events.isEmpty())
      throw new TestException("No events to simulate.");
  }

  @Override
  public void run( AtomicBoolean running )
  {
    if (numberOfEvents > 0)
    {
      if (runningStateListener != null)
        runningStateListener.onStateChange(RunningState.STARTED);

      int eventIx = 0;
      Long[] timeStamp = new Long[2];
      timeStamp[0] = System.currentTimeMillis();
      long messageLifespan = 30 * 60 * 1000; // 30 minutes
      for (int i=0; i < numberOfEvents; i++)
      {
        if (eventIx == events.size())
          eventIx = 0;
        try
        {
          producer.send(session.createTextMessage(events.get(eventIx++)), DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, messageLifespan);
          successfulEvents.incrementAndGet();
        }
        catch (JMSException e)
        {
          e.printStackTrace();
        }
        if (running.get() == false)
          break;
      }
      System.out.println("Sent "+numberOfEvents+".  Done.");
      timeStamp[1] = System.currentTimeMillis();
      timeStamps.put(timeStamps.size(), timeStamp);
      running.set(false);
      if (runningStateListener != null)
        runningStateListener.onStateChange(RunningState.STOPPED);
    }    
  }

  @Override
  public void destroy()
  {
    super.destroy();
    events.clear();
    try { producer.close(); } catch(JMSException e) { ; } finally { producer = null; }
    try { session.close(); } catch(JMSException e) { ; } finally { session = null; }    
    try { connection.close(); } catch(JMSException e) { ; } finally { connection = null; }    
  }
}
