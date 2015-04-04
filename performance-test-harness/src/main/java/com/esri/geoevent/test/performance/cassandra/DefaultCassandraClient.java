package com.esri.geoevent.test.performance.cassandra;

import java.io.IOException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;

public class DefaultCassandraClient implements CassandraClient
{
	//setup the cluster
	private Cluster cluster;
	private Metadata metadata;
 
	public DefaultCassandraClient(String nodeName)
	{
		this.cluster = Cluster.builder().addContactPoint(nodeName).build();
		this.metadata = cluster.getMetadata();
	}
	
	@Override
	public void truncate(String keyspace, String tableName)
	{
		if( cluster == null )
			return;
		
		cluster.newSession().execute(String.format("TRUNCATE %s.%s;", keyspace, tableName));
	}

	@Override
	public ResultSet queryForLastWriteTimes(String keyspace, String tableName, String columnName)
	{
		if( cluster == null )
			return null;
		
		return cluster.newSession().execute(String.format("SELECT WRITETIME (%s) FROM %s.%s;", columnName, keyspace, tableName));
	}
	
	@Override
	public void close() throws IOException
	{
		if( cluster ==null )
			return;
		
		cluster.close();
	}

	public Metadata getMetadata()
	{
		return metadata;
	}
}
