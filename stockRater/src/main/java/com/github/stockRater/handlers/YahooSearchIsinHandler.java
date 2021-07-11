package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class YahooSearchIsinHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "Yahoo-searched-" + stock.isin + ".json";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response) throws Exception {

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
		stock.yahooSuffix = data;

		return success;
	}
}
