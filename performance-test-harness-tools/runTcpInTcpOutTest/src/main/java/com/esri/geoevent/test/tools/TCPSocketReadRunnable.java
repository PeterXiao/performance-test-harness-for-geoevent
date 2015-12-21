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
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Class reads lines from a specified server and socket.  
 * The clock starts when first line is read.
 * The clock stops when specified numEvents is read
 * If output stops for 10 seconds; Times out
 * 
 * @author David Jennings
 */
public class TCPSocketReadRunnable implements Runnable {
    
    final private Long numEvents;
    final private String server;
    final private Integer port;
    final private Boolean isSingle;
    private Boolean ready = false;

    public Boolean getReady() {
        return ready;
    }
    
    private double average_read_per_second;

    private long num_events_read;

    public long getNum_events_read() {
        return num_events_read;
    }

    public double getAverage_read_per_second() {
        return average_read_per_second;
    }

    public TCPSocketReadRunnable(String server, Integer port, Long numEvents, Boolean isSingle) {
        this.server = server;
        this.port = port;
        this.numEvents = numEvents;
        this.isSingle = isSingle;
    }

    @Override
    public void run() {
        this.average_read_per_second = -1;

        
        BufferedReader br = null;
        LocalDateTime st = null;
        LocalDateTime s1 = null;
        Integer cnt = 0;
        try {
            Socket sckt = new Socket(server,port);
            BufferedReader sckt_in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
            
            // After 10 Seconds the reader will give up
            sckt.setSoTimeout(10000);
     
            this.ready = true;
            
            while (cnt < numEvents) {
                cnt += 1;
                sckt_in.readLine();
                
                if (cnt == 1) {
                    // First Event
                    st = LocalDateTime.now();
                    s1 = st;
                }
                
                if (isSingle & cnt % 1000 == 0) {
                    
                    LocalDateTime e1 = LocalDateTime.now();  

                    Duration delta = Duration.between(s1, e1);

                    Double secnds = (double) delta.getSeconds() + delta.getNano() / 1000000000.0;

                    Double rate = (double) cnt/secnds;
                    s1 = e1;
                    System.out.println(cnt + ":" + rate.toString());
                }
            }
                        
        } catch (SocketException e) {
            System.out.println(e.getMessage());
            cnt = 0;            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {

            if (st != null) {
                LocalDateTime et = LocalDateTime.now();  

                Duration delta = Duration.between(st, et);

                Double elapsed_seconds = (double) delta.getSeconds() + delta.getNano() / 1000000000.0;

                Double average_output = (double) cnt/elapsed_seconds;

                this.average_read_per_second = average_output;
                this.num_events_read = cnt;
            }
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    // OK to ignore
                }
            }
        }
        
    }
    
}
