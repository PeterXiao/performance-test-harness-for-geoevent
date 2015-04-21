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
import com.esri.geoevent.test.performance.db.elasticsearch.ElasticSearchClient;
import com.esri.geoevent.test.performance.jaxb.Config;

public class ElasticSearchProvisioner implements Provisioner
{
	private String							hostName;
	private String							clusterName;
	private String							indexName;
	private String							indexType;

	private static final String	NAME	= "{es}";

	@Override
	public void init(Config config) throws ProvisionException
	{
		if (config == null)
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_INIT_ERROR", getClass().getSimpleName()));

		this.hostName = config.getPropertyValue("hostName");
		this.clusterName = config.getPropertyValue("clusterName");
		this.indexName = config.getPropertyValue("indexName");
		this.indexType = config.getPropertyValue("indexType");

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
			removeAndRecreateIndex();
			System.out.println(ImplMessages.getMessage("PROVISIONER_FINISH_MSG", NAME));
		}
		catch (Exception ex)
		{
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_ERROR", NAME, ex.getMessage()));
		}
	}

	private void validate() throws ProvisionException
	{
		if (StringUtils.isEmpty(hostName))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "hostName"));
		if (StringUtils.isEmpty(indexName))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "indexName"));
		if (StringUtils.isEmpty(indexType))
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "indexType"));

		// check if we can connect
		try (DBClient client = new ElasticSearchClient(hostName, clusterName, indexName, indexType))
		{
			;
		}
		catch (Exception error)
		{
			throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName", error.getMessage()), error);
		}
	}

	private void removeAndRecreateIndex()
	{
		try (DBClient client = new ElasticSearchClient(hostName, clusterName, indexName, indexType))
		{
			client.truncate();
			client.createSchema();
		}
		catch (Exception error)
		{
			System.err.println(ImplMessages.getMessage("PROVISIONER_TRUNCATE_FAILED", indexName, indexType, error.getMessage()));
			error.printStackTrace();
		}
	}
}
