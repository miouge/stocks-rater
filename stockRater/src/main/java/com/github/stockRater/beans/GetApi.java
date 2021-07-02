package com.github.stockRater.beans;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
// import java.net.URLEncoder;
import java.util.Map;
import java.util.function.BiConsumer;

public class GetApi  {
	
	private String urlSuffix;
	private String method; // "GET", ...
	
	public GetApi( Stock stock, BiConsumer<Stock, GetApi> consumer ) {

		this.method = "GET";
		consumer.accept( stock, this );
	}

	public String getUrlSuffix() {
		return urlSuffix;
	}

	public void setUrlSuffix(String urlSuffix) {
		this.urlSuffix = urlSuffix;
	}

	/*
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
	*/
	
	public int perform( TargetServer target, Map<String,String> parameters, Stock stock, AnswerHandler specificHandler ) throws Exception {

		if( this.urlSuffix == null || this.urlSuffix.isEmpty() ) {
			throw new Exception( "urlSuffix is undefined");
		}
		
		StringBuilder urlstring = new StringBuilder( target.assembleUrl( this.urlSuffix ) );
		
		//this.useParameters( urlstring, parameters );
			
		AnswerHandler handler;
		
		if( specificHandler != null ) {
			handler = specificHandler;
		}
		else
		{
			handler = new AnswerHandler(); // use the default handler
		}
		
		System.out.println( String.format( "%s for api %s", this.method, this.urlSuffix ));
		
		URL url = new URL( urlstring.toString() );
		
		HttpURLConnection connection = null;
		
		int status = -1; 
		
		try {

			connection = ( HttpURLConnection ) url.openConnection();
			
			// reuse the cookie present in store if any
			target.applyCookies(connection);
			
			connection.setRequestMethod( method );
			
			connection.getContent();
			target.traceCookieStoreContent();
	
			status = connection.getResponseCode();	
			
			if( status == 200 ) {
	
				System.out.println( String.format( "\t=> HTTP code = <%-3d>", status ) );
				BufferedReader in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
						
				StringBuilder answer = new StringBuilder();
					
				int lineCount = 0;
				do {
					String line = in.readLine();
					if( line == null ) {
						break;
					}
					answer.append( line );
					lineCount++;
				}
				while( true );				
				in.close();
				
				System.out.println( String.format( "\tanswer %s line(s) length = %d", lineCount, answer.length()));
				
				try {
					
					//handler.processAnswer( answer );
					handler.processAnswerHTML( stock, answer );
					
				} catch (Exception e) {
					
					System.err.println( String.format( "[%s]", urlstring ) );
					
					throw new Exception( e );
				}

			} else {
	
				System.err.println( String.format( "\t=> HTTP code = <%-3d>", status ) );
				System.err.println( String.format( "[%s]", urlstring ) );
				throw new Exception("unsuccessful call of url (HTTP code <> 200)");
			}
		}
		finally {
			
			if( connection != null ) {
				connection.disconnect();
			}
		}

		return status;
	}
}
