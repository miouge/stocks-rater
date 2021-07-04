package com.github.stockRater.handlers;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.stockRater.beans.Stock;

// import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
// import org.jsoup.nodes.Element;
// import org.jsoup.select.Elements;

public abstract class ResponseHandlerTemplate {

	public boolean success = false;
	
	 // folder where to store cache content if any needed (if null mean never cache)
	public String cacheFolder = null;

	public String getDumpFilename( Stock stock ) { return null;	}
	
	public void dumpToFile( Stock stock, StringBuilder answer ) throws Exception {

		if( this.cacheFolder == null ) {
			// no dump
			return;
		}
		
		String dumpFilename = this.getDumpFilename( stock );
		if( dumpFilename == null ) {
			// no dump
			return;
		}

		// create folder if doesn't exist ...		
		Files.createDirectories(Paths.get( this.cacheFolder ));
		
		// output to file
		String destination = this.cacheFolder + "/" + dumpFilename;		
        System.out.println( String.format( "\tdumped into %s", dumpFilename ));                
        Files.write( Paths.get( destination ), answer.toString().getBytes() );
	}	
	
	public boolean customProcess( Stock stock, StringBuilder response ) throws Exception { return false; }
	
	public void process( Stock stock, StringBuilder response, boolean fromCache ) throws Exception {
		
		if( response == null ) {
			return;
		}
			
		if( fromCache != true ) {
			dumpToFile( stock, response );
		}
		
		try {
			
			this.success = this.customProcess( stock, response );			
			
		} catch ( Exception e ) {
		
			this.success = false;
			System.err.println( String.format( "exception in customProcess : <%s> %s", stock.name, e ));
			throw e;
			// e.printStackTrace();
		}		
	}
}
