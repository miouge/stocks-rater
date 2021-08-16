package com.github.stockRater.beans;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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
	public Context context;
	public boolean cacheLoaded = false;
	public boolean debug = false;
	public boolean onlyUseCache = false;
	
    // ISO-8859-1 =  ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
    // UTF-8      =  Eight-bit UCS Transformation Format	
	
	public Charset charset = StandardCharsets.UTF_8;
	
	public Stock stock;
	public Map<String,String> parameters; 
	public ResponseHandlerTemplate handler;

	public GetApi( Context context, Consumer< GetApi> consumer ) {

		this.method = "GET";
		this.context = context;
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
	
	public void perform( TargetServer target ) {

		StringBuilder urlString = new StringBuilder();
		boolean urlStringOK = false;

		try {

			if( this.urlSuffix == null || this.urlSuffix.isEmpty() ) {
				throw new Exception( "urlSuffix is undefined");
			}

			urlString = new StringBuilder( target.assembleUrl( this.urlSuffix ) );
			this.useParameters( urlString, this.parameters );
			urlStringOK = true;
		
		} catch ( Exception e ) {
			
			System.err.println( String.format( "exception in urlstring formation : <%s>", stock.name ));
		}
		
		if( urlStringOK != true ) {
			return;
		}

		String cacheFile = handler.getDumpFilename(stock);		
		StringBuilder response = new StringBuilder();
		
		try {

			if( handler.cacheSubFolder != null && cacheFile != null ) {
				
				Path path = Paths.get( this.context.rootFolder + "/" + handler.cacheSubFolder + "/" + cacheFile );
					
				// file exists and it is not a directory
				if( Files.exists(path) && !Files.isDirectory(path)) {
					
					// System.out.println("response is already present in cache folder");
	
					// load the file then call the handler directly
		            List<String> content = Files.readAllLines( path, charset );
		            content.forEach( line -> response.append( line ) );
		            cacheLoaded = true;
				}
			}
			
		} catch ( Exception e ) {

			System.err.println( String.format( "exception in cache detection/loading of : <%s>", cacheFile ));
		}
		
		if( cacheLoaded != true ) {
			
			if( onlyUseCache == true ) {
				
				// no HTTP communication allowed
				return;
			}
		
			try {
							
				// System.out.println( String.format( "%s for api %s", this.method, this.urlSuffix ));
				
				URL url = new URL( urlString.toString() );
				
				HttpURLConnection connection = null;
				
				try {
		
					int status = -1;
					
					connection = ( HttpURLConnection ) url.openConnection();
					
					// reuse the cookie present in store if any
					target.applyCookies(connection);
					
					connection.setRequestMethod( method );
					
					if( debug ) {
						target.traceCookieStoreContent();
					}
					
					System.out.println( String.format( "%s %s", this.method, urlString ));
															
					// Object content = connection.getContent();					
					// String encoding = connection.getContentEncoding();

					status = connection.getResponseCode();
					
					// long charCount = 0;
					if( status == 200 ) {
		
						try( InputStream is = connection.getInputStream() )
						{
						    int BUFFER_SIZE = 8192;
						    String encoding = charset.toString();

						    BufferedReader in = new BufferedReader( new InputStreamReader( is, encoding ), BUFFER_SIZE );
						    
							do {
								String line = in.readLine();
								if( line == null ) {
									break;
								}
								// charCount += line.length();
								response.append( line );
							}
							while( true );
							in.close();
						    
					    	// System.out.println( String.format( "GET %d character(s)", charCount ));

						} catch ( Exception e ) {
							
							e.printStackTrace();
							throw e;
						}

					} else {

						System.err.println( String.format( "%s %s", this.method, urlString ));
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

				this.handler.process( this.context, this.stock, response, this.cacheLoaded, this.charset.toString() );

			} catch( Exception e ) {

				System.err.println( String.format( "exception in handler : <%s> <%s>", stock.name, urlString.toString() ));
			}
		}
	}
}
