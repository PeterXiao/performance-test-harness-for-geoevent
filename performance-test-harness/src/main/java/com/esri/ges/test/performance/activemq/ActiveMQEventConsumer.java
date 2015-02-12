package com.esri.ges.test.performance.activemq;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.esri.ges.test.performance.DiagnosticsCollectorBase;
import com.esri.ges.test.performance.Mode;
import com.esri.ges.test.performance.RunningState;
import com.esri.ges.test.performance.TestException;
import com.esri.ges.test.performance.jaxb.Config;

public class ActiveMQEventConsumer extends DiagnosticsCollectorBase
{
  protected Connection      connection;
  protected Session         session;
  protected MessageConsumer consumer;

  public ActiveMQEventConsumer()
	{
  	super(Mode.CONSUMER);
	}
  
  @Override
  public void init(Config config) throws TestException
  {
    String providerUrl = config.getPropertyValue("providerUrl");
    System.out.println("---> providerUrl = "+providerUrl);
    String destinationType = config.getPropertyValue("destinationType");
    String destinationName = config.getPropertyValue("destinationName");
    if (providerUrl == null)
      throw new TestException("providerUrl property must be specified");
    if (destinationType == null)
      throw new TestException("destinationType property must be specified");
    if (!(destinationType.equals("Queue") || destinationType.equals("Topic")))
      throw new TestException(
          "destinationType property must be 'Queue' or 'Topic'");
    if (destinationName == null)
      throw new TestException("destinationName property must be specified");
    try
    {
      String userName = ActiveMQConnection.DEFAULT_USER;
      String password = ActiveMQConnection.DEFAULT_PASSWORD;
      String tuning = "?tcpNoDelay=true&socketBufferSize=65536&wireFormat.tcpNoDelayEnabled=true&wireFormat.cacheEnabled=true&wireFormat.cacheSize=1024&wireFormat.tightEncodingEnabled=true";
      ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
          userName, password, providerUrl + tuning);
      connection = factory.createConnection();
      connection.start();
      boolean transacted = false;
      session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
      Destination destination = (destinationType.equals("Queue")) ? session
          .createQueue(destinationName) : session.createTopic(destinationName);
      consumer = session.createConsumer(destination);
    }
    catch (JMSException e)
    {
      throw new TestException("Problem Setting up JMS Connection "
          + e.getMessage());
    }
  }

  @Override
  public void validate() throws TestException
  {
    if (consumer == null)
      throw new TestException("JMS Consumer could not be created.");
  }

  @Override
  public void run( AtomicBoolean running )
  {
    System.out.println("------> Running Consumer"); 
    if (numberOfEvents > 0)
    {
      if (runningStateListener != null)
        runningStateListener.onStateChange(RunningState.STARTED);
      int eventIx = 0;
      Long[] timeStamp = new Long[2];
      timeStamp[0] = System.currentTimeMillis();
      while (eventIx < numberOfEvents)
      {
        try
        {
          System.out.println("Receiving ...");
          TextMessage m = (TextMessage) consumer.receive(1000);
          if (m != null)
          {
            System.out.println("Received "+m.getText());
            successfulEvents.incrementAndGet();
          }
        }
        catch (JMSException e)
        {
          System.out.println("Exception!!!");
          e.printStackTrace();
        }
        if (!running.get())
          break;
      }
      timeStamp[1] = System.currentTimeMillis();
      timeStamps.put(timeStamps.size(), timeStamp);
      System.out.println("Adding timestamp");
      running.set(false);
      if (runningStateListener != null)
        runningStateListener.onStateChange(RunningState.STOPPED);
    }
  }
}
