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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class WebsocketServerEventProducer extends ProducerBase
{
	private WebsocketOutboundServlet	webSocketServlet;
	private Server										server;
	private int 											port = 5665;
	private String 										uri = "/ws-out";
	
	private static final String 			URI_SUFFIX = "/*";
	
	public WebsocketServerEventProducer(int port)
	{
		this.port = port;
		setup(port);
	}

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		try
		{
			int port = Integer.parseInt(config.getPropertyValue("port", "5665"));
			if( this.port != port )
			{
				this.port = port;
				shutdown();
				setup(port);
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
		if (webSocketServlet == null)
			throw new TestException("Socket connection is not established. Please initialize WebsocketServerEventProducer before it starts collecting diagnostics.");
	}

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		int eventIndex = index;
		for (int i = 0; i < numEventsToSend; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;

			// send the events
			String message = augmentMessage(events.get(eventIndex++));
			webSocketServlet.sendEvent(message);
			messageSent(message);
			if (running.get() == false)
				break;
		}
		return eventIndex;
	}

	public void shutdown()
	{
		if (server != null)
		{
			try
			{
				server.stop();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			server = null;
		}
	}
	
	private void setup(int port)
	{
		if( server == null )
		{
			server = new Server(port);
			ServletHandler servletHandler = new ServletHandler();
			webSocketServlet = new WebsocketOutboundServlet();
			ServletHolder holder = new ServletHolder(webSocketServlet);
			servletHandler.addServletWithMapping(holder, uri + URI_SUFFIX);
	
			ResourceHandler resourceHandler = new ResourceHandler();
			resourceHandler.setBaseResource(Resource.newClassPathResource("com/example/docroot/"));
	
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] { servletHandler, resourceHandler, new DefaultHandler() });
			server.setHandler(handlers);
			try
			{
				server.start();
				String url = "ws://localhost:" + port + uri;
				System.out.println( ImplMessages.getMessage("WS_SERVER_START_MSG", url) );
				// add the shutdown hook
				Runtime.getRuntime().addShutdownHook(new Thread(()->shutdown()));
			}
			catch (Exception error)
			{
				System.out.println(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()));
				error.printStackTrace();
			}
		}
	}
}
