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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.esri.geoevent.test.performance.statistics.FixtureStatistic;

public class XLSXReportWriter extends AbstractFileRollOverReportWriter implements ReportWriter {

    @Override
    public void writeReport(String reportFile, List<String> testNames, List<String> columnNames, Map<String, List<FixtureStatistic>> stats) throws IOException {
        //create the parent directories - if needed
        createParentDirectoriesIfNeeded(reportFile);

        // rollover the file - keep backups
        rollOver(reportFile);

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook();

            // header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            headerStyle.setFont(font);

            // copy the column names - add the test name as the first column
            List<String> columnNamesCopy = new ArrayList<String>();
            columnNamesCopy.add("Test Name");
            columnNamesCopy.addAll(columnNames);

            // create the sheet
            Sheet sheet = workbook.createSheet("Summary");

            // create the header row
            int rowIndex = 0;
            Row headers = sheet.createRow(rowIndex);
            headers.setRowStyle(headerStyle);
            int cellIndex = 0;
            for (String columnName : columnNamesCopy) {
                Cell cell = headers.createCell(cellIndex);
                cell.setCellValue(columnName);
                cell.setCellStyle(headerStyle);
                cellIndex++;
            }
            for (String testName : testNames) {
                // get each test's fixture stats and sort them accordingly
                List<FixtureStatistic> fixtureStats = stats.get(testName);
                if (fixtureStats == null || fixtureStats.size() == 0) {
                    continue;
                }
                Collections.sort(fixtureStats);
                rowIndex++;

                for (FixtureStatistic fixtureStat : fixtureStats) {
                    Row data = sheet.createRow(rowIndex);
                    cellIndex = 0;

                    //write out the test name first
                    Cell cell = data.createCell(cellIndex);
                    cell.setCellValue(testName);
                    cellIndex++;

                    for (String columnName : columnNames) {
                        cell = data.createCell(cellIndex);
                        Object rawValue = fixtureStat.getStat(columnName);
                        if (rawValue == null) {
                            cell.setCellValue("");
                        } else {
                            if (rawValue instanceof Integer) {
                                cell.setCellValue((Integer) rawValue);
                            } else if (rawValue instanceof Double) {
                                cell.setCellValue((Double) rawValue);
                            } else if (rawValue instanceof Long) {
                                cell.setCellValue((Long) rawValue);
                            } else if (rawValue instanceof Boolean) {
                                cell.setCellValue((Boolean) rawValue);
                            } else {
                                cell.setCellValue(rawValue.toString());
                            }
                        }
                        // adjust column width to fit the content
                        sheet.autoSizeColumn(cellIndex);
                        cellIndex++;
                    }
                    //rowIndex++;
                }
            }

            //write out the total time
            if (getTotalTestingTime() != -1) {
                rowIndex = rowIndex + 2;
                Row data = sheet.createRow(rowIndex);
                Cell cell = data.createCell(0);
                cell.setCellValue("Total Testing Time:");
                cell.setCellStyle(headerStyle);
                cell = data.createCell(1);
                cell.setCellValue(formatTime(getTotalTestingTime()));
            }
        } finally {
            // write out the file
            FileOutputStream out = null;
            try {
                String fullPath = FilenameUtils.getFullPathNoEndSeparator(reportFile);
                // create all non exists folders else you will hit FileNotFoundException for report file path
                new File(fullPath).mkdirs();

                out = new FileOutputStream(reportFile);
                if (workbook != null) {
                    workbook.write(out);
                }
            } finally {
                IOUtils.closeQuietly(out);
            }
        }
    }
}
