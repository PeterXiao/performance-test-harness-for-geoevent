# Documentation

## 5-minute quick start guide

In this tutorial, you'll learn how to setup the Performance Test Harness for GeoEvent and run a simple test.
Before you start, you need to have basic knowledge of Java and how to build Java with Apache Maven.
Refer to [Java documentation](http://docs.oracle.com/javase/8/docs/) 
and [Apache Maven](http://maven.apache.org/run-maven/index.html). 

### Prerequisites

- Install and configure Java 8 with Apache Maven 3.2.x or higher.

- Install ArcGIS Server 10.3.x

- Install GeoEvent Extension for ArcGIS Server 10.3.x

### Building
See [Building](1_building.md)

### Preparing GeoEvent for Performance testing
You will need to configure GeoEvent with the appropriate Inputs and Outputs so that the Performance Test Harness can measure the throughput.
As a quick sample, you can Import [this](GeoEventConfig-TCP.xml) GeoEvent configuration file to create a simple TCP Text Input to TCP Text Output service.

### Preparing Testing Fixtures
Create a ```fixture.xml``` file to configure the Orchestrator on how to run the performance tests.

Sample:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Fixtures>
	<Report type="CSV">
		<ReportFile>target/reports/simple_tcp.csv</ReportFile>
	</Report>
	
	<DefaultFixture>
		<DefaultSharedConfig protocol="TCP">
			<Properties>
				<Property name="hosts">rtrujillo</Property>
			</Properties>
		</DefaultSharedConfig>
		<ProducerConfig commandPort="5010">
			<Properties>
				<Property name="simulationFile">src/test/resources/simulations/county_envelopes_1000_points.csv</Property>
				<Property name="port">5565</Property>
			</Properties>
		</ProducerConfig>
		<ConsumerConfig commandPort="5020" timeoutInSec="5">
			<Properties>
				<Property name="port">5575</Property>
			</Properties>
		</ConsumerConfig>
		<Simulation>
			<TimeTest eventsPerSec="100" totalTimeInSec="10" staggeringInterval="10" />
		</Simulation>
	</DefaultFixture>
	
	<Fixture name="100 e/s" />
	
	<Fixture name="200 e/s">
		<Simulation>
			<TimeTest eventsPerSec="200"/>
		</Simulation>
	</Fixture>
	
	<Fixture name="300 e/s">
		<Simulation>
			<TimeTest eventsPerSec="300"/>
		</Simulation>
	</Fixture>
</Fixtures>
```
See [Configuration](2_configuration.md)
Now you're ready to run your first test.

### Performance Testing

You will need to start the producer and consumer first (the order does not matter), afterwards, start the orchestrator. Navigate to the ```performance-test-harness\target\main\app``` directory.

Start a TCP producer
```
run.bat -m producer -t tcp -p local
```

Start a TCP consumer
```
run.bat -m consumer -t tcp -p local
```

Start the orchestrator 
```
run.bat -f fixtures\my_fixtures.xml
```

### Reading the Results
After the tests are finish, a report file will be written out (see [Configuration](2_configuration.md) for more details). 
  
[Next - Building](1_building.md)
