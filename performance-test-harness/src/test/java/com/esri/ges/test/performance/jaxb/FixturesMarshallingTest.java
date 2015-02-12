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

import com.esri.ges.test.performance.Protocol;
import com.esri.ges.test.performance.report.ReportType;
import com.esri.ges.test.performance.utils.KryoUtils;

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

	/**
	 * <b>NOTE:</b> This is a very specific test. If you modify the <code>fixtures_sanity.xml</code> file, please adjust
	 * the test below.
	 * 
	 * @throws Exception
	 */
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
		Assert.assertTrue(fixtures.getReport().getType() != ReportType.UNKNOWN);
		Assert.assertTrue(fixtures.getReport().getMaxNumberOfReportFiles() > 0);
		Assert.assertTrue(fixtures.getReport().isSimpleColumnNames());

		// check the default fixture object
		sanityCheckFixture(fixtures.getDefaultFixture());
		Assert.assertNull(fixtures.getDefaultFixture().getName());

		// check the fixtures config
		Assert.assertNotNull(fixtures.getFixtures());
		Assert.assertNotNull(fixtures.getFixtures().get(0));
		Assert.assertNotNull(fixtures.getFixtures().get(0).getName());
		Assert.assertNull(fixtures.getFixtures().get(0).getConsumerConfig());
		Assert.assertNull(fixtures.getFixtures().get(0).getDefaultConfig());
		Assert.assertNull(fixtures.getFixtures().get(0).getProducerConfig());
		Assert.assertNull(fixtures.getFixtures().get(0).getSimulation());

		Assert.assertNotNull(fixtures.getFixtures().get(1));
		Assert.assertNotNull(fixtures.getFixtures().get(1).getName());
		Assert.assertNull(fixtures.getFixtures().get(1).getConsumerConfig());
		Assert.assertNull(fixtures.getFixtures().get(1).getDefaultConfig());

		// check the producer config
		Assert.assertNotNull(fixtures.getFixtures().get(1).getProducerConfig());
		Assert.assertNotNull(fixtures.getFixtures().get(1).getProducerConfig().getProperties());
		Assert.assertNotNull(fixtures.getFixtures().get(1).getProducerConfig().getProperties().get(0));
		Assert.assertNotNull(fixtures.getFixtures().get(1).getProducerConfig().getProperties().get(0).getName());
		Assert.assertNotNull(fixtures.getFixtures().get(1).getProducerConfig().getProperties().get(0).getValue());
		Assert.assertNotNull(fixtures.getFixtures().get(1).getProducerConfig().getPropertyByName("simulationFile"));
		Assert.assertNotNull(fixtures.getFixtures().get(1).getProducerConfig().getPropertyByName("simulationFile").getValue());

		// check the simulation
		Assert.assertNotNull(fixtures.getFixtures().get(1).getSimulation());
		Assert.assertTrue(fixtures.getFixtures().get(1).getSimulation().getEventsToAddPerIteration() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(1).getSimulation().getExpectedResultCount() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(1).getSimulation().getMaxEvents() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(1).getSimulation().getMinEvents() > 0);
		Assert.assertTrue(fixtures.getFixtures().get(1).getSimulation().getIterations() > 0);
	}

	/**
	 * <b>NOTE:</b> This is a very specific test. If you modify the <code>fixtures_sanity.xml</code> file, please adjust
	 * the test below.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testApply() throws Exception
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
		Assert.assertTrue(fixtures.getReport().getType() != ReportType.UNKNOWN);
		Assert.assertTrue(fixtures.getReport().getMaxNumberOfReportFiles() > 0);
		Assert.assertTrue(fixtures.getReport().isSimpleColumnNames());

		// check the default fixture object
		sanityCheckFixture(fixtures.getDefaultFixture());
		Assert.assertNull(fixtures.getDefaultFixture().getName());
		
		// check the fixtures config
		Assert.assertNotNull(fixtures.getFixtures());
		Assert.assertNotNull(fixtures.getFixtures().get(0));
		Assert.assertNotNull(fixtures.getFixtures().get(0).getName());
		Assert.assertNull(fixtures.getFixtures().get(0).getConsumerConfig());
		Assert.assertNull(fixtures.getFixtures().get(0).getDefaultConfig());
		Assert.assertNull(fixtures.getFixtures().get(0).getProducerConfig());
		Assert.assertNull(fixtures.getFixtures().get(0).getSimulation());

		// Test Apply
		Fixture fixture1 = fixtures.getFixtures().get(0);
		fixture1.apply(fixtures.getDefaultFixture());

		// check the default fixture object
		sanityCheckFixture(fixture1);
		Assert.assertNotNull(fixture1.getName());
		
		Fixture fixture2 = fixtures.getFixtures().get(1);
		fixture2.apply(fixtures.getDefaultFixture());

		// check the default fixture object
		sanityCheckFixture(fixture2);
		Assert.assertNotNull(fixture2.getName());
		Assert.assertNotSame(fixture2.getProducerConfig().getPropertyByName("simulationFile").getValue(), fixtures.getDefaultFixture().getProducerConfig().getPropertyByName("simulationFile").getValue());
		Assert.assertNotSame(fixture2.getSimulation().getIterations(), fixtures.getDefaultFixture().getSimulation().getIterations());
	}

	@Test
	public void testKryoSerialization() throws Exception
	{
		Fixtures fixtures = fromXML(basePath + "/fixtures_sanity.xml");
		Fixture fixture1 = fixtures.getFixtures().get(0);
		fixture1.apply(fixtures.getDefaultFixture());
		
		String data = KryoUtils.toString(fixture1, Fixture.class);
		Fixture kryoFixture = KryoUtils.fromString(data, Fixture.class);
		Assert.assertEquals(fixture1, kryoFixture);
		
		String configData = KryoUtils.toString(fixture1.getConsumerConfig(), Config.class);
		ConsumerConfig kryoConsumerConfig = KryoUtils.fromString(configData, ConsumerConfig.class);
		Assert.assertEquals(fixture1.getConsumerConfig(), kryoConsumerConfig);
	}
	
	//-------------------------------------------------------
	// Validation Methods
	// ------------------------------------------------------
	
	private void sanityCheckFixture(Fixture fixture)
	{
		// check the default fixture object
		Assert.assertNotNull(fixture);
		
		// check the default config
		Assert.assertNotNull(fixture.getDefaultConfig());
		Assert.assertNotNull(fixture.getDefaultConfig().getHost());
		Assert.assertNotNull(fixture.getDefaultConfig().getProtocol());
		Assert.assertTrue(fixture.getDefaultConfig().getProtocol() != Protocol.UNKNOWN);

		// check the properties
		Assert.assertNotNull(fixture.getDefaultConfig().getProperties());
		Assert.assertNotNull(fixture.getDefaultConfig().getProperties().get(0));
		Assert.assertNotNull(fixture.getDefaultConfig().getProperties().get(0).getName());
		Assert.assertNotNull(fixture.getDefaultConfig().getProperties().get(0).getValue());
		Assert.assertNotNull(fixture.getDefaultConfig().getPropertyByName("eventsPerSec"));
		Assert.assertNotNull(fixture.getDefaultConfig().getPropertyByName("eventsPerSec").getValue());
		Assert.assertTrue(Integer.parseInt(fixture.getDefaultConfig().getPropertyByName("eventsPerSec").getValue()) > 0);

		// check the producer config
		Assert.assertNotNull(fixture.getProducerConfig());
		Assert.assertNotNull(fixture.getProducerConfig().getHost());
		Assert.assertTrue(fixture.getProducerConfig().getCommandPort() > 0);
		Assert.assertTrue(fixture.getProducerConfig().getNumOfConnections() > 0);
		Assert.assertNotNull(fixture.getProducerConfig().getProtocol());
		Assert.assertTrue(fixture.getProducerConfig().getProtocol() != Protocol.UNKNOWN);

		// check the properties
		Assert.assertNotNull(fixture.getProducerConfig().getProperties());
		Assert.assertNotNull(fixture.getProducerConfig().getProperties().get(0));
		Assert.assertNotNull(fixture.getProducerConfig().getProperties().get(0).getName());
		Assert.assertNotNull(fixture.getProducerConfig().getProperties().get(0).getValue());
		Assert.assertNotNull(fixture.getProducerConfig().getPropertyByName("simulationFile"));
		Assert.assertNotNull(fixture.getProducerConfig().getPropertyByName("simulationFile").getValue());

		// check the producers
		Assert.assertNotNull(fixture.getProducerConfig().getProducers());
		Assert.assertTrue(fixture.getProducerConfig().getNumOfConnections() > 0);
		Assert.assertNotNull(fixture.getProducerConfig().getProducers());
		Assert.assertNotNull(fixture.getProducerConfig().getProducers().get(0));
		Assert.assertNotNull(fixture.getProducerConfig().getProducers().get(0).getHost());
		Assert.assertTrue(fixture.getProducerConfig().getProducers().get(0).getCommandPort() > 0);
		Assert.assertNotNull(fixture.getProducerConfig().getProducerByName("localhost"));
		Assert.assertNotNull(fixture.getProducerConfig().getProducerByName("localhost").getHost());
		Assert.assertTrue(fixture.getProducerConfig().getProducerByName("localhost").getCommandPort() > 0);

		// check the consumer config
		Assert.assertNotNull(fixture.getConsumerConfig());
		Assert.assertNotNull(fixture.getConsumerConfig().getHost());
		Assert.assertTrue(fixture.getConsumerConfig().getCommandPort() > 0);
		Assert.assertTrue(fixture.getConsumerConfig().getNumOfConnections() > 0);
		Assert.assertNotNull(fixture.getConsumerConfig().getProtocol());
		Assert.assertTrue(fixture.getConsumerConfig().getProtocol() != Protocol.UNKNOWN);
		Assert.assertTrue(fixture.getConsumerConfig().getTimeoutInSec() > 0);

		// check the properties
		Assert.assertNotNull(fixture.getConsumerConfig().getProperties());
		Assert.assertNotNull(fixture.getConsumerConfig().getProperties().get(0));
		Assert.assertNotNull(fixture.getConsumerConfig().getProperties().get(0).getName());
		Assert.assertNotNull(fixture.getConsumerConfig().getProperties().get(0).getValue());
		Assert.assertNotNull(fixture.getConsumerConfig().getPropertyByName("host"));
		Assert.assertNotNull(fixture.getConsumerConfig().getPropertyByName("host").getValue());

		// check the consumers
		Assert.assertNotNull(fixture.getConsumerConfig().getConsumers());
		Assert.assertTrue(fixture.getConsumerConfig().getNumOfConnections() > 0);
		Assert.assertNotNull(fixture.getConsumerConfig().getConsumers());
		Assert.assertNotNull(fixture.getConsumerConfig().getConsumers().get(0));
		Assert.assertNotNull(fixture.getConsumerConfig().getConsumers().get(0).getHost());
		Assert.assertTrue(fixture.getConsumerConfig().getConsumers().get(0).getCommandPort() > 0);
		Assert.assertNotNull(fixture.getConsumerConfig().getConsumerByName("localhost"));
		Assert.assertNotNull(fixture.getConsumerConfig().getConsumerByName("localhost").getHost());
		Assert.assertTrue(fixture.getConsumerConfig().getConsumerByName("localhost").getCommandPort() > 0);

		// check the simulation
		Assert.assertNotNull(fixture.getSimulation());
		Assert.assertTrue(fixture.getSimulation().getEventsToAddPerIteration() > 0);
		Assert.assertTrue(fixture.getSimulation().getExpectedResultCount() > 0);
		Assert.assertTrue(fixture.getSimulation().getMaxEvents() > 0);
		Assert.assertTrue(fixture.getSimulation().getMinEvents() > 0);
		Assert.assertTrue(fixture.getSimulation().getIterations() > 0);
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
