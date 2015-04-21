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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBResult;

public class ElasticSearchClient implements DBClient
{
	// member vars
	private String	indexName;
	private String	indexType;
	private String	fieldName = "_timestamp";

	// setup the cluster
	private Client	client;

	@SuppressWarnings("resource")
	public ElasticSearchClient(String hostName, String clusterName, String indexName, String indexType)
	{
		this.indexName = indexName;
		this.indexType = indexType;
		
		// TODO: Fixed the hard coding of the port number
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build();
		this.client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(hostName, 9300));
	}

	@Override
	public void close() throws IOException
	{
		client.close();
	}

	@Override
	public void createSchema()
	{
		String mappingJSON = "{\"trucks\":{\"_timestamp\":{\"enabled\":true,\"store\":true},\"properties\":{\"eventid\":{\"type\":\"string\"},\"eventtime\":{\"type\":\"date\"},\"speed\":{\"type\":\"float\"}}}}";

		CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(indexName);
		builder.addMapping(indexType, mappingJSON);
		CreateIndexResponse createResponse = builder.execute().actionGet();
		if ( ! createResponse.isAcknowledged())
		{
			System.err.println("Index was not created!");
		}
	}
	
	@Override
	public void truncate()
	{
//		DeleteMappingRequest request = new DeleteMappingRequest(indexName);
//		request.types(indexType);
//		request.indices(new String[] {indexName});
//		DeleteMappingResponse response = client.admin().indices().deleteMapping(request).actionGet();
		
		IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(indexName).execute().actionGet();
		if( indicesExistsResponse.isExists() )
		{
			DeleteIndexResponse response = client.admin().indices().prepareDelete(indexName).execute().actionGet();
			if ( ! response.isAcknowledged())
			{
				System.err.println("Index wasn't deleted");
			}
		}
	}

	@Override
	public DBResult queryForLastWriteTimes()
	{
		SearchResponse response = 
				client.prepareSearch(indexName)
					.setTypes(indexType)
					.setQuery(QueryBuilders.matchAllQuery())
					.addFields(fieldName)
					.setFrom(0)
					.setSize(Integer.MAX_VALUE)
					.execute().actionGet();
		
		List<SearchHit> hits = Arrays.asList(response.getHits().getHits());

		// sort all of the rows accordingly
		hits.sort(new RowComparator());

		// gather the information we need
		long startTime = new Long(hits.get(0).field(fieldName).getValue());
		long endTime = new Long(hits.get(hits.size() - 1).field(fieldName).getValue());
		int totalCount = hits.size();
		return new DBResult(startTime, endTime, totalCount);
	}

	/**
	 * Inner class used to sort the incoming Cassandra rows by last updated (long)
	 */
	class RowComparator implements Comparator<SearchHit>
	{
		@Override
		public int compare(SearchHit row1, SearchHit row2)
		{
			if (row1 == null || row2 == null)
				return 0;

			return Long.compare(row1.field(fieldName).getValue(), row2.field(fieldName).getValue());
		}
	}
}
