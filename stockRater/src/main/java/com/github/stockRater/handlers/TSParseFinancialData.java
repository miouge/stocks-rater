package com.github.stockRater.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.stockRater.beans.Stock;

public class TSParseFinancialData extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "TS-" + stock.isin + "-donnees-financieres.html";		
	}

	void addIfNonNull( String data, Function<String,Object> converter, List<Long> list ) {
		
		if( data == null ) {
			return;
		}
		
		if( data.equals( "-" )) {
			return;
		}
		
		Object converted = converter.apply( data );
		list.add( (Long) converted );
	}
	
	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		PatternFinder pf;
		String data;
		
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

		return true;
	}
}
