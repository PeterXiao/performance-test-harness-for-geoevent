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
	private Fixture defaultFixture;
	private List<Fixture> fixtures;
	private ProvisionerConfig provisionerConfig;
	
	@XmlElement(name = "Report")
	public Report getReport()
	{
		return report;
	}
	public void setReport(Report report)
	{
		this.report = report;
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
	
	@XmlElement(name = "ProvisionerConfig")
	public ProvisionerConfig getProvisionerConfig()
	{
		return provisionerConfig;
	}
	public void setProvisionerConfig(ProvisionerConfig provisionerConfig)
	{
		this.provisionerConfig = provisionerConfig;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
