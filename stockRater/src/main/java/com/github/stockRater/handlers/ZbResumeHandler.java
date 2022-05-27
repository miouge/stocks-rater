package com.github.stockRater.handlers;

import com.github.stockRater.beans.Stock;

public class ZbResumeHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-resume.html";	
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response, boolean debug ) throws Exception {

		// System.out.println( String.format( "processing %s ...", this.getDumpFilename(stock)));

		PatternFinder pf;
		String data;
		
		// "Trésorerie nette 2021"

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<td>Trésorerie nette 2021</td>" );		
			thePf.outOfContextPattern = "<nobr>Plus de Données financières</nobr>";			
			thePf.leftPattern = "<b>";
			thePf.rightPattern = "</b>"; // <b>3,62&nbsp;M</b>
		});		
		
		data = pf.find().replace(" ", "").replace("&nbsp;", "").replace("M", "").replace(",", ".").trim();

		if( data.equals("-") == false ) {
		
			stock.dfnZb = Double.parseDouble( data ) * -1.0; 
			return true;
		}

		// "Dette nette 2021"

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<td>Dette nette 2021</td>" );		
			thePf.outOfContextPattern = "<nobr>Plus de Données financières</nobr>";			
			thePf.leftPattern = "<b>";
			thePf.rightPattern = "</b>"; // <b>3,62&nbsp;M</b>
		});		
		
		data = pf.find().replace(" ", "").replace("&nbsp;", "").replace("M", "").replace(",", ".").trim();

		if( data.equals("-") == false ) {
		
			stock.dfnZb = Double.parseDouble( data ); 
			return true;
		}
		
		// "Trésorerie nette 2020"

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<td>Trésorerie nette 2020</td>" );		
			thePf.outOfContextPattern = "<nobr>Plus de Données financières</nobr>";			
			thePf.leftPattern = "<b>";
			thePf.rightPattern = "</b>"; // <b>3,62&nbsp;M</b>
		});		
		
		data = pf.find().replace(" ", "").replace("&nbsp;", "").replace("M", "").replace(",", ".").trim();

		if( data.equals("-") == false ) {
		
			stock.dfnZb = Double.parseDouble( data ) * -1.0; 
			return true;
		}
		
		// "Dette nette 2020"

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "<td>Dette nette 2020</td>" );		
			thePf.outOfContextPattern = "<nobr>Plus de Données financières</nobr>";			
			thePf.leftPattern = "<b>";
			thePf.rightPattern = "</b>"; // <b>3,62&nbsp;M</b>
		});		
		
		data = pf.find().replace(" ", "").replace("&nbsp;", "").replace("M", "").replace(",", ".").trim();

		if( data.equals("-") == false ) {
		
			stock.dfnZb = Double.parseDouble( data ); 
			return true;
		}
		
		return true;
	}
}
