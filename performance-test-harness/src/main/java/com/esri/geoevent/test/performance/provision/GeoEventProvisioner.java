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

import com.esri.geoevent.test.performance.jaxb.Config;

public class GeoEventProvisioner implements Provisioner
{
	private String	configFile;
	private String	hostName;
	private String	userName;
	private String	password;

	private String	token;
	private long		expiration;
	private String	referer;

	@Override
	public void init(Config config) throws ProvisionException
	{
		if( config == null )
			throw new ProvisionException("Could not configure the \"GeoEventClusterProvisioner\". The configuration is NULL!");
		
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
			System.out.println("-----------------------------------------------");
			System.out.println(" Provisioning GeoEvent for Performance Testing ");
			System.out.println("-----------------------------------------------");
			
			refreshToken();
			resetConfiguration();
			uploadConfiguration();
			System.out.println("Done provisioning GeoEvent for testing.");
		}
		catch (IOException ex)
		{
			throw new ProvisionException("Failed to provision GeoEvent for testing. Error: " + ex.getMessage());
		}
	}
	
	private void validate() throws ProvisionException
	{
		if(StringUtils.isEmpty(hostName))
			throw new ProvisionException("Could not find the property \"hostName\" in the \"ProvisionerConfig\". Failed to initialze the provisioner!");
		if(StringUtils.isEmpty(userName))
			throw new ProvisionException("Could not find the property \"userName\" in the \"ProvisionerConfig\". Failed to initialze the provisioner!");
		if(StringUtils.isEmpty(password))
			throw new ProvisionException("Could not find the property \"password\" in the \"ProvisionerConfig\". Failed to initialze the provisioner!");
		if(StringUtils.isEmpty(configFile))
			throw new ProvisionException("Could not find the property \"configFile\" in the \"ProvisionerConfig\". Failed to initialze the provisioner!");
		if(!new File(configFile).exists())
			throw new ProvisionException("Could not find the file \"" + configFile + "\". Make sure the path to the configuration file is correct and the file exist. Failed to initialze the provisioner!");
	}
	
	private void resetConfiguration() throws IOException
	{
		System.out.print("Resetting configuration ... ");
		String url = "https://" + hostName + ":" + 6143 + "/geoevent/admin/configuration/reset/.json";

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(getSSLSocketFactory()).build();
		HttpUriRequest request = RequestBuilder.get().setUri(url).addParameter("token", token).addHeader("referer", referer).build();

		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unused")
		JsonNode jsonResponse = mapper.readTree(entity.getContent());
		sleep(10 * 1000);
		System.out.println("Done");
	}

	private void uploadConfiguration() throws IOException
	{
		System.out.print("Uploading the new configuration ... ");
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
		System.out.println("Done");
	}

	private void refreshToken() throws IOException
	{
		//System.out.println("Refreshing token with username=" + userName + " and password=" + password);
		long now = System.currentTimeMillis();
		if (now < expiration)
			return;

		System.out.print("Fetching the application token ... ");
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
				System.out.println("The erroneous response was " + schema.toString());
				throw new IOException("No token granted.");
			}
			token = schema.get("token").asText();
			expiration = schema.get("expires").asLong();
			//System.out.println("Token refreshed.  expires at " + expiration);
			System.out.println("Done");
		}
		catch (UnsupportedEncodingException | ClientProtocolException | URISyntaxException e)
		{
			System.err.println("Error while refreshing token.");
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
