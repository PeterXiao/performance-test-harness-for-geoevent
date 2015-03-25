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
package com.esri.geoevent.test.performance.streamservice;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.esri.geoevent.test.performance.FileType;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.websocket.WebsocketConnection;

public class StreamServiceEventProducer extends ProducerBase
{
	private static final String			BROADCAST	= "/broadcast";

	private WebsocketConnection[]					connections;
	private String url;
	private WebSocketClientFactory	factory;
	private WebSocketClient					client;
	private int											connectionCount;
	private StreamMetadata					metaData;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		
		try
		{
			if( factory == null )
			{
				factory = new WebSocketClientFactory();
				factory.start();
			}

			if( client == null )
			{
				client = factory.newWebSocketClient();
				client.setMaxIdleTime(30000);
				client.setProtocol("input");
			}

			connectionCount = Integer.parseInt(config.getPropertyValue("connectionCount", "1"));
			url = config.getPropertyValue("url");

			String serviceMetadataUrl = url +"?f=json";
			metaData = new StreamMetadata(serviceMetadataUrl);
			List<String> wsUrls = metaData.gerUrls();

			connections = new WebsocketConnection[connectionCount];
			for (int i = 0; i < connectionCount; i++)
			{
				String wsUrl = wsUrls.get(i % wsUrls.size()) + BROADCAST;
				URI uri = new URI(wsUrl);
				connections[i] = new WebsocketConnection();
				connections[i].setConnection(client.open(uri, connections[i], 10, TimeUnit.SECONDS));
			}
		}
		catch (Throwable error)
		{
			throw new TestException( ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error );
		}
	}

	@Override
	public void validate() throws TestException
	{
		super.validate();
		
		// validate the event file is of type feature json
		if( getEventFileType() != FileType.JSON )
			throw new TestException( ImplMessages.getMessage("INVALID_SIMULATION_FILE_TYPE", getSimulationFile(), FileType.JSON.name().toLowerCase() ) );
		
		for( WebsocketConnection connection : connections )
			connection.validate();
	}

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		int eventIndex = index;
		for (int i = 0; i < numEventsToSend; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;
			try
			{
				String message = events.get(eventIndex++);
				for( WebsocketConnection connection : connections )
					connection.send(message);
				messageSent(message);
				if (running.get() == false)
					break;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return eventIndex;
	}

	@Override
	public void destroy()
	{
		super.destroy();

		for (WebsocketConnection connection : connections)
			connection.close();

		try
		{
			factory.stop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		factory.destroy();
		factory = null;
		client = null;
		connections = null;
	}
}
