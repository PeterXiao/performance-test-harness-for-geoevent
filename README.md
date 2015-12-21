# Performance Test Harness for GeoEvent

The Performance Test Harness is an application which measures the throughput performance for ArcGIS GeoEvent Extension for Server. This application can be run via the command line or as a JavaFX application (In progress).

![App](performance-test-harness.png?raw=true)

## Features
- Available Transports:
 - TCP client and server.
 - WebSockets client and server.
 - [Stream Services](https://server.arcgis.com/en/geoevent-extension/latest/process-event-data/stream-services.htm).
 - [RabbitMQ](https://www.rabbitmq.com/).
 - [Kafka](http://kafka.apache.org/) (Producing only).
 - [ActiveMQ](http://activemq.apache.org/).
- Support for Text(csv) and JSON simulation files.
- Reports output in CSV or XLSX formats.

## Requirements

* ArcGIS 10.4.x GeoEvent Extension for Server.
* Java JDK 1.8 or greater.
* Maven 3.2.x or greater.

## Building
See [Building](doc/1_building.md)
 
## Documentation
- [Quick-start guide](doc/0_quick_start.md)
- [Building](doc/1_building.md)
- [Configuration](doc/2_configuration.md)
- [Running](doc/3_running.md)
- [Results](doc/4_results.md)

## Resources

* [GeoEvent gallery item](http://www.arcgis.com/home/item.html?id=) on the ArcGIS GeoEvent Extension Gallery
* [ArcGIS GeoEvent Extension for Server Resources](http://links.esri.com/geoevent)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2015 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt?raw=true) file.

[](ArcGIS, GeoEvent, Processor)
[](Esri Tags: ArcGIS GeoEvent Extension for Server)
[](Esri Language: Java)
