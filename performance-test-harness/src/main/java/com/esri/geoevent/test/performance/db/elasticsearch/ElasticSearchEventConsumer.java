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
package com.esri.geoevent.test.performance.db.elasticsearch;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBConsumerBase;
import com.esri.geoevent.test.performance.jaxb.Config;

public class ElasticSearchEventConsumer extends DBConsumerBase
{
	// member vars
	private String	hostName;
	private String	clusterName;
	private String	indexName;
	private String	indexType;

	@Override
	public void init(Config config) throws TestException
	{
		super.init(config);

		this.hostName = config.getPropertyValue("hostName");
		this.clusterName = config.getPropertyValue("clusterName");
		this.indexName = config.getPropertyValue("indexName");
		this.indexType = config.getPropertyValue("indexType");
	}

	@Override
	public void validate() throws TestException
	{
		if (StringUtils.isEmpty(hostName))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "hostName"));
		if (StringUtils.isEmpty(clusterName))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "clusterName"));
		if (StringUtils.isEmpty(indexName))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "indexName"));
		if (StringUtils.isEmpty(indexType))
			throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "indexType"));
	
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
		return new ElasticSearchClient(hostName, clusterName, indexName, indexType);
	}
}
