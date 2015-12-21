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

import com.esri.geoevent.test.performance.jaxb.Fixture;
import com.esri.geoevent.test.performance.jaxb.Fixtures;
import com.esri.geoevent.test.performance.jaxb.ProvisionerConfig;
import com.esri.geoevent.test.performance.provision.DefaultProvisionerFactory;
import com.esri.geoevent.test.performance.provision.ProvisionException;
import com.esri.geoevent.test.performance.provision.Provisioner;
import com.esri.geoevent.test.performance.provision.ProvisionerFactory;
import com.esri.geoevent.test.performance.report.CSVReportWriter;
import com.esri.geoevent.test.performance.report.ReportType;
import com.esri.geoevent.test.performance.report.ReportWriter;
import com.esri.geoevent.test.performance.report.XLSXReportWriter;
import com.esri.geoevent.test.performance.statistics.FixturesStatistics;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author davi5017
 */
public class OrchestratorRunner implements RunnableComponent {

    private List<String> testNames;
    private boolean reportComplete;
    private long startTime;
    private Fixtures fixtures;

    // Runnable
    private RunningStateListener listener;
    protected AtomicBoolean running = new AtomicBoolean(false);

    public OrchestratorRunner(Fixtures fixtures) {
        this.fixtures = fixtures;
    }

    @Override
    public void start() throws RunningException {
        running = new AtomicBoolean(true);
        run();
        if (listener != null) {
            listener.onStateChange(new RunningState(RunningStateType.STARTED));
        }
    }

    @Override
    public void stop() {
        running.set(false);
        if (listener != null) {
            listener.onStateChange(new RunningState(RunningStateType.STOPPED));
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public RunningStateType getRunningState() {
        return running.get() ? RunningStateType.STARTED : RunningStateType.STOPPED;
    }

    @Override
    public void setRunningStateListener(RunningStateListener listener) {
        this.listener = listener;
    }

    /**
     * Main Test Harness Orchestrator Method
     */
    public void run() {
        // parse the xml file
        testNames = new ArrayList<String>();

        // add this runtime hook to write out whatever results we have to a report in case of exit or failures
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            long totalTime = System.currentTimeMillis() - startTime;
            writeReport(fixtures, testNames, totalTime);
        }));

        // Check the master fixtures configuration to see if we need to provision all of the test
        ProvisionerFactory provisionerFactory = new DefaultProvisionerFactory();
        try {
            ProvisionerConfig masterProvisionerConfig = fixtures.getProvisionerConfig();
            if (masterProvisionerConfig != null) {
                Provisioner provisioner = provisionerFactory.createProvisioner(masterProvisionerConfig);
                if (provisioner != null) {
                    provisioner.provision();
                }
            }
        } catch (ProvisionException error) {
            System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_PROVISIONING_ERROR"));
            error.printStackTrace();
            return;
        }

        // start
        startTime = System.currentTimeMillis();

        // process all fixtures in sequence/series
        final Fixture defaultFixture = fixtures.getDefaultFixture();
        Queue<Fixture> processingQueue = new ConcurrentLinkedQueue<Fixture>(fixtures.getFixtures());
        while (!processingQueue.isEmpty() && isRunning()) {
            Fixture fixture = processingQueue.remove();
            fixture.apply(defaultFixture);
            try {
                ProvisionerConfig fixtureProvisionerConfig = fixture.getProvisionerConfig();
                if (fixtureProvisionerConfig != null) {
                    Provisioner provisioner = provisionerFactory.createProvisioner(fixtureProvisionerConfig);
                    if (provisioner != null) {
                        provisioner.provision();
                    }
                }
            } catch (Exception error) {
                System.err.println(ImplMessages.getMessage("TEST_HARNESS_EXECUTOR_FIXTURE_PROVISIONING_ERROR", fixture.getName()));
                error.printStackTrace();
                continue;
            }

            testNames.add(fixture.getName());
            Orchestrator orchestrator = new PerformanceTestHarness(fixture);
            try {
                orchestrator.init();
                orchestrator.runTest();
            } catch (Exception error) {
                error.printStackTrace();
                orchestrator.destroy();
                orchestrator = null;
                continue;
            }

            // check if we are running and sleep accordingly
            while (orchestrator.isRunning() && isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            orchestrator = null;

            // pause for 1/2 second before continuing with the next test
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;
        // write out the report
        writeReport(fixtures, testNames, totalTime);
        //notify
        stop();
    }

    private void writeReport(final Fixtures fixtures, final List<String> testNames, long totalTestingTime) {
        if (fixtures == null || testNames.size() == 0 || reportComplete) {
            return;
        }

        // write out the report
        ReportWriter reportWriter = null;
        ReportType type = fixtures.getReport().getType();
        switch (type) {
            case XLSX:
                reportWriter = new XLSXReportWriter();
                break;
            case CSV:
            default:
                reportWriter = new CSVReportWriter();
                break;
        }

        //write the report			
        try {
            List<String> columnNames = reportWriter.getReportColumnNames(fixtures.getReport().getReportColumns(), fixtures.getReport().getAdditionalReportColumns());
            reportWriter.setMaxNumberOfReportFiles(fixtures.getReport().getMaxNumberOfReportFiles());
            reportWriter.setTotalTestingTime(totalTestingTime);
            reportWriter.writeReport(fixtures.getReport().getReportFile(), testNames, columnNames, FixturesStatistics.getInstance().getStats());
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            reportComplete = true;
        }
    }

}
