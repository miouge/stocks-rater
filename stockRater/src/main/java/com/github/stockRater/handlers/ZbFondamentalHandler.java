package com.github.stockRater.handlers;

import java.util.ArrayList;

import com.github.stockRater.beans.Stock;

public class ZbFondamentalHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "Zb-" + stock.isin + "-fondamentaux.html";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		System.out.println( String.format( "processing %s ...", this.getDumpFilename(stock)));

		
		
		
		
/*		
		PatternFinder pf;
		String data;

		// -------------------- EBIT ----------------
		stock.histoEBIT = new ArrayList<Double>();
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "sultat d'exploitation (EBIT)" );
			thePf.contextPatterns.add( "bc2V tableCol0" );			
			thePf.outOfContextPattern = "<i>Marge d'exploitation</i>";			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" );
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoEBIT );

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "sultat d'exploitation (EBIT)" );
			thePf.contextPatterns.add( "bc2V tableCol1" );
			thePf.outOfContextPattern = "<i>Marge d'exploitation</i>";
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" );
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoEBIT );

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "sultat d'exploitation (EBIT)" );
			thePf.contextPatterns.add( "bc2V tableCol2" );	
			thePf.outOfContextPattern = "<i>Marge d'exploitation</i>";
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" ); // N-3
   		addDoubleIfNonNull( data, Double::parseDouble, stock.histoEBIT );		
		
		if( stock.histoEBIT.size() > 0 ) {
			
			stock.histoEBIT.forEach( ebit -> {
				
				System.out.println( String.format( "stock <%s> ebit =%.2f", stock.name, ebit ));
				
			});
		}
		
		// -------------------- Dette nette ----------------
		
		stock.histoNetDebt = new ArrayList<Double>();
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( ">Dette Nette</a>" );
			thePf.contextPatterns.add( "bc2V tableCol0" );			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" );
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoNetDebt );

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( ">Dette Nette</a>" );
			thePf.contextPatterns.add( "bc2V tableCol1" );			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" );
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoNetDebt );

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( ">Dette Nette</a>" );
			thePf.contextPatterns.add( "bc2V tableCol2" );			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" ); // N-3
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoNetDebt );		
		
		if( stock.histoNetDebt.size() > 0 ) {
			
			stock.histoNetDebt.forEach( histoNetDebt -> {
				
				System.out.println( String.format( "stock <%s> debt =%.2f", stock.name, histoNetDebt ));
				
			});
		}

		// -------------------- Tresorie nette ----------------
		
		stock.histoNetTres = new ArrayList<Double>();
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "sorerie Nette</a>" );
			thePf.contextPatterns.add( "bc2V tableCol0" );			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" );
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoNetTres );

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "sorerie Nette</a>" );
			thePf.contextPatterns.add( "bc2V tableCol1" );			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" );
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoNetTres );

		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "sorerie Nette</a>" );
			thePf.contextPatterns.add( "bc2V tableCol2" );			
			thePf.leftPattern = "style=\"\">";
			thePf.rightPattern = "</td>";
		});
		data = pf.find().replace( " ", "" ); // N-3
		addDoubleIfNonNull( data, Double::parseDouble, stock.histoNetTres );		
		
		if( stock.histoNetTres.size() > 0 ) {
			
			stock.histoNetTres.forEach( histoNetTres -> {
				
				System.out.println( String.format( "stock <%s> tres =%.2f", stock.name, histoNetTres ));
				
			});
		}
*/		
		return true;
	}
}
