package miouge.handlers;

import miouge.beans.Stock;

public class YahooSearchHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + ".json";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response, boolean debug) throws Exception {

		boolean success = false;
		
		// System.out.println( String.format( "customProcess for %s ...", stock.name ));		
		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "exchange" );
			thePf.contextPatterns.add( "EQUITY" );			
			thePf.outOfContextPattern = "news";
			
			thePf.leftPattern = "symbol\":\"";
			thePf.rightPattern = "\",";
		}); 		
		data = pf.find().trim();
		if( data.equals("-") == false ) {
			stock.yahooSymbol = data;
			// System.out.println( String.format("%s->%s", stock.mnemo, stock.yahooSuffix ));
		}

		return success;
	}
}
