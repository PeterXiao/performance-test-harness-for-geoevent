package com.esri.ges.test.performance.jaxb;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FixturesMarshallingTest
{

	private String	basePath;

	@Before
	public void setUp() throws Exception
	{
		basePath = new File("src/test/resources").getAbsolutePath();
	}

	@After
	public void tearDown() throws Exception
	{
		basePath = null;
	}

	// ---------------------------------------------------------
	// Tests
	// ---------------------------------------------------------

	@Test
	public void testFromXML() throws Exception
	{
		Fixtures fixtures = fromXML(basePath + "/fixtures_sanity.xml");

		// check the list
		Assert.assertNotNull(fixtures);
		Assert.assertNotNull(fixtures.getFixtures());
		Assert.assertTrue(fixtures.getFixtures().size() > 0);

		// check the report object
		Assert.assertNotNull(fixtures.getReport());
		Assert.assertNotNull(fixtures.getReport().getReportFile());
		Assert.assertNotNull(fixtures.getReport().getType());
		Assert.assertTrue(fixtures.getReport().getMaxNumberOfReportFiles() > 0);
		Assert.assertTrue(fixtures.getReport().isSimpleColumnNames());
		
		// check the fixture object
		Assert.assertNotNull(fixtures.getFixtures().get(0));
		Assert.assertNotNull(fixtures.getFixtures().get(0).getName());
		Assert.assertNotNull(fixtures.getFixtures().get(0).getProtocol());
		Assert.assertTrue(fixtures.getFixtures().get(0).getCommandPort() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).isLocal());

		// check the geoevent host
		Assert.assertNotNull(fixtures.getFixtures().get(0).getGeoEventHost());
		Assert.assertNotNull(fixtures.getFixtures().get(0).getGeoEventHost().getHostName());
		Assert.assertTrue(fixtures.getFixtures().get(0).getGeoEventHost().getConsumerPort() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getGeoEventHost().getProducerPort() > 0);

		// check the simulation
		Assert.assertNotNull(fixtures.getFixtures().get(0).getSimulation());
		Assert.assertNotNull(fixtures.getFixtures().get(0).getSimulation().getSourceFile());
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getEventsToAddPerIteration() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getExpectedResultCount() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getMaxEvents() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getMinEvents() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getIterations() > 0);

		// check the producers
		Assert.assertNotNull(fixtures.getFixtures().get(0).getProducers());
		Assert.assertTrue(fixtures.getFixtures().get(0).getProducers().getNumOfConnections() > 0);
		Assert.assertNotNull(fixtures.getFixtures().get(0).getProducers().getProducers());
		Assert.assertTrue(fixtures.getFixtures().get(0).getProducers().getProducers().size() > 0);

		// check the consumers
		Assert.assertNotNull(fixtures.getFixtures().get(0).getConsumers());
		Assert.assertTrue(fixtures.getFixtures().get(0).getConsumers().getNumOfConnections() > 0);
		Assert.assertNotNull(fixtures.getFixtures().get(0).getConsumers().getConsumers());
		Assert.assertTrue(fixtures.getFixtures().get(0).getConsumers().getConsumers().size() > 0);
	}
	
	@Test
	public void testFromXML_BASIC() throws Exception
	{
		Fixtures fixtures = fromXML(basePath + "/fixtures_basic.xml");

	// check the list
		Assert.assertNotNull(fixtures);
		Assert.assertNotNull(fixtures.getFixtures());
		Assert.assertTrue(fixtures.getFixtures().size() > 0);

		// check the report object
		Assert.assertNotNull(fixtures.getReport());
		Assert.assertNotNull(fixtures.getReport().getReportFile());
		Assert.assertNotNull(fixtures.getReport().getType());
		Assert.assertFalse(fixtures.getReport().isSimpleColumnNames());
			
		// check the fixture object
		Assert.assertNotNull(fixtures.getFixtures().get(0));
		Assert.assertNotNull(fixtures.getFixtures().get(0).getName());
		Assert.assertNotNull(fixtures.getFixtures().get(0).getProtocol());
		Assert.assertTrue(fixtures.getFixtures().get(0).getCommandPort() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).isLocal());

		// check the geoevent host
		Assert.assertNotNull(fixtures.getFixtures().get(0).getGeoEventHost());
		Assert.assertNotNull(fixtures.getFixtures().get(0).getGeoEventHost().getHostName());
		Assert.assertTrue(fixtures.getFixtures().get(0).getGeoEventHost().getConsumerPort() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getGeoEventHost().getProducerPort() > 0);

		// check the simulation
		Assert.assertNotNull(fixtures.getFixtures().get(0).getSimulation());
		Assert.assertNotNull(fixtures.getFixtures().get(0).getSimulation().getSourceFile());
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getEventsToAddPerIteration() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getExpectedResultCount() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getMaxEvents() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getMinEvents() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(0).getSimulation().getIterations() > 0);

		// check the producers
		Assert.assertNull(fixtures.getFixtures().get(0).getProducers());

		// check the consumers
		Assert.assertNull(fixtures.getFixtures().get(0).getConsumers());
	}

	// -------------------------------------------------------
	// Conversion Methods
	// ------------------------------------------------------

	private static Fixtures fromXML(String xmlLocation) throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(Fixtures.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource xml = new StreamSource(xmlLocation);
		return (Fixtures) unmarshaller.unmarshal(xml);
	}
}
