package com.esri.geoevent.test.performance.provision;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.cassandra.CassandraClient;
import com.esri.geoevent.test.performance.cassandra.DefaultCassandraClient;
import com.esri.geoevent.test.performance.jaxb.Config;

public class CassandraProvisioner implements Provisioner
{
	private String	nodeName;
	private String	keyspace;
	private String	tableName;
	
	private static final String NAME = "Cassandra";
	
	@Override
	public void init(Config config) throws ProvisionException
	{
		if( config == null )
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_INIT_ERROR", getClass().getSimpleName()) );
		
		this.nodeName = config.getPropertyValue("nodeName");
		this.keyspace = config.getPropertyValue("keyspace");
		this.tableName = config.getPropertyValue("tableName");
		
		validate();
	}
	
	@Override
	public void provision() throws ProvisionException
	{
		try
		{
			System.out.println("-----------------------------------------------");
			System.out.println( ImplMessages.getMessage("PROVISIONER_START_MSG", NAME) );
			System.out.println("-----------------------------------------------");
			truncateTable();
			System.out.println( ImplMessages.getMessage("PROVISIONER_FINISH_MSG", NAME) );
		}
		catch (Exception ex)
		{
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_ERROR", NAME, ex.getMessage() ) );
		}
	}
	
	private void validate() throws ProvisionException
	{
		if(StringUtils.isEmpty(nodeName))
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "nodeName") );
		if(StringUtils.isEmpty(keyspace))
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "keyspace") );
		if(StringUtils.isEmpty(tableName))
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "tableName") );
		
		// check if we can connect
		try( CassandraClient client = new DefaultCassandraClient(nodeName) )
		{
			;
		} 
		catch( Exception error)
		{
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName") );
		}
	}
	
	private void truncateTable()
	{
		try( CassandraClient client = new DefaultCassandraClient(nodeName) )
		{
			client.truncate(keyspace, tableName);
		} 
		catch( Exception error)
		{
			System.err.println( ImplMessages.getMessage("PROVISIONER_TRUNCATE_FAILED", keyspace, tableName) );
			error.printStackTrace();
		}
	}
}
