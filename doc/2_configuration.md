# Documentation

## Configuration

The Performance Test Harness uses a configuration file to configure both the producer, consumer, and orchestrator to simulate, measure, and report performance tests. In this section we will discuss all of the many different configuration options.

### Fixtures schema

The fixtures schema is a custom made schema used exclusively for the Performance Test Harness. Here is a sample of the fixtures xml configuration files:

``` xml
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
#### Report Configuration

The `<Report>` tag is used to configure  the output report. Here are the Report options:

Minimal Report Configuration
``` xml
<Report type="CSV">
  <ReportFile>target/reports/simple_tcp.csv</ReportFile>
</Report>
```

Report configuration with all available options:
``` xml
<Report type="XLSX">
	<ReportFile>target/reports/report-options.xlsx</ReportFile>
	<ReportColumns>Rate,totalEvents,successes,expectedResultCount,failures</ReportColumns>
	<AdditionalReportColumns>totalBytesConsumed,avgBytesConsumedPerMessage</AdditionalReportColumns>
</Report>
``` 

- `type`: the type of report to output.
   - The available types are: `CSV` or `XLSX`
   - This is a <b>required</b> attribute 
- `<ReportFile>`: the path to write out the report file
  - this is a <b>required</b> tag
- `<ReportColumns>`: the minimum report columns to output
  - a comma seperated list of columns to be outputted
  - this is an optional tag. When this tag is missing the [default columns](#report-columns) are used.
- `<AdditionalReportColumns>`: In addition to the default columns (or `<ReportColumns>` when specified), the columns specified here are added to to the report.
  - a comman seperated list of columns to be outputted in addition to the defaults.
  - this is an optional tag.
  
<b>NOTE:</B> Typically you will not used both optional tags `<ReportColumns>` and `<AdditionalReportColumns>` together. You should use one or the other or none. If you want to override the default columns, then you will use the  `<ReportColumns>` tag. If you want a few extra columns along with the default columns, then you will use the `<AdditionalReportColumns>` tag. If you are content with the default columns, then both of these tags are unnecessary.

##### Report Columns

Here is a list of all of the available report columns:
```
Rate, %, totalEvents, successes, failures, expectedResultCount, producerConnections, consumerConnections, minProductionTime, maxProductionTime, avgProductionTime, medProductionTime, devProductionTime, minConsumptionTime, maxConsumptionTime, avgConsumptionTime, medConsumptionTime, devConsumptionTime, minTotalTime, maxTotalTime, avgTotalTime, medTotalTime, devTotalTime, minEventsPerSec, maxEventsPerSec, avgEventsPerSec, minFirstReceivedLatency, maxFirstReceivedLatency, avgFirstReceivedLatency, medFirstReceivedLatency, devFirstReceivedLatency, minLastReceivedLatency, maxLastReceivedLatency, avgLastReceivedLatency, medLastReceivedLatency, devLastReceivedLatency, totalBytesConsumed, avgBytesConsumedPerMessage
```

Here is the list (in order) of the default columns:
```
Rate, totalEvents, successes, expectedResultCount, failures, avgTotalTime, avgEventsPerSec, %, totalBytesConsumed, avgBytesConsumedPerMessage
```

<b>TODO:</b> Describe the report columns

#### DefaultFixture and Fixture Configuration

The `<DefaultFixture>` and `<Fixture>` configuration are identical. The reason why there are two seperate tags is to eliminiate verbosity of individual tests. Everything specified in the `<DefaultFixture>` will be copied to all of the other `<Fixture>` configurations. <b>Note:</b> the `<DefaultFixture>` will not override anything specified in the `<Fixture>` configuration, it will simply fill in the missing holes.

There can ony be one `<DefaultFixture>` configuration and this tag is <b>required</b>. There must be at least one `<Fixture>` configuration.

Minimal DefaultFixture Configuration
``` xml
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
```
##### DefaultSharedConfig

This tag is used to share configuration between Producers and Consumers. Everything specified in the `<DefaultSharedConfig>` will be copied to the `<ProducerConfig>` and `<ConsumerConfig>` configurations. <b>Note:</b> the `<DefaultSharedConfig>` will not override anything specified in the `<ProducerConfig>` or `<ConsumerConfig>` configuration, it will simply fill in the missing holes.

Minimal DefaultSharedConfig Configuration
``` xml
<DefaultSharedConfig>
	<Properties>
		<Property name="hosts">rtrujillo</Property>
	</Properties>
</DefaultSharedConfig>
```
DefaultSharedConfig configuration with all available options:
``` xml
<DefaultSharedConfig protocol="TCP" host="rtrujillo" >
	<Properties>
		<Property name="hosts">rtrujillo</Property>
		<Property name="port">5565</Property>
		...
	</Properties>
