package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class ZbSearchSociete extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "Zb-" + stock.isin + "-searched.html";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			//thePf.contextPatterns.add( "</td><!-- inner td --></tr>" );
			thePf.contextPatterns.add( "id=\"ALNI0\"" );
			thePf.outOfContextPattern = "$('#ALNI0')";
			thePf.leftPattern = "href=\"/cours/action/";
			thePf.rightPattern = "/\" codezb=\"";
		}); 		
		data = pf.find().trim();
		if( data.equals( "-" ) == false ) {
			stock.zbSuffix = data;  
		}
		
		return true;
	}
}
