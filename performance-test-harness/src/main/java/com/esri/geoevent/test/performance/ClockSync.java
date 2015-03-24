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
package com.esri.geoevent.test.performance;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;

public class ClockSync implements Runnable
{
	int port = 7720;
	AtomicBoolean isRunning = new AtomicBoolean(false);
	
	public ClockSync(int clockPort)
	{
		this.port = clockPort;
	}

	public ClockSync()
	{
		isRunning.set(true);
	}
	
	@Override
	public void run()
	{
		DatagramSocket socket = null;
		try
		{
			byte[] incomingBuffer = new byte[1024];
			DatagramPacket packet = new DatagramPacket(incomingBuffer, incomingBuffer.length);

			ByteBuffer bb = ByteBuffer.allocate(8);  
			DatagramPacket outgoingPacket = new DatagramPacket(bb.array(), 0, 8, null, port);
			socket = new DatagramSocket( port );
			socket.setSoTimeout(50);
			while(isRunning.get())
			{
				try
				{
					socket.receive(packet);
					long now = System.currentTimeMillis();
					bb.putLong(now);  
					outgoingPacket.setAddress( packet.getAddress() );
					outgoingPacket.setPort( packet.getPort() );
					socket.send(outgoingPacket);
					bb.clear();
					//System.out.println("Sent the time " + now);
				}catch(SocketTimeoutException ex)
				{
					// Do nothing if nothing was sent.
				}
			}
		}
		catch (BindException e)
		{
			// port is in use - increment and try again
			port++;
			this.run();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(socket);
		}
	}
	
	public void stop()
	{
		isRunning.set(false);
	}
}
