package com.esri.geoevent.test.performance.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MessagesImpl implements Messages
{	
	/**
	 * The main {@link ResourceBundle}
	 */
	private final ResourceBundle bundle;
	
	/**
	 * Protected constructor
	 * 
	 * @param name of the resource bundle to load (relative to its classpath)
	 */
	protected MessagesImpl(String name)
	{
		bundle = getResourceBundle(name);
	}
	
	/**
	 * This convenience method fetches a localized string according to the parameter <code>key</code> which is passed in. The 
	 * ResourceBundle it looks for is this class's name {@link Messages.class.getName()}
	 * 
	 * @param key
	 *          The property key to look up the translated string.
	 *          
	 * @return the message
	 */
	@Override
	public String getMessage(String key)
	{
		if (bundle == null || key == null)
			return key;

		String message = null;
		try
		{
			message = bundle.getString(key);
		} 
		catch( Exception ignored )
		{
		}
		
		if (message == null)
			// if the key is not found - return the key
			return key;
		
		return message;
	}
	
	/**
	 * This convenience method fetches a localized string according to the parameter <code>key</code> which is passed in. The 
	 * ResourceBundle it looks for is this class's name {@link Messages.class.getName()}
	 * 
	 * @param key
	 *          The property key to look up the translated string.
	 * @param params
	 *          optional parameters to be used when constructing the message string reference
	 *          {@link MessageFormat#format(String, Object...)}
	 *          
	 * @return the message
	 */
	@Override
	public String getMessage(String key, Object... params)
	{
		if (bundle == null || key == null)
			return key;

		String message = bundle.getString(key);
		if (message == null)
			// if the key is not found - return the key
			return key;

		return formatMessage(message, params);
	}
	
	/**
	 * Private method get retrieve the message bundle from the class loader as a resource.
	 * @return
	 */
	private ResourceBundle getResourceBundle(String location)
	{
		ResourceBundle bundle = null;
		try
		{
			bundle = ResourceBundle.getBundle(location);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
		return bundle;
	}
	
	/**
	 * This method formats the string using the passes parameters (replaces them from the placeholders) and then runs the
	 * string message through the {@link #translate(String)} method to replace any ${param} with its translated String
	 * located in a bundle.
	 * 
	 * @param message The String to be formatted
	 * @param params of type Object[] or Object...params
	 * @return The formatted and translated message.
	 */
	private String formatMessage(String message, Object... params)
	{
		// else format with params
		String formattedString = null;
		try
		{
			// else format with params
			formattedString = MessageFormat.format(message, params);
		}
		catch (Throwable t)
		{
			// If there was a formatting error, just use the message
			formattedString = message;
		}
		return formattedString;
	}
}
