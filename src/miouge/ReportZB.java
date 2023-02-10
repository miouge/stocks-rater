package miouge;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math.stat.regression.SimpleRegression;
//import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miouge.beans.Context;
import miouge.beans.ExclusionResume;
import miouge.beans.GetApi;
import miouge.beans.Stock;
import miouge.beans.TargetServer;
import miouge.handlers.ZbFondamentalHandler;

public class ReportZB extends ReportGeneric {

	public ReportZB(Context context) {
		super(context);
	}
	
	public void fetchData() throws Exception {
		
		System.out.println( String.format( "stocks list size = %d", this.stocks.size()) );
		
		// ---------------- ZONE BOURSE -------------------
		
		System.out.println( "fetch data from ZONE BOURSE ..." );
				
		TargetServer zoneBourse = new TargetServer();
		zoneBourse.setBaseUrl( "https://www.zonebourse.com" );
				
/** BROKEN
		System.out.println( "	search ZbSuffix ..." );
		
		// https://www.zonebourse.com/recherche/instruments/?aComposeInputSearch=s_FR		
		// retrieve Zone Bourse custom company urlSuffix from ISIN code
		// la reponse est une liste ou l'on prend l'action coté a paris
		this.stocks.forEach( stock -> {	
			
			if( stock.toIgnore == true ) { return; }
			// if( stock.withinPEA == false ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/recherche/instruments/?aComposeInputSearch=s_%S", stock.isin );
				theApi.stock = stock;
				theApi.handler = new ZbSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-searched";
			});			
			api.perform( zoneBourse, false );
		});		
*/
		
		System.out.println( "	search EBIT, VE, BNA, DIV  ..." );		
		// https://www.zonebourse.com/cours/action/VICAT-5009/fondamentaux/
		// retrieve Zone Bourse fondamentaux		
		this.stocks.forEach( stock -> {

			if( stock.toIgnore == true ) { return; }			
			if( stock.zbSuffix == null ) { return; }
			
			stock.zbUrl = String.format("https://www.zonebourse.com/cours/action/%s/fondamentaux/", stock.zbSuffix );
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/cours/action/%s/fondamentaux/", stock.zbSuffix );
				theApi.stock = stock;				
				theApi.handler = new ZbFondamentalHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-fondamentaux";
				theApi.charset = StandardCharsets.ISO_8859_1;
			});			
			api.perform( zoneBourse, false );
		});
		
		System.out.println( "fetch data completed." );
	}		
	
	Double computeRegression(ArrayList<Double> data) {

		int n = data.size();
	    
	    if( n < 5 ) {
	        return null;
	    }		

	    SimpleRegression regression = new SimpleRegression();

	    for (int i = 0; i < data.size(); i++) {
	      regression.addData( i+1, data.get(i) );
	    }
	    // y = intercept + slope * x
	    double slope = regression.getSlope();
	    //double intercept = regression.getIntercept();
	    
	    Double average = data.stream().mapToDouble( i -> i ).average().getAsDouble();
	    
	    Double growthRate = (slope / average) * 100.0;
	    return growthRate;
	}
		
	@Override
	public void compute( Stock stock ) {
		
		// available data :
		// stock.lastVE
		// stock.lastQuote
		// stock.shareCount
		// stock.histoEBITDA -> avgEBITDA
		// stock.histoEBIT   -> avgEBIT
		// stock.histoRN     -> avgRN
		// stock.histoDIV	 -> avgDIV (il y a pb des divisions)
				
//		System.out.println( String.format( "compute for stock <%s> ...", stock.name ));
//		
//		if( stock.name.equals( "Akwel" )) {
//			int i=0;
//			i++;
//		}
		
		// compute average, and growth
		if( stock.histoEBITDA != null && stock.histoEBITDA.size() > 0 ) {
			stock.avgEBITDA = stock.histoEBITDA.stream().mapToDouble( i -> i ).average().getAsDouble();
			stock.sizeEBITDA = stock.histoEBITDA.size();
			stock.growthEBITDA = computeRegression( stock.histoEBITDA );
		}
		if( stock.histoEBIT != null && stock.histoEBIT.size() > 0 ) {
			stock.avgEBIT = stock.histoEBIT.stream().mapToDouble( i -> i ).average().getAsDouble();
			stock.sizeEBIT = stock.histoEBIT.size();
			stock.growthEBIT = computeRegression( stock.histoEBIT );
		}
		if( stock.histoRN != null && stock.histoRN.size() > 0 ) {
			stock.avgRN = stock.histoRN.stream().mapToDouble( i -> i ).average().getAsDouble();
			stock.sizeRN = stock.histoRN.size();
			stock.growthRN = computeRegression( stock.histoRN );

			if( stock.histoDIV != null && stock.histoDIV.size() > 0 ) {
				stock.avgDIV = ( stock.histoDIV.stream().mapToDouble( i -> i ).sum() / stock.histoRN.size() ); // on prend le nb de RN et pas le nb de dividende pour tenir compte des dividendes non versés
				stock.sizeDIV = stock.histoDIV.size();
				stock.growthDIV = computeRegression( stock.histoDIV );
			}
		}
		
		// ratio EBIT/VE
		if( stock.lastVE != null && stock.avgEBIT != null && stock.avgEBIT > 0 ) {
			
			stock.ratioVeOverEBIT = stock.lastVE / stock.avgEBIT;

			if( stock.ratioVeOverEBIT < 8.0 ) {
				stock.reasonsPos.add( String.format("VE/EBIT=%.1f", stock.ratioVeOverEBIT ));
			}
			
			// rating
			// max 1.0
			// VE/EBIT <=4 -> 1
			// VE/EBIT   8 -> 1.0
			// VE/EBIT  12 -> 0.0
			stock.ratingProfitability = Math.min( 1.5 - 0.125 * stock.ratioVeOverEBIT, 1.0 );
		}

		// avg BNA
		if( stock.avgRN != null && stock.avgRN > 0 && stock.sharesCount != null ) {
			
			stock.avgBNA = ( stock.avgRN * 1000000.0 ) / stock.sharesCount;
		}		
		
		// PER
		if( stock.lastQuote != null && stock.lastQuote > 0 && stock.avgBNA != null && stock.avgBNA > 0 ) {
			
			stock.avgPER = stock.lastQuote / stock.avgBNA;
			
			if( stock.avgPER < 10.0 ) {
				stock.reasonsPos.add( String.format("PER=%.1f", stock.avgPER ));
			}
			
			// max 1.0
			// PER <= 5     -> 1.0 
			// PER = 10     -> 0.5
			// PER = 15 	->  0
			Double ratingPER = Math.min( 1.5 - 0.10 * stock.avgPER, 1.0 );
			stock.ratingProfitability = Math.max( stock.ratingProfitability, ratingPER );
		}
		
		// Rendement %
		if( stock.lastQuote != null && stock.lastQuote > 0 && stock.avgDIV != null && stock.avgDIV > 0 ) {
		
			stock.rdtPerc = ( stock.avgDIV / stock.lastQuote ) * 100.0;
		
			// payout %
			if( stock.avgBNA != null && stock.avgBNA > 0 ) {
				
				stock.payoutPerc = ( stock.avgDIV / stock.avgBNA ) * 100.0;
			
				if( stock.rdtPerc > 4.5 && stock.payoutPerc < 95 ) {
					stock.reasonsPos.add( String.format("RDT=%.1f%%", stock.rdtPerc ));
				}				
			}			
		}		
		
		// DFN
		if( stock.lastVE != null && stock.lastQuote != null && stock.sharesCount != null ) {
			
			stock.dfn = stock.lastVE - (stock.lastQuote * stock.sharesCount)/1000000.0; // DFN = VE - Capitalization
			// stock.dfn peut etre négatif -> trésorerie nette
		}
		
		// ratio DFN / EBITDA
		if( stock.avgEBITDA != null && stock.avgEBITDA > 0 && stock.dfn != null && stock.dfn > 0 ) {
			
			stock.ratioDfnOverEBITDA = stock.dfn / stock.avgEBITDA;
			
			if( stock.ratioDfnOverEBITDA > 2.5 ) {
				stock.reasonsAgainst.add( String.format("DFN/EBIDA=%.1f", stock.ratioDfnOverEBITDA ));
			}
			
			// [1.0 -> -0.5]
			Double rating = Math.min( 1 - 0.25 * stock.ratioDfnOverEBITDA, 1.0 );			
			stock.ratingSolidity = Math.max( -0.5, rating );			
		}
		
		//leverage TODO : solidiy
		
		// ratio trésorerie per share
		if( stock.sharesCount != null && stock.sharesCount > 0 && stock.dfn != null && stock.dfn < 0 ) {
			
			stock.netCashPS = -1.0 * (( stock.dfn * 1000000) / stock.sharesCount);
						
			if( stock.lastQuote != null && stock.lastQuote > 0 ) {				
				Double cashPerc = stock.netCashPS / stock.lastQuote;				
				if( cashPerc > 0.1 ) {				
					stock.reasonsPos.add( String.format("CashPS=%.1f€", stock.netCashPS ));
					stock.ratingSolidity += 0.25;
				}
			}						
		}
		
		// ratio last Quote / Book value
		if( stock.lastQuote != null && stock.BVPS != null && stock.BVPS > 0) {			
			stock.ratioQuoteBV = stock.lastQuote / stock.BVPS;
			
			if( stock.ratioQuoteBV < 1.0 ) {
				stock.reasonsPos.add( String.format("Quote/BV=%.1f", stock.ratioQuoteBV ));
				
				// [+1.0 -> 0 ]
				// Quote/BV = 1.0 -> +0.5
				// Quote/BV = 0.5 -> +0.75				
				stock.ratingValue = Math.max( 0.0, (-0.5 * stock.ratioQuoteBV + 1 ));
			}
		}

		if( stock.growthEBITDA != null && stock.growthEBITDA > 0.0 ) {
			if( stock.growthEBIT != null && stock.growthEBIT > 0.0 ) {
				
				if( stock.growthEBIT > 5.0 ) {
					
					stock.reasonsPos.add( String.format("growth=%.0f", stock.growthEBIT ));					
				}
				
				// max : ]-1.0 -> 1.0]
				// growthEBIT = 15.0 -> + 1.0
				// growthEBIT =  5.0 -> + 0.3				
				stock.ratingGrowth = Math.max( -1.0, Math.min( stock.growthEBIT / 15.0, 1.0 ) );
				
				if( stock.ratingProfitability > 0.0 ) {
					stock.ratingGrowth *= stock.ratingProfitability;
				}
				else {
					stock.ratingGrowth = 0.0;
				}				
				
			}
		}
		
		stock.ratingProfitability *= 2.0;
		stock.rating = stock.ratingProfitability + stock.ratingSolidity + stock.ratingGrowth + stock.ratingValue;
		
		// remove 0.0 value like
		
		if( stock.ratingProfitability <= 0.01 ) { stock.ratingProfitability = null; }
		if( stock.ratingSolidity      <= 0.01 ) { stock.ratingSolidity = null;      }
		if( stock.ratingGrowth        <= 0.01 ) { stock.ratingGrowth = null;        }
		if( stock.ratingValue         <= 0.01 ) { stock.ratingValue = null;		    }
	}
	
	private boolean removeUnadequate( ExclusionResume resume, Stock stock, boolean verbose ) {
		
		if( stock.inPortfolio ) {			
			// never exclude
			return false;
		}
		
		boolean fondamentalAvailable = false;
		
		if( stock.avgPER != null ) {
			fondamentalAvailable = true;
		} 	
		if( stock.ratioVeOverEBIT != null ) {
			fondamentalAvailable = true;
		} 	
		if( stock.ratioQuoteBV != null ) {
			fondamentalAvailable = true;
		}
		
		if( fondamentalAvailable == false ) {		
			
			// données fondamentales non disponible
			resume.fundamentalsUnavailable++;
			// System.out.println( String.format( "EXCLUDED : fundamentals not available [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			return true;
		}
	
		if( stock.lastVE != null && stock.lastVE < 30.0 ) {
	
			// société trop petite			
			// System.out.println( String.format( "EXCLUDED : too small VE [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			resume.tooSmallVE++;
			return true;
		}
	
		if( stock.avgRN != null && ( stock.avgRN < 0 ) ) {
			
			// société non rentable			
			// System.out.println( String.format( "EXCLUDED : non profitable [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			resume.nonProfitable++;
			return true;
		}
	
		if( stock.histoEBITDA != null ) {
		
			for( Double ebitda : stock.histoEBITDA ) {
				if( ebitda < 0 ) {					
					
					// System.out.println( String.format( "EXCLUDED : negative ebit found in history [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
					resume.operationalLoss++;
					return true;
				}
			}									
		}
		
		if( stock.histoEBIT != null ) {
			
			int count = 0;
			for( Double ebit : stock.histoEBIT ) {
				if( ebit < 0 ) {
					count++;
				}
			}
			if( count >= 2 ) {
				// at least two operational lost
				resume.operationalLoss++;
				return true;
			}			
		}
		
		if( stock.rating != null && ( stock.rating < 1.0 ) ) {
			
			// société non rentable			
			// System.out.println( String.format( "EXCLUDED : non profitable [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			resume.insufficientRating++;
			return true;
		}
		
		return false;
	}
		
	private boolean selectMinRatingRequirement( ExclusionResume resume, Stock stock, boolean verbose ) {
		
//		if( stock.inPortfolio ) {
//			// never exclude
//			return false;
//		}
		
		if( stock.ratingProfitability == null ) { return true; }
		if( stock.ratingSolidity      == null ) { return true; }
		if( stock.ratingGrowth        == null ) { return true; }
		if( stock.ratingValue         == null ) { return true; }			
		if( stock.ratingProfitability <= 1.1 ) { return true; }
		//if( stock.ratingSolidity      <= 0.3 ) { return true; }
		//if( stock.ratingGrowth        <= 0.3 ) { return true; }
		if( stock.ratingValue         <= 0.3 ) { return true; }
		return false;
	}

	@Override
	protected boolean excludeFromReport( ExclusionResume resume, Stock stock, boolean verbose ) {
		
		//return removeUnadequate( resume, stock, verbose );
		return selectMinRatingRequirement( resume, stock, verbose );
	}
	
	@Override
	void composeReport( XSSFWorkbook wb, HashMap<Integer,CellStyle> precisionStyle, ArrayList<Stock> selection ) throws Exception {
				
	    // sort the selection by rating (best rating first)	    
	    Collections.sort( selection, (o1, o2) -> {
	    	
	    	Double crit1 = o1.rating;
	    	Double crit2 = o2.rating;
	    	if( crit1 > crit2 ) { return -1; }
	    	if( crit1 < crit2 ) { return  1; }
	    	return 0;
	    });
		
		CreationHelper ch = wb.getCreationHelper();
		
	    // create an empty work sheet
	    XSSFSheet ratioSheet = wb.createSheet("report");
	    
	    // create an empty work sheet
	    XSSFSheet reasonSheet = wb.createSheet("reasons");
	    		    
	    // header row
	    ratioSheet.createRow( 0 ); // [ 0 : first row
	    reasonSheet.createRow( 0 ); // [ 0 : first row	    
		
	    // prepare 1 row for each selected stock		
		for( int row = 0 ; row < selection.size() ; row++ ) {
		
	    	ratioSheet.createRow( row + 1 );
	    	reasonSheet.createRow( row + 1 );
		}
		
		System.out.println( String.format( "XSSFSheet row [ %d - %d ]", ratioSheet.getFirstRowNum(),  ratioSheet.getLastRowNum() ));
	    
	    // ********************* Compose Report Sheet ***********************

	    XSSFSheet sheet = ratioSheet;

	    int column;
	    int iMax = selection.size();
	    	   
	    // elligible PEA
	    column = 0;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "PEA" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).withinPEALabel ); }

	    // Rating
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "RATING" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).rating ).setCellStyle( precisionStyle.get(-1)); }
	    
	    // NAME
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "NAME" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	
	    	//System.out.println( String.format( "stock name <%s> zbSuffix <%s> ...", stock.name,  stock.zbSuffix ));
	    	
	    	createCell( sheet.getRow( i + 1 ), column, stock.name ).setCellStyle( createStyle( wb, stock, style -> {
	    		
	        	if( stock.inPortfolio ) {
	    		
		    		final Font font = wb.createFont ();
		    		//font.setFontName( "Calibri" );
		    		font.setItalic( true );
		    		font.setBold( true );
		    		style.setFont(font);
		    	}
	        	style.setAlignment(HorizontalAlignment.LEFT);	    		
	    		// if( stock.inPortfolio ) { setBackgroundColor( style, HSSFColor.HSSFColorPredefined.PALE_BLUE );	}
	    }));}
	    
	    // in Portfolio
	    // column++;
	    // sheet.getRow(0).createCell( column ).setCellValue( (String) "OWNED" );
	    // for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).inPortfolio ); }	    

	    // ISIN
	    // column++;
	    // sheet.getRow(0).createCell( column ).setCellValue( (String) "ISIN" );
	    // for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).isin ); }
	    	    
	    // MNEMO
	    // column++;
	    // sheet.getRow(0).createCell( column ).setCellValue( (String) "Mnemo" );
	    // for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).mnemo ); }

	    // lastVE
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "VE (M€)" );	    
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).lastVE ).setCellStyle( precisionStyle.get(1000)); }	    

	    // VE / EBIT
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "VE/EBIT" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, stock.ratioVeOverEBIT ).setCellStyle( createStyle( wb, stock, style -> {
	    					
				style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));

				if( stock.ratioVeOverEBIT != null ) {
					
					if( stock.ratioVeOverEBIT < 8.0 && stock.ratioVeOverEBIT > 0.0 ) {
						
					    Font font = wb.createFont();
					    font.setBold( true );
					    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
					    font.setColor(IndexedColors.GREEN.getIndex());
					    style.setFont(font);
						//setBackgroundColor( style, HSSFColor.HSSFColorPredefined.LIGHT_GREEN );
					}
					if( stock.ratioVeOverEBIT > 12.0 ) {
						
					    Font font = wb.createFont();
					    font.setBold( true );
					    font.setColor(IndexedColors.RED.getIndex());
					    style.setFont(font);												
						// setBackgroundColor( style, HSSFColor.HSSFColorPredefined.LIGHT_ORANGE );
					}
				}
	    }));}

	    // PER
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "PER" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, stock.avgPER ).setCellStyle( createStyle( wb, stock, style -> {
	    					
				style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));

				if( stock.avgPER != null ) {
					if( stock.avgPER < 10.0 && stock.avgPER > 0.0 ) {
					    Font font = wb.createFont();
					    font.setBold( true );
					    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
					    font.setColor(IndexedColors.GREEN.getIndex());
					    style.setFont(font);
						//setBackgroundColor( style, HSSFColor.HSSFColorPredefined.LIGHT_GREEN );
					}
					if( stock.avgPER > 15.0 ) {
					    
						Font font = wb.createFont();
					    font.setBold( true );
					    font.setColor(IndexedColors.RED.getIndex());
					    style.setFont(font);						
						//setBackgroundColor( style, HSSFColor.HSSFColorPredefined.LIGHT_ORANGE );
					}
				}
	    }));}
	    	    
	    // Rendement %
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "RDT %" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).rdtPerc ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.rdtPerc != null && stock.rdtPerc > 5.0 ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
			    font.setColor(IndexedColors.GREEN.getIndex());
			    style.setFont(font);	    				
			}
		}));}

	    // payout %
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "PAYOUT %" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).payoutPerc ).setCellStyle( precisionStyle.get(0)); }		    

	    // DFN / EBITDA
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "DFN/EBITDA" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratioDfnOverEBITDA ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.ratioDfnOverEBITDA != null && stock.ratioDfnOverEBITDA > 3.0 ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setColor(IndexedColors.RED.getIndex());
			    style.setFont(font);	    				
			}
		}));}	    

	    // ratio cours / active Net
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "QUOTE/BV" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratioQuoteBV ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.ratioQuoteBV != null && stock.ratioQuoteBV < 0.8 ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
			    font.setColor(IndexedColors.GREEN.getIndex());
			    style.setFont(font);	    				
			}
		}));}	    
	    
	    // Book value per share
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "BVPS" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).BVPS ).setCellStyle( precisionStyle.get(-1)); }
	    
	    // Net Cash per Share
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "CashPS" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).netCashPS ).setCellStyle( precisionStyle.get(-2)); }	    
	    
	    // cours de référence %
	    column++;	    
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "REF_QUOTE" );
	    //for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).lastQuote ).setCellStyle( precisionStyle.get(-1)); }	    
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).lastQuote ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.lastQuote != null ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setColor(IndexedColors.BLUE.getIndex());
			    style.setFont(font);	    				
			}
		}));}		    
	    
	    // avg BNA
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgBNA (€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgBNA ).setCellStyle( precisionStyle.get(-1)); }    
	    
	    // avg DIV - nb DIV
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgDIV (€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgDIV ).setCellStyle( precisionStyle.get(-2)); }	    
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "DIV Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sizeDIV ); }
	    
	    // Croissance DIV %
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwDIV" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthDIV ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.growthDIV != null && stock.growthDIV > 5.0 ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
			    font.setColor(IndexedColors.GREEN.getIndex());
			    style.setFont(font);	    				
			}
		}));}	    

	    // sortie de calculs 
       	    
	    // avg EBITDA - nb EBITDA
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgEBITDA (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgEBITDA ).setCellStyle( precisionStyle.get(-1)); }	    
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "EBITDA Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sizeEBITDA ); }		    
	    // Croissance EBITDA %
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwEBITDA" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthEBITDA ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.growthEBITDA != null && stock.growthEBITDA > 5.0 ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
			    font.setColor(IndexedColors.GREEN.getIndex());
			    style.setFont(font);	    				
			}
		}));}
	    
	    // avg EBIT - nb EBIT
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgEBIT (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgEBIT ).setCellStyle( precisionStyle.get(-1)); }
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "EBIT Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sizeEBIT ); }		    
	    // Croissance EBIT %
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwEBIT" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthEBIT ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.growthEBIT != null && stock.growthEBIT > 5.0 ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
			    font.setColor(IndexedColors.GREEN.getIndex());
			    style.setFont(font);	    				
			}
		}));}
	    
	    // avg RN - nb RN
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgRN (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgRN ).setCellStyle( precisionStyle.get(-1)); }
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "RN Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sizeRN ); }		    
	    // Croissance RN %
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwRN" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthRN ).setCellStyle( createStyle( wb, stock, style -> {			
			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
			if( stock.growthRN != null && stock.growthRN > 5.0 ) {
			    Font font = wb.createFont();
			    font.setBold( true );
			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
			    font.setColor(IndexedColors.GREEN.getIndex());
			    style.setFont(font);	    				
			}
		}));}
	    
	    // ---------------- Web Sites URL ...		    
	    
	    // Zone Bourse URL
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse URL" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).zbUrl ); }
		
	    // ********************* Compose Reason Sheet ***********************
	    
	    sheet = reasonSheet;
	    	   
	    // elligible PEA
	    column = 0;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "PEA" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).withinPEALabel ); }

	    // NAME
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "NAME" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	
	    	//System.out.println( String.format( "stock name <%s> zbSuffix <%s> ...", stock.name,  stock.zbSuffix ));
	    	
	    	createCell( sheet.getRow( i + 1 ), column, stock.name ).setCellStyle( createStyle( wb, stock, style -> {
	    		
	        	if( stock.inPortfolio ) {
	    		
		    		final Font font = wb.createFont ();
		    		//font.setFontName( "Calibri" );
		    		font.setItalic( true );
		    		font.setBold( true );
		    		style.setFont(font);
		    	}
	        	style.setAlignment(HorizontalAlignment.LEFT);	    		
	    		// if( stock.inPortfolio ) { setBackgroundColor( style, HSSFColor.HSSFColorPredefined.PALE_BLUE );	}
	    }));}	    
	    
	    // Rating
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "RATING" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).rating ).setCellStyle( precisionStyle.get(-1)); }

	    // profitability Rating
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Profitability" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratingProfitability ).setCellStyle( precisionStyle.get(-1)); }

	    // solidity Rating
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Solidity" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratingSolidity ).setCellStyle( precisionStyle.get(-1)); }

	    // growth Rating
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Growth" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratingGrowth ).setCellStyle( precisionStyle.get(-1)); }

	    // value Rating
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Value" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratingValue ).setCellStyle( precisionStyle.get(-1)); }
	    
	    // 5 positives reasons
	    for( Integer r = 1 ; r < 6 ; r++ ) {
	    	
	    	// for each reasons
	    	
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) ("Reasons#" + r.toString()) );
		    for( int i = 0 ; i < iMax ; i++ ) {
		    	
		    	final Stock stock = selection.get(i);
		    	if( stock.reasonsPos.size() < r ) {
		    		continue;
		    	}	    	
		    	createCell( sheet.getRow( i + 1 ), column, stock.reasonsPos.get(r-1)).setCellStyle( createStyle( wb, stock, style -> {			
		    		
				    Font font = wb.createFont();
				    //font.setBold( true );
				    //font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
				    font.setColor(IndexedColors.GREEN.getIndex());
				    style.setFont(font);
				    style.setAlignment(HorizontalAlignment.CENTER);	
		    	}));
		    }
	    }
	    
	    // 5 negatives reasons
	    for( Integer r = 1 ; r < 3 ; r++ ) {
	    	
	    	// for each reasons
	    	
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) ("Reasons#" + r.toString()) );
		    for( int i = 0 ; i < iMax ; i++ ) {
		    	
		    	final Stock stock = selection.get(i);
		    	if( stock.reasonsAgainst.size() < r ) {
		    		continue;
		    	}	    	
		    	createCell( sheet.getRow( i + 1 ), column, stock.reasonsAgainst.get(r-1)).setCellStyle( createStyle( wb, stock, style -> {			
		    		
				    Font font = wb.createFont();
				    //font.setBold( true );
				    //font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
				    font.setColor(IndexedColors.RED.getIndex());
				    style.setFont(font);
				    style.setAlignment(HorizontalAlignment.CENTER);	
		    	}));
		    }
	    }
	    
	    // Zone Bourse URL
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse URL" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).zbUrl ); }	    
	}
}
