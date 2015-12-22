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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author davi5017
 */
public class GetMapServiceCountRunnable implements Runnable {
    
    final private int sampleInterval = 1000;    
    final private Long numEvents;
    final private Boolean isSingle;

    // Example . http://w12ags104a.jennings.home/arcgis/rest/services/Hosted/FAA-Stream/MapServer/0
    // /query?where=1%3D1&returnCountOnly=true&f=pjson
    final private String msLayerUrl;
    // Assumes that Portal has granted Everyone access to the Map Service so that no username/password is required

    public GetMapServiceCountRunnable(Long numEvents, String msLayerUrl, Boolean isSingle) {
        this.numEvents = numEvents;
        this.msLayerUrl = msLayerUrl;
        this.isSingle = isSingle;
    }
    
    private Boolean ready = false;
    private double average_read_per_second;    
    private long num_events_read;
    
    public Boolean getReady() {
        return ready;
    }
    
    public double getAverage_read_per_second() {
        return average_read_per_second;
    }
    
    public long getNum_events_read() {
        return num_events_read;
    }
    
    private int getMsLayerCount(String url) {
        int cnt = -1;
        
        try {
            URL obj = new URL(url + "/query");
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            
            con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Accept", "text/plain");
            
            String urlParameters
                    = "where=" + URLEncoder.encode("1=1", "UTF-8")
                    + "&returnCountOnly=" + URLEncoder.encode("true", "UTF-8")
                    + "&f=" + URLEncoder.encode("json", "UTF-8");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            String jsonString = response.toString();
            
            JSONTokener tokener = new JSONTokener(jsonString);
            
            JSONObject root = new JSONObject(tokener);
            
            cnt = root.getInt("count");
            
        } catch (Exception e) {
            cnt = -2;
        } finally {
            return cnt;
        }
        
    }    
    
    private void trustAll() {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                
                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
        
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
            System.out.println("Oops");
        }
    }    
    
    @Override
    public void run() {

        // Trust https urls 
        trustAll();

        // Get start count
        int stCnt = -3;
        stCnt = getMsLayerCount(this.msLayerUrl);
        int cnt1 = stCnt;
        
        if (stCnt < 0) {
            throw new UnsupportedOperationException("Couldn't get start count from BDS");
        }

        // Wait for count to increase by the right number of events
        int curCnt = -3;
        
        curCnt = getMsLayerCount(this.msLayerUrl);
        
        if (curCnt < 0) {
            throw new UnsupportedOperationException("Couldn't get count from BDS");
        }

        // if count stop increase for 30 seconds then exit
        int newCnt = curCnt;
        
        LocalDateTime et = LocalDateTime.now();
        LocalDateTime st = LocalDateTime.now();
        Boolean firstCountChange = true;
        
        LocalDateTime s1 = LocalDateTime.now();
        LocalDateTime s2 = LocalDateTime.now();
        
        int sampleRateCnt = stCnt + sampleInterval;
        
        while (curCnt < stCnt + numEvents) {
            newCnt = getMsLayerCount(this.msLayerUrl);

            //System.out.println(newCnt);
            if (newCnt < 0) {
                System.out.println("Couldn't get count from BDS");
            }
            
            if (newCnt > curCnt) {
                if (firstCountChange) {
                    sampleRateCnt = newCnt + sampleInterval;
                    st = LocalDateTime.now();
                    s1 = st;
                    firstCountChange = false;
                }
                curCnt = newCnt;
                et = LocalDateTime.now();
            }
            
            LocalDateTime et2 = LocalDateTime.now();
            Duration delta = Duration.between(et, et2);
            
            Double elapsed_seconds = (double) delta.getSeconds() + delta.getNano() / 1000000000.0;
            
            if (isSingle && curCnt > sampleRateCnt) {
                // Calculate the rate for the sample rates
                s2 = LocalDateTime.now();
                Duration tm = Duration.between(s1, s2);
                Double secnds = (double) tm.getSeconds() + tm.getNano() / 1000000000.0;
                int cntChg = curCnt - cnt1;
                cnt1 = curCnt;
                Double rt = (double) cntChg / secnds;
                sampleRateCnt = cnt1 + sampleInterval;
                s1 = s2;                
                System.out.println(curCnt - stCnt + "," + rt);
                if (rt < 200.0) {
                    this.num_events_read = curCnt - stCnt;
                    throw new UnsupportedOperationException("Rate has dropped below 200 e/s");
                }
            }
            
            if (elapsed_seconds > 30.0) {
                // count hasn't changed for 30 seconds
                System.out.println("Features lost");
                System.out.println(curCnt);
                break;
            }
            
            try {
                // This delay was added to prevent calls from overloading map service
                Thread.sleep(100);                
              
            } catch (InterruptedException ex) {
                Logger.getLogger(GetMapServiceCountRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (st != null) {
            et = LocalDateTime.now();
            
            Duration delta = Duration.between(st, et);
            
            Double elapsed_seconds = (double) delta.getSeconds() + delta.getNano() / 1000000000.0;
            
            int eventsRcvd = curCnt - stCnt;
            //System.out.println("Events received: " + eventsRcvd);
            this.average_read_per_second = (double) eventsRcvd / elapsed_seconds;
            this.num_events_read = eventsRcvd;
        }        
    }
    
}
