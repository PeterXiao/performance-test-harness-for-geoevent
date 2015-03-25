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
package com.esri.geoevent.test.performance.streamservice;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class StreamMetadata
{
	private List<String>	wsUrls	= new ArrayList<String>();

	public StreamMetadata(String url)
	{
		try(CloseableHttpClient httpClient = HttpClients.createDefault())
		{
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode schema = mapper.readTree(entity.getContent());
			JsonNode streamUrls = schema.get("streamUrls");
			// JsonNode fields = schema.get("fields");
			
			//parse out the stream service urls
			for (JsonNode protocol : streamUrls)
			{
				if (protocol.get("transport").asText().equals("ws"))
				{
					for (JsonNode urlNode : protocol.get("urls"))
					{
						wsUrls.add(urlNode.asText());
					}
				}
			}
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}

	public List<String> gerUrls()
	{
		return wsUrls;
	}

}
