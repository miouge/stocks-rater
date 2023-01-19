package miouge.handlers;

import miouge.beans.Stock;

public class BmaSearchHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + ".html";
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response, boolean debug ) throws Exception {
		
		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "class=\"search__list-link\"" );
			thePf.contextPatterns.add( "href=\"/cours" );
			thePf.leftPattern = "/";
			thePf.rightPattern = "/\"";
		}); 		
		data = pf.find().trim();
		if( data.equals("-") == false ) {
			stock.bmaSuffix = data;
		}
				
		return true;
	}
}
