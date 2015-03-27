# Documentation

## Running

The Performance Test Harness has a few configuration options that can be passed in at runtime. These options are used to launch the producer, consumer, or orchestrator with different modes. In this section we will discuss all of the many different runtime options.

### Options

Both Producer and Consumer have similar runtime options. All of the options must be prefixed with a `-`. The following section describes these options.

- `m` or `mode`: Mode option. This tells the Performance Test Harness which component to run (either a Producer or Consumer).
  - The available options are: `producer` or `consumer`
  - This is a <b>required</b> option 
  - Example: 
    - Producer: `java -jar Performance-Test-Harness-10.3.0.jar -m producer -t tcp`
    - Consumer: `java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t tcp`
- `t` or `type`: The type of transport to run (ex: TCP, WebSockets, etc.)
  - The available options are: `tcp`,`tcp_server`, `websockets`, `websocket_server`,  `stream_service`, `rabbit_mq`, `active_mq` and `kafka` (Producer only).
  - This is a <b>required</b> option
  - Example: 
    - TCP: `java -jar Performance-Test-Harness-10.3.0.jar -m producer -t tcp`
    - TCP Server: `java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t tcp_server -sp 8888`
    - WebSockets: `java -jar Performance-Test-Harness-10.3.0.jar -m producer -t websockets`
    - WebSocket Server: `java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t websocket_server -sp 8888`
    - Stream Service: `java -jar Performance-Test-Harness-10.3.0.jar -m producer -t stream_service`
    - RabbitMQ: `java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t rabbit_mq`
    - ActiveMQ: `java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t active_mq`
    - Kafka: `java -jar Performance-Test-Harness-10.3.0.jar -m producer -t kafka`
- `p` or `commandListenerPort`: The port where the Consumer or Producer will listen to commands from the Orchestrator.
  - A numeric value is expected. Ex: `5555`.
  - This is a optional option.  
  - The default values are:
    - Producer: `5010`
    - Consumer: `5020`
  - Example: 
    - Producer: `java -jar Performance-Test-Harness-10.3.0.jar -m producer -t tcp -p 5555`
    - Consumer: `java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t tcp -p 6666`
- `sp` or `serverPort`: The port where the server transport will be running on. <b>Note</b> this is only used for the following types or transports: `tcp_server` or `websocket_server`.
  - A numeric value is expected. Ex: `7777`.
  - This is a optional option.  
  - The default values are:
    - Producer: `5665`
    - Consumer: `5775`
  - Example: 
    - Producer: `java -jar Performance-Test-Harness-10.3.0.jar -m producer -t tcp_server -sp 8888`
    - Consumer: `java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t websocket_server -sp 9999`

#### Producer

In order to start a TCP Producer, just execute the following command:

`java -jar Performance-Test-Harness-10.3.0.jar -m producer -t tcp`

#### Consumer

In order to start a TCP Consumer, just execute the following command:

`java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t tcp`

### Orchestrator

The orchestrator just has one option it requires. The orchestrator looks for the `f` option.
- `f` or `fixtures`: Fixtures option. This tells the Performance Test Harness to start the Orchestrator and use the value of this option as the main fixtures configuration file to execute Performance Tests.
  - The value must point to a valid `fixtures.xml` file.
  - This is a <b>required</b> option 
  - Example:  `java -jar Performance-Test-Harness-10.3.0.jar -f fixtures\myfixtures.xml`

In order to start the Orchestrator, just execute the following command:

`java -jar Performance-Test-Harness-10.3.0.jar -f fixtures\myfixtures.xml`

[Next - Results](4_results.md)
