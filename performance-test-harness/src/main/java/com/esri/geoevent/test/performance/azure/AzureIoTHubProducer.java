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

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.microsoft.azure.iothub.Message;

import java.io.IOException;

import com.microsoft.azure.iot.service.exceptions.IotHubException;
import com.microsoft.azure.iot.service.sdk.Device;
import com.microsoft.azure.iot.service.sdk.RegistryManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AzureIoTHubProducer extends ProducerBase
{
	private String							connectionString	= null;
	private IotHubEventCallback	callback;
	private Device[]						devices;
	private DeviceClient[]			clients;
	private int									deviceCount				= 1;

	class EventCallback implements IotHubEventCallback
	{
		@Override
		public void execute(IotHubStatusCode responseStatus, Object callbackContext)
		{
			System.out.println("A sent ack for " + callbackContext.toString() + " - " + responseStatus.toString());
		}
	}

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);

		connectionString = config.getPropertyValue("deviceConnectionString");

		if (connectionString == null)
			throw new TestException("Azure Iot Hub event producer ERROR: 'deviceConnectionString' property must be specified");

		devices = new Device[deviceCount];
		clients = new DeviceClient[deviceCount];
		callback = new EventCallback();
		IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

		try
		{
			for (int i = 0; i < deviceCount; i++)
			{
				//Device device = getDevice("device-" + i);

				String deviceConnectionString = "HostName=esri-simulator-test.azure-devices.net;DeviceId=testdevice1;SharedAccessKey=OWChjfJ9t1+XQKXZArA1wvNliENL+v5VJ4eedeWBmf4=";
				// String deviceConnectionString = "HostName=esri-simulator-test.azure-devices.net;DeviceId=" +
				// device.getDeviceId() + ";SharedAccessKey=" + device.getPrimaryKey();
				DeviceClient client = new DeviceClient(deviceConnectionString, protocol);
				client.open();

				//devices[i] = device;
				clients[i] = client;
			}
		}
		catch (Exception error)
		{
			throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
		}
	}

	@Override
	public void validate() throws TestException
	{
		super.validate();
		if (clients == null)
			throw new TestException("Azure Iot Hub Event Producer could not be created.");
		if (events.isEmpty())
			throw new TestException("Azure Iot Hub Event Producer has no events to simulate.");
	}

	@Override
	public int sendEvents(int index, int numEventsToSend)
	{
		int eventIndex = index;
		for (int i = 0; i < numEventsToSend; i++)
		{
			if (eventIndex == events.size())
				eventIndex = 0;
			String msgStr = augmentMessage(events.get(eventIndex++));
			try
			{
				msgStr = getCurrentTime() + "," + msgStr;
				byte[] bytes = msgStr.getBytes();
				Message msg = new Message(bytes);
				msg.setProperty("messageCount", Integer.toString(i));

				clients[0].sendEventAsync(msg, callback, i);
				System.out.println("S sent message - " + msgStr.trim());

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
		super.destroy();
		// TODO ...
	}

	private String getCurrentTime()
	{
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// df.setTimeZone(tz);
		return df.format(new Date());
	}

	/*
	private Device getDevice(String deviceId) throws Exception
	{
		RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

		Device device;
		try
		{
			device = registryManager.getDevice(deviceId);
		}
		catch (Exception ignored)
		{
			device = null;
		}

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
	*/

}
