package com.esri.geoevent.test.performance;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.apache.commons.io.IOUtils;

public class ClockSync implements Runnable
{
	int port = 7720;
	
	public ClockSync(int clockPort)
	{
		this.port = clockPort;
	}

	public ClockSync()
	{
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
			while(true)
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
}
