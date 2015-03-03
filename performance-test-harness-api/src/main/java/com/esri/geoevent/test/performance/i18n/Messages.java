package com.esri.geoevent.test.performance.i18n;

import java.text.MessageFormat;

public interface Messages
{
	/**
	 * This convenience method fetches a localized string according to the parameter <code>key</code> which is passed in. The 
	 * ResourceBundle it looks for is this class's name {@link Messages.class.getName()}
	 * 
	 * @param key
	 *          The property key to look up the translated string.
	 *          
	 * @return the message
	 */
	String getMessage(String key);
	
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
	String getMessage(String key, Object... params);
	
}
