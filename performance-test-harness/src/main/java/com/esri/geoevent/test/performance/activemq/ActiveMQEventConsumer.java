package com.esri.geoevent.test.performance.activemq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class ActiveMQEventConsumer extends ConsumerBase
{
  protected Connection      connection;
  protected Session         session;
  protected MessageConsumer consumer;
  
  @Override
  public void init(Config config) throws TestException
  {
  	super.init(config);
  	
    String providerUrl = config.getPropertyValue("providerUrl");
    String destinationType = config.getPropertyValue("destinationType");
    String destinationName = config.getPropertyValue("destinationName");
    
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
      ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
          userName, password, providerUrl + tuning);
      connection = factory.createConnection();
      connection.start();
      boolean transacted = false;
      session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
      Destination destination = (destinationType.equals("Queue")) ? session.createQueue(destinationName) : session.createTopic(destinationName);
      consumer = session.createConsumer(destination);
    }
    catch (JMSException e)
    {
      throw new TestException("Problem Setting up JMS Connection.", e);
    }
  }

  @Override
  public void validate() throws TestException
  {
    if (consumer == null)
      throw new TestException("JMS Consumer could not be created.");
  }

  @Override
  public String pullMessage()
  {
  	String message = null;
  	try
		{
  		TextMessage textMessage = (TextMessage) consumer.receive(1000);
    	message = textMessage.getText();
		}
		catch(Exception error)
		{
		}
  	return message;
  }
}
