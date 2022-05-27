package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class YahooHistoQuoteHandler extends ResponseHandlerTemplate {

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

			thePf.contextPatterns.add( "adjclose" );
			thePf.contextPatterns.add( "adjclose" );			
			
			thePf.leftPattern = "[";
			thePf.rightPattern = "]";
		}); 		
		data = pf.find().trim();
		
		if( data.equals("-") == false ) {
			
			stock.previousQuote1 = Double.parseDouble( data );
		}

		return success;
	}
}
