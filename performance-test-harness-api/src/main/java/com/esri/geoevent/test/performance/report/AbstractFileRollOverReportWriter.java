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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;

public abstract class AbstractFileRollOverReportWriter
{
	private int	maxNumberOfReportFiles	= 10;
	private long totalTestingTime = -1;
	
	protected void rollOver(String fileName)
	{
		File target;
		File file;
		boolean renameSucceeded = true;

		if (!new File(fileName).exists())
			return;

		// If maxBackups <= 0, then there is no file renaming to be done.
		if (getMaxNumberOfReportFiles() > 0)
		{
			// Delete the oldest file, to keep Windows happy.
			String ext = FilenameUtils.getExtension(fileName);
			String baseFileName = FilenameUtils.getFullPath(fileName) + FilenameUtils.getBaseName(fileName);

			file = new File(baseFileName + getMaxNumberOfReportFiles() + '.' + ext);
			if (file.exists())
				renameSucceeded = file.delete();

			// Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
			for (int i = getMaxNumberOfReportFiles() - 1; i >= 1 && renameSucceeded; i--)
			{
				file = new File(baseFileName + i + "." + ext);
				if (file.exists())
				{
					target = new File(baseFileName + (i + 1) + '.' + ext);
					renameSucceeded = file.renameTo(target);
				}
			}

			if (renameSucceeded)
			{
				// Rename fileName to fileName.1
				target = new File(baseFileName + 1 + "." + ext);
				file = new File(fileName);
				renameSucceeded = file.renameTo(target);
			}
		}
	}

	protected String formatTime(final long timeInMillisec)
	{
		final long hr = TimeUnit.MILLISECONDS.toHours(timeInMillisec);
		final long min = TimeUnit.MILLISECONDS.toMinutes(timeInMillisec - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(timeInMillisec - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(timeInMillisec - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
		if (hr > 0)
			return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
		else
			return String.format("%02d:%02d.%03d", min, sec, ms);
	}
	
	public List<String> getDetailedColumnNames()
	{
		String[] columns = new String[] 
		{
				"totalEvents", "successes", "failures", "expectedResultCount", "producerConnections", "consumerConnections", "minProductionTime", "maxProductionTime", "avgProductionTime", 
				"medProductionTime", "devProductionTime", "minConsumptionTime", "maxConsumptionTime", "avgConsumptionTime", "medConsumptionTime", "devConsumptionTime", 
				"minTotalTime", "maxTotalTime", "avgTotalTime", "medTotalTime", "devTotalTime", "minEventsPerSec", "maxEventsPerSec", "avgEventsPerSec", 
				"minFirstReceivedLatency", "maxFirstReceivedLatency", "avgFirstReceivedLatency", "medFirstReceivedLatency", "devFirstReceivedLatency", 
				"minLastReceivedLatency", "maxLastReceivedLatency", "avgLastReceivedLatency", "medLastReceivedLatency", "devLastReceivedLatency" 
		};
		return Arrays.asList(columns);
	}
	
	public List<String> getSimpleColumnNames()
	{
		String[] columns = new String[] 
		{
				"Rate", "totalEvents", "successes", "expectedResultCount", "failures",
				"avgTotalTime", "avgEventsPerSec", "%"
		};
		return Arrays.asList(columns);
	}
	
	public int getMaxNumberOfReportFiles()
	{
		return maxNumberOfReportFiles;
	}

	public void setMaxNumberOfReportFiles(int maxNumberOfReportFiles)
	{
		this.maxNumberOfReportFiles = maxNumberOfReportFiles;
	}

	public long getTotalTestingTime()
	{
		return totalTestingTime;
	}

	public void setTotalTestingTime(long totalTestingTime)
	{
		this.totalTestingTime = totalTestingTime;
	}
}
