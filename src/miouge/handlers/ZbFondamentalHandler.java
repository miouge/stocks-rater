package miouge.handlers;

import java.util.ArrayList;

import miouge.beans.Stock;

public class ZbFondamentalHandler extends ResponseHandlerTemplate {

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
		
		// boolean debug = true;
	
		// -------------------- Valeur Entreprise ----------------
		
		stock.histoVE = new ArrayList<Double>(); // will content only 1 value
		
		for( int i = 0 ; i <= 7 ; i++ ) {
						
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
		
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( ">Valeur Entreprise</a>" );
				thePf.contextPatterns.add( tag.toString() );
				thePf.outOfContextPattern = ">PER</a>";			
				thePf.leftPattern = "style=\"background-color:#DEFEFE;\">"; // estimate coloring
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, stock.histoVE, debug );
			
			if( stock.histoVE.size() > 0 ) {
				// stop when getting the first estimate value
				break;
			}
		}
		
		if( stock.histoVE.size() > 0 ) {
			
			// take last (only one) element of the list to be the current VE
			stock.lastVE = stock.histoVE.get( stock.histoVE.size() - 1 );
			if( debug ) {
				System.out.println( String.format( "stock <%s> VE =%.2f", stock.name, stock.lastVE ));
			}
		}
		
		// -------------------- Nbr de Titres (en Milliers)  ----------------		
		ArrayList<Long> shareCounts = new ArrayList<Long>();
				
		for( int i = 0 ; i <= 7 ; i++ ) {
			
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
			
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( ">Nbr de Titres (en Milliers)</td>" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				thePf.outOfContextPattern = ">Cours de référence (";			
				thePf.leftPattern = ">";
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addLongIfNonNull( data, Long::parseLong, shareCounts, debug );
		}

		if( shareCounts.size() > 0 ) {

			// take last value of the list, that's supposed to be the last known quotation
			stock.sharesCount = ( shareCounts.get(shareCounts.size()-1) * 1000 );
			if( debug ) {
				System.out.println( String.format( "stock <%s> shareCount =%d K (list size=%d)", stock.name, stock.sharesCount, shareCounts.size() ));
			}
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

			// take last value of the list, that's supposed to be the last known quotation
			stock.lastQuote = quotes.get(quotes.size()-1);
			if( debug ) {
				System.out.println( String.format( "stock <%s> lastQuote =%.2f (list size=%d)", stock.name, stock.lastQuote, quotes.size() ));
			}
		}
		else {

			System.out.println( String.format( "stock <%s> NO QUOTE", stock.name ));
		}
		
		// -------------------- EBITDA ----------------
		stock.histoEBITDA = new ArrayList<Double>();
				
		for( int i = 0 ; i <= 7 ; i++ ) {
			
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
			
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( ">EBITDA</a>" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				thePf.outOfContextPattern = ">Résultat d'exploitation (EBIT)</a>";			
				thePf.leftPattern = ">";
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, stock.histoEBITDA, debug );
		}
		
		if( stock.histoEBITDA.size() > 0 ) {
			if( debug ) {
				System.out.println( String.format( "stock <%s> histoEBITDA size = %d", stock.name, stock.histoEBITDA.size() ));
			}
			stock.histoEBITDA.forEach( value -> {
				if( debug ) {
					System.out.println( String.format( "stock <%s> ebitda = %.2f", stock.name, value ));
				}
			});
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
			if( debug ) {
				System.out.println( String.format( "stock <%s> histoEBIT size = %d", stock.name, stock.histoEBIT.size() ));
			}
			
			stock.histoEBIT.forEach( value -> {
				if( debug ) {
					System.out.println( String.format( "stock <%s> ebit = %.2f", stock.name, value ));
				}
			});
		}
		
		// -------------------- RN ----------------
		stock.histoRN = new ArrayList<Double>();
		
		for( int i = 0 ; i <= 7 ; i++ ) {
			
			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );
			
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( ">Résultat net</a>" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				thePf.outOfContextPattern = "><i>Marge nette</i></a>";			
				thePf.leftPattern = ">";
				thePf.rightPattern = "</td>";
			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, stock.histoRN, debug );
		}

		if( stock.histoRN.size() > 0 ) {
			if( debug ) {
				System.out.println( String.format( "stock <%s> histoRN size = %d", stock.name, stock.histoRN.size() ));
			}
			
			stock.histoRN.forEach( value -> {
				if( debug ) {
					System.out.println( String.format( "stock <%s> RN = %.2f", stock.name, value ));
				}
			});
		}		
		
		// -------------------- Dividendes ----------------
		stock.histoDIV = new ArrayList<Double>();

		for( int i = 0 ; i <= 7 ; i++ ) {

			tag.setLength( 0 );
			tag.append( "bc2V tableCol" + i  );

			pf = new PatternFinder( response, thePf -> {

				thePf.contextPatterns.add( ">Dividende / Action</a>" );
				thePf.contextPatterns.add( tag.toString() ); // [ bc2V tableCol0 -> bc2V tableCol7 ]
				//thePf.outOfContextPattern = "Evolution du Compte de Résultat";
				thePf.outOfContextPattern = "navigateTable('Tableau_Histo_ECR_a','next')";
				thePf.leftPattern = "style=\"background-color:#DEFEFE;\">"; // estimate coloring
				thePf.rightPattern = "</td>";

			});
			data = pf.find().replace( " ", "" );
			addDoubleIfNonNull( data, Double::parseDouble, stock.histoDIV, debug );
		}

		if( stock.histoDIV.size() > 0 ) {
			if( debug ) {
				System.out.println( String.format( "stock <%s> histoDIV size = %d", stock.name, stock.histoDIV.size() ));
			}

			stock.histoDIV.forEach( value -> {
				if( debug ) {
					System.out.println( String.format( "stock <%s> DIV = %.2f", stock.name, value ));
				}
			});
		}

		return true;
	}
}
