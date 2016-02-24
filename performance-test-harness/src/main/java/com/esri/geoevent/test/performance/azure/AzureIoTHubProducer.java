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
package com.esri.geoevent.test.performance.azure;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
import com.esri.geoevent.test.performance.statistics.Statistics;
import com.microsoft.azure.iot.service.exceptions.IotHubException;
import com.microsoft.azure.iot.service.sdk.Device;
import com.microsoft.azure.iot.service.sdk.RegistryManager;
import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.microsoft.azure.iothub.Message;

public class AzureIoTHubProducer extends ProducerBase
{
	private RegistryManager							registryManager;
	private List<Device>								devicesInRegistry;
	private DeviceClient[]							clients;
	private Device[]										devices;

	private String											eventHubName					= null;
	private String											deviceSharedAccessKey	= null;
	private int													deviceCount						= 4;

	private int													currentDeviceId				= 0;
	private AtomicInteger								ackCount							= new AtomicInteger(0);
	private Date												startTime;
	private Date												lastAckTime;
	private Date												firstAckTime;
	private int													threadCount						= 100;
	private boolean											sendEventStarted			= false;
	private ConcurrentLinkedQueue<Long>	latencies;
	private IotHubClientProtocol deviceProtocolToUse;
	private static String deviceConnectionStringFormat = "HostName=%s.azure-devices.net;SharedAccessKeyName=device;SharedAccessKey=%s";
	private static String deviceIdConnectionStringFormat = "HostName=%s.azure-devices.net;DeviceId=%s;SharedAccessKey=%s";
	public AtomicInteger deviceClientsOpened;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		ackCount.set(0);
		deviceClientsOpened = new AtomicInteger(0);
		sendEventStarted = false;
		if (latencies == null)
			latencies = new ConcurrentLinkedQueue<>();
		else
			latencies.clear();

