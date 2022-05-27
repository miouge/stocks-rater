package com.github.stockRater.handlers;

import java.util.ArrayList;

import com.github.stockRater.beans.Stock;

public class AbcSocieteHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-societe.html";
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response, boolean debug ) throws Exception {
		
		// System.out.println( String.format( "processing %s ...", this.getDumpFilename(stock)));
		
		PatternFinder pf;
		String data;

//		pf = new PatternFinder( response, thePf -> {
//
//			thePf.contextPatterns.add( "Eligible au PEA" );
//			thePf.leftPattern = "<td>";
//			thePf.rightPattern = "</td>";
//		}); 		
//		data = pf.find().toLowerCase().trim();		
//		if( data.equals( "oui" )) {
//		
//			stock.withinPEA = new Boolean(true);
//		}
//		else {
//			
//			// don't parse anymore as not withinPEA 
//			return true;
//		}

		// Share count
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Nombre de titres" );
			thePf.leftPattern = "<td>";
			thePf.rightPattern = "</td>";
		}); 		
		data = pf.find().toLowerCase().trim().replaceAll("\u00a0",""); // \u00a0 est l'espace insécable  

		if( data.equals("-") == false ) {			
			Long sharesCount = Long.parseLong(data);
			if( sharesCount != null && sharesCount > 0 ) {				
				stock.abcSharesCount = sharesCount;
				stock.shareCounts.add(stock.abcSharesCount);
			}
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
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio, debug );

		data = pf.find().replace( " ", "" ); // N-4
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio, debug );

		data = pf.find().replace( " ", "" ); // N-3
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio, debug );

		data = pf.find().replace( " ", "" ); // N-2
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio, debug );

		data = pf.find().replace( " ", "" ); // N-1
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoDebtRatio, debug );

		// Chiffre d'affaire
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<td class=\"allf\">Chiffre d'affaires</td>" );
			thePf.outOfContextPattern = "<td class=\"allf\">Produits des activités ordinaires</td>";
			thePf.leftPattern = "<td>";
			thePf.rightPattern = "</td>";
		});
		
		stock.histoCA = new ArrayList<Long>();
				
		data = pf.find().replaceAll("\u00a0",""); // N-5
		addIfNonNull( data, Long::parseLong, stock.histoCA, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-4
		addIfNonNull( data, Long::parseLong, stock.histoCA, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-3
		addIfNonNull( data, Long::parseLong, stock.histoCA, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-2
		addIfNonNull( data, Long::parseLong, stock.histoCA, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-1
		addIfNonNull( data, Long::parseLong, stock.histoCA, debug );
		
		// RNPG

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<td class=\"allf\">Résultat net (part du groupe)</td>" );
			thePf.outOfContextPattern = "</table>";
			thePf.leftPattern = "<td>";
			thePf.rightPattern = "</td>";
		});

		if( stock.histoRNPG == null ) {
			stock.histoRNPG = new ArrayList<Long>();
		}
		
		data = pf.find().replaceAll("\u00a0",""); // N-5
		addIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-4
		addIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-3
		addIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-2
		addIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );

		data = pf.find().replaceAll("\u00a0",""); // N-1
		addIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );
		
		return true;
	}
}
 