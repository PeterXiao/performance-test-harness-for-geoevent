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
package com.esri.geoevent.test.performance;

import java.util.Comparator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.math.NumberUtils;
import com.esri.geoevent.test.performance.activemq.ActiveMQEventConsumer;
import com.esri.geoevent.test.performance.azure.AzureIoTHubConsumer;
import com.esri.geoevent.test.performance.bds.BdsEventConsumer;
import com.esri.geoevent.test.performance.kafka.KafkaEventConsumer;
import com.esri.geoevent.test.performance.rabbitmq.RabbitMQEventConsumer;
import com.esri.geoevent.test.performance.streamservice.StreamServiceEventConsumer;
import com.esri.geoevent.test.performance.tcp.TcpServerEventConsumer;
import com.esri.geoevent.test.performance.tcp.TcpEventConsumer;
import com.esri.geoevent.test.performance.ui.ConsumerUI;
import com.esri.geoevent.test.performance.websocket.WebsocketEventConsumer;
import com.esri.geoevent.test.performance.websocket.WebsocketServerEventConsumer;

/*
 This App listens for commands on specified port from the Ochestrator and receives messages to the target server.
 */
public class ConsumerMain {

    public static boolean DEBUG = false;

    //-------------------------------------------------------
    // Constructor
    // ------------------------------------------------------
    public ConsumerMain() {

    }

    /**
     * Main method - this is used to when running from command line
     *
     * @param args Command Line Parameters
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {

        // performer options
        Options performerOptions = new Options();
        performerOptions.addOption(OptionBuilder.withLongOpt("type").withDescription("One of the following values: [" + Protocol.getAllowableValues() + "]. (Default value is tcp).").hasArg().create("t"));
        performerOptions.addOption(OptionBuilder.withLongOpt("commandListenerPort").withDescription("The TCP Port where consumer will listen for commands from the orchestrator. (Default value is 5010).").hasArg().create("p"));
        performerOptions.addOption(OptionBuilder.withLongOpt("serverPort").withDescription("The TCP Port where the server will listen for events. (Default value is 5675)").hasArg().create("sp"));
        performerOptions.addOption("h", "help", false, "print the help message");

        // parse the command line
        CommandLineParser parser = new BasicParser();
        CommandLine cmd;

        // parse
        try {
            cmd = parser.parse(performerOptions, args, false);
        } catch (ParseException error) {
            printHelp(performerOptions);
            return;
        }

        // User requested Help 
        if (cmd.hasOption("h")) {
            printHelp(performerOptions);
            return;
        }

        if (cmd.getOptions().length == 0) {

            ConsumerUI ui = new ConsumerUI();
            ui.run();

        } else {
            // parse out the performer options
            String protocolValue = cmd.getOptionValue("t");
            String commandListenerPortValue = cmd.getOptionValue("p");

            if (protocolValue == null) {
                protocolValue = "tcp";
            }
            if (commandListenerPortValue == null) {
                commandListenerPortValue = "5020";
            }
            // validate
            if (!validateTestHarnessOptions(protocolValue, commandListenerPortValue)) {
                printHelp(performerOptions);
                return;
            }

            // parse the values
            Protocol protocol = Protocol.fromValue(protocolValue);
            boolean isLocal = "local".equalsIgnoreCase(commandListenerPortValue);
            int commandListenerPort = -1;
            if (!isLocal) {
                commandListenerPort = Integer.parseInt(commandListenerPortValue);
            }

            int serverPort = NumberUtils.toInt(cmd.getOptionValue("sp"), 5775);
            PerformanceCollector consumer;
            switch (protocol) {
                case TCP:
                    consumer = new TcpEventConsumer();
                    break;
                case TCP_SERVER:
                    consumer = new TcpServerEventConsumer(serverPort);
                    break;
                case WEBSOCKETS:
                    consumer = new WebsocketEventConsumer();
                    break;
                case WEBSOCKET_SERVER:
                    consumer = new WebsocketServerEventConsumer(serverPort);
                    break;
                case ACTIVE_MQ:
                    consumer = new ActiveMQEventConsumer();
                    break;
                case RABBIT_MQ:
                    consumer = new RabbitMQEventConsumer();
                    break;
                case STREAM_SERVICE:
                    consumer = new StreamServiceEventConsumer();
                    break;
                case KAFKA:
                    consumer = new KafkaEventConsumer();
                    break;
                case BDS:
                    consumer = new BdsEventConsumer();
                    break;
                case AZURE:
                    consumer = new AzureIoTHubConsumer();
                    break;
                default:
                    return;
            }
            consumer.listenOnCommandPort((isLocal ? 5020 : commandListenerPort), true);

        }

    }

    // -------------------------------------------------------------------------------
    // Main Helper Methods
    // -------------------------------------------------------------------------------
    private static void printHelp(Options performerOptions) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptPrefix("-");
        formatter.setArgName("value");
        formatter.setWidth(100);
        // do not sort the options in any order
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return 0;
            }
        });

        formatter.printHelp(ImplMessages.getMessage("CONSUMER_EXECUTOR_HELP_TITLE_MSG"), performerOptions, true);
        System.out.println("");
    }

    private static boolean validateTestHarnessOptions(String protocolStr, String commandListenerPort) {
        Protocol protocol = Protocol.fromValue(protocolStr);
        if (protocol == Protocol.UNKNOWN) {
            System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_PROTOCOL_VALIDATION", protocolStr, Protocol.getAllowableValues()));
            return false;
        }

        // validate the port - it could be set to local
        if (!"local".equalsIgnoreCase(commandListenerPort)) {
            try {
                Integer.parseInt(commandListenerPort);
            } catch (NumberFormatException error) {
                System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_COMMAND_PORT_VALIDATION", String.valueOf(commandListenerPort)));
                return false;
            }
        }
        return true;
    }

}
