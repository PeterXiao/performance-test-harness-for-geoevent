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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

import com.esri.geoevent.test.performance.MessageListener;

@SuppressWarnings("serial")
public class WebsocketOutboundServlet extends HttpServlet
{
	private WebSocketFactory						wsFactory;
	private Deque<WebsocketConnection>	clients	= new ConcurrentLinkedDeque<>();
	private MessageListener listener;
	
	public WebsocketOutboundServlet()
	{
	}
	
	public WebsocketOutboundServlet(MessageListener listener)
	{
		this.listener = listener;
	}
	
	@Override
	public void init() throws ServletException
	{
		// Create and configure WS factory
		wsFactory = new WebSocketFactory(new WebSocketFactory.Acceptor()
			{
				@Override
				public boolean checkOrigin(HttpServletRequest request, String origin)
				{
					// Allow all origins
					return true;
				}

				@Override
				public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol)
				{
					System.out.println("Received a socket connection with protocol \"" + protocol + "\"");
					WebsocketConnection socket = new WebsocketConnection(listener);
					clients.push(socket);
					return socket;
				}
			});
		wsFactory.setBufferSize(4096);
		wsFactory.setMaxIdleTime(600000);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if (wsFactory.acceptWebSocket(request, response))
			return;
		PrintWriter out = response.getWriter();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("index.html");
		int c = -1;
		while ((c = in.read()) != -1)
			out.write(c);
		out.flush();
		return;
	}

	/**
	 * Send the event in a round robin fashion to distribute the load
	 * @param message to send
	 */
	public void sendEvent(String message)
	{
		WebsocketConnection socket = clients.pop();
		try
		{
			socket.send(message);
		}
		catch( Exception error )
		{
			error.printStackTrace();
		}
		clients.push(socket);
	}
}
