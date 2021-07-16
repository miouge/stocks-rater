package com.github.stockRater.handlers;

import java.util.ArrayList;

import com.github.stockRater.beans.Stock;

public class AbcSocieteHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-societe.html";
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

		// ratio d'endettement
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<td class=\"allf\">Ratio d'endettement</td>" );
			thePf.outOfContextPattern = "<td class=\"allf\">Effectif en fin d'année</td>";
			thePf.leftPattern = "<td>";
			thePf.rightPattern = "</td>";
		});
		
		stock.histoDebtRatio = new ArrayList<Double>();	
		
		data = pf.find().replace( " ", "" ); // N-5
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio );

		data = pf.find().replace( " ", "" ); // N-4
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio );

		data = pf.find().replace( " ", "" ); // N-3
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio );

		data = pf.find().replace( " ", "" ); // N-2
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio );

		data = pf.find().replace( " ", "" ); // N-1
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio );
		
		return true;
	}
}
 