package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class BoursoramaSearchHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "Boursorama-" + stock.isin + ".html";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {
		
		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "class=\"search__list-link\"" );
			thePf.outOfContextPattern = "class=\"search__item-content\"";
			thePf.leftPattern = "href=\"/cours/";
			thePf.rightPattern = "/\"";
		}); 		
		data = pf.find().trim(); 
		stock.bmaSuffix = data;
				
		return true;
	}
}
