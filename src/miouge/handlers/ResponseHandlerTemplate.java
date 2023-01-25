package miouge.handlers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;

import miouge.beans.Context;
import miouge.beans.Stock;


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
	
	protected void addIfNonNull( String data, Function<String, Object> converter, List<Long> list, boolean debug ) {
		
		if( data == null ) {
			return;
		}
		
		if( data.equals( "-" )) {
			return;
		}
		
		Object converted = converter.apply( data );
		if( debug ) {
			System.out.println( "addding :" + converted );
		}
		list.add( (Long) converted );
	}	

	protected void addDoubleIfNonNull( String data, Function<String,Object> converter, List<Double> list, boolean debug ) {
		
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
			if( debug ) {
				System.out.println( "adding <" + converted + ">" );
			}			
			list.add( (Double) converted );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void dumpToFile( Stock stock, StringBuilder answer, String charset ) throws Exception {

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
        System.out.println( String.format( "   response dumped into %s", (cacheSubFolder + "/" + dumpFilename)));
        FileUtils.writeStringToFile( new File( destination ), answer.toString(), charset ); // "ISO-8859-1" );
        
        /*
        byte[] bytes = StringUtils.getBytesUtf8(answer.toString());         
        String utf8EncodedString = StringUtils.newStringUtf8(bytes);        
        File f = new File( destination ); 
        FileUtils.writeStringToFile(f, utf8EncodedString, "UTF-8");
        */
        
	}	
	
	public boolean customProcess( Stock stock, StringBuilder response, boolean debug ) throws Exception { return false; }
	
	public void process( Context context, Stock stock, StringBuilder response, boolean cacheLoaded, String charset, boolean debug ) throws Exception {
		
		this.context = context;
		
		if( response == null ) {
			return;
		}
			
		if( cacheLoaded != true ) {
			dumpToFile( stock, response, charset );
		}
		
		try {
			
			this.success = this.customProcess( stock, response, debug );			
			
		} catch ( Exception e ) {
		
			this.success = false;
			System.err.println( String.format( "exception in customProcess : <%s> %s", stock.name, e ));
			throw e;
			// e.printStackTrace();
		}		
	}
}
