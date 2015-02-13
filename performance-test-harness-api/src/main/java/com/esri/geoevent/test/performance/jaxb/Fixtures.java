package com.esri.geoevent.test.performance.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Fixtures")
public class Fixtures
{
	private Report report;
	private List<Fixture> fixtures;
	private GeoEventConfiguration geoEventConfiguration;
	private Fixture defaultFixture;
	
	@XmlElement(name = "Report")
	public Report getReport()
	{
		return report;
	}
	public void setReport(Report report)
	{
		this.report = report;
	}
	
	@XmlElement(name = "GeoEventConfiguration")
	public GeoEventConfiguration getGeoEventConfiguration()
	{
		return geoEventConfiguration;
	}
	public void setGeoEventConfiguration(GeoEventConfiguration geoEventConfiguration)
	{
		this.geoEventConfiguration = geoEventConfiguration;
	}

	@XmlElement(name = "Fixture")
	public List<Fixture> getFixtures()
	{
		return fixtures;
	}
	public void setFixtures(List<Fixture> fixtures)
	{
		this.fixtures = fixtures;
	}
	
	@XmlElement(name = "DefaultFixture")
	public Fixture getDefaultFixture()
	{
		return defaultFixture;
	}
	public void setDefaultFixture(Fixture defaultFixture)
	{
		this.defaultFixture = defaultFixture;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
