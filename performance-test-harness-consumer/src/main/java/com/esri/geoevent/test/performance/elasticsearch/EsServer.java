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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import org.json.JSONObject;

/**
 *
 * @author David Jennings
 */
public class EsServer {

    public enum HttpMethod {

        GET, DELETE, POST
    }

    private final String url;
    private final String username;
    private final String password;

    /**
     * 
     * @return set to false if error is encountered
     */
    public Boolean isOK() {
        return isOK;
    }

    /**
     * 
     * @return set to last error message
     */
    public String getLastErrorMessage() {
        return errorMessage;
    }

    private Boolean isOK;
    private String errorMessage;

    /**
     *
     * @return Host that is running elasticSearh
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     *
     * @param url url of running elasticSearh
     * @param username Username
     * @param password Password (Clear Text)
     */
    public EsServer(String url, String username, String password) {
        // User provides these values
        this.url = url;
        this.username = username;
        this.password = password;

        this.errorMessage = "OK";
        this.isOK = true;

    }

    /**
     * 
     * @return clusterName as read from ElasticServer 
     * @throws Exception 
     */
    public String getClusterName() throws Exception {
        String clusterName = null;
        // Read the clusterName from the service

        String line;

        line = request("", null, HttpMethod.GET, null);
        JSONObject json = new JSONObject(line);
        clusterName = json.getString("cluster_name");

        return clusterName;
    }

    /**
     *
     * @param path URL path to call
     * @param params null or parm and values to send with get
     * @param rm GET,POST,etc.
     * @param data to post 
     * @return null or line starting with ERROR if Exception occurs
     * @throws java.lang.Exception
     */
    public String request(String path, HashMap<String, String> params, HttpMethod rm, String data) throws Exception {

        String response;
        response = null;
        
        //System.out.println(this.url + "/" + path);

        URL url = new URL(this.url + "/" + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        String credentialString = Base64.getEncoder().encodeToString((this.username + ":" + this.password).getBytes());
        con.setRequestProperty("Authorization", "Basic " + credentialString);

        String urlParameters = "";
        // Don't believe elastic search support POST of parameters; at this time the follow code doesn't do anything (still learning)
        if (params != null) {
            Iterator it = params.values().iterator();
            while (it.hasNext()) {
                Map.Entry mentry = (Map.Entry) it.next();
                String param = mentry.getKey().toString();
                String val = mentry.getValue().toString();
                urlParameters += param + "=" + URLEncoder.encode(val, "UTF-8");
                if (it.hasNext()) {
                    urlParameters += "&";
                }
            }
        }

        switch (rm) {
            case GET:
                con.setRequestMethod("GET");
                break;
            case DELETE:
                con.setRequestMethod("DELETE");
                break;
            case POST:
                con.setRequestProperty("content-type", "application/json");
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();
                break;
        }

        int responseCode = con.getResponseCode();

        if (responseCode == 200) {
            StringBuilder sb;
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
            }

            response = sb.toString();

        } else if (responseCode >= 400 && responseCode < 500) {
            this.isOK = false;
            this.errorMessage = con.getResponseMessage();
            response = null;
        }

        return response;

    }

    /**
     *
     * @param indexNameType IndexName/IndexType if you don't specify type the
     * count will include all types
     * @return
     */
    public Integer getCount(String indexNameType) {
        Integer cnt = null;
        try {
            String line = request(indexNameType + "/_count", null, HttpMethod.GET, null);
            JSONObject json = new JSONObject(line);
            cnt = json.getInt("count");

        } catch (Exception e) {

        } finally {
            return cnt;
        }

    }
    
    public String deleteData(String indexNameType) throws Exception {
        //Example Call: deleteData(t,"/FAA-BDS/FAA-BDS");
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObject value = factory.createObjectBuilder()
                .add("query", factory.createObjectBuilder()
                        .add("match_all", factory.createObjectBuilder()))
                .build();

        //System.out.println(value.toString());
        
        String line = request(indexNameType, null, HttpMethod.DELETE, value.toString());
                
        return line;

    }   
    
    public static void main(String[] args) {
        // Test code
        EsServer es = new EsServer("http://w12ags104b.jennings.home:9220", "els_zoozhgx", "iw17z3rtxv");
        System.out.println(es.getCount("FAA-BDS/FAA-BDS"));
        try {
            System.out.println(es.deleteData("FAA-BDS/FAA-BDS"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(es.getLastErrorMessage());
        System.out.println(es.getCount("FAA-BDS/FAA-BDS"));
    }

}
