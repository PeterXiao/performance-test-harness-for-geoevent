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
package com.esri.geoevent.test.performance.tcp;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class TcpServerEventProducer extends ProducerBase {

    private int port = 5665;
    private ProducerTcpSocketServer socketServer;

    public TcpServerEventProducer(int port) {
        this.port = port;
        if (socketServer == null) {
            socketServer = new ProducerTcpSocketServer();
            socketServer.setPort(port);
            socketServer.start();
            // add the shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
        }
    }

    @Override
    public synchronized void init(Config config) throws TestException {
        super.init(config);
        try {
            port = Integer.parseInt(config.getPropertyValue("port", "5565"));
            if (socketServer != null) {
                socketServer.setPort(port);
            }
        } catch (Throwable error) {
            throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
        }
    }

    @Override
    public void validate() throws TestException {
        super.validate();
        if (socketServer == null) {
            throw new TestException("Socket connection is not established. Please initialize TcpServerEventProducer before it starts collecting diagnostics.");
        }
    }

    @Override
    public int sendEvents(int index, int numEventsToSend) {
        int eventIndex = index;
        for (int i = 0; i < numEventsToSend; i++) {
            if (eventIndex == events.size()) {
                eventIndex = 0;
            }

            // send the events
            String message = augmentMessage(events.get(eventIndex++));
            socketServer.sendEvent(message);
            messageSent(message);
            if (running.get() == false) {
                break;
            }
        }
        return eventIndex;
    }

    public void shutdown() {
        if (socketServer != null) {
            socketServer.destroy();
            socketServer = null;
        }
    }
}
