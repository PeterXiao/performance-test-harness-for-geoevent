package com.esri.geoevent.test.performance.db.cassandra;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBResult;

public class CassandraClient implements DBClient
{
	// member vars
	private String		keyspace;
	private String		tableName;
	private String 		columnName;
	
	// setup the cluster
	private Cluster		cluster;
	private Metadata	metadata;

	public CassandraClient(String nodeName, String keyspace, String tableName, String columnName)
	{
		this.keyspace = keyspace;
		this.tableName = tableName;
		this.columnName = columnName;
		
		String[] nodeNames = new String[] { nodeName };
		if( nodeName != null && nodeName.contains(",") )
			nodeNames = nodeName.split(",");
		
		
		this.cluster = Cluster.builder().addContactPoints(nodeNames).build();
		this.metadata = cluster.getMetadata();
	}

	@Override
	public void truncate()
	{
		if (cluster == null)
			return;

		try (Session session = cluster.newSession())
		{
			Statement statement = new SimpleStatement(String.format("TRUNCATE %s.%s;", keyspace, tableName));
			statement.setConsistencyLevel(ConsistencyLevel.ALL);
			session.execute(statement);
		}
	}

	@Override
	public DBResult queryForLastWriteTimes()
	{
		if (cluster == null)
			return null;

		try (Session session = cluster.newSession())
		{
			Statement statement = new SimpleStatement(String.format("SELECT WRITETIME (%s) FROM %s.%s;", columnName, keyspace, tableName));
			statement.setConsistencyLevel(ConsistencyLevel.ALL);
			ResultSet results = session.execute(statement);
			List<Row> allRows = results.all();

			// sort all of the rows accordingly
			allRows.sort(new RowComparator());

			// gather the information we need
			long startTime = allRows.get(0).getLong(0) / 1000;
			long endTime = allRows.get(allRows.size() - 1).getLong(0) / 1000;
			int totalCount = allRows.size();
			return new DBResult(startTime, endTime, totalCount);
		}
	}

	@Override
	public void close() throws IOException
	{
		if (cluster == null)
			return;

		cluster.close();
	}

	public Metadata getMetadata()
	{
		return metadata;
	}

	/**
	 * Inner class used to sort the incoming Cassandra rows by last updated (long)
	 */
	class RowComparator implements Comparator<Row>
	{
		@Override
		public int compare(Row row1, Row row2)
		{
			if (row1 == null || row2 == null)
				return 0;

			return Long.compare(row1.getLong(0), row2.getLong(0));
		}
	}
}
