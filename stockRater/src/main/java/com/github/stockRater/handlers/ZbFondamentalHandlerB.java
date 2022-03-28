package com.github.stockRater.handlers;

import java.util.ArrayList;

import com.github.stockRater.beans.Stock;

public class ZbFondamentalHandlerB extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-fondamentaux.html";	
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		// System.out.println( String.format( "processing %s ...", this.getDumpFilename(stock)));

		PatternFinder pf;
		String data;

		boolean debug = false;
	
		// -------------------- EBIT ----------------
		stock.histoEBIT = new ArrayList<Double>();
		
		StringBuilder tag = new StringBuilder();
		
		for( int i = 0 ; i <= 7 ; i++ ) {
			
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
			
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( ">Résultat d'exploitation (EBIT)</a>" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				thePf.outOfContextPattern = "<i>Marge d'exploitation</i>";			
				thePf.leftPattern = ">";
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, stock.histoEBIT, debug );
		}
		
		if( stock.histoEBIT.size() > 0 ) {			
			stock.histoEBIT.forEach( ebit -> {
				
				// System.out.println( String.format( "stock <%s> histoEBIT size = %d", stock.name, stock.histoEBIT.size() ));
				// System.out.println( String.format( "stock <%s> ebit =%.2f", stock.name, ebit ));				
			});
		}
				
		// -------------------- Valeur Entreprise ----------------
		stock.histoVE = new ArrayList<Double>();
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( ">Valeur Entreprise</a>" );
			thePf.contextPatterns.add( "bc2V tableCol0" );			
			thePf.outOfContextPattern = ">PER</a>";			
			thePf.leftPattern = "style=\"background-color:#DEFEFE;\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" );
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoVE, debug );
		
		if( stock.histoVE.size() > 0 ) {
			
			stock.histoVE.forEach( ebit -> {

				// System.out.println( String.format( "stock <%s> histoVE size = %d", stock.name, stock.histoVE.size() ));
				// System.out.println( String.format( "stock <%s> ve =%.2f", stock.name, ebit ));
			});
		}	
	
		return true;
	}
}
