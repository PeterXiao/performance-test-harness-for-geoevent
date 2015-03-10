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
  - this is an optional tag. When this tag is missing the [default columns](#Report Columns) are used.
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
