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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
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
	private IotHubEventCallback	callback;
	private RegistryManager			registryManager;
	private List<Device>				devicesInRegistry;
	private DeviceClient[]			clients;
	private Device[]						devices;

	private String							eventHubName					= null;
	private String							deviceSharedAccessKey	= null;
	private int									deviceCount						= 4;

	private int									currentDeviceId				= 0;
	private AtomicInteger				ackCount							= new AtomicInteger(0);
	private Date								startTime;
	private Date								lastAckTime;
	private Date								firstAckTime;
	private int									threadPoolSize				= 100;
	private boolean							sendEventStarted			= false;

	class EventCallback implements IotHubEventCallback
	{
		@Override
		public void execute(IotHubStatusCode responseStatus, Object callbackContext)
		{
			if (ackCount.get() == 0)
				firstAckTime = Calendar.getInstance().getTime();
			ackCount.getAndIncrement();
			lastAckTime = Calendar.getInstance().getTime();
			// System.out.println("A sent ack for " + callbackContext.toString() + " - " + responseStatus.toString());
		}
	}

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);
		ackCount.set(0);
		sendEventStarted = false;

		if (clients == null)
		{
			System.out.println("Iniializing Producer...");
			// add the shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
			
			eventHubName = config.getPropertyValue("eventHubName");
			deviceSharedAccessKey = config.getPropertyValue("deviceSharedAccessKey");
			deviceCount = Integer.parseInt(config.getPropertyValue("deviceCount", String.valueOf(4)));

			if (eventHubName == null)
				throw new TestException("Azure Iot Hub event producer ERROR: 'eventHubName' property must be specified");
			if (deviceSharedAccessKey == null)
				throw new TestException("Azure Iot Hub event producer ERROR: 'deviceSharedAccessKey' property must be specified");

			callback = new EventCallback();
			IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

			// create/get all devices and open client connections
			try
			{
				String deviceConnectionStringFormat = "HostName=%s.azure-devices.net;SharedAccessKeyName=device;SharedAccessKey=%s";
				String deviceIdConnectionStringFormat = "HostName=%s.azure-devices.net;DeviceId=%s;SharedAccessKey=%s";

				String deviceConnectionString = String.format(deviceConnectionStringFormat, eventHubName, deviceSharedAccessKey);
				registryManager = RegistryManager.createFromConnectionString(deviceConnectionString);
				// devicesInRegistry = registryManager.getDevices(1000);

				devices = new Device[deviceCount];

				for (int i = 0; i < deviceCount; i++)
				{
					devices[i] = registryManager.getDevice("device-" + i);
					// System.out.println("got " + i);
				}

				clients = new DeviceClient[deviceCount];

				for (int index = 0; index < deviceCount; index++)
				{
					Device device = devices[index];
					String deviceIdConnectionString = String.format(deviceIdConnectionStringFormat, eventHubName, device.getDeviceId(), device.getPrimaryKey());
					DeviceClient client = new DeviceClient(deviceIdConnectionString, protocol);
					clients[index] = client;

					boolean cont = true;
					int count = 0;
					while (cont && count < 3)
					{
						try
						{
							client.open();
							cont = false;
						}
						catch (Exception e)
						{
							count++;
							System.out.println("Unable to open client for " + index);
						}

					}
					System.out.println("Opened " + index);
				}
			}
			catch (Throwable error)
			{
				// TODO ...
				error.printStackTrace();
				throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
			}

			System.out.println("Producer Client opened...");
		}
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

	// @Override
	// public int sendEvents(int index, int numEventsToSend)
	// {
	// if (!sendEventStarted)
	// {
	// startTime = Calendar.getInstance().getTime();
	// System.out.println("Capturing the time the first event is sent. " + startTime.getTime());
	// sendEventStarted = true;
	// }
	// // System.out.println("Producer sending events...");
	// int eventIndex = index;
	//
	// ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
	// for (int i = 0; i < numEventsToSend; i++)
	// {
	// if (eventIndex == events.size())
	// eventIndex = 0;
	//
	// String msgStr = augmentMessage(events.get(eventIndex++));
	// SendThread sendThread = new SendThread(clients[currentDeviceId], msgStr, i);
	// // System.out.println("A new task has been added for client: " + currentDeviceId + " and event index " + i);
	// currentDeviceId = (currentDeviceId + 1) % this.deviceCount;
	// executor.execute(sendThread);
	// if (running.get() == false)
	// break;
	// }
	// executor.shutdown();
	//
	// return eventIndex;
	// }

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		if (!sendEventStarted)
		{
			startTime = Calendar.getInstance().getTime();
			System.out.println("Capturing the time the first event is sent. " + startTime.getTime());
			sendEventStarted = true;
		}
		// System.out.println("Producer sending events... Number of Events to Send is " + numEventsToSend);
		int eventIndex = index;

		for (int i = 0; i < numEventsToSend; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;

			String msgStr = augmentMessage(events.get(eventIndex++));
			try
			{
				// msgStr = getCurrentTime() + "," + msgStr;
				byte[] bytes = msgStr.getBytes();
				Message msg = new Message(bytes);
				// msg.setProperty("messageCount", Integer.toString(i));

				clients[currentDeviceId].sendEventAsync(msg, callback, i);
				currentDeviceId = (currentDeviceId + 1) % this.deviceCount;
				messageSent(msgStr);
				// System.out.println("S sent message - " + msgStr.trim());

				if (running.get() == false)
					break;
			}
			catch (Exception error)
			{
				error.printStackTrace();
			}
		}
		return eventIndex;
	}

	@Override
	public void destroy()
	{
		System.out.println("The number of device ack received: " + ackCount.get());
		double ackAvgTime = (double) (lastAckTime.getTime() - startTime.getTime()) / ackCount.get();
		System.out.println("Based on ACK, latency is " + (firstAckTime.getTime() - startTime.getTime()) + ", average time per event is " + ackAvgTime + " and average events per second is " + (double) 1000 / ackAvgTime);
		super.destroy();
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

	class SendThread implements Runnable
	{
		private DeviceClient	client;
		private String				event;
		private int						index;

		public SendThread(DeviceClient client, String event, int index)
		{
			this.client = client;
			this.event = event;
			this.index = index;
		}

		@Override
		public void run()
		{
			byte[] bytes = event.getBytes();
			Message msg = new Message(bytes);
			client.sendEventAsync(msg, callback, index);
			messageSent(event);
		}
	}

}
