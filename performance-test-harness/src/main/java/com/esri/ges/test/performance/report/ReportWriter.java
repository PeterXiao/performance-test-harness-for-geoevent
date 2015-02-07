package com.esri.ges.test.performance.report;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.esri.ges.test.performance.statistics.FixtureStatistic;

public interface ReportWriter
{
	void setMaxNumberOfReportFiles(int maxNumberOfReportFiles);
	
	void setTotalTestingTime(long totalTestingTime);
	
	List<String> getDetailedColumnNames();
	
	List<String> getSimpleColumnNames();
	
	void writeReport(String reportFile, List<String> testNames, List<String> columnNames, Map<String, List<FixtureStatistic>> stats) throws IOException;
	
	
}
