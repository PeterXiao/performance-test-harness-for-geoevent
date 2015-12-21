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
package com.esri.geoevent.test.tools;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 *
 * @author davi5017
 */
public class RunTcpInTcpOutTest {
    private Double send_rate;
    private Double rcv_rate;

    public void send(String server, Integer port, Long numEvents, Integer rate) {
        Random rnd = new Random();
        BufferedReader br = null;
        LocalDateTime st = null;
        
        
        try {
            Socket sckt = new Socket(server, port);
            OutputStream os = sckt.getOutputStream();
            //PrintWriter sckt_out = new PrintWriter(os, true);

            Integer cnt = 0;

            st = LocalDateTime.now();

            Double ns_delay = 1000000000.0 / (double) rate;

            long ns = ns_delay.longValue();
            if (ns < 0) {
                ns = 0;
            }

            while (cnt < numEvents) {
                cnt += 1;
                LocalDateTime ct = LocalDateTime.now();
                String dtg = ct.toString();
                Double lat = 180 * rnd.nextDouble() - 90.0;
                Double lon = 360 * rnd.nextDouble() - 180.0;
                String line = "RandomPoint," + cnt.toString() + "," + dtg + ",\"" + lon.toString() + "," + lat.toString() + "\"," + cnt.toString() + "\n";

                final long stime = System.nanoTime();

                long etime = 0;
                do {
                    etime = System.nanoTime();
                } while (stime + ns >= etime);

                if (cnt % 1000 == 0) {
                    //System.out.println(cnt);
                }

                //sckt_out.write(line);
                os.write(line.getBytes());
                os.flush();

            }

            if (st != null ) {
                LocalDateTime et = LocalDateTime.now();

                Duration delta = Duration.between(st, et);

                Double elapsed_seconds = (double) delta.getSeconds() + delta.getNano() / 1000000000.0;

                send_rate = (double) numEvents / elapsed_seconds;                
            }
            


            //sckt_out.close();
            sckt.close();
            os = null;
            //sckt_out = null;

        } catch (Exception e) {
            System.err.println(e.getMessage());
            send_rate = -1.0;
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                //
            }

            this.send_rate = send_rate;

        }

    }

    private void runTest(long numberEvents, int rate, String server, int in_port, int out_port, Boolean isSingle) {

        this.send_rate = -1.0;
        this.rcv_rate = -1.0;

        try {

            TCPSocketReadRunnable reader = new TCPSocketReadRunnable(server, out_port, numberEvents, isSingle);
            Thread readerThread = new Thread(reader);

            readerThread.start();

            int secs = 0;
            while (!reader.getReady()) {
                Thread.sleep(1000);
                secs += 1;
                if (secs > 10) {
                    throw new Exception("Starting Read Thread timeout. Make sure target server is started and firewall is open for ports specified.");
                }
            }

            this.send(server, in_port, numberEvents, rate);

            readerThread.join();

            rcv_rate = reader.getAverage_read_per_second();

            if (reader.getNum_events_read() != numberEvents) {
                // Number of events read differs from send 
                throw new Exception("Number of Events received (" + Long.toString(reader.getNum_events_read()) + ") did not match what was sent (" + Long.toString(numberEvents) + ") !");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            rcv_rate = -1.0;
        } finally {
            this.rcv_rate = rcv_rate;
        }

    }

    public static void main(String args[]) {
        int numberEvents = 10000;
        //int brake = 1000;
        String server = "ags104";
        int in_port = 5565;
        int out_port = 5575;
        int start_rate = 5000;
        int end_rate = 5005;
        int rate_step = 1;

        Options opts = new Options();
        opts.addOption("n", true, "Number of Events");
        opts.addOption("s", true, "Server");
        opts.addOption("i", true, "Input TCP Port");
        opts.addOption("o", true, "Output TCP Port");
        opts.addOption("r", true, "Range");
        opts.addOption("h", false, "Help");
        try {

            CommandLineParser parser = new BasicParser();
            CommandLine cmd = null;

            try {
                cmd = parser.parse(opts, args, false);
            } catch (ParseException ignore) {
                System.err.println(ignore.getMessage());
            }

            String cmdInputErrorMsg = "";

            if (cmd.getOptions().length == 0 || cmd.hasOption("h")) {
                throw new ParseException("Show Help");
            }

            if (cmd.hasOption("n")) {
                String val = cmd.getOptionValue("n");
                try {
                    numberEvents = Integer.valueOf(val);
                } catch (NumberFormatException e) {
                    cmdInputErrorMsg += "Invalid value for n. Must be integer.\n";
                }
            }

            if (cmd.hasOption("s")) {
                server = cmd.getOptionValue("s");
            }

            if (cmd.hasOption("i")) {
                String val = cmd.getOptionValue("i");
                try {
                    in_port = Integer.valueOf(val);
                } catch (NumberFormatException e) {
                    cmdInputErrorMsg += "Invalid value for i. Must be integer.\n";
                }
            }

            if (cmd.hasOption("o")) {
                String val = cmd.getOptionValue("o");
                try {
                    out_port = Integer.valueOf(val);
                } catch (NumberFormatException e) {
                    cmdInputErrorMsg += "Invalid value for o. Must be integer.\n";
                }
            }

            if (cmd.hasOption("r")) {
                String val = cmd.getOptionValue("r");
                try {
                    String parts[] = val.split(",");
                    if (parts.length == 3) {
                        start_rate = Integer.parseInt(parts[0]);
                        end_rate = Integer.parseInt(parts[1]);
                        rate_step = Integer.parseInt(parts[2]);
                    } else if (parts.length == 1) {
                        // Run single rate
                        start_rate = Integer.parseInt(parts[0]);
                        end_rate = start_rate;
                        rate_step = start_rate;
                    } else {
                        throw new ParseException("Rate must be three comma seperated values or a single value");
                    }
                    
                    
                } catch (ParseException e) {
                    cmdInputErrorMsg += e.getMessage();
                } catch (NumberFormatException e) {
                    cmdInputErrorMsg += "Invalid value for r. Must be integers.\n";
                }
            }

            if (!cmdInputErrorMsg.equalsIgnoreCase("")) {
                throw new ParseException(cmdInputErrorMsg);
            }
            DecimalFormat df = new DecimalFormat("##0");
            RunTcpInTcpOutTest t = new RunTcpInTcpOutTest();

            if (start_rate == end_rate) {
                // Single Rate Test
                System.out.println("*********************************");
                System.out.println("Incremental testing at requested rate: " + start_rate);
                System.out.println("Count,Incremental Rate");
                
                t.runTest(numberEvents, start_rate, server, in_port, out_port, true);                
                if (t.send_rate < 0 || t.rcv_rate < 0) {
                    throw new Exception("Test Run Failed!");
                }
                System.out.println("Overall Average Send Rate, Received Rate");
                System.out.println(df.format(t.send_rate) + "," + df.format(t.rcv_rate));
                
            } else {
                System.out.println("*********************************");
                System.out.println("rateRqstd,avgSendRate,avgRcvdRate");


                //for (int rate: rates) {
                for (int rate = start_rate; rate <= end_rate; rate += rate_step) {
                    
                    t.runTest(numberEvents, rate, server, in_port, out_port, false);
                    if (t.send_rate < 0 || t.rcv_rate < 0) {
                        throw new Exception("Test Run Failed!");
                    }
                    System.out.println(Integer.toString(rate) + "," + df.format(t.send_rate) + "," + df.format(t.rcv_rate));
                }
                
            }

        } catch (ParseException e) {
            System.out.println("Invalid Command Options: ");
            System.out.println(e.getMessage());
            System.out.println("Command line options: -n NumEvents -s Server -i InputPort -o OutputPort -r StartRate,EndRate,Step");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    
}
