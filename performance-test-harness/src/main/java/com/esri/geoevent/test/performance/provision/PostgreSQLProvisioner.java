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

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.pgsql.PostgreSQLClient;
import com.esri.geoevent.test.performance.jaxb.Config;

public class PostgreSQLProvisioner implements Provisioner
{
	private String driver;
	private String url;
	private String username;
	private String password;
	private String schemaName;
	private String tableName;
	private String timezoneOffsetInHours;

	private static final String	NAME	= "PostgreSQL";

	@Override
	public void init(Config config) throws ProvisionException
	{
		if (config == null)
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_INIT_ERROR", getClass().getSimpleName()));

		this.driver = config.getPropertyValue("driver");
		this.url = config.getPropertyValue("url");
		this.username = config.getPropertyValue("username");
		this.password = config.getPropertyValue("password");
		this.schemaName = config.getPropertyValue("schemaName");
		this.tableName = config.getPropertyValue("tableName");
		this.timezoneOffsetInHours =config.getPropertyValue("timezoneOffsetInHours");

		validate();
	}

	@Override
	public void provision() throws ProvisionException
	{
		try
		{
			System.out.println("-------------------------------------------------------");
			System.out.println(ImplMessages.getMessage("PROVISIONER_START_MSG", NAME));
			System.out.println("-------------------------------------------------------");
			truncateTable();
			System.out.println(ImplMessages.getMessage("PROVISIONER_FINISH_MSG", NAME));
		}
		catch (Exception ex)
		{
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_ERROR", NAME, ex.getMessage()));
		}
	}

	private void validate() throws ProvisionException
	{
		if (StringUtils.isEmpty(driver))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "driver"));
		if (StringUtils.isEmpty(url))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "url"));
		if (StringUtils.isEmpty(username))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "username"));
		if (StringUtils.isEmpty(password))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "password"));
		if (StringUtils.isEmpty(schemaName))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "schemaName"));
		if (StringUtils.isEmpty(tableName))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "tableName"));
		if (StringUtils.isEmpty(timezoneOffsetInHours))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "timezoneOffsetInHours"));

		// check if we can connect
		try (DBClient client = new PostgreSQLClient(driver, url, username, password, schemaName, tableName, timezoneOffsetInHours))
		{
			;
		}
		catch (Exception error)
		{
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName", error.getMessage()), error);
		}
	}

	private void truncateTable()
	{
		try (DBClient client = new PostgreSQLClient(driver, url, username, password, schemaName, tableName, timezoneOffsetInHours))
		{
			client.truncate();
		}
		catch (Exception error)
		{
			System.err.println(ImplMessages.getMessage("PROVISIONER_TRUNCATE_FAILED", schemaName, tableName, error.getMessage()));
			error.printStackTrace();
		}
	}
}
