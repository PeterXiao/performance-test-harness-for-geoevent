package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Report")
public class Report
{
	private String type;
	private String reportFile;
	private boolean simpleColumnNames = false;
	private int maxNumberOfReportFiles = 10;
	
	@XmlAttribute
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
	
	@XmlElement(name = "ReportFile")
	public String getReportFile()
	{
		return reportFile;
	}

	public void setReportFile(String reportFile)
	{
		this.reportFile = reportFile;
	}
	
	@XmlAttribute(name = "simpleColumnNames")
	public boolean isSimpleColumnNames()
	{
		return simpleColumnNames;
	}
	public void setSimpleColumnNames(boolean simpleColumnNames)
	{
		this.simpleColumnNames = simpleColumnNames;
	}

	@XmlAttribute(name = "maxNumberOfReportFiles")
	public int getMaxNumberOfReportFiles()
	{
		return maxNumberOfReportFiles;
	}

	public void setMaxNumberOfReportFiles(int maxNumberOfReportFiles)
	{
		this.maxNumberOfReportFiles = maxNumberOfReportFiles;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
