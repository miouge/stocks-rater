package miouge.handlers;

import miouge.beans.Stock;

public class BmaSocieteHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + ".html";
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response, boolean debug ) throws Exception {
		
		PatternFinder pf;
		String data;

		// Effectif
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Effectif" );
			thePf.outOfContextPattern = "Nombre de titres";
			thePf.leftPattern = "<p class=\"c-list-info__value\">";
			thePf.rightPattern = "</p>";
		}); 		
		data = pf.find().replace(" ", "").trim();

		if( data.equals("-") == false ) {
			Long effectif = Long.parseLong(data);
			if( effectif != null && effectif > 0 ) {
				stock.effectif = effectif;
			}
		}
		
		// Nombre de titre
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Nombre de titres" );
			thePf.outOfContextPattern = "Capitalisation boursière";
			thePf.leftPattern = "<p class=\"c-list-info__value\">";
			thePf.rightPattern = "</p>";
		}); 		
		data = pf.find().replace(" ", "").trim();

		if( data.equals("-") == false ) {
			Long sharesCount = Long.parseLong(data);
			if( sharesCount != null && sharesCount > 0 ) {
				stock.bmaSharesCount = sharesCount;
				stock.shareCounts.add(stock.bmaSharesCount);
			}
		}
		
		return true;
	}
}
