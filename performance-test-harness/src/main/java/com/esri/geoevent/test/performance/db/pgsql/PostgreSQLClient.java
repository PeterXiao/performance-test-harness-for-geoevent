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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBResult;


public class PostgreSQLClient implements DBClient
{
	// member vars
	@SuppressWarnings("unused")
	private static String DBDriver;
	@SuppressWarnings("unused")
	private static String DBURL;
	@SuppressWarnings("unused")
	private static String DBUsername;
	@SuppressWarnings("unused")
	private static String DBPassword;
	private static String DBschemaName;
	private static String DBtableName;
	private static String DBtimezoneOffsetInHours;
	private static Connection connection = null;
	private static Statement statement = null;
	private static String created_date = "created_date";//Must enable Edit Tracking in Feature Service data
	@SuppressWarnings("unused")
	private static String last_edited_date = "last_edited_date";//Must enable Edit Tracking in Feature Service data; for update Feature Service


	public PostgreSQLClient(String DBDriver, String DBURL, String DBUsername, String DBPassword, String DBschemaName, String DBtableName, String DBtimezoneOffsetInHours)
	{
      try {
    	  	 
	         PostgreSQLClient.DBDriver = DBDriver;
	         PostgreSQLClient.DBURL = DBURL;
	         PostgreSQLClient.DBUsername = DBUsername;
	         PostgreSQLClient.DBPassword = DBPassword;
	         PostgreSQLClient.DBschemaName = DBschemaName;
	         PostgreSQLClient.DBtableName = DBtableName;
	         PostgreSQLClient.DBtimezoneOffsetInHours = DBtimezoneOffsetInHours;
	         
    	     Class.forName(DBDriver);
	         connection = DriverManager.getConnection(DBURL, DBUsername, DBPassword);
	         System.out.println("Connected to database successfully.");

	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	}

	@Override
	public void createSchema()
	{
	}

	@Override
	public void truncate()
	{
		if (connection == null){
			return;
		}
		else{
			try 
			{

			  Statement statement  = connection.createStatement();
			  String sql = "TRUNCATE "+DBschemaName +"."+DBtableName;
		      statement.executeUpdate(sql);
			  return;
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void close()
	{
		if (connection == null)
			return;

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public DBResult queryForLastWriteTimes()
	{
		List<Long> allRows = new LinkedList<Long>();
	    long startTime = 0;
	    long endTime = 0;
	    int totalCount = 0;
	    int count = 0; 
	    try 
	    {
		 // Make sure autocommit is off
		 connection.setAutoCommit(false);
		 statement = connection.createStatement();
		 ResultSet rs = statement.executeQuery("SELECT "+created_date+" FROM "+DBschemaName +"."+DBtableName );
		 
		 while (rs.next())
		 {	     
		     Timestamp timestamp = rs.getTimestamp(created_date);
		     	     
		     // Add to List
		     allRows.add(timestamp.getTime());
			 count++;
		 }		     		     
		} catch (Exception e) {
		     e.printStackTrace();
		     System.err.println(e.getClass().getName()+": "+e.getMessage());
		     System.exit(0);
		}
	    //Sort all rows accordingly
	    Collections.sort(allRows);

	    // Gather the information we need
	    // To accommodate for DB TimeZone not in sync with local machine: i.e. (3600sec*7hours)*1000=25200000
	    int DBtimezoneOffsetInHoursInt = Integer.parseInt(DBtimezoneOffsetInHours);
	    startTime = allRows.get(0)-(DBtimezoneOffsetInHoursInt*3600*1000);
	    endTime = allRows.get(count-1)-(DBtimezoneOffsetInHoursInt*3600*1000); 
	    totalCount = count; 
	    return new DBResult(startTime, endTime, totalCount);
	}

	public ResultSetMetaData getMetadata(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metadata = rs.getMetaData(); 
		return metadata;
	}
}
