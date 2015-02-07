package com.esri.ges.test.performance.featureservice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.esri.ges.test.performance.Mode;

public class FeatureServiceProducer
{
  private String featureLayerUrl = null;
  private int[] objectIds = null;

  public FeatureServiceProducer(String featureLayerUrl)
  {
    this.featureLayerUrl = featureLayerUrl;
  }

  public long addFeatures(int numberOfEvents)
  {
    return produce(numberOfEvents, false);
  }
  
  public long updateFeatures(int numberOfEvents)
  {
    return produce(numberOfEvents, true);
  }
  
  public long deleteFeatures()
  {
    long duration = 0l;
    CloseableHttpClient httpClient = HttpClients.createDefault();
    String url = featureLayerUrl + "/deleteFeatures";
    HttpPost httpPost = new HttpPost(url);
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    nvps.add(new BasicNameValuePair("f", "pjson"));
    nvps.add(new BasicNameValuePair("where", "1=1"));
    nvps.add(new BasicNameValuePair("rollbackOnFailure", "true"));
    try
    {
      httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
      Date start = new Date();
      HttpResponse response = httpClient.execute(httpPost);
      duration = new Date().getTime() - start.getTime();
      if (!response.getStatusLine().toString().equals("HTTP/1.1 200 OK"))
        System.out.println("ERROR");
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
    catch (ClientProtocolException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        httpClient.close();
      }
      catch( Throwable t )
      {
        t.printStackTrace();
      }
    }
    
    return duration;
  }
  
  private long produce(int numberOfEvents, boolean update)
  {
    long duration = 0l;
    CloseableHttpClient httpClient = HttpClients.createDefault();
    String url = featureLayerUrl + ((update) ? "/updateFeatures" : "/addFeatures");
    HttpPost httpPost = new HttpPost(url);
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    nvps.add(new BasicNameValuePair("f", "pjson"));
    nvps.add(new BasicNameValuePair("features", createFeatures(numberOfEvents, update)));
    nvps.add(new BasicNameValuePair("rollbackOnFailure", "true"));
    try
    {
      httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
      Date start = new Date();
      HttpResponse response = httpClient.execute(httpPost);
      duration = new Date().getTime() - start.getTime();
      if (!response.getStatusLine().toString().equals("HTTP/1.1 200 OK"))
        System.out.println("ERROR");
      if (!update)
        loadObjectIds(numberOfEvents, EntityUtils.toString(response.getEntity()));
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
    catch (ClientProtocolException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        httpClient.close();
      }
      catch( Throwable t )
      {
        t.printStackTrace();
      }
    }
    return duration;
  }

  private String createFeatures(int numberOfEvents, boolean update)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("[ ");
    for (int ix=0; ix<numberOfEvents; ix++)
    {
       sb.append("{ ");
       sb.append("\"geometry\": {");
       sb.append(" \"x\": "+random(100, 112)*-1);
       sb.append(", \"y\": "+random(32, 41));
       sb.append(" }, ");
       sb.append("\"attributes\": {");
       if (update)
         sb.append(" \"OBJECTID\": "+ objectIds[ix] +",");
       sb.append(" \"LastUpdated\": 1343137800000,");
       sb.append(" \"AssetName\": \"CargoVan-"+(ix+1)+"\",");
       sb.append(" \"AssetGroupName\": \"CargoVan\",");
       sb.append(" \"Speed\": " + random(30, 80) + ",");
       sb.append(" \"Panic\": \"Off\"");
       sb.append(" }");
       sb.append(" }");
       if (ix+1 < numberOfEvents)
         sb.append(",");
    }
    sb.append(" ]");
    return sb.toString();
  }

  private int random(int min, int max)
  {
    return min + (int) (Math.random() * ((max - min)+1));
  }

  private void loadObjectIds(int numberOfEvents, String response)
  {
    objectIds = new int[numberOfEvents];
    int oIx = 0;
    String lines[] = response.split("\n");
    for (int ix=0; ix<lines.length; ix++)
      if (lines[ix].contains("objectId"))
        objectIds[oIx++] = new Integer(lines[ix].split(":")[1].replace(",", "").trim());
  }

  //java -cp performance-test\target\performance-test-0.8.5.jar com.esri.ges.test.performance.featureservice.FeatureServiceProducer
  //  http://localhost:6080/arcgis/rest/services/AssetMonitor/FeatureServer/1 update 10 200 10
  public static void main(String[] args)
  {
    if (args.length < 5)
    {
      System.err.println("Usage: com.esri.ges.test.performance.featureservice.FeatureServiceProducer <urlToLayer> <update|add> <min> <max> <step>");
      return;
    }
    String urlToLayer = args[0];
    boolean update = args[1].equals("update") ? true : false;
    int min = new Integer(args[2]);
    int max = new Integer(args[3]);
    int step = new Integer(args[4]);
    FeatureServiceProducer producer = new FeatureServiceProducer(urlToLayer);
    producer.deleteFeatures();
    if (update)
      producer.addFeatures(max);
    for (int numberOfEvents=min; numberOfEvents<=max; numberOfEvents+=step)
      if (update)
        System.out.println("Update,"+numberOfEvents+","+producer.updateFeatures(numberOfEvents));
      else
        System.out.println("Add,"+numberOfEvents+","+producer.addFeatures(numberOfEvents));
  }
}
