package com.esri.geoevent.test.performance.provision;

import com.esri.geoevent.test.performance.jaxb.ProvisionerConfig;

/**
 * This factory interface is used to create and instantiate {@link Provisioner} objects.
 * The {@ Provisioner} objects are used to setup the Performance Test Harness.
 * 
 * @see Provisioner
 * @see ProvisionerConfig
 */
public interface ProvisionerFactory
{
	/**
	 * This method creates a new instance of a {@link Provisioner} using the {@link ProvisionerConfig#getClassName()}
	 * of the configuration object. Then it will call the {@link Provisioner#init(com.esri.geoevent.test.performance.jaxb.Config)}
	 * method to finish the creation process. 
	 * 
	 * @param config object used to pass parameters the {@link Provisioner} object will use to
	 * initialize itself.
	 * @return {@link Provisioner}
	 * @throws ProvisionException if the creation has failed. This can be caused by the {@link Provisioner#init(com.esri.geoevent.test.performance.jaxb.Config)}
	 * method.
	 * 
	 * @see Provisioner
	 * @see ProvisionerConfig
	 */
	 Provisioner createProvisioner(ProvisionerConfig config) throws ProvisionException;
}
