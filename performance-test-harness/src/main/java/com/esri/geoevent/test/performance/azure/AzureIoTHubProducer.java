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

public class AzureIoTHubProducer extends ProducerBase
{
	private String							connectionString	= null;
	private DeviceClient				client;
	private IotHubEventCallback	callback;

	class EventCallback implements IotHubEventCallback
	{
		@Override
		public void execute(IotHubStatusCode responseStatus, Object callbackContext)
		{
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);

		connectionString = config.getPropertyValue("producerConnectionString");

		if (connectionString == null)
			throw new TestException("Azure Iot Hub event producer ERROR: 'uri' property must be specified");

		try
		{
			callback = new EventCallback();
			IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
			client = new DeviceClient("HostName=esri-iot-hub2.azure-devices.net;DeviceId=A12345;SharedAccessKey=PF6Rwt0cPpTkLY7G8JX9/Gx46DUakhsnalQP01vaZus=", protocol);
			client.open();
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
		if (client == null)
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
				byte[] bytes = msgStr.getBytes();
				Message msg = new Message(bytes);
				msg.setProperty("messageCount", Integer.toString(i));
				// System.out.println(msgStr);

				client.sendEventAsync(msg, callback, i);

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
}
