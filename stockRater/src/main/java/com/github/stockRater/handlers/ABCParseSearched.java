package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class ABCParseSearched extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "ABC-" + stock.isin + "-cotation.html";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {
		
		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<script type=\"application/ld+json\">" );
			thePf.leftPattern = "www.abcbourse.com/cotation/";
			thePf.rightPattern = "\"";
		}); 		
		data = pf.find().trim(); 
		stock.abcSuffix = data;
				
		return true;
	}
}
