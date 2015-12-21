/*
 Copyright 1995-2015 Esri

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
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
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class ActiveMQEventConsumer extends ConsumerBase {

    protected Connection connection;
    protected Session session;
    protected MessageConsumer consumer;

    @Override
    public void init(Config config) throws TestException {
        super.init(config);

        String providerUrl = config.getPropertyValue("providerUrl");
        String destinationType = config.getPropertyValue("destinationType");
        String destinationName = config.getPropertyValue("destinationName");

        if (providerUrl == null) {
            throw new TestException("providerUrl property must be specified");
        }
        if (destinationType == null) {
            throw new TestException("destinationType property must be specified");
        }
        if (!(destinationType.equals("Queue") || destinationType.equals("Topic"))) {
            throw new TestException("destinationType property must be 'Queue' or 'Topic'");
        }
        if (destinationName == null) {
            throw new TestException("destinationName property must be specified");
        }

        try {
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
        } catch (JMSException error) {
            throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
        }
    }

    @Override
    public void validate() throws TestException {
        if (consumer == null) {
            throw new TestException("JMS Consumer could not be created.");
        }
    }

    @Override
    public String pullMessage() {
        String message = null;
        try {
            TextMessage textMessage = (TextMessage) consumer.receive(1000);
            message = textMessage.getText();
        } catch (Exception error) {
        }
        return message;
    }
}
