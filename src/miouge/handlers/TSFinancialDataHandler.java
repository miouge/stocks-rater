package miouge.handlers;

import java.util.ArrayList;

import miouge.beans.Stock;

public class TSFinancialDataHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-donnees-financieres.html";
	}
	
	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		boolean debug = false;
		
		PatternFinder pf;
		String data;
		
		// last 5 years RNPG (K€)	
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<strong>COMPTE DE RÉSULTATS EN MILLIERS D'EUROS</strong>" );
			thePf.contextPatterns.add( "Résultat net (part du groupe)" ); // ou RN part du groupe
			thePf.outOfContextPattern = "<strong>BILAN EN MILLIERS D'EUROS</strong>";
			thePf.leftPattern = "<td class=\"text-right\">";
			thePf.rightPattern = "</td>";
		});
		
//		if( stock.histoRNPG == null ) {
//			stock.histoRNPG = new ArrayList<Long>();
//		}
//				
//		data = pf.find().replace( "&nbsp;", "" ); // N-5
//		addLongIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-4
//		addLongIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-3
//		addLongIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-2
//		addLongIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-1
//		addLongIfNonNull( data, Long::parseLong, stock.histoRNPG, debug );

		// last 5 years CP (K€), le dernier etant le plus récent
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<strong>BILAN EN MILLIERS D'EUROS</strong>" );
			thePf.contextPatterns.add( "Capitaux propres" ); // ou RN part du groupe
			thePf.outOfContextPattern = "Provisions pour risques et charges non courantes";
			thePf.leftPattern = "<td class=\"text-right\">";
			thePf.rightPattern = "</td>";
		});
		
//		stock.histoCP = new ArrayList<Long>();	
//		
//		data = pf.find().replace( "&nbsp;", "" ); // N-5
//		addLongIfNonNull( data, Long::parseLong, stock.histoCP, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-4
//		addLongIfNonNull( data, Long::parseLong, stock.histoCP, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-3
//		addLongIfNonNull( data, Long::parseLong, stock.histoCP, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-2
//		addLongIfNonNull( data, Long::parseLong, stock.histoCP, debug );
//
//		data = pf.find().replace( "&nbsp;", "" ); // N-1
//		addLongIfNonNull( data, Long::parseLong, stock.histoCP, debug );
		
		
		return true;
	}
}
