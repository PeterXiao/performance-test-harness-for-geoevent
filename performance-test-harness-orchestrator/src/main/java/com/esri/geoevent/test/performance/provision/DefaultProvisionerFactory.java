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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.jaxb.ProvisionerConfig;

public class DefaultProvisionerFactory implements ProvisionerFactory {

    @Override
    public Provisioner createProvisioner(ProvisionerConfig config) throws ProvisionException {
        if (config == null) {
            return null;
        }

        String className = config.getClassName();
        if (StringUtils.isEmpty(className)) {
            throw new ProvisionException("Failed to create a valid Provisioner. The attribute \"ClassName\" is missing in the \"ProvisionerConfig\"!");
        }

        try {
            Class<?> clazz = ClassUtils.getClass(className);
            Provisioner provisioner = (Provisioner) clazz.newInstance();
            provisioner.init(config);
            return provisioner;
        } catch (ClassNotFoundException error) {
            throw new ProvisionException("Failed to create a valid Provisioner. The className \"" + className + "\" was not found!", error);
        } catch (IllegalAccessException | InstantiationException error) {
            throw new ProvisionException("Failed to instatiate a valid Provisioner. The className \"" + className + "\" must have a parameter-less constructor!", error);
        }
    }
}
