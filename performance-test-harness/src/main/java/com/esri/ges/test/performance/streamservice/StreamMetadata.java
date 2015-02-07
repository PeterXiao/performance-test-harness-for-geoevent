package com.esri.ges.test.performance.streamservice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class StreamMetadata 
{
	private List<String> wsUrls = new ArrayList<String>();

	public StreamMetadata(String url)
	{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
	try
	{
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode schema = mapper.readTree(entity.getContent());
		JsonNode fields = schema.get("fields");
		
		
		JsonNode streamUrls = schema.get("streamUrls");
		for( JsonNode protocol : streamUrls )
		{
			if( protocol.get("transport").asText().equals("ws") )
			{
				for( JsonNode urlNode : protocol.get("urls") )
				{
					wsUrls.add( urlNode.asText() );
				}
			}
		}
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
	}

	public List<String> gerUrls()
	{
		return wsUrls;
	}

}
