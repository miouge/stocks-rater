package miouge.handlers;

import miouge.beans.Stock;

public class TSSocieteHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-societe.html";	
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		PatternFinder pf;
		String data;

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Nombre d'actions" );
			thePf.leftPattern = "<div class=\"item-data\">";
			thePf.rightPattern = "</div>";
		}); 		
		data = pf.find().replace( "&nbsp;", "" ).trim();
		
		if( data.equals("-") == false ) {
			
			Long sharesCount = Long.parseLong(data);
			if( sharesCount != null && sharesCount > 0 ) {
				stock.tsSharesCount = sharesCount; 
				stock.shareCounts.add(stock.tsSharesCount);
			}
		}
					
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Ratio d'endettement" );
			thePf.leftPattern = "<div class=\"item-data\">";
			thePf.rightPattern = "</div>";
		}); 		
		data = pf.find().replace( "%", "" ).replace( "&nbsp;", "" ).trim();
		if( data.equals( "-" ) == false ) { stock.debtRatio = Double.parseDouble( data ); }
		
//		pf = new PatternFinder( response, thePf -> {
//
//			thePf.contextPatterns.add( "Capitaux propres" );
//			thePf.leftPattern = "<div class=\"item-data\">";
//			thePf.rightPattern = "</div>";
//		}); 		
//		data = pf.find().replace( "&nbsp;", "" ).trim(); 
//		if( data.equals( "-" ) == false ) { stock.capitauxPropres = Long.parseLong( data ) / 1000; } // en millier d'euro dans la page
//
		
		return true;
	}
}
