package miouge.handlers;

import java.util.ArrayList;

import miouge.beans.Stock;

public class ZbFondamentalHandlerB extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-fondamentaux.html";	
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response, boolean debug ) throws Exception {

		// System.out.println( String.format( "ZbFondamentalHandlerB : processing %s ...", this.getDumpFilename(stock)));

		PatternFinder pf;
		String data;
		StringBuilder tag = new StringBuilder();
	
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

				if( debug ) {
					System.out.println( String.format( "stock <%s> histoVE size = %d", stock.name, stock.histoVE.size() ));
					System.out.println( String.format( "stock <%s> ve =%.2f", stock.name, ebit ));
				}
			});
		}
		
		// -------------------- Cours de référence  ----------------		
		ArrayList<Double> quotes = new ArrayList<Double>();
				
		for( int i = 0 ; i <= 7 ; i++ ) {
			
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
			
			pf = new PatternFinder( response, thePf -> {
	
				//thePf.contextPatterns.add( ">Cours de référence (EUR)</td>" );
				thePf.contextPatterns.add( ">Cours de référence (" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				thePf.outOfContextPattern = ">Date de publication</td>";			
				thePf.leftPattern = ">";
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, quotes, debug );
		}
		
		if( quotes.size() > 0 ) {
			
			// take last value of the list, that's supposed to be the last quotation
			
			stock.lastQuote = quotes.get(quotes.size()-1);
			//System.out.println( String.format( "stock <%s> lastQuote =%.2f", stock.name, stock.lastQuote ));
			// return true;
		}
		else {
			
			System.out.println( String.format( "stock <%s> NO QUOTE", stock.name ));
			return false;
		}	
		
		// -------------------- EBIT ----------------
		stock.histoEBIT = new ArrayList<Double>();
		
				
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
				if( debug ) {
					System.out.println( String.format( "stock <%s> histoEBIT size = %d", stock.name, stock.histoEBIT.size() ));
					System.out.println( String.format( "stock <%s> ebit =%.2f", stock.name, ebit ));
				}
			});
		}
		
		// -------------------- BNA ----------------
		stock.histoBNA = new ArrayList<Double>();
		
		for( int i = 0 ; i <= 7 ; i++ ) {
			
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
			
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( ">BNA</a>" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				thePf.outOfContextPattern = ">Free Cash Flow</a>";			
				thePf.leftPattern = ">";
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, stock.histoBNA, debug );
		}
				
		// -------------------- Dividendes ----------------
		stock.histoDIV = new ArrayList<Double>();
		
		for( int i = 0 ; i <= 7 ; i++ ) {
			
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
			
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( ">Dividende / Action</a>" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				thePf.outOfContextPattern = "Evolution du Compte de Résultat";			
				thePf.leftPattern = ">";
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, stock.histoDIV, debug );
		}
		
		return true;

	}
}
