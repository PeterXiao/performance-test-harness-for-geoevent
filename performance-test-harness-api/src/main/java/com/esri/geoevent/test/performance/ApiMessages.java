package com.esri.geoevent.test.performance;

import java.text.MessageFormat;

import com.esri.geoevent.test.performance.i18n.Messages;
import com.esri.geoevent.test.performance.i18n.MessagesFactory;

public class ApiMessages
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
			messages = MessagesFactory.getMessages(ApiMessages.class);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
		return messages;
	}
}
