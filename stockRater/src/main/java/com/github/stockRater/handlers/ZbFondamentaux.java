package com.github.stockRater.handlers;

import java.util.ArrayList;

import com.github.stockRater.beans.Stock;

public class ZbFondamentaux extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "Zb-" + stock.isin + "-fondamentaux.html";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		PatternFinder pf;
		String data;

		stock.histoEBIT = new ArrayList<Double>();
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "sultat d'exploitation (EBIT)" );
			thePf.contextPatterns.add( "bc2V tableCol0" );			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" ); // N-3
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoEBIT );

		
		if( stock.histoEBIT.size() > 0 ) {
			
			stock.histoEBIT.forEach( ebit -> {
				
				System.out.println( String.format( "stock <%s> ebit =%d", stock.name, ebit ));
				
			});
			
		}
		
		return true;
	}
}
