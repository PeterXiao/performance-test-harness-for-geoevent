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

package com.esri.geoevent.test.performance.db.pgsql;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBConsumerBase;
import com.esri.geoevent.test.performance.db.pgsql.PostgreSQLClient;
import com.esri.geoevent.test.performance.jaxb.Config;

import org.postgresql.*;

@SuppressWarnings("unused")
public class PostgreSQLEventConsumer extends DBConsumerBase
{
	// member vars
	private String driver;
	private String url;
	private String username;
	private String password;
	private String schemaName;
	private String tableName;
	private String timezoneOffsetInHours;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);

		this.driver = config.getPropertyValue("driver");
		this.url = config.getPropertyValue("url");
		this.username = config.getPropertyValue("username");
		this.password = config.getPropertyValue("password");
		this.schemaName = config.getPropertyValue("schemaName");
		this.tableName = config.getPropertyValue("tableName");
		this.timezoneOffsetInHours = config.getPropertyValue("timezoneOffsetInHours");
		
	}

	@Override
	public void validate() throws TestException
	{
		if (StringUtils.isEmpty(driver))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "driver"));
		if (StringUtils.isEmpty(url))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "url"));
		if (StringUtils.isEmpty(username))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "username"));
		if (StringUtils.isEmpty(password))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "password"));
		if (StringUtils.isEmpty(schemaName))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "schemaName"));
		if (StringUtils.isEmpty(tableName))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "tableName"));
		if (StringUtils.isEmpty(timezoneOffsetInHours))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "timezoneOffsetInHours"));


		// check if we can connect
		try (DBClient client = getDBClient())
		{
			;
		}
		catch (Exception error)
		{
			throw new TestException(ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName"));
		}
	}

	@Override
	public DBClient getDBClient()
	{
		return new PostgreSQLClient(driver, url, username, password, schemaName, tableName, timezoneOffsetInHours);
	}	
}
