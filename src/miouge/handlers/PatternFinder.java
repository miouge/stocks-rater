package miouge.handlers;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class PatternFinder {

	boolean located = false;
	ArrayList<String> contextPatterns = new ArrayList<String>();

	String outOfContextPattern = "";
	int outOfContextDistance = -1;
	
	private int outOfContextPos = -1;	

	String leftPattern = "";
	String rightPattern = "";

	private int currentPos = 0;

	private String raw;
	
	PatternFinder( StringBuilder sb, Consumer< PatternFinder> consumer ) {
		
		this.raw = sb.toString();
		consumer.accept( this ); // allow customizations
	}
	
	Optional<String> findOptional() {
			
		Optional<String> result = Optional.empty(); 
				
		if( this.leftPattern.length() == 0 ) {
			System.err.println( "PatternFinder : badly initialized" );
			return result;
		}
		if( this.rightPattern.length() == 0 ) {
			System.err.println( "PatternFinder : badly initialized" );
			return result;
		}
		
		int firstContextPatternPos = -1;
		
		if( contextPatterns.size() > 0 && this.located == false ) {
						
			for( String contextPattern : contextPatterns ) {

				int pos = raw.indexOf( contextPattern, currentPos ); // at beginning currentPos = 0 
				if( pos == -1 ) {
					// not found
					return result;
				}
				
				if( firstContextPatternPos == -1 ) {
					// save for further use
					firstContextPatternPos = pos;
				}

				int len = contextPattern.length();
				currentPos = pos + len;
				// continue search with next pattern if any but from this new position
			}

			if( outOfContextPattern.length() > 0 ) {
				outOfContextPos = raw.indexOf( outOfContextPattern, firstContextPatternPos );
				if( outOfContextPos == -1 ) {
					System.err.println( String.format( "outOfContextPattern <%s> not found", outOfContextPattern ));
				}
				//System.out.println( "outOfContextPos = " + outOfContextPos );
			}
									
			if( currentPos <= 0 ) {
				return result;
			}
			
			this.located = true;
		}

		int posleft  = raw.indexOf( leftPattern, currentPos );
 		int posright = raw.indexOf( rightPattern, posleft + leftPattern.length()  );
				
		if( posleft > 0 && posright > 0 && posright > posleft ) {
			
			if( this.outOfContextDistance != -1 ) {
			
				// max distance from the starting context
				int distance = posright - firstContextPatternPos;			
				// System.out.println( "distance=" + distance );				
				
				if( distance > this.outOfContextDistance ) {
					
					// too far away from the last context pattern
					// out of context
					System.out.println( "found a match but too far away from the last context pattern : distance = " + distance );					
					return result;					
				}
			}
			// OK
		}
		else {
			return result;
		}
		
		if( outOfContextPos != -1 && posright > outOfContextPos ) {
			// out of context
			return result;
		}
		else {

			String data = raw.substring( posleft + leftPattern.length(), posright).trim();				
			currentPos = posright + rightPattern.length(); // to further use
			result = Optional.of( data );
			return result;
		}
	}

	String find() {
		
		Optional<String> result = this.findOptional();
		
		if( result.isPresent() ) {
			return result.get();
		}
		else {
			return "-";
		}
	}
	
	public static void main( String[] args )
	{
		// unit test
		
		StringBuilder sb = new StringBuilder();
		PatternFinder pf;
		String data;

		sb.setLength(0);
		sb.append("xx--//123478/---xxx----x//a/aaa**///***aa123//456789/");
		
		pf = new PatternFinder( sb, thePf -> {
	
			thePf.contextPatterns.add( "xx" );
			thePf.contextPatterns.add( "xx" );
			thePf.leftPattern = "//";
			thePf.rightPattern = "/";
		}); 
		data = pf.find(); 
		System.out.println( "data=" + data );		
		data = pf.find(); 
		System.out.println( "data=" + data );
		data = pf.find(); 
		System.out.println( "data=" + data );
		data = pf.find(); 
		System.out.println( "data=" + data );
	}
}
