package com.github.stockRater.handlers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;

import com.github.stockRater.beans.Context;
import com.github.stockRater.beans.Stock;

// import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
// import org.jsoup.nodes.Element;
// import org.jsoup.select.Elements;
// Document doc = Jsoup.parse( response.toString(), "UTF-8" );

public abstract class ResponseHandlerTemplate {

	public boolean success = false;
	Context context;
	
	 // folder where to store cache content if any needed (if null mean never cache)
	public String cacheSubFolder = null;
	
	public String getDumpFilename( Stock stock ) { return null;	}
	
	protected void addIfNonNull( String data, Function<String,Object> converter, List<Long> list ) {
		
		if( data == null ) {
			return;
		}
		
		if( data.equals( "-" )) {
			return;
		}
		
		Object converted = converter.apply( data );
		list.add( (Long) converted );
	}	

	protected void addDoubleIfNonNull( String data, Function<String,Object> converter, List<Double> list ) {
		
		if( data == null ) {
			return;
		}
		
		if( data.equals( "-" )) {
			return;
		}

		if( data.equals( "" )) {
			return;
		}
		
		try {
			
			String tmp = data.replace(",", "." );			
			Object converted = converter.apply( tmp  );
			list.add( (Double) converted );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void dumpToFile( Stock stock, StringBuilder answer ) throws Exception {

		if( this.cacheSubFolder == null ) {
			// no dump
			return;
		}
		
		String dumpFilename = this.getDumpFilename( stock );
		if( dumpFilename == null ) {
			// no dump
			return;
		}

		// create folder if doesn't exist ...		
		Files.createDirectories(Paths.get( this.cacheSubFolder ));
		
		// output to file
		String destination = this.context.rootFolder + '/' + cacheSubFolder + "/" + dumpFilename;		
        System.out.println( String.format( "response dumped into %s", (cacheSubFolder + "/" + dumpFilename)));                
        //Files.write( Paths.get( destination ), answer.toString().getBytes() );

        //Files.newBufferedReader(Paths.get( destination ), "ISO-8859-15");
        
        FileUtils.writeStringToFile(new File( destination ), answer.toString(), "ISO-8859-1" );
        
        /*
        byte[] bytes = StringUtils.getBytesUtf8(answer.toString());         
        String utf8EncodedString = StringUtils.newStringUtf8(bytes);        
        File f = new File( destination ); 
        FileUtils.writeStringToFile(f, utf8EncodedString, "UTF-8");
        */
        
	}	
	
	public boolean customProcess( Stock stock, StringBuilder response ) throws Exception { return false; }
	
	public void process( Context context, Stock stock, StringBuilder response, boolean fromCache ) throws Exception {
		
		this.context = context;
		
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
