package com.esri.geoevent.test.performance.provision;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.jaxb.ProvisionerConfig;

public class DefaultProvisionerFactory implements ProvisionerFactory
{
	@Override
	public Provisioner createProvisioner(ProvisionerConfig config) throws ProvisionException
	{
		if( config == null )
			return null;
		
		String className = config.getClassName();
		if( StringUtils.isEmpty(className) )
		{
			throw new ProvisionException( "Failed to create a valid Provisioner. The attribute \"ClassName\" is missing in the \"ProvisionerConfig\"!" );
		}
		
		try
		{
			Class<?> clazz = ClassUtils.getClass(className);
			Provisioner provisioner = (Provisioner) clazz.newInstance();
			provisioner.init(config);
			return provisioner;
		}
		catch (ClassNotFoundException error)
		{
			throw new ProvisionException( "Failed to create a valid Provisioner. The className \"" + className + "\" was not found!", error );
		}
		catch (IllegalAccessException | InstantiationException error)
		{
			throw new ProvisionException( "Failed to instatiate a valid Provisioner. The className \"" + className + "\" must have a parameter-less constructor!", error );
		}
	}
}
