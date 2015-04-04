package com.esri.geoevent.test.performance.cassandra;

import java.io.Closeable;

import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;

public interface CassandraClient extends Closeable
{
	 void truncate(String keyspace, String tableName);
	  
	 ResultSet queryForLastWriteTimes(String keyspace, String tableName, String columnName);
	 
	 Metadata getMetadata();
}
