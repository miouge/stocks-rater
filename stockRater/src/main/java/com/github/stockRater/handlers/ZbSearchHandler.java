package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class ZbSearchHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-searched.html";
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		PatternFinder pf;
		String data;

		// custom suffix
		
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
			stock.zbUrl = String.format("https://www.zonebourse.com/cours/action/%s/fondamentaux/", stock.zbSuffix );
		}
		
		return true;
	}
}
