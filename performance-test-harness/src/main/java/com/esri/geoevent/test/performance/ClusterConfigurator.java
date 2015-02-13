package com.esri.geoevent.test.performance;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class ClusterConfigurator 
{
	private Log LOGGER = LogFactory.getLog(ClusterConfigurator.class);
	private String configFile;
	private String hostname;
	private String userName;
	private String password;

	private String token;
	private long expiration;
	private String referer;

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}



	public void setUserName(String userName) {
		this.userName = userName;
	}



	public void setPassword(String password) {
		this.password = password;
	}



	public ClusterConfigurator( String configurationFile )
	{
		this.configFile = configurationFile;
	}

	private void resetConfiguration() throws IOException
	{
		LOGGER.info("reseting the configuration.");
		String url = "https://"+hostname+":"+6143+"/geoevent/admin/configuration/reset/.json";

		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(getSSLSocketFactory())
				.build();
		HttpUriRequest request = RequestBuilder.get()
				.setUri( url )
				.addParameter( "token", token )
				.addHeader("referer", referer)
				.build();

		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonResponse = mapper.readTree(entity.getContent());
		System.out.println("Done resetting configuration.");
		try
		{
			Thread.sleep( 10 * 1000 );
		}catch(InterruptedException ex)
		{
			
		}

	}

	private void uploadConfiguration() throws IOException
	{
		System.out.println("Uploading the new configuration.");
		//String url = "https://"+hostname+":"+6143+"/geoevent/admin/configuration/install/.json";
		String url = "https://"+hostname+":"+6143+"/geoevent/admin/configuration/.json";
		
		File configAsFile = new File(configFile);

		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(getSSLSocketFactory())
				.build();
		FileEntity file = new FileEntity( configAsFile, ContentType.APPLICATION_XML);
		HttpUriRequest post = RequestBuilder.put()
				.setUri( url )
				.addHeader("GeoEventAuthorization", token)
				.addHeader("referer", referer)
				.setEntity(file)
				.build();

		HttpResponse response = httpClient.execute(post);
		HttpEntity entity = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonResponse = mapper.readTree(entity.getContent());
		System.out.println("Done uploading the configuration.");
		try
		{
			Thread.sleep( 10 * 1000 );
		}catch(InterruptedException ex)
		{
			
		}
	}

	public void applyConfiguration()
	{
		try
		{
			refreshToken();
			resetConfiguration();
			uploadConfiguration();
		}catch(IOException ex)
		{
			System.err.println(ex.getMessage());
		}
	}

	private void refreshToken() throws IOException
	{
		System.out.println("Refreshing token with username="+userName+" and password="+password);
		long now = System.currentTimeMillis();
		if( now < expiration )
			return;

		referer = "https://"+hostname+":6143/geoevent/admin";
		String serverTokenUrl = "http://"+hostname+":6080/arcgis/tokens/generateToken";
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try
		{
			HttpUriRequest postRequest = RequestBuilder.post()
					.setUri(new URI(serverTokenUrl))
					.addParameter("username", userName)
					.addParameter("password", password)
					.addParameter("client", "referer")
					.addParameter("referer", referer)
					.addParameter("f", "json")
					.build();
			HttpResponse tokenGenerationResponse = httpClient.execute(postRequest);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode schema = mapper.readTree(tokenGenerationResponse.getEntity().getContent());

			if( ! schema.has("token") )
			{
				System.out.println("The erroneous response was " + schema.toString());
				throw new IOException("No token granted.");
			}
			token = schema.get("token").asText();
			expiration = schema.get("expires").asLong();
			System.out.println("Token refreshed.  expires at " + expiration);
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
			catch( Throwable t )
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
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory ( sslContext );
			return sslSocketFactory;
		}
		catch( GeneralSecurityException | IOException e )
		{
			System.err.println( "SSL Error : " + e.getMessage() );
		}	
		return null;
	}
}
