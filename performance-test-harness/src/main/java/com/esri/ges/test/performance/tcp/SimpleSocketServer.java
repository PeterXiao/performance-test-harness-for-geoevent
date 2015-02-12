package com.esri.ges.test.performance.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SimpleSocketServer
{
	ServerSocket serverSocket;
	int port;
	private ConnectionListener listenerThread;
	private Deque<Socket> clients = new ConcurrentLinkedDeque<>();
	
	public SimpleSocketServer() 
	{
		port = -1;
	}
	
	public synchronized void setPort( int port )
	{
		if( this.port == port )
			return;
		
		this.port = port;
		
		if( listenerThread != null )
		{
			stop();
			start();
		}
		
	}
	
	public synchronized void start()
	{
		if( listenerThread != null )  // the server is already running.
			return;  
		
		listenerThread = new ConnectionListener();
		listenerThread.start();
	}
	
	public synchronized void stop()
	{
		if( listenerThread == null )
			return;
		
		listenerThread.interrupt();
	}
	
	private class ConnectionListener extends Thread
	{
		private volatile boolean running = true;
		
		public ConnectionListener()
		{
			setName("SimpleSocketServer Connection Listener");
		}
		
		@Override
		public void interrupt()
		{
			running = false;
			super.interrupt();
		}
		
		public void run()
		{
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e1) {
				System.err.println("Error while trying to bind to port "+port+" : "+e1.getMessage());
				return;
			}
			while(running)
			{
				try {
					Socket client = serverSocket.accept();
					System.out.println("New client connected.");
					clients.add(client);
				} catch (IOException e) 
				{
				}
			}
			try {
				serverSocket.close();
			} catch (IOException e)
			{
				System.err.println("Error while trying to close the server bound to port "+port+" : "+e.getMessage());
				return;
			}
		}
	}

	public Socket peekClient() 
	{
		return clients.peekLast();
	}

	public void clearClientList() 
	{
		clients.clear();
	}
}
