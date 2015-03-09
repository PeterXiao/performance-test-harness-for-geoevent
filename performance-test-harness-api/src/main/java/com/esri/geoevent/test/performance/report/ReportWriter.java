package com.esri.geoevent.test.performance.report;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.esri.geoevent.test.performance.statistics.FixtureStatistic;

public interface ReportWriter
{
	void setMaxNumberOfReportFiles(int maxNumberOfReportFiles);
	
	void setTotalTestingTime(long totalTestingTime);
	
	List<String> getReportColumnNames(List<String> reportColumns, List<String> additionalReportColumns);
	
	void writeReport(String reportFile, List<String> testNames, List<String> columnNames, Map<String, List<FixtureStatistic>> stats) throws IOException;
	
	
}
