///*
// Copyright 1995-2015 Esri
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// For additional information, contact:
// Environmental Systems Research Institute, Inc.
// Attn: Contracts Dept
// 380 New York Street
// Redlands, California, USA 92373
//
// email: contracts@esri.com
// */
//package com.esri.geoevent.test.tools;
//
//import com.esri.arcgis.discovery.util.KeyUtil;
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.FileReader;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.Socket;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.security.GeneralSecurityException;
//import java.security.cert.X509Certificate;
//import java.text.DecimalFormat;
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Base64;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import org.apache.commons.cli.BasicParser;
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//import org.json.JSONObject;
//
///**
// *
// * @author davi5017
// */
//public class RunTcpInEsOutTest {
//
//    private Double send_rate;
//    private Double rcv_rate;
//
//
//    public void send(String server, Integer port, Long numEvents, Integer rate, String data_file, String bdsUrl) {
//
//        BufferedReader br = null;
//        ArrayList<String> lines = new ArrayList<>();
//        LocalDateTime st = null;
//
//        try {
//
//            // Read the file into String array
//            br = new BufferedReader(new FileReader(data_file));
//
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                lines.add(line);
//            }
//
//            Socket sckt = new Socket(server, port);
//            OutputStream os = sckt.getOutputStream();
//
//            Integer cnt = 0;
//
//            st = LocalDateTime.now();
//
//            Double ns_delay = 1000000000.0 / (double) rate;
//
//            long ns = ns_delay.longValue();
//            if (ns < 0) {
//                ns = 0;
//            }
//
//            int i = 0;
//            int j = 0;
//
//            while (i < numEvents) {
//                i++;
//                j++;
//                if (j >= lines.size()) {
//                    j = 0;
//                }
//                line = lines.get(j) + "\n";
//
//                final long stime = System.nanoTime();
//
//                long etime = 0;
//                do {
//                    etime = System.nanoTime();
//                } while (stime + ns >= etime);
//
//                os.write(line.getBytes());
//                os.flush();
//
//            }
//
//            LocalDateTime et = LocalDateTime.now();
//            if (st != null) {
//                et = LocalDateTime.now();
//
//                Duration delta = Duration.between(st, et);
//
//                Double elapsed_seconds = (double) delta.getSeconds() + delta.getNano() / 1000000000.0;
//
//                send_rate = (double) numEvents / elapsed_seconds;
//            }
//
//            sckt.close();
//            os = null;
//            
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//            send_rate = -1.0;
//        } finally {
//            try {
//                br.close();
//            } catch (Exception e) {
//                //
//            }
//
//            this.send_rate = send_rate;
//
//        }
//
//    }
//
//    private void runTest(long numberEvents, int rate, String server, int in_port, String data_file, String bdsUrl, String bdsUsername, String bdsEncryptedPassword, Boolean isSingle) {
//
//        this.send_rate = -1.0;
//        this.rcv_rate = -1.0;
//
//        try {
//
//            GetESCountRunnable reader = new GetESCountRunnable(numberEvents, bdsUrl, bdsUsername, bdsEncryptedPassword, isSingle);
//            Thread readerThread = new Thread(reader);
//
//            readerThread.start();            
//            
//            this.send(server, in_port, numberEvents, rate, data_file, bdsUrl);
//            
//            readerThread.join();
//            
//            rcv_rate = reader.getAverage_read_per_second();
//            
//            if (reader.getNum_events_read() != numberEvents) {
//                // Number of events read differs from send 
//                throw new Exception("Number of Events received (" + Long.toString(reader.getNum_events_read()) + ") did not match what was sent (" + Long.toString(numberEvents) + ") !");
//            }            
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            rcv_rate = -1.0;
//        } finally {
//            this.rcv_rate = rcv_rate;
//        }
//
//    }
//
//    public static void main(String args[]) {
//        
//        //-n 10000 -g c7hp.jennings.home -i 5565 -d c7hp.jennings.home -u els_v0w19nm -e {crypt}qRQJuv8T+zo6PFoNpyJrMQ== -s FAA-Stream -f D:\github\performance-test-harness-for-geoevent\app\simulations\faa-stream.csv -r 1000,3000,1000
//        //-n 10000 -g w12ags104a.jennings.home -i 5565 -d w12ags104a.jennings.home -u els_n8secvw -e {crypt}eQnEBupnm7WMrM2bNFyQKw== -s FAA-Stream -f D:\github\performance-test-harness-for-geoevent\app\simulations\faa-stream.csv -r 1000,3000,1000
//        int numberEvents = 10000; // Number of Events    
//        String gisServer = "w12ags104a.jennings.home"; // GIS Server
//        int inputTcpPort = 5565;  // TCP Input Port        
//
//        String dataServer = "w12ags104b.jennings.home"; // Data Server
//        String bdsUsername = "els_ro5fnhw";  // BDS Username
//        String bdsEncryptedPassword = "{crypt}VovBzQGONbtJnPw02rmmag=="; // BDS Encrypted Password
//        String dataSourceName = "FAA-Stream"; // Data Source Name (GeoEvent)                
//
//        String EventsInputFile = "D:\\github\\performance-test-harness-for-geoevent\\app\\simulations\\faa-stream.csv"; // Events input File        
//
//        int start_rate = 5000;
//        int end_rate = 5005;
//        int rate_step = 1;
//
//        Options opts = new Options();
//        opts.addOption("n", true, "Number of Events");
//        opts.addOption("g", true, "GIS Server");
//        opts.addOption("i", true, "Input TCP Port");
//        opts.addOption("d", true, "Data Server");
//        opts.addOption("u", true, "BDS Username");
//        opts.addOption("e", true, "BDS Encrypted Password");
//        opts.addOption("s", true, "GeoEvent Data Source Name");
//        opts.addOption("f", true, "File with GeoEvents to Send");
//        opts.addOption("r", true, "Rates to test Start,End,Step");
//        opts.addOption("h", false, "Help");
//
//        try {
//
//            CommandLineParser parser = new BasicParser();
//            CommandLine cmd = null;
//
//            try {
//                cmd = parser.parse(opts, args, false);
//            } catch (org.apache.commons.cli.ParseException ignore) {
//                System.err.println(ignore.getMessage());
//            }
//
//            String cmdInputErrorMsg = "";
//
//            if (cmd.getOptions().length == 0 || cmd.hasOption("h")) {
//                throw new org.apache.commons.cli.ParseException("Show Help");
//            }
//
//            if (cmd.hasOption("n")) {
//                String val = cmd.getOptionValue("n");
//                try {
//                    numberEvents = Integer.valueOf(val);
//                } catch (NumberFormatException e) {
//                    cmdInputErrorMsg += "Invalid value for n. Must be integer.\n";
//                }
//            }
//
//            if (cmd.hasOption("g")) {
//                gisServer = cmd.getOptionValue("g");
//            }
//
//            if (cmd.hasOption("i")) {
//                String val = cmd.getOptionValue("i");
//                try {
//                    inputTcpPort = Integer.valueOf(val);
//                } catch (NumberFormatException e) {
//                    cmdInputErrorMsg += "Invalid value for i. Must be integer.\n";
//                }
//            }
//
//            if (cmd.hasOption("d")) {
//                dataServer = cmd.getOptionValue("d");
//            }
//
//            if (cmd.hasOption("u")) {
//                bdsUsername = cmd.getOptionValue("u");
//            }
//
//            if (cmd.hasOption("e")) {
//                bdsEncryptedPassword = cmd.getOptionValue("e");
//            }
//
//            if (cmd.hasOption("s")) {
//                dataSourceName = cmd.getOptionValue("s");
//            }
//
//            if (cmd.hasOption("f")) {
//                EventsInputFile = cmd.getOptionValue("f");
//            }
//
//            if (cmd.hasOption("r")) {
//                String val = cmd.getOptionValue("r");
//                try {
//                    String parts[] = val.split(",");
//                    if (parts.length == 3) {
//                        start_rate = Integer.parseInt(parts[0]);
//                        end_rate = Integer.parseInt(parts[1]);
//                        rate_step = Integer.parseInt(parts[2]);
//                    } else if (parts.length == 1) {
//                        // Run single rate
//                        start_rate = Integer.parseInt(parts[0]);
//                        end_rate = start_rate;
//                        rate_step = start_rate;
//                    } else {
//                        throw new org.apache.commons.cli.ParseException("Rate must be three comma seperated values or a single value");
//                    }
//
//                } catch (org.apache.commons.cli.ParseException e) {
//                    cmdInputErrorMsg += e.getMessage();
//                } catch (NumberFormatException e) {
//                    cmdInputErrorMsg += "Invalid value for r. Must be integers.\n";
//                }
//            }
//
//            if (!cmdInputErrorMsg.equalsIgnoreCase("")) {
//                throw new org.apache.commons.cli.ParseException(cmdInputErrorMsg);
//            }
//
//            // Assuming the ES port is 9220 
//            String bdsURL = "http://" + dataServer + ":9220/" + dataSourceName + "/" + dataSourceName;
//            RunTcpInEsOutTest t = new RunTcpInEsOutTest();
//            DecimalFormat df = new DecimalFormat("##0");
//
//            if (start_rate == end_rate) {
//                // Single Rate Test
//                System.out.println("*********************************");
//                System.out.println("Incremental testing at requested rate: " + start_rate);
//                System.out.println("Count,Incremental Rate");
//                
//                t.runTest(numberEvents, start_rate, gisServer, inputTcpPort, EventsInputFile, bdsURL, bdsUsername, bdsEncryptedPassword, true);                
//                if (t.send_rate < 0 || t.rcv_rate < 0) {
//                    throw new Exception("Test Run Failed!");
//                }
//                System.out.println("Overall Average Send Rate, Received Rate");
//                System.out.println(df.format(t.send_rate) + "," + df.format(t.rcv_rate));
//
//            
//            } else {
//                System.out.println("*********************************");
//                System.out.println("rateRqstd,avgSendRate,avgRcvdRate");            
//
//
//                for (int rate = start_rate; rate <= end_rate; rate += rate_step) {
//
//                    t.runTest(numberEvents, rate, gisServer, inputTcpPort, EventsInputFile, bdsURL, bdsUsername, bdsEncryptedPassword, false);                
//                    if (t.send_rate < 0 || t.rcv_rate < 0) {
//                        throw new Exception("Test Run Failed!");
//                    }
//                    System.out.println(Integer.toString(rate) + "," + df.format(t.send_rate) + "," + df.format(t.rcv_rate));
//                    Thread.sleep(3 * 1000);
//                }
//                
//            }
//            
//            
//
//        } catch (ParseException e) {
//            System.out.println("Invalid Command Options: ");
//            System.out.println(e.getMessage());
//            System.out.println("Command line options: -n NumberOfEvents -g GISServer -i InputTCPPort -d DataServer -u BDSUsername -e BDSEncryptedPassword -s GeoevetDataSourceName -f FileWithEvents -r StartRate,EndRate,Step");
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//
//    }
//
//}
