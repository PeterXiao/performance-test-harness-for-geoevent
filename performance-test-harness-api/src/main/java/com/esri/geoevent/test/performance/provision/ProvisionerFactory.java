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
