package com.github.stockRater.beans;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.stockRater.handlers.ResponseHandlerTemplate;

public class GetApi  {
	
	public String urlSuffix;
	public String method; // "GET", ...
	
	public Stock stock;
	public Map<String,String> parameters; 
	public ResponseHandlerTemplate handler;

	public GetApi( Consumer< GetApi> consumer ) {

		this.method = "GET";
		consumer.accept( this );
	}

	private void useParameters( StringBuilder urlstring, Map<String,String> parameters ) throws Exception {
		
		if( parameters == null || parameters.isEmpty() ) {
			return;
		}
		
		urlstring.append( "?" );
		
		int paramCounter = 1;

		for( Map.Entry<String, String> entry : parameters.entrySet() ) {

			if( paramCounter > 1 ) {

				urlstring.append( "&" );
			}

			urlstring.append( URLEncoder.encode( entry.getKey(), "UTF-8" ) );
			urlstring.append( "=" );
			urlstring.append( URLEncoder.encode( entry.getValue(), "UTF-8" ) );

			paramCounter++;
		}
	}
	
	public void perform( TargetServer target ) throws Exception {

		int status = -1;
		StringBuilder response = new StringBuilder();		
		String cacheFile = handler.getDumpFilename(stock);
		boolean useCache = false;
		
		if( this.urlSuffix == null || this.urlSuffix.isEmpty() ) {
			throw new Exception( "urlSuffix is undefined");
		}
		
		StringBuilder urlstring = new StringBuilder( target.assembleUrl( this.urlSuffix ) );
		this.useParameters( urlstring, this.parameters );		
		// System.out.println( String.format( "%s for api %s", this.method, this.urlSuffix ));
				
		if( cacheFile != null && handler.cacheFolder != null ) {
		
			Path path = Paths.get( handler.cacheFolder + "/" + cacheFile );
				
			// file exists and it is not a directory
			if( Files.exists(path) && !Files.isDirectory(path)) {
				
				// System.out.println("response is already present in cache folder");

				// load the file then call the handler directly
	            List<String> content = Files.readAllLines( path, StandardCharsets.UTF_8);
	            content.forEach( line -> response.append( line ) );
	            useCache = true;
			}
		}
		else {
		
			try {
							
				URL url = new URL( urlstring.toString() );
				
				HttpURLConnection connection = null;
				
				try {
		
					connection = ( HttpURLConnection ) url.openConnection();
					
					// reuse the cookie present in store if any
					target.applyCookies(connection);
					
					connection.setRequestMethod( method );
					
					connection.getContent();
					// target.traceCookieStoreContent();
			
					status = connection.getResponseCode();	
					
					if( status == 200 ) {
									
						BufferedReader in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
							
						do {
							String line = in.readLine();
							if( line == null ) {
								break;
							}
							response.append( line  + "\n" );
						}
						while( true );
						in.close();
		
					} else {
								
						System.err.println( String.format( "%s %s", this.method, urlstring ));
						System.err.println( String.format( "\t=> HTTP code = <%-3d>", status ) );
						// throw new Exception("unsuccessful call of url (HTTP code <> 200)");
					}
				}
				finally {
					
					if( connection != null ) {
						connection.disconnect();
					}
				}
			}
			catch( Exception e ) {
				
				System.err.println( String.format( "%s", e ) );
			}
		}

		if( response.length() > 0 ) {

			try {
				
				this.handler.process( stock, response, useCache );
				
			} catch (Exception e) {
				
				System.err.println( String.format( "exception in handler : <%s> <%s>", stock.name, urlstring.toString() ));				
			}
		}
	}
}
