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
package com.esri.geoevent.test.performance.websocket;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class WebsocketEventConsumer extends ConsumerBase {

    private static final int MAX_IDLE_TIME = 30000;

    //private WebSocket.Connection connection;
    private WebSocketClientFactory factory;
    private WebSocketClient client;
    private String url;
    private int connectionCount;
    private WebsocketConnection[] connections;

    @Override
    public void init(Config config) throws TestException {
        try {
            super.init(config);

            if (factory == null) {
                factory = new WebSocketClientFactory();
                factory.start();
            }

            if (client == null) {
                client = factory.newWebSocketClient();
                client.setMaxIdleTime(MAX_IDLE_TIME);
                client.setProtocol("output");
            }
            connectionCount = Integer.parseInt(config.getPropertyValue("connectionCount", "1"));

            url = config.getPropertyValue("url");
            URI uri = new URI(url);

            connections = new WebsocketConnection[connectionCount];
            for (int i = 0; i < connectionCount; i++) {
                connections[i] = new WebsocketConnection(message -> receive(message));
                connections[i].setConnection(client.open(uri, connections[i], 10, TimeUnit.SECONDS));
            }
        } catch (Throwable error) {
            throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
        }
    }

    @Override
    public void validate() throws TestException {
        for (WebsocketConnection connection : connections) {
            connection.validate();
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        for (WebsocketConnection connection : connections) {
            connection.close();
        }

        try {
            factory.stop();
        } catch (Exception e) {
        }

        factory.destroy();
        factory = null;
        connections = null;
        client = null;
    }
}
