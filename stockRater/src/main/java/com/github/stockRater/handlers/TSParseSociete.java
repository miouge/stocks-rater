package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class TSParseSociete extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "TS-" + stock.isin + "-societe.html";		
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
		if( data.equals( "-" ) == false ) { stock.sharesCount = Long.parseLong( data ); }
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Capitaux propres" );
			thePf.leftPattern = "<div class=\"item-data\">";
			thePf.rightPattern = "</div>";
		}); 		
		data = pf.find().replace( "&nbsp;", "" ).trim(); 
		if( data.equals( "-" ) == false ) { stock.capitauxPropres = Long.parseLong( data ) / 1000; } // en millier d'euro dans la page

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Capitalisation boursière" );
			thePf.leftPattern = "<div class=\"item-data\">";
			thePf.rightPattern = "</div>";
		}); 		
		data = pf.find().replace( "&nbsp;", "" ).replace( "M€", "" ).trim(); 
		if( data.equals( "-" ) == false ) { stock.capitalisation = Long.parseLong( data ); }		
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Ratio d'endettement" );
			thePf.leftPattern = "<div class=\"item-data\">";
			thePf.rightPattern = "</div>";
		}); 		
		data = pf.find().replace( "%", "" ).replace( "&nbsp;", "" ).trim();
		if( data.equals( "-" ) == false ) { stock.endettement = Double.parseDouble( data ); }
		
		return true;
	}
}
