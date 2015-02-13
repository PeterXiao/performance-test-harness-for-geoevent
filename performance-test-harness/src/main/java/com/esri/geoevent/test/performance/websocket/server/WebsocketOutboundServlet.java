package com.esri.ges.test.performance.websocket.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

@SuppressWarnings("serial")
public class WebsocketOutboundServlet extends HttpServlet
{
	private WebSocketFactory _wsFactory;
	private Set<GenericWebSocket> _members = new CopyOnWriteArraySet<GenericWebSocket>();

	@Override
	public void init() throws ServletException
	{
		// Create and configure WS factory
		_wsFactory=new WebSocketFactory(new WebSocketFactory.Acceptor()
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
				System.out.println("Received a socket connection with protocol \""+protocol+"\"");
				//if ("chat".equals(protocol))
				GenericWebSocket socket = new GenericWebSocket();
				_members.add(socket);
				return socket;
				//return null;
			}
		});
		_wsFactory.setBufferSize(4096);
		_wsFactory.setMaxIdleTime(600000);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if (_wsFactory.acceptWebSocket(request,response))
			return;
		PrintWriter out = response.getWriter();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("index.html");
		int c = -1;
		while( (c = in.read()) != -1 )
			out.write(c);
		out.flush();
		return;
	}

	public void publish(String message )
	{
		for( GenericWebSocket socket : _members )
		{
			try
			{
				socket.send(message);
			}catch(IOException ex)
			{
			  ex.printStackTrace();
//				_members.remove(socket);
			}
		}
	}


}

