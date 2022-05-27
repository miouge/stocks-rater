package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class AbcSearchHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.isin + "-cotation.html";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response, boolean debug ) throws Exception {
		
		PatternFinder pf;
		String data;

		// Custom suffix
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<script type=\"application/ld+json\">" );
			thePf.leftPattern = "www.abcbourse.com/cotation/";
			thePf.rightPattern = "\"";
		}); 		
		data = pf.find().trim();
		if( data.equals("-") == false ) {
			stock.abcSuffix = data;
		}

		// Mnemo
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<script type=\"application/ld+json\"> " );
			thePf.leftPattern = "\"tickerSymbol\":\"";
			thePf.rightPattern = "\"";
		}); 		
		data = pf.find().trim().toUpperCase();		
		if( stock.mnemo == null || stock.mnemo.length() == 0 ) {
			stock.mnemo = data;
		}

		// Name
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<script type=\"application/ld+json\">" );
			thePf.leftPattern = "\"name\":\"";
			thePf.rightPattern = "\"";
		}); 		
		data = pf.find().replace("&#x2B;", "").replace("&#x27;", "").replace("&amp;", "").replace("&#x2019;","").replace("  "," ").trim();
		if( stock.name == null || stock.name.length() == 0 ) {
			stock.name = data;
		}
		
		// withinPEA
		
		if( stock.withinPEA == null ) { 
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( "<td>SRD / PEA</td>" );
				thePf.outOfContextPattern = ">TTF</a>";
				thePf.leftPattern = " / ";
				thePf.rightPattern = "</td>";
			}); 		
			data = pf.find().replace("/", "").replace("  "," ").toLowerCase().trim();
			if( data.equals("oui")) {
				stock.withinPEA = true;
			}
		}
		
		// TTF
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( ">TTF</a>" );
			thePf.outOfContextPattern = "</table>";
			thePf.leftPattern = "<td class=\"alri\">";
			thePf.rightPattern = "</td>";
		}); 		
		data = pf.find().trim().toLowerCase();
		if( data.equals("oui")) {
			stock.withTTF = true;
			stock.withTTFLabel = "TTF";
		}
		
		// System.out.println(String.format("%s-%s-%s", stock.isin, stock.mnemo, stock.name));
		
		return true;
	}
}
