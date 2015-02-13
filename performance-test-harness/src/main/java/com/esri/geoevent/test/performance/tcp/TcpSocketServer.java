package com.esri.geoevent.test.performance.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.esri.geoevent.test.performance.RunnableComponent;
import com.esri.geoevent.test.performance.RunningState;
import com.esri.geoevent.test.performance.RunningStateListener;

public class TcpSocketServer implements RunnableComponent, Runnable
{
	private static final String LF = "\n";

	private Selector selector = null;
	private ServerSocketChannel socketChannel = null;
	private SocketChannel channel = null;

	private final Map<SocketChannel, ConnectionData> socketMap = new ConcurrentHashMap<SocketChannel, ConnectionData>();
	private long nextChannelId = 1;
	private Charset charset = StandardCharsets.UTF_8;

	private Thread thread;
	private int port = 5775;
	private String handshake = "";

	private RunningState runningState = RunningState.STOPPED;
	private String stateSemaphore = "stateSemaphore";
	private RunningStateListener runningStateListener = null;
	private String errorMessage;

	private String messageSeparator = LF;
	private MessageListener messageListener;

	public TcpSocketServer(MessageListener messageListener)
	{
		this.messageListener = messageListener;
	}

	public void setPort(int port)
	{
		if( this.port != port )
		{
			this.port = port;

			//reinitialize if started
			if( getRunningState() == RunningState.STARTED )
			{
				stop();
				start();
			}
		}
	}

	private void init() throws IOException
	{
		cleanup();
		selector = Selector.open();
		socketChannel = ServerSocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.socket().bind(new InetSocketAddress(port));
		socketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println( "Listening for a TCP connections on port: " + port );
	}

	private synchronized void reset()
	{
		thread = null;
	}

