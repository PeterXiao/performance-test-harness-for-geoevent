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

import com.esri.geoevent.test.performance.activemq.ActiveMQEventProducer;
import com.esri.geoevent.test.performance.kafka.KafkaEventProducer;
import com.esri.geoevent.test.performance.rabbitmq.RabbitMQEventProducer;
import com.esri.geoevent.test.performance.streamservice.StreamServiceEventProducer;
import com.esri.geoevent.test.performance.tcp.TcpServerEventProducer;
import com.esri.geoevent.test.performance.tcp.TcpEventProducer;
import com.esri.geoevent.test.performance.ui.ProducerUI;
import com.esri.geoevent.test.performance.websocket.WebsocketEventProducer;
import com.esri.geoevent.test.performance.websocket.WebsocketServerEventProducer;

/*
 This App listens for commands on specified port from the Ochestrator and sends test messages to the target server.
 */
public class ProducerMain {

    public static boolean DEBUG = false;

    //-------------------------------------------------------
    // Constructor
    // ------------------------------------------------------
    public ProducerMain() {

    }

    //-------------------------------------------------------
    // Statics Methods
    // ------------------------------------------------------
    /**
     * Main method - this is used to when running from command line
     *
     * @param args Command Line Options
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {

        // performer options
        Options performerOptions = new Options();
        performerOptions.addOption(OptionBuilder.withLongOpt("type").withDescription("One of the following values: [" + Protocol.getAllowableValues() + "]. (Default value is tcp).").hasArg().create("t"));
        performerOptions.addOption(OptionBuilder.withLongOpt("commandListenerPort").withDescription("The TCP Port where producer will listen for commands from the orchestrator. (Default value is 5020).").hasArg().create("p"));
        performerOptions.addOption(OptionBuilder.withLongOpt("serverPort").withDescription("The TCP Port where the server will produce events. (Default value is 5665)").hasArg().create("sp"));
        performerOptions.addOption("h", "help", false, "print the help message");

        // parse the command line
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        // parse
        try {
            cmd = parser.parse(performerOptions, args, false);
        } catch (ParseException error) {
            printHelp(performerOptions);
            return;
        }

        if (cmd.getOptions().length == 0) {
            // No Args Start GUI
            ProducerUI ui = new ProducerUI();
            ui.run();
        } else {
            // User Request Help Page
            if (cmd.hasOption("h")) {
                printHelp(performerOptions);
                return;
            }

            // parse out the performer options
            String protocolValue = cmd.getOptionValue("t");
            String commandListenerPortValue = cmd.getOptionValue("p");

            if (protocolValue == null) {
                protocolValue = "tcp";
            }
            if (commandListenerPortValue == null) {
                commandListenerPortValue = "5010";
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

            int serverPort = NumberUtils.toInt(cmd.getOptionValue("sp"), 5665);
            PerformanceCollector producer = null;
            switch (protocol) {
                case TCP:
                    producer = new TcpEventProducer();
                    break;
                case TCP_SERVER:
                    producer = new TcpServerEventProducer(serverPort);
                    break;
                case WEBSOCKETS:
                    producer = new WebsocketEventProducer();
                    break;
                case ACTIVE_MQ:
                    producer = new ActiveMQEventProducer();
                    break;
                case RABBIT_MQ:
                    producer = new RabbitMQEventProducer();
                    break;
                case STREAM_SERVICE:
                    producer = new StreamServiceEventProducer();
                    break;
                case KAFKA:
                    producer = new KafkaEventProducer();
                    break;
                case WEBSOCKET_SERVER:
                    producer = new WebsocketServerEventProducer(serverPort);
                    break;
                default:
                    return;
            }
            producer.listenOnCommandPort((isLocal ? 5010 : commandListenerPort), true);

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

        formatter.printHelp(ImplMessages.getMessage("PRODUCER_EXECUTOR_HELP_TITLE_MSG"), performerOptions, true);
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
