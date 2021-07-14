package com.github.stockRater.handlers;

import java.util.ArrayList;
import java.util.function.Consumer;

public class PatternFinder {

	boolean locate = false;
	ArrayList<String> contextPatterns = new ArrayList<String>();
	
	String outOfContextPattern;
	int outOfContextPos = -1;
	
	String leftPattern = "";
	String rightPattern = "";
	
	int currentPos  = 0;
	
	String raw;
	
	PatternFinder( StringBuilder sb, Consumer< PatternFinder> consumer ) {
		consumer.accept( this );
		 this.raw = sb.toString();
	}
	
	String find() {
		
		String data = "-";
		
		if( locate == false ) {
			
			if( outOfContextPattern != null ) {				
				outOfContextPos = raw.indexOf( outOfContextPattern, 0 );
			}			
			
			for( String contextPattern : contextPatterns ) {
			
				int pos = raw.indexOf( contextPattern, currentPos );
				if( pos == -1 ) {
					// not found
					return data;
				}
				// continue search with next pattern but from this new postion
				currentPos = pos;			
			}
									
			if( currentPos <= 0 ) {
				return data;
			}
			
			locate = true;
		}

		int posleft  = raw.indexOf( leftPattern, currentPos );
		int posright = raw.indexOf( rightPattern, posleft );
				
		if( posleft != -1 && posright != -1 && posright > posleft ) {
			// OK
		}
		else {
			return data;
		}
		
		if( outOfContextPos != -1 && posright > outOfContextPos ) {
			// out of context
		}
		else {

			data = raw.substring( posleft + leftPattern.length(), posright).trim();				
			currentPos = posright + rightPattern.length(); // to further use
		}
		
		return data;
	}
	
	@SuppressWarnings("unused")
	void unitTest() {
	
		StringBuilder sb = new StringBuilder();
		PatternFinder pf;
		String data;

		sb.setLength(0);
		sb.append("xx--123478---xxx----xaaaa*****aa123456789");
		
		pf = new PatternFinder( sb, thePf -> {
	
			thePf.contextPatterns.add( "xx" );
			thePf.contextPatterns.add( "xx" );
			thePf.contextPatterns.add( "aaa" );
			thePf.contextPatterns.add( "aa" );
			thePf.leftPattern = "1234";
			thePf.rightPattern = "78";
		}); 
		data = pf.find();		
	}
}
