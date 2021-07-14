package com.github.stockRater.handlers;

import java.util.ArrayList;

import com.github.stockRater.beans.Stock;

public class TSFinancialDataHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "TS-" + stock.isin + "-donnees-financieres.html";		
	}
	
	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		PatternFinder pf;
		String data;
		
		// RNPG 5 years		
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<strong>COMPTE DE RÉSULTATS EN MILLIERS D'EUROS</strong>" );
			thePf.contextPatterns.add( "Résultat net (part du groupe)" ); // ou RN part du groupe
			thePf.outOfContextPattern = "<strong>BILAN EN MILLIERS D'EUROS</strong>";
			thePf.leftPattern = "<td class=\"text-right\">";
			thePf.rightPattern = "</td>";
		});
		
		stock.histoRNPG = new ArrayList<Long>();	
		
		data = pf.find().replace( "&nbsp;", "" ); // N-5
		addIfNonNull( data, Long::parseLong, stock.histoRNPG );

		data = pf.find().replace( "&nbsp;", "" ); // N-4
		addIfNonNull( data, Long::parseLong, stock.histoRNPG );

		data = pf.find().replace( "&nbsp;", "" ); // N-3
		addIfNonNull( data, Long::parseLong, stock.histoRNPG );

		data = pf.find().replace( "&nbsp;", "" ); // N-2
		addIfNonNull( data, Long::parseLong, stock.histoRNPG );

		data = pf.find().replace( "&nbsp;", "" ); // N-1
		addIfNonNull( data, Long::parseLong, stock.histoRNPG );

		// Capitaux propres 5 years
		// en K€ le dernier etant le plus récent
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<strong>BILAN EN MILLIERS D'EUROS</strong>" );
			thePf.contextPatterns.add( "Capitaux propres" ); // ou RN part du groupe
			thePf.outOfContextPattern = "Provisions pour risques et charges non courantes";
			thePf.leftPattern = "<td class=\"text-right\">";
			thePf.rightPattern = "</td>";
		});
		
		stock.histoCP = new ArrayList<Long>();	
		
		data = pf.find().replace( "&nbsp;", "" ); // N-5
		addIfNonNull( data, Long::parseLong, stock.histoCP );

		data = pf.find().replace( "&nbsp;", "" ); // N-4
		addIfNonNull( data, Long::parseLong, stock.histoCP );

		data = pf.find().replace( "&nbsp;", "" ); // N-3
		addIfNonNull( data, Long::parseLong, stock.histoCP );

		data = pf.find().replace( "&nbsp;", "" ); // N-2
		addIfNonNull( data, Long::parseLong, stock.histoCP );

		data = pf.find().replace( "&nbsp;", "" ); // N-1
		addIfNonNull( data, Long::parseLong, stock.histoCP );
		
		
		return true;
	}
}