	protected void cleanup()
	{
		try
		{
			if (socketChannel != null && socketChannel.isOpen())
			{
				socketChannel.close();
				synchronized (socketMap)
				{
					for (SocketChannel channel : socketMap.keySet())
					{
						if (channel != null && channel.isOpen())
						{
							channel.close();
						}
					}
				}
			}
			if (channel != null && channel.isOpen())
				channel.close();
			if (selector != null)
				selector.close();
			selector = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void destroy()
	{
		stop();
		cleanup();
	}

	@Override
	public synchronized void start()
	{
		switch (getRunningState())
		{
		case STARTING:
		case STARTED:
			return;
		default:
			;
		}
		setRunningState(RunningState.STARTING);
		thread = new Thread(null, this, "Tcp Socket Server");
		thread.start();
	}

	@Override
	public synchronized void stop()
	{
		errorMessage = null;
		switch (getRunningState())
		{
		case STARTING:
		case STARTED:
			setRunningState(RunningState.STOPPING);
		default:
			;
		}
	}

	@Override
	public synchronized boolean isRunning()
	{
		return (getRunningState() == RunningState.STARTED);
	}

	@Override
	public RunningState getRunningState()
	{
		synchronized (stateSemaphore)
		{
			return runningState;
		}
	}

	protected void setRunningState(RunningState newState)
	{
		synchronized (stateSemaphore)
		{
			this.runningState = newState;
		}
		if (runningStateListener != null)
			runningStateListener.onStateChange(newState);
	}

	@Override
	public void setRunningStateListener(RunningStateListener listener)
	{
		this.runningStateListener = listener;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	@Override
	public String getStatusDetails()
	{
		return errorMessage;
	}

	public void setMessageListener(MessageListener messageListener)
	{
		this.messageListener = messageListener;
	}

	@Override
	public void run()
	{
		try
		{
			errorMessage = null;
			init();
			if (getRunningState() == RunningState.STARTING)
				setRunningState(RunningState.STARTED);
			while (isRunning())
			{
				try
				{
					readBuffer();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			cleanup();
			if (getRunningState() == RunningState.STOPPING)
				setRunningState(RunningState.STOPPED);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			setRunningState(RunningState.ERROR);
		}
		reset();
	}

	protected void readBuffer()
	{
		try
		{
			if(selector != null )
			{
				selector.select(100);
				if( selector.isOpen() )
				{
					for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext();)
					{
						SelectionKey selectionKey = iterator.next();
						iterator.remove();
						try
						{
							processServerSelectionKey(selectionKey);
						}
						catch (Exception ex)
						{
							//ex.printStackTrace();
							selectionKey.cancel();
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void processServerSelectionKey(SelectionKey key) throws IOException
	{
		if (!key.isValid())
			return;

		if (key.isAcceptable())
		{
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			SocketChannel socketChannel = serverSocketChannel.accept();
			if (socketChannel != null)
			{
				socketChannel.configureBlocking(false);
				socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

				ConnectionData conn = new ConnectionData(nextChannelId++);
				synchronized (socketMap)
				{
					socketMap.put(socketChannel, conn);
				}
			}
		}

		processReadKey(key);
		processWriteKey(key);
	}

	private void processReadKey(SelectionKey key) throws IOException
	{
		if (key.isReadable())
		{
			SocketChannel channel = (SocketChannel) key.channel();
			ConnectionData info = socketMap.get(channel);
			ByteBuffer buf = info.buf;
			try
			{
				int numberOfBytesRead = channel.read(buf);
				if (numberOfBytesRead > 0)
				{
					buf.flip();
					readBuffer(buf, String.valueOf(info.channelId));
					buf.compact();
				}
				else if (numberOfBytesRead == -1)
				{
					// The channel is closed.
					synchronized (socketMap)
					{
						socketMap.remove(channel);
					}
					channel.register(selector, 0);
					channel.close();
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				// Connection was not able to be read from, or the message type was not
				// recognized
				synchronized (socketMap)
				{
					socketMap.remove(channel);
				}
				channel.register(selector, 0);
				channel.close();
			}
		}
	}

	private void processWriteKey(SelectionKey key) throws IOException
	{
		if (key.isWritable())
		{
			SocketChannel channel = (SocketChannel) key.channel();
			int interest = key.interestOps();
			if ((interest & SelectionKey.OP_WRITE) != 0)
				interest = interest ^ SelectionKey.OP_WRITE;
			channel.register(selector, interest);

			try
			{
				ByteBuffer buffer = charset.encode(handshake);
				if (buffer.position() > 0)
					buffer.flip();
				channel.write(buffer);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				// Connection was not able to be read from, or the message type was not
				// recognized
				synchronized (socketMap)
				{
					socketMap.remove(channel);
				}
				channel.register(selector, 0);
				channel.close();
			}
		}
	}

	protected void readBuffer(ByteBuffer buffer, String channelId)
	{
		StringBuilder stringBuilder = new StringBuilder();
		try
		{
			Charset charset = StandardCharsets.UTF_8;
			CharsetDecoder decoder = charset.newDecoder();
			decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
			CharBuffer charBuffer = decoder.decode(buffer);
			String decodedBuffer = charBuffer.toString();
			stringBuilder.append(decodedBuffer);
		}
		catch (CharacterCodingException e)
		{
			e.printStackTrace();
			buffer.position(buffer.limit());
			return;
		}

		try
		{
			String rawString = getNextString(stringBuilder);
			while (rawString != null)
			{
				// hit();
				if (rawString != null && messageListener != null)
					messageListener.receive(rawString);
				rawString = getNextString(stringBuilder);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String getNextString(StringBuilder stringBuilder)
	{
		int eomIndex = stringBuilder.indexOf(messageSeparator);

		if (eomIndex == -1)
			return null;

		String substring = stringBuilder.substring(0, eomIndex);
		stringBuilder.delete(0, eomIndex + messageSeparator.length());
		return substring;
	}

	// ------------------------------------------------------
	// Private Class
	// ------------------------------------------------------

	private class ConnectionData
	{
		private final long				channelId;
		private final ByteBuffer	buf;

		public ConnectionData(long cid)
		{
			this.channelId = cid;
			buf = ByteBuffer.allocate(2048);
			buf.clear();
		}
	}
}
