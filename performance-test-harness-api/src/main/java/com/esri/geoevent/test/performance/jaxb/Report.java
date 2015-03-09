package com.esri.geoevent.test.performance.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.esri.geoevent.test.performance.report.ReportType;

@XmlRootElement(name = "Report")
public class Report
{
	private ReportType type;
	private String reportFile;
	private int maxNumberOfReportFiles = 10;
	private List<String> reportColumns;
	private List<String> additionalReportColumns;
	
	@XmlAttribute
	public ReportType getType()
	{
		return type;
	}

	public void setType(ReportType type)
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

	@XmlAttribute(name = "maxNumberOfReportFiles")
	public int getMaxNumberOfReportFiles()
	{
		return maxNumberOfReportFiles;
	}

	public void setMaxNumberOfReportFiles(int maxNumberOfReportFiles)
	{
		this.maxNumberOfReportFiles = maxNumberOfReportFiles;
	}
	
	@XmlList
	@XmlElement(name = "ReportColumns", required=false)
	public List<String> getReportColumns()
	{
		return reportColumns;
	}

	public void setReportColumns(List<String> reportColumns)
	{
		this.reportColumns = reportColumns;
	}

	@XmlList
	@XmlElement(name="AdditionalReportColumns", required = false)
	public List<String> getAdditionalReportColumns()
	{
		return additionalReportColumns;
	}

	public void setAdditionalReportColumns(List<String> additionalReportColumns)
	{
		this.additionalReportColumns = additionalReportColumns;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Report))
      return false;
		
		Report report = (Report) obj;
    if (!ObjectUtils.equals(getMaxNumberOfReportFiles(), report.getMaxNumberOfReportFiles()))
      return false;
    if (!ObjectUtils.equals(getReportFile(), report.getReportFile()))
      return false;
    if (!ObjectUtils.equals(getType(), report.getType()))
      return false;
    if (!ObjectUtils.equals(getReportColumns(), report.getReportColumns()))
      return false;
    if (!ObjectUtils.equals(getAdditionalReportColumns(), report.getAdditionalReportColumns()))
      return false;
    
    return true;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
