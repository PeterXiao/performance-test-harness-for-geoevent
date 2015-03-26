# Documentation

## Running

The Performance Test Harness has a few configuration options that can be passed in at runtime. These options are used to launch the producer, consumer, or orchestrator with different modes. In this section we will discuss all of the many different runtime options.

### Producer

In order to start a TCP Producer, just execute the following command:

`java -jar Performance-Test-Harness-10.3.0.jar -m producer -t tcp`

### Consumer

In order to start a TCP Consumer, just execute the following command:

`java -jar Performance-Test-Harness-10.3.0.jar -m consumer -t tcp`

### Orchestrator

In order to start the Orchestrator, just execute the following command:

`java -jar Performance-Test-Harness-10.3.0.jar -f fixtures\myfixtures.xml

[Next - Results](4_results.md)
