package miouge.beans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class TargetServer {
	
	private Integer cumul = 0;
	private String baseUrl;
	
	
	private CookieManager cookieManager = new CookieManager();
	
	public TargetServer() {
		
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

		// The HTTP state management mechanism specifies a way to create a stateful session with HTTP requests and responses.
		CookieHandler.setDefault(cookieManager);
	}
	
	public void traceCookieStoreContent() {

		CookieStore cookieStore = (CookieStore) cookieManager.getCookieStore();	
		
		List<HttpCookie> cookieList = cookieStore.getCookies();

		// iterate HttpCookie object
		for( HttpCookie cookie : cookieList ) 
		{
			System.out.println(String.format( "<%s>" , cookie ));
			
			// gets domain set for the cookie
			System.out.println("Domain: " + cookie.getDomain());
			 
			// gets max age of the cookie
			System.out.println("max age: " + ((HttpCookie) cookie).getMaxAge());
			 			 
			// gets path of the server
			System.out.println("server path: " + cookie.getPath());
			 
			// gets boolean if cookie is being sent with secure protocol
			System.out.println("is cookie secure: " + ((HttpCookie) cookie).getSecure());

			// gets the version of the protocol with which the given cookie is related.
			System.out.println("version of cookie: " + cookie.getVersion());
			 
			// gets name cookie
			System.out.println("name of cookie: " + cookie.getName());
						
			// gets the value of the cookie
			System.out.println("value of cookie: " + cookie.getValue());
		}			
	}	

	public void applyCookies( URLConnection urlConnection ) {
		
		CookieStore cookieStore = (CookieStore) cookieManager.getCookieStore();	
			
		List<HttpCookie> cookieList = cookieStore.getCookies();
		
		if( cookieList.size() < 1 ) {
			
			return;
		}

		StringBuilder cookieHeader = new StringBuilder();
		
		// iterate HttpCookie object
		for( HttpCookie cookie : cookieList ) {		
			cookieHeader.append(cookie).append(";");
		}
		cookieHeader.deleteCharAt(cookieHeader.length() - 1);

		urlConnection.setRequestProperty("Cookie", cookieHeader.toString());
	}
	
	public void purgeCookieStore() {
		
		CookieStore cookieStore = (CookieStore) cookieManager.getCookieStore();		
		cookieStore.removeAll();
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Integer getCumul() {
		return cumul;
	}
	
	public String assembleUrl( String urlSuffix ) {
		this.cumul++;
		return new String( this.baseUrl + urlSuffix );
	}

	public void authenticate() {
		
		// username: ADMIN
		// password: ADMIN
		
		String data="username=ADMIN&password=ADMIN&testMode=true&home_page=undefined";
		String requestURL= baseUrl + "/ewks/login";
		
		// String requestURL = baseUrl + https://localhost:8443/ewks/login
		
		String result = "";
		try {

			// Send the request
			URL url = new URL(requestURL);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

			// write parameters
			writer.write(data);
			writer.flush();

			// Get the response
			StringBuffer answer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				answer.append(line);
			}
			writer.close();
			reader.close();

			// Output the response
			result = answer.toString();
			System.out.println(result);

		} catch( MalformedURLException ex ) {

			ex.printStackTrace();

		} catch( IOException ex ) {

			ex.printStackTrace();

		}
	}
}
