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
package com.esri.geoevent.test.performance.websocket.server;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import com.esri.geoevent.test.performance.PerformanceCollectorBase;
import com.esri.geoevent.test.performance.Mode;
import com.esri.geoevent.test.performance.RunningState;
import com.esri.geoevent.test.performance.RunningStateType;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class WebsocketServerEventProducer extends PerformanceCollectorBase
{
	private static final String				STREAM_SERVICE				= "/streamservice";
	private static final String				INBOUND								= "/inbound";
	private WebsocketOutboundServlet	_webSocketServlet;

	private static final int					DEFAULT_UNSECURE_PORT	= 80;
	private static final int					DEFAULT_SECURE_PORT		= 443;

	// private MyConnection[] connections;
	private Server										server;
	private String										host;
	private int												port;

	private int												eventsPerSec					= -1;
	private int												staggeringInterval		= 10;

	// private WebSocketClientFactory factory;
	// private WebSocketClient client;
	// private int connectionCount;

	public WebsocketServerEventProducer()
	{
		super(Mode.Producer);
		server = new Server(8089);
		ServletHandler servletHandler = new ServletHandler();
		_webSocketServlet = new WebsocketOutboundServlet();
		ServletHolder holder = new ServletHolder(_webSocketServlet);
		// servletHandler.addServlet(holder);
		servletHandler.addServletWithMapping(holder, "/test/*");

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setBaseResource(Resource.newClassPathResource("com/example/docroot/"));

		DefaultHandler defaultHandler = new DefaultHandler();

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { servletHandler, resourceHandler, defaultHandler });
		server.setHandler(handlers);

		try
		{
			server.start();
			// server.join();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void init(Config config) throws TestException
	{
		try
		{
			loadEvents(new File(config.getPropertyValue("simulationFile", "")));
			port = Integer.parseInt(config.getPropertyValue("port", "5565"));
			eventsPerSec = Integer.parseInt(config.getPropertyValue("eventsPerSec", "-1"));
			staggeringInterval = Integer.parseInt(config.getPropertyValue("staggeringInterval", "1"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new TestException(e.getMessage());
		}
	}

	@Override
	public void validate() throws TestException
	{

		if (events.isEmpty())
			throw new TestException(WebsocketServerEventProducer.class.getName() + " is missing events to produce.");
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

			// send out events with delay
			if (eventsPerSec > -1)
			{
				// determine the events to send and delay
				// use a staggering approach to
				int staggeringInterval = (this.staggeringInterval > 0) ? this.staggeringInterval : 10;
				int eventsToSend = eventsPerSec / staggeringInterval;
				long delay = 1000 / staggeringInterval;
				long targetTimeStamp = timeStamp[0];
				long sleepTime = 0;

				// loop through all events until we are finished
				while (successfulEvents.get() < numberOfEvents)
				{
					targetTimeStamp = targetTimeStamp + delay;
					// send the events
					sendEvents(eventIx, eventsToSend);

					sleepTime = targetTimeStamp - System.currentTimeMillis();

					// add the delay
					if (sleepTime > 0)
					{
						try
						{
							Thread.sleep(sleepTime);
						}
						catch (InterruptedException ignored)
						{
							;
						}
					}

					// check if we need to break
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
			synchronized (timeStamps)
			{
				timeStamps.put(timeStamps.size(), timeStamp);
			}
			running.set(false);
			long totalTime = (timeStamp[1] - timeStamp[0]) / 1000;
			System.out.println("Produced a total of: " + successfulEvents.get() + " events in " + totalTime + " secs (rate=" + ((double) numberOfEvents / (double) totalTime) + " e/s).");
			if (runningStateListener != null)
				runningStateListener.onStateChange(new RunningState(RunningStateType.STOPPED));
		}
	}

	private void sendEvents(int eventIndex, int numEventsToSend)
	{
		try
		{
			for (int i = 0; i < numEventsToSend; i++)
			{
				if (eventIndex == events.size())
					eventIndex = 0;
				String thisEvent = events.get(eventIndex++);
				_webSocketServlet.publish(thisEvent);
				successfulEvents.incrementAndGet();
				successfulEventBytes.addAndGet(thisEvent.getBytes().length);
				if (running.get() == false)
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void shutdown()
	{
		if (server != null)
			try
			{
				server.stop();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		events.clear();
	}
}
