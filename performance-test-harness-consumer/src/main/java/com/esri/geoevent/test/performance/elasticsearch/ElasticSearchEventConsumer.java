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
package com.esri.geoevent.test.performance.elasticsearch;

import com.esri.arcgis.discovery.util.KeyUtil;
import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;


/**
 *
 * @author davi5017
 */
public class ElasticSearchEventConsumer extends ConsumerBase {

    private String esUrl;
    private String username;
    private String passwordEncrypted;
    private String password;
    private String indexName;
    private String indexType;
    
    boolean firstChange;
    
    int startCount;
    int curCount;
    int endCount;
    
    long timeLastChange;
    long timeLast;
    
    EsServer es;
    
    final long timoutInSec = 10;
    
   
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
            
            esUrl = config.getPropertyValue("esUrl");           
            username = config.getPropertyValue("username");
            passwordEncrypted = config.getPropertyValue("password");
            //Note: I had to install arcgis-common which is part of GeoEvent install
            password = KeyUtil.decryptKey(this.passwordEncrypted);
            indexName = config.getPropertyValue("indexName");
            indexType = config.getPropertyValue("indexType");            
            
            // Get Start Count from BDS.
            es = new EsServer(this.esUrl, this.username, this.password);
            startCount = es.getCount(indexName + "/" + indexType);
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
        int cnt = es.getCount(indexName + "/" + indexType);
        
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
        // It any of these inputs are not provided throw an error 
        try {            
            es.getClusterName();
        } catch (Exception e) {
            throw new TestException("Could not connect to Elastic Search Server."); 
        }
        
        
    }
    
    
}
