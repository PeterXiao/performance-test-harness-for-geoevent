package com.esri.geoevent.test.performance.db.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBResult;

public class ElasticSearchClient implements DBClient
{
	// member vars
	private String		indexName;
	
	// setup the cluster
	private Node node;
	private Client client;
	
	public ElasticSearchClient(String clusterName, String indexName)
	{
		this.indexName = indexName;
		this.node = nodeBuilder().clusterName(clusterName).node();
		this.client = node.client();
	}
	
	@Override
	public void close() throws IOException
	{
		node.close();
	}

	@Override
	public void truncate()
	{
//		client.prepareDeleteByQuery(indexName)
//			.setQuery(QueryBuilders.matchAllQuery())
//			.setTypes(indexType)
//			.execute()
//			.actionGet();
//		
		DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
		if ( ! delete.isAcknowledged() ) {
	    System.err.println("Index wasn't deleted");
		}
	}

	@Override
	public DBResult queryForLastWriteTimes()
	{
		return null;
	}

}
