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
