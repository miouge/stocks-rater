package miouge.handlers;

import miouge.beans.Stock;

public class AbcEventsAndQuoteHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-societe.html";
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {
		
		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<h1 class=\"h1b\">Evénements de marché sur" );
			thePf.outOfContextPattern = "</span>";
			thePf.leftPattern = "<b class=\"f20\">";
			thePf.rightPattern = "</b>"; 
		}); 		
		data = pf.find().replace(",", ".").replace( "&#xA0;", "" ).replace("&nbsp;", "" ).replace("&#x20AC;", "" );		
		if( data.equals( "-" ) == false && data.length() > 0 ) {
		
			stock.lastQuote = Double.parseDouble(data);
		}
		
		return true;
	}
}
 