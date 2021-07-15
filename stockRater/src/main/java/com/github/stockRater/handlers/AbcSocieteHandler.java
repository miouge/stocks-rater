package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class AbcSocieteHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "ABC-" + stock.isin + "-societe.html";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {
		
		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Eligible au PEA" );
			thePf.leftPattern = "<td>";
			thePf.rightPattern = "</td>";
		}); 		
		data = pf.find().toLowerCase().trim();		
		if( data.equals( "oui" )) {
		
			stock.withinPEA = new Boolean(true);
		}

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Nombre de titres" );
			thePf.leftPattern = "<td>";
			thePf.rightPattern = "</td>";
		}); 		
		data = pf.find().toLowerCase().trim().replaceAll("\u00a0",""); // \u00a0 est l'espace insécable  

		if( data.equals("-") == false ) {
			stock.abcSharesCount = Long.parseLong(data);
		}

		return true;
	}
}
 