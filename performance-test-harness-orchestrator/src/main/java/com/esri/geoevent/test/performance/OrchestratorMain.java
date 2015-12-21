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

import com.esri.geoevent.test.performance.jaxb.Fixtures;
import com.esri.geoevent.test.performance.ui.OrchestratorUI;
import java.io.File;
import java.util.Comparator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public class OrchestratorMain {

    public static boolean DEBUG = false;

    /**
     * Main method - this is used to when running from command line
     *
     * @param args Command Line Arguments
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        // TODO: Localize the messages
        // test harness options
        Options testHarnessOptions = new Options();
        testHarnessOptions.addOption(OptionBuilder.withLongOpt("fixtures").withDescription("The fixtures xml file to load and configure the performance test harness.").hasArg().create("f"));
        testHarnessOptions.addOption("h", "help", false, "print the help message");

        // parse the command line
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(testHarnessOptions, args, false);
        } catch (ParseException ignore) {
            printHelp(testHarnessOptions);
            return;
        }

        if (cmd.getOptions().length == 0) {
            OrchestratorUI ui = new OrchestratorUI();
            ui.run(args);
        } else {
            if (cmd.hasOption("h")) {
                printHelp(testHarnessOptions);
                return;
            }

            if (cmd.hasOption("h") || !cmd.hasOption("f")) {
                printHelp(testHarnessOptions);
                return;
            }

            String fixturesFilePath = cmd.getOptionValue("f");
            // validate
            if (!validateFixturesFile(fixturesFilePath)) {
                printHelp(testHarnessOptions);
                return;
            }

            // parse the xml file
            final Fixtures fixtures;
            try {
                fixtures = fromXML(fixturesFilePath);
            } catch (JAXBException error) {
                System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_CONFIG_ERROR", fixturesFilePath));
                error.printStackTrace();
                return;
            }

            // run the test harness
            try {
                OrchestratorRunner executor = new OrchestratorRunner(fixtures);

                executor.start();
            } catch (RunningException error) {
                error.printStackTrace();
            }

        }

    }

    // -------------------------------------------------------------------------------
    // Main Helper Methods
    // -------------------------------------------------------------------------------
    private static void printHelp(Options testHarnessOptions) {
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

        formatter.printHelp(ImplMessages.getMessage("ORCHESTRATOR_EXECUTOR_HELP_TITLE_MSG"), testHarnessOptions, true);
        System.out.println("");

    }

    private static boolean validateFixturesFile(String fixturesFilePath) {
        if (StringUtils.isEmpty(fixturesFilePath)) {
            System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_FIXTURE_VALIDATION", fixturesFilePath));
            return false;
        }
        File fixturesFile = new File(fixturesFilePath);
        if (!fixturesFile.exists() || fixturesFile.isDirectory()) {
            System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_FIXTURE_FILE_VALIDATION", fixturesFilePath));
            return false;
        }
        return true;
    }

    // -------------------------------------------------------
    // Conversion Methods
    // ------------------------------------------------------
    private static Fixtures fromXML(String xmlLocation) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Fixtures.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StreamSource xml = new StreamSource(xmlLocation);
        return (Fixtures) unmarshaller.unmarshal(xml);
    }
}
