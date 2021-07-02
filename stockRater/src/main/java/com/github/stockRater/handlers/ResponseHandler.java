package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

// import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
// import org.jsoup.nodes.Element;
// import org.jsoup.select.Elements;

public abstract class ResponseHandler {
		
	public boolean processAsRaw( Stock stock, StringBuilder answer ) throws Exception {

		System.out.println( String.format( "processAsRaw for %s", stock.getName()));
		return false;
	}

	public boolean processAsHTML( Stock stock, StringBuilder answer ) throws Exception {

		// answer is a HTML document
		System.out.println( String.format( "processAsHTML for %s", stock.getName()));		
		// Document doc = Jsoup.parse( answer.toString(), "UTF-8" );
		
		return false;
	}
	
	public boolean processAsJSON( Stock stock, StringBuilder answer ) throws Exception {
		
		// answer is a json string form
		System.out.println( String.format( "processAsJSON for %s", stock.getName()));		
		
/*			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY );
			mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );

			ResponseBody responseBody = mapper.readValue( answer, ResponseBody.class );

			int responseCode = responseBody.getMessage().getCode();
			
			String feedback = String.format( "\t=> code <%d> answer size =%d message <%s>\n",
					responseBody.getMessage().getCode(), answer.length(), responseBody.getMessage().getText() );
			
			if( responseCode == 0 ) {
				
				System.out.print( feedback );

				this.answer = answer;
				this.checkAnswer();
				
			} else {

				System.err.print( feedback );
				throw new Exception( "StatusMessage code not = 0");
			}
*/
		return false;
	}
	
	public void process( Stock stock, StringBuilder response ) throws Exception {
		
		if( response != null ) {
			
			if( processAsRaw(  stock, response ) == true ) { return; }
			if( processAsHTML( stock, response ) == true ) { return; }
			if( processAsJSON( stock, response ) == true ) { return; }			
		}
	}
}
