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

public class CSVReportWriter extends AbstractFileRollOverReportWriter {

    @Override
    public void writeReport(String reportFile, List<String> testNames, List<String> columnNames, Map<String, List<FixtureStatistic>> stats) throws IOException {
        CSVWriter reportWriter = null;
        try {
            //create the parent directories - if needed
            createParentDirectoriesIfNeeded(reportFile);

            //rollover the file - keep backups
            rollOver(reportFile);

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
            for (String testName : testNames) {
				//insert an empty line break
//				if( index > 0 )
//					reportWriter.writeNext(new String[]{""});

                // get each test's fixture stats and sort them accordingly
                List<FixtureStatistic> fixtureStats = stats.get(testName);
                if (fixtureStats == null || fixtureStats.size() == 0) {
                    continue;
                }
                Collections.sort(fixtureStats);
                for (FixtureStatistic fixtureStat : fixtureStats) {
                    reportWriter.writeNext(writeRow(testName, columnNames, fixtureStat));
                    reportWriter.flush();
                }
                //index++;
            }

            //write out the total time
            if (getTotalTestingTime() != -1) {
                reportWriter.writeNext(new String[]{""});
                reportWriter.writeNext(new String[]{""});
                reportWriter.writeNext(new String[]{"Total Testing Time: " + formatTime(getTotalTestingTime())});
            }
        } finally {
            if (reportWriter != null) {
                reportWriter.flush();
            }
            IOUtils.closeQuietly(reportWriter);
        }
    }

    private String[] writeRow(String testName, List<String> columnNames, FixtureStatistic fixtureStat) {
        StringBuffer buffer = new StringBuffer(testName);
        for (String columnName : columnNames) {
            buffer.append(",");
            buffer.append(fixtureStat.getStatAsString(columnName));
        }
        return buffer.toString().split(",");
    }
}
