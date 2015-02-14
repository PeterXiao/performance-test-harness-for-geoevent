package com.esri.geoevent.test.performance.report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import au.com.bytecode.opencsv.CSVWriter;

import com.esri.geoevent.test.performance.statistics.FixtureStatistic;

public class CSVReportWriter extends AbstractFileRollOverReportWriter implements ReportWriter
{
	@Override
	public void writeReport(String reportFile, List<String> testNames, List<String> columnNames, Map<String, List<FixtureStatistic>> stats) throws IOException
	{
		CSVWriter reportWriter = null;
		try
		{
			//create the parent directories - if needed
			createParentDirectoriesIfNeeded( reportFile );
			
			//rollover the file - keep backups
			rollOver( reportFile );
			
			//init the report writer
			reportWriter = new CSVWriter(new FileWriter(reportFile), ',');
			
			//copy the column names - add the test name as the first column
			List<String> columnNamesCopy = new ArrayList<String>();
			columnNamesCopy.add("Test Name");
			columnNamesCopy.addAll(columnNames);
			
			//write the headers
			reportWriter.writeNext(columnNamesCopy.toArray(new String[columnNamesCopy.size()]));
			reportWriter.flush();
			
			//int index = 0;
			for( String testName: testNames )
			{
				//insert an empty line break
//				if( index > 0 )
//					reportWriter.writeNext(new String[]{""});

				// get each test's fixture stats and sort them accordingly
				List<FixtureStatistic> fixtureStats = stats.get(testName);
				if( fixtureStats == null || fixtureStats.size() == 0)
					continue;
				Collections.sort(fixtureStats);
				for( FixtureStatistic fixtureStat : fixtureStats )
				{
					reportWriter.writeNext(writeRow(testName,columnNames,fixtureStat));
					reportWriter.flush();
				}
				//index++;
			}
			
			//write out the total time
			if( getTotalTestingTime() != -1 )
			{
				reportWriter.writeNext(new String[]{""});
				reportWriter.writeNext(new String[]{""});
				reportWriter.writeNext(new String[]{"Total Testing Time: " + formatTime(getTotalTestingTime())});
			}
		} 
		finally 
		{
			if( reportWriter != null )
				reportWriter.flush();
			IOUtils.closeQuietly(reportWriter);
		}
	}
	
	private String[] writeRow(String testName, List<String> columnNames, FixtureStatistic fixtureStat)
	{
		StringBuffer buffer = new StringBuffer(testName);
		for(String columnName : columnNames )
		{
			buffer.append(",");
			buffer.append(fixtureStat.getStatAsString(columnName));
		}
		return buffer.toString().split(",");
	}
}
