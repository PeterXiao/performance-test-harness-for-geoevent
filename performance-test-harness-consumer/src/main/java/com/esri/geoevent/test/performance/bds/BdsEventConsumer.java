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
package com.esri.geoevent.test.performance.bds;


import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
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
public class BdsEventConsumer extends ConsumerBase {
    
    private String msLayerUrl;
    
    boolean firstChange;
    
    int startCount;
    int curCount;
    int endCount;
    
    long timeLastChange;
    long timeLast;

    
    final long timoutInSec = 10;

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
        endCount = startCount + numberOfExpectedResults;
        firstChange = true;
        super.run();
    }
    
    @Override
    public synchronized void init(Config config) throws TestException {
        try {
            super.init(config);
            
            msLayerUrl = config.getPropertyValue("msLsyerUrl");           
         
            
            // Get Start Count from BDS.
            trustAll();
            
            startCount = getMsLayerCount(msLayerUrl);
            if (startCount < 0) {
                // Wait a second and try again
                Thread.sleep(1000);
                startCount = getMsLayerCount(msLayerUrl);
            }
            timeLast = System.currentTimeMillis();
            curCount = startCount;
            timeLastChange = timeLast;
            
            
            //System.out.println("OK");
            
        } catch (Throwable error) {
            throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
        }
    }    
    
    @Override
    public void receive(String message) {
        
        int cnt = getMsLayerCount(this.msLayerUrl);
        if (cnt < 0) {
            try {
                // Wait a second and try again
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BdsEventConsumer.class.getName()).log(Level.SEVERE, null, ex);
            }
            cnt = getMsLayerCount(this.msLayerUrl);
        }
        
        
        int cntChange = cnt - curCount;       
        
        if (cntChange > 0) {      
            timeLastChange = System.currentTimeMillis();
            if (firstChange) {                
                // The first message actually happend sometime between last cnt and this one
                // We'll estimate by setting start time to mid point between last and current
                setStartTime((timeLastChange + timeLast)/2);
                firstChange = false;
            }
            successfulEvents.set(cnt - startCount);
            curCount = cnt;
        } else {
            timeLast = System.currentTimeMillis();
        }
        
        if (timeLast - timeLastChange > timoutInSec * 1000) {
            System.out.println(ImplMessages.getMessage("CONSUMER_TIMEOUT_MSG"));
            finishConsuming(timeLastChange);            
            
        }
        
        if (cnt == endCount) {
            finishConsuming(System.currentTimeMillis());
        }            
    }    
    
    @Override
    public String pullMessage() {
        /* Get the count from BDS */
        //System.out.println(System.currentTimeMillis());
        return "Get Count";
    }    
        
    
    @Override
    public void validate() throws TestException {
        try {            
            int cnt = getMsLayerCount(this.msLayerUrl);
            if (cnt < 0) {
                // Wait a second and try again
                Thread.sleep(1000);
                cnt = getMsLayerCount(this.msLayerUrl);
            }
            if (cnt < 0) throw new Exception("Get Count Failed");
        } catch (Exception e) {
            throw new TestException("Could to get count from Map Server."); 
        }    }
    
}
