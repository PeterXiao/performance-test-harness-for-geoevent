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

import org.eclipse.jetty.websocket.WebSocket;

import com.esri.geoevent.test.performance.MessageListener;
import com.esri.geoevent.test.performance.TestException;

/**
 * Helper class used with WebSockets
 */
public class WebsocketConnection implements WebSocket.OnTextMessage
{
	// member vars
	private Connection connection;
	private MessageListener listener;
	
	public WebsocketConnection()
	{
	}
	
	public WebsocketConnection(MessageListener listener)
	{
		this.listener = listener;
	}
	
	@Override
	public void onOpen(Connection connection )
	{
		this.connection = connection;
	}
	
	@Override
	public void onClose(int closeCode, String message)
	{
	}
	
	@Override
	public void onMessage(String message)
	{
		if( listener != null )
			listener.handleMessage(message);
	}
	
	/**
	 * Sends a message to the WebSocket connection
	 * 
	 * @param message to send
	 * @throws IOException if the message cannot be sent
	 */
	public void send(String message) throws IOException
	{
		connection.sendMessage(message);
	}
	
	/**
	 * validation method for this WebSocket connection
	 * 
	 * @throws TestException
	 */
	public void validate() throws TestException
	{
		if (connection == null)
			throw new TestException("Socket connection is not established. Please initialize the event producer/consumer before it starts collecting diagnostics.");
	}
	
	/**
	 * Closes the underlying connection
	 */
	public void close()
	{
		if( connection != null )
			connection.close();
	}
	
	/**
	 * Sets the connection
	 * 
	 * @param connection of {@link Connection}
	 */
	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}
}
