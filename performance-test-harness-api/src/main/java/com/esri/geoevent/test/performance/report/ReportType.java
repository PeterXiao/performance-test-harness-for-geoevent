package com.esri.geoevent.test.performance.report;

import javax.xml.bind.annotation.XmlEnum;

import org.apache.commons.lang3.StringUtils;

@XmlEnum
public enum ReportType
{
	CSV, XLSX, XML, UNKNOWN;
	
	public static ReportType fromValue(String valueStr)
	{
		if( StringUtils.isBlank(valueStr) )
			return UNKNOWN;
		if( CSV.toString().equalsIgnoreCase(valueStr))
			return CSV;
		else if( XLSX.toString().equalsIgnoreCase(valueStr))
			return XLSX;
		else if( XML.toString().equalsIgnoreCase(valueStr))
			return XML;
		else 
			return UNKNOWN;
	}
}
