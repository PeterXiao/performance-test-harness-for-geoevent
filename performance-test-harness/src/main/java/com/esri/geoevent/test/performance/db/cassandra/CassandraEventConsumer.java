package com.esri.geoevent.test.performance.db.cassandra;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBConsumerBase;
import com.esri.geoevent.test.performance.jaxb.Config;

public class CassandraEventConsumer extends DBConsumerBase
{	
	// member vars
	private String nodeName;
	private String keyspace;
	private String tableName;
	private String columnName;
	
	@Override
	public void init(Config config) throws TestException 
	{
		super.init(config);
		
		this.nodeName = config.getPropertyValue("nodeName");
		this.keyspace = config.getPropertyValue("keyspace");
		this.tableName = config.getPropertyValue("tableName");
		this.columnName = config.getPropertyValue("columnName");
	}
	
	@Override
	public void validate() throws TestException
	{
		if(StringUtils.isEmpty(nodeName))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "nodeName") );
		if(StringUtils.isEmpty(keyspace))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "keyspace") );
		if(StringUtils.isEmpty(tableName))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "tableName") );
		if(StringUtils.isEmpty(columnName))
      throw new TestException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "columnName") );

		// check if we can connect
		try( DBClient client = getDBClient() )
		{
			;
		} 
		catch( Exception error)
		{
			throw new TestException( ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName") );
		}
	}
	
	@Override
	public DBClient getDBClient()
	{
		return new CassandraClient(nodeName, keyspace, tableName, columnName);
	}
}
