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
package com.esri.geoevent.test.performance.provision;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.jaxb.Config;

/**
 *	<p>This class provisions GeoEvent wth a configurable GeoEvent configuration file. 
 *	It achieves this by doing the following: </p>
 *	<ol>
 *	<li>Fetches a new user token</li>
 *	<li>Resets the current configuration</li>
 *	<li>Uploads the new configuration XML file</li>
 *	</ol> 
 */
public class GeoEventProvisioner implements Provisioner
{
	private String	configFile;
	private String	hostName;
	private String	userName;
	private String	password;

	private String	token;
	private long		expiration;
	private String	referer;
	
	private static final String NAME = "GeoEvent";
	
	@Override
	public void init(Config config) throws ProvisionException
	{
		if( config == null )
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_INIT_ERROR", getClass().getSimpleName()) );
		
		this.hostName = config.getPropertyValue("hostName");
		this.userName = config.getPropertyValue("userName");
		this.password = config.getPropertyValue("password");
		this.configFile = config.getPropertyValue("configFile");
		
		validate();
	}
	
	@Override
	public void provision() throws ProvisionException
	{
		try
		{
			System.out.println("-------------------------------------------------------");
			System.out.println( ImplMessages.getMessage("PROVISIONER_START_MSG", NAME) );
			System.out.println("-------------------------------------------------------");
			
			refreshToken();
			resetConfiguration();
			uploadConfiguration();
			System.out.println( ImplMessages.getMessage("PROVISIONER_FINISH_MSG", NAME) );
		}
		catch (IOException ex)
		{
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_ERROR", NAME, ex.getMessage() ) );
		}
	}
	
	private void validate() throws ProvisionException
	{
		if(StringUtils.isEmpty(hostName))
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "hostName") );
		if(StringUtils.isEmpty(userName))
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "userName") );
		if(StringUtils.isEmpty(password))
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "password") );
		if(StringUtils.isEmpty(configFile))
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "configFile") );
		if(!new File(configFile).exists())
			throw new ProvisionException( ImplMessages.getMessage("PROVISIONER_CONFIG_FILE_VALIDATION", "configFile") );
	}
	
	private void resetConfiguration() throws IOException
	{
		System.out.print( ImplMessages.getMessage("PROVISIONER_RESETTING_CONFIG_MSG") );
		String url = "https://" + hostName + ":" + 6143 + "/geoevent/admin/configuration/reset/.json";

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(getSSLSocketFactory()).build();
		HttpUriRequest request = RequestBuilder.get().setUri(url).addParameter("token", token).addHeader("referer", referer).build();

		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unused")
		JsonNode jsonResponse = mapper.readTree(entity.getContent());
		sleep(10 * 1000);
		System.out.println( ImplMessages.getMessage("DONE") );
	}

	private void uploadConfiguration() throws IOException
	{
		System.out.print( ImplMessages.getMessage("PROVISIONER_UPLOADING_CONFIG_MSG") );
		// String url = "https://"+hostname+":"+6143+"/geoevent/admin/configuration/install/.json";
		String url = "https://" + hostName + ":" + 6143 + "/geoevent/admin/configuration/.json";

		File configAsFile = new File(configFile);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(getSSLSocketFactory()).build();
		FileEntity file = new FileEntity(configAsFile, ContentType.APPLICATION_XML);
		HttpUriRequest post = RequestBuilder.put().setUri(url).addHeader("GeoEventAuthorization", token).addHeader("referer", referer).setEntity(file).build();

		HttpResponse response = httpClient.execute(post);
		HttpEntity entity = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unused")
		JsonNode jsonResponse = mapper.readTree(entity.getContent());
		sleep(10 * 1000);
		System.out.println( ImplMessages.getMessage("DONE") );
	}

	private void refreshToken() throws IOException
	{
		//System.out.println("Refreshing token with username=" + userName + " and password=" + password);
		long now = System.currentTimeMillis();
		if (now < expiration)
			return;

		System.out.print( ImplMessages.getMessage("PROVISIONER_FETCHING_TOKEN_MSG") );
		referer = "https://" + hostName + ":6143/geoevent/admin";
		String serverTokenUrl = "http://" + hostName + ":6080/arcgis/tokens/generateToken";
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try
		{
			HttpUriRequest postRequest = RequestBuilder.post().setUri(new URI(serverTokenUrl)).addParameter("username", userName).addParameter("password", password).addParameter("client", "referer").addParameter("referer", referer).addParameter("f", "json").build();
			HttpResponse tokenGenerationResponse = httpClient.execute(postRequest);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode schema = mapper.readTree(tokenGenerationResponse.getEntity().getContent());

			if (!schema.has("token"))
			{
				System.out.println( ImplMessages.getMessage("PROVISIONER_TOKEN_ERROR_RESPONSE", schema.toString()));
				throw new IOException( ImplMessages.getMessage("PROVISIONER_TOKEN_ERROR") );
			}
			token = schema.get("token").asText();
			expiration = schema.get("expires").asLong();
			System.out.println( ImplMessages.getMessage("DONE") );
		}
		catch (UnsupportedEncodingException | ClientProtocolException | URISyntaxException e)
		{
			System.err.println( ImplMessages.getMessage("PROVISIONER_TOKEN_EXCEPTION_ERROR", e.getMessage()));
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		finally
		{
			try
			{
				httpClient.close();
			}
			catch (Throwable t)
			{
				throw new IOException(t.getMessage());
			}
		}
	}

	private SSLConnectionSocketFactory getSSLSocketFactory()
	{
		KeyStore trustStore;
		try
		{
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			TrustStrategy trustStrategy = new TrustStrategy()
				{
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException
					{
						return true;
					}

				};

			SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
			sslContextBuilder.loadTrustMaterial(trustStore, trustStrategy);
			sslContextBuilder.useTLS();
			SSLContext sslContext = sslContextBuilder.build();
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
			return sslSocketFactory;
		}
		catch (GeneralSecurityException | IOException e)
		{
			System.err.println("SSL Error : " + e.getMessage());
		}
		return null;
	}
	
	private void sleep(long timeInMs)
	{
		try
		{
			Thread.sleep(timeInMs);
		}
		catch (InterruptedException ignored)
		{
		}
	}
}
