package com.esri.geoevent.test.performance.rest;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.ProducerBase;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

/**
 * Created by david on 2/17/2016.
 */



public class JsonEventProducer extends ProducerBase {

    private String urlString;
    private URL url;

    @Override
    public synchronized void init(Config config) throws TestException {
        super.init(config);

        // Allow self signed certificates
        trustAll();

        try {
            // Read url from config file and create the HTTP connection.
            urlString = config.getPropertyValue("url", "");
            url = new URL(urlString);


        } catch (Throwable error) {
            throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
        }
    }

    @Override
    public void validate() throws TestException {
        super.validate();
        if (url == null) {
            throw new TestException("HTTP connection is not established. Ensure the service endpoint is accessible from test server.");
        }
    }

    @Override
    public int sendEvents(int index, int numEventsToSend) {
        int eventIndex = index;
        for (int i = 0; i < numEventsToSend; i++) {
            if (eventIndex == events.size()) {
                eventIndex = 0;
            }
            try {
                String message = augmentMessage(events.get(eventIndex++));

                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                con.setRequestMethod("POST");

                con.setRequestProperty("Content-type", "application/json");

                // Post the Message
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(message);
                wr.flush();
                wr.close();

                int code = con.getResponseCode();

                if (code == 200) messageSent(message);
                if (running.get() == false) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return eventIndex;
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
    public void destroy() {
        super.destroy();


    }
}
