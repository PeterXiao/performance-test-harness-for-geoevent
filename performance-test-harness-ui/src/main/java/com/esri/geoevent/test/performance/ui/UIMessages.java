package com.esri.geoevent.test.performance.ui;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class UIMessages
{	
	// the resource bundle with messages
	private static ResourceBundle bundle;
	
	/**
	 * This convenience method fetches a localized string according to the parameter <code>key</code> which is passed in. The 
	 * ResourceBundle it looks for is this class's name {@link Messages.class.getName()}
	 * 
	 * @param key
	 *          The property key to look up the translated string.
	 *          
	 * @return the message
	 */
	public static String getMessage(String key)
	{
		ResourceBundle bundle = getResourceBundle();
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
	public static String getMessage(String key, Object... params)
	{
		ResourceBundle bundle = getResourceBundle();
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
	protected static ResourceBundle getResourceBundle()
	{
		if( bundle != null )
			return bundle;
		
		try
		{
			bundle = ResourceBundle.getBundle(UIMessages.class.getName());
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
	private static String formatMessage(String message, Object... params)
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
