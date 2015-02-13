package com.esri.geoevent.test.performance.websocket.server;

import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket;

public class GenericWebSocket implements WebSocket.OnTextMessage
{
	Connection _connection;

	public GenericWebSocket()
	{

	}

	@Override
	public void onOpen(Connection connection )
	{
		_connection=connection;
		System.out.println("------------");
    System.out.println("Socket opened.  ");
    System.out.println("------------");
	}

	@Override
	public void onClose(int closeCode, String message)
	{
	  System.out.println("------------");
    System.out.println("Socket closed.  ");
    System.out.println("------------");
	}

	@Override
	public void onMessage(String data)
	{
	}

	public void send(String message ) throws IOException
	{
		_connection.sendMessage(message);
	}
}
