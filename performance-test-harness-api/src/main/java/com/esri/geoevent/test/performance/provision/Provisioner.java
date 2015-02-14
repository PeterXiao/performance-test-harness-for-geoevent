package com.esri.geoevent.test.performance.provision;

import com.esri.geoevent.test.performance.jaxb.Config;

/**
 * The Provisioner role is used to setup the fixture to be run. If anything needs to be "provision"
 * (prepared or setup) before the test fixture is run, then configuring and creating one of these
 * objects is necessary. This is a simple interface with two main methods. 
 * 
 */
public interface Provisioner
{
	/**
	 * The init method is used to configure the Provisioner. It will look for properties within 
	 * the configuration as necessary.
	 * 
	 * @param config a {@link Config} object with properties.
	 * @throws ProvisionException if the initialization has failed by either properties are missing or 
	 * property validation has failed.
	 * 
	 * @see Config
	 */
	void init(Config config) throws ProvisionException;
	
	/**
	 * The provision method is called to setup a fixture. This is where the main work is performed. 
	 * 
	 * @throws ProvisionException if the provisioning has failed.
	 */
	void provision() throws ProvisionException;
}