</DefaultSharedConfig>
``` 

- `protocol`: the type of protocol to be used for both Consumer and Producer (may be over witten in `<ProducerConfig>` and `<ConsumerConfig>`).
   - The available types are: `TCP`, `TCP_SERVER`, `WEBSOCKETS`, `WEBSOCKET_SERVER`(Producer only), `ACTIVE_MQ`, `RABBIT_MQ`, `STREAM_SERVICE`, or `KAFKA` (Producer only).
   - This is an optional attribute
- `host`: the host where the Producer and Consumer are running from (may be over witten in `<ProducerConfig>` and `<ConsumerConfig>`).
   -  This is an optional attribute
   -  the default value is `localhost`
-  `<Property>`: each configuration can have 0 to many properties which help configure the transports accordingly. 
  
##### ProducerConfig

This tag is used to configuration the Producers. 
<b>Note:</b> Everything specified in the `<DefaultSharedConfig>` will be copied to the `<ProducerConfig>` configuration. The `<DefaultSharedConfig>` will not override anything specified in the `<ProducerConfig>` configuration, it will simply fill in the missing holes.

Minimal ProducerConfig Configuration
``` xml
<ProducerConfig>
	<Properties>
		<Property name="hosts">rtrujillo</Property>
	</Properties>
</ProducerConfig>
```
ProducerConfig configuration with all available options:
``` xml
<ProducerConfig protocol="TCP" host="rtrujillo" commandPort="5010">
	<Properties>
		<Property name="hosts">rtrujillo</Property>
		<Property name="port">5565</Property>
		...
	</Properties>
	<Producers>
		<Producer port="5010">machine1</Producer>
		<Producer port="5010">machine2</Producer>
		...
	</Producers>
</ProducerConfig>
``` 

- `protocol`: the type of protocol to be used for the Producer.
   - The available types are: `TCP`, `TCP_SERVER`, `WEBSOCKETS`, `WEBSOCKET_SERVER`, `ACTIVE_MQ`, `RABBIT_MQ`, `STREAM_SERVICE`, or `KAFKA`.
   - This is an optional attribute only if it was specified in the `<DefaultSharedConfig>`, otherwise it is required.
- `host`: the host where the Producer is running from.
   -  This is an optional attribute
   -  the default value is `localhost` if it is not specified.
- `commandPort`: the port number where the Producer will listen to commands from the orchestrator.
   -  This is an optional attribute
   -  the default value is `5010` if it is not specified.
-  `<Property>`: each configuration can have 0 to many properties which help configure the transports accordingly.
-  `<Producer>`: each Producer will define a Producer running on a remote machine.
    -  	This is an optional tag
    -  	When this tag is missing the attribute tags `host` and `commandPort` are used to define the default Producer.
    -  	When this tag is present the attribute tags `host` and `commandPort` are ignored.
 
##### ConsumerConfig

This tag is used to configuration the Consumers. 
<b>Note:</b> Everything specified in the `<DefaultSharedConfig>` will be copied to the `<ConsumerConfig>` configuration. The `<DefaultSharedConfig>` will not override anything specified in the `<ConsumerConfig>` configuration, it will simply fill in the missing holes.

Minimal ConsumerConfig Configuration
``` xml
<ConsumerConfig>
	<Properties>
		<Property name="hosts">rtrujillo</Property>
	</Properties>
</ConsumerConfig>
```
ConsumerConfig configuration with all available options:
``` xml
<ConsumerConfig protocol="TCP" host="rtrujillo" commandPort="5010" timeoutInSec="5">
	<Properties>
		<Property name="hosts">rtrujillo</Property>
		<Property name="port">5565</Property>
		...
	</Properties>
	<Consumers>
		<Consumer port="5010">machine1</Consumer>
		<Consumer port="5010">machine2</Consumer>
		...
	</Consumers>
</ConsumerConfig>
``` 

- `protocol`: the type of protocol to be used for the Consumer.
   - The available types are: `TCP`, `TCP_SERVER`, `WEBSOCKETS`, `ACTIVE_MQ`, `RABBIT_MQ`, or `STREAM_SERVICE`.
   - This is an optional attribute only if it was specified in the `<DefaultSharedConfig>`, otherwise it is required.
- `host`: the host where the Consumer is running from.
   -  This is an optional attribute
   -  the default value is `localhost` if it is not specified.
- `commandPort`: the port number where the Consumer will listen to commands from the orchestrator.
   -  This is an optional attribute
   -  the default value is `5020` if it is not specified.
- `timeoutInSec`: the number of seconds between event received before the Consumer times out.
   -  This is an optional attribute
   -  the default value is `10` if it is not specified.
-  `<Property>`: each configuration can have 0 to many properties which help configure the transports accordingly.
-  `<Consumer>`: each Consumer will define a Consumer running on a remote machine.
    -  	This is an optional tag
    -  	When this tag is missing the attribute tags `host` and `commandPort` are used to define the default Consumer.
    -  	When this tag is present the attribute tags `host` and `commandPort` are ignored.
  