		if (clients == null)
		{
			System.out.println("Iniializing Producer...");
			// add the shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));

			eventHubName = config.getPropertyValue("eventHubName");
			deviceSharedAccessKey = config.getPropertyValue("deviceSharedAccessKey");
			deviceCount = Integer.parseInt(config.getPropertyValue("deviceCount", String.valueOf(4)));
			threadCount = Integer.parseInt(config.getPropertyValue("threadCount", String.valueOf(100)));

			if (eventHubName == null)
				throw new TestException("Azure Iot Hub event producer ERROR: 'eventHubName' property must be specified");
			if (deviceSharedAccessKey == null)
				throw new TestException("Azure Iot Hub event producer ERROR: 'deviceSharedAccessKey' property must be specified");

			deviceProtocolToUse = IotHubClientProtocol.MQTT;

			try
			{
				String deviceConnectionString = String.format(deviceConnectionStringFormat, eventHubName, deviceSharedAccessKey);
				registryManager = RegistryManager.createFromConnectionString(deviceConnectionString);
				devices = new Device[deviceCount];
				 for (int i = 0; i < deviceCount; i++)
				 {
				 devices[i] = registryManager.getDevice("device-" + i);
				 }

				clients = new DeviceClient[deviceCount];
				for(int i = 0; i < devices.length; i++)
				{
					this.createAndCacheDeviceClient(devices[i], i);
				}

				Object openNotify = new Object();
				Runnable waitForOpen = () ->
				{
					while(true)
					{
						if(this.deviceClientsOpened.get() == deviceCount)
						{
							synchronized(openNotify)
							{
								openNotify.notify();
							}
							return;
						}
					}
				};
				
				new Thread(waitForOpen).start();
				synchronized(openNotify)
				{
					openNotify.wait();			
				}
				
				System.out.println("Devices and clients initialized...");
			}
			catch (Throwable error)
			{
				error.printStackTrace();
				throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
			}

			System.out.println("Producer Client opened...");
		}
	}

	private void createAndCacheDeviceClient(Device device, int index) throws IOException, URISyntaxException
	{
		String connectionString = String.format(deviceIdConnectionStringFormat, eventHubName, device.getDeviceId(), device.getPrimaryKey());
		DeviceClient toAdd = new DeviceClient(connectionString, deviceProtocolToUse);
		try
		{
			clients[index] = toAdd;
		}
		catch(Exception e)
		{
			System.out.print("index is " + index + " clients length is " + clients.length);
		}
		
		Runnable cacheAndOpen = () ->
		{			
			boolean opened = false;
			while(!opened)
			{
				try
				{
					toAdd.open();
					opened = true;
				}
				catch(Exception e)
				{
					
				}
			}
			
			int devicesOpened = this.deviceClientsOpened.incrementAndGet();
			
			System.out.println(devicesOpened + " device clients opened and cached");
		};
		
		new Thread(cacheAndOpen).start();
	}
	
	@Override
	public void validate() throws TestException
	{
		super.validate();
		for (DeviceClient client : clients)
			if (client == null)
				throw new TestException("Azure Iot Hub Event Producer does not have all clients initiated.");
		if (events.isEmpty())
			throw new TestException("Azure Iot Hub Event Producer has no events to simulate.");
	}

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		if (!sendEventStarted)
		{
			startTime = Calendar.getInstance().getTime();
			System.out.println("Capturing the time the first event is sent. " + startTime.getTime());
			sendEventStarted = true;
		}
		int eventIndex = index;
		String[] allEvents = new String[numEventsToSend];
		for (int i = 0; i < numEventsToSend; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;

			String msgStr = augmentMessage(events.get(eventIndex++));
			allEvents[i] = msgStr;
		}
		
		int lowEventPerDevice = numEventsToSend/threadCount;
		int devicesWithHigherEvents = numEventsToSend -(lowEventPerDevice * threadCount);

		int lastIndex =0;
		for(int i = 0; i < this.threadCount; i++)
		{
			int eventsToSend = 0;
			if(i<devicesWithHigherEvents)
				eventsToSend = lowEventPerDevice+1;
			else
				eventsToSend = lowEventPerDevice;
			
			if(eventsToSend == 0)
				break;
			else
			{
				String[] events = Arrays.copyOfRange(allEvents, lastIndex, (lastIndex + eventsToSend));
				//System.out.println("calling send thread with device id " + currentDeviceId + " and events from " + lastIndex + " to " + lastIndex + eventsToSend);
				AzureIoTHubProducerRunnable runner = new AzureIoTHubProducerRunnable(clients[currentDeviceId], events);
				currentDeviceId = (currentDeviceId + 1) % this.deviceCount;
				Thread toAdd = new Thread(runner);
				toAdd.start();
				lastIndex = lastIndex + eventsToSend;
				for(String msg : events)
					messageSent(msg);
			}
			if (running.get() == false)
				break;
		}
		return eventIndex;
	}

	@Override
	public void destroy()
	{
		System.out.println("The number of device ack received: " + ackCount.get());
		double ackAvgTime = (double) (lastAckTime.getTime() - startTime.getTime()) / ackCount.get();
		System.out.println("Based on ACK, latency is " + (firstAckTime.getTime() - startTime.getTime()) + ", average time per event is " + ackAvgTime + " and average events per second is " + (double) 1000 / ackAvgTime);
		//processLatencyStats();
		super.destroy();
	}

	private void processLatencyStats()
	{
		long[] latenciesArray = ArrayUtils.toPrimitive(latencies.toArray(new Long[latencies.size()])); // ArrayUtils.toPrimitive((Long[])
																																																		// latencies.toArray());
		try
		{
			Statistics latencyStats = new Statistics(latenciesArray);
			System.out.println(MessageFormat.format("Latency min: {0} max: {1} mean: {2} median: {3} stdDev: {4}.", latencyStats.getMinimum(), latencyStats.getMaximum(), latencyStats.getMean(), latencyStats.getMedian(), latencyStats.getStdDev()));
			FileWriter fw = new FileWriter("C:\\latency.log");
			for (long latency : latenciesArray)
			{
				fw.write(latency + "\n");
			}
			fw.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		catch (TestException e)
		{
			e.printStackTrace();
		}
	}

	public void shutdown()
	{
		System.out.println("Producer shutting down...");
		if (clients != null)
		{
			for (DeviceClient client : clients)
			{
				try
				{
					client.close();
					client = null;
				}
				catch (IOException e)
				{
					System.out.println("Error in closing iot hub produder client.");
					e.printStackTrace();
				}
			}
		}
	}

	private String getCurrentTime()
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// TimeZone tz = TimeZone.getTimeZone("UTC");
		// df.setTimeZone(tz);
		return df.format(new Date());
	}

	private Device getDevice(String deviceId) throws Exception
	{
		// try to find the device in the registry
		Device device = findDeviceInRegistry(deviceId);

		// if not found, cretae the device and add it to the registry
		if (device == null)
		{
			device = Device.createFromId(deviceId);
			try
			{
				device = registryManager.addDevice(device);

				System.out.println("Device created: " + device.getDeviceId());
				System.out.println("Device key: " + device.getPrimaryKey());
			}
			catch (IotHubException iote)
			{
				iote.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return device;
	}

	private Device findDeviceInRegistry(String deviceId)
	{
		for (Device device : devicesInRegistry)
		{
			if (device.getDeviceId().equalsIgnoreCase(deviceId))
				return device;
		}

		return null;
	}

	class AzureIoTHubProducerRunnable implements Runnable
	{
		public Semaphore			stopMutex;
		private DeviceClient	deviceClient;
		private String[]				messages;

		public AzureIoTHubProducerRunnable(DeviceClient deviceClient, String[] messages)
		{
			this.stopMutex = new Semaphore(1);
			this.deviceClient = deviceClient;
			this.messages = messages;
		}

		public void run()
		{
			this.stopMutex.tryAcquire();

			try
			{
				for(String msg : messages)
				{
					try
					{
//						System.out.println("Sending event " + msg);
							
						SendEventCallback callback = new SendEventCallback();
						//deviceClient.sendEventAsync(new Message(msg), callback, Calendar.getInstance().getTime().getTime());	
						deviceClient.sendEventAsync(new Message(msg), callback, 1);	
						synchronized (callback.notificationObject) 
						{
							callback.notificationObject.wait();
						}
					}
					catch(Exception e)
					{
						System.out.println("Exception: " + e);
					}		
				}
			}
			catch (Exception e)
			{
				System.out.println("Exception: " + e);
			}

			this.stopMutex.release();
		}

		class SendEventCallback implements IotHubEventCallback
		{
			public Object notificationObject;

			public SendEventCallback()
			{
				this.notificationObject = new Object();
			}

			public void execute(IotHubStatusCode status, Object callbackContext)
			{
				if (ackCount.get() == 0)
					firstAckTime = Calendar.getInstance().getTime();
				ackCount.getAndIncrement();
				lastAckTime = Calendar.getInstance().getTime();
				//latencies.add(lastAckTime.getTime() - (long) callbackContext);
				synchronized (this.notificationObject)
				{
					this.notificationObject.notify();
				}
			}
		}
	}
}
