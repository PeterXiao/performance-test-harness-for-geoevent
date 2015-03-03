package com.esri.geoevent.test.performance.i18n;

import java.util.Hashtable;

public class MessagesFactory
{
	/**
	 * The sole instance of the <code>MessagesFactory</code>.
	 */
	private static MessagesFactory			factory;

	/**
	 * The {@link Messages} instances that have already been created, keyed by name.
	 */
	private Hashtable<String, Messages>	instances	= new Hashtable<String, Messages>();

	/**
	 * Protected constructor that is not available for public use.
	 */
	protected MessagesFactory()
	{
	}

	/**
	 * Main factory method to create or fetch an existing instance of the {@link Messages} used to retrieve localized
	 * strings.
	 * 
	 * @param clazz
	 *          to reference the Messages from.
	 * @return {@link Messages}
	 */
	public static Messages getMessages(Class<?> clazz)
	{
		return getMessages(clazz.getName());
	}

	/**
	 * Main factory method to create or fetch an existing instance of the {@link Messages} used to retrieve localized
	 * strings.
	 * 
	 * @param name
	 *          to reference the Messages from.
	 * @return {@link Messages}
	 */
	public static Messages getMessages(String name)
	{
		return getFactory().getInstance(name);
	}

	/**
	 * instance method to make sure we only create one instance of this factory class
	 * 
	 * @return {@link MessagesFactory}
	 */
	private static MessagesFactory getFactory()
	{
		if (factory == null)
		{
			factory = new MessagesFactory();
		}
		return factory;
	}

	/**
	 * Convenience method to derive {@link Messages} instance from the specified name.
	 * 
	 * @param name
	 *          to be used to retrieve the ResourceBundle used for messages.
	 * 
	 */
	private Messages getInstance(String name)
	{
		Messages instance = instances.get(name);
		if (instance == null)
		{
			instance = newInstance(name);
			instances.put(name, instance);
		}
		return instance;
	}

	/**
	 * Create and return a new {@link Messages} instance for the specified name.
	 * 
	 * @param name
	 *          used to define the messages bundle
	 */
	private Messages newInstance(String name)
	{
		return new MessagesImpl(name);
	}
}
