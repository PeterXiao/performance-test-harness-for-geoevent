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

import java.text.MessageFormat;

import com.esri.geoevent.test.performance.i18n.Messages;
import com.esri.geoevent.test.performance.i18n.MessagesFactory;

public class ImplMessages
{
	// the resource bundle with messages
	private static Messages	messages;

	/**
	 * Pass through method to the {@link Messages#getMessage(String)} method.
	 * 
	 * @param key
	 *          of the message to look up.
	 * @return the message
	 */
	public static String getMessage(String key)
	{
		return getMessages().getMessage(key);
	}

	/**
	 * Pass through method to the {@link Messages#getMessage(String, Object...)} method.
	 * 
	 * @param key
	 *          of the message to look up.
	 * @param params
	 *          optional parameters to be used when constructing the message string reference
	 *          {@link MessageFormat#format(String, Object...)}
	 * @return the message
	 */
	public static String getMessage(String key, Object... params)
	{
		return getMessages().getMessage(key, params);
	}

	/**
	 * Protected helper method to get keep of the these messages in a static way.
	 * 
	 * @return {@link Messages}
	 */
	protected static Messages getMessages()
	{
		if (messages != null)
			return messages;

		try
		{
			messages = MessagesFactory.getMessages(ImplMessages.class);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
		return messages;
	}
}
