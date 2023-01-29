package miouge;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math.stat.regression.SimpleRegression;
//import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miouge.beans.Context;
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
	    double intercept = regression.getIntercept();
	    
	    Double annualGrowthRate = (slope * 100.0)/intercept;		

	    //System.out.println( String.format( "tauxCroissanceAnnuel <%.2f>", annualGrowthRate ));
	    return annualGrowthRate;
	}
		
	@Override
	public void compute( Stock stock ) {
		
		// données disponibles
		// stock.lastVE
		// stock.lastQuote
		// stock.shareCount
		// stock.histoEBITDA -> avgEBITDA
		// stock.histoEBIT   -> avgEBIT
		// stock.histoRN     -> avgRN
		// stock.histoDIV	 -> avgDIV (pb des divisions)
				
		// System.out.println( String.format( "compute for stock <%s> ...", stock.name ));
		
//		if( stock.name.equals( "1000Mercis" )) {
//		
//		int i=0;
//		i++;
//	}		
		
		if( stock.histoEBITDA != null && stock.histoEBITDA.size() > 0 ) {
			stock.avgEBITDA = stock.histoEBITDA.stream().mapToDouble( i -> i ).average().getAsDouble();
			stock.sizeEBITDA = stock.histoEBITDA.size();
			
			//stock.histoEBITDA  = new ArrayList<>(Arrays.asList(526.0, 557.0, 619.0, 582.0, 563.0, 592.0));
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
				stock.avgDIV = ( stock.histoDIV.stream().mapToDouble( i -> i ).sum() / stock.histoRN.size() ); // on prend le nb de RN et pas le nb de dividende
				stock.sizeDIV = stock.histoDIV.size();
				stock.growthDIV = computeRegression( stock.histoDIV );
			}			
		}		
		
		// ratio EBIT/VE
		if( stock.lastVE != null && stock.avgEBIT != null && stock.avgEBIT > 0 ) {
			
			stock.ratioVeOverEBIT = stock.lastVE / stock.avgEBIT;
		}

		// Rendement %
		if( stock.lastQuote != null && stock.lastQuote > 0 && stock.avgDIV != null && stock.avgDIV > 0 ) {
		
			stock.rdtPerc = ( stock.avgDIV / stock.lastQuote ) * 100.0;
		}
		
		// avg BNA
		if( stock.avgRN != null && stock.avgRN > 0 && stock.sharesCount != null ) {
			
			stock.avgBNA = ( stock.avgRN * 1000000.0 ) / stock.sharesCount;
		}
		
		// payout %
		if( stock.avgBNA != null && stock.avgBNA > 0 && stock.avgDIV != null && stock.avgDIV > 0 ) {
			
			stock.payoutPerc = ( stock.avgDIV / stock.avgBNA ) * 100.0;
		}
		
		// PER
		if( stock.lastQuote != null && stock.lastQuote > 0 && stock.avgBNA != null && stock.avgBNA > 0 ) {
			
			stock.avgPER = stock.lastQuote / stock.avgBNA;
		}
		
		// DFN
		if( stock.lastVE != null && stock.lastQuote != null && stock.sharesCount != null ) {
			
			stock.dfn = stock.lastVE - (stock.lastQuote * stock.sharesCount)/1000000.0; // DFN = VE - Capitalization
			// stock.dfn peut etre négatif -> trésorerie nette
		}
		
		// ratio DFN / EBITDA
		if( stock.avgEBITDA != null && stock.avgEBITDA > 0 && stock.dfn != null && stock.dfn > 0 ) {
			
			stock.ratioDfnOverEBITDA = stock.dfn / stock.avgEBITDA;
		}
		
		// ratio trésorerie per share
		if( stock.sharesCount != null && stock.sharesCount > 0 && stock.dfn != null && stock.dfn < 0 ) {
			
			stock.netCashPS = -1.0 * (( stock.dfn * 1000000) / stock.sharesCount);
		}
		
		// ratio last Quote / Book value
		if( stock.lastQuote != null && stock.BVPS != null && stock.BVPS > 0) {			
			stock.ratioQuoteBV = stock.lastQuote / stock.BVPS;
		}
	}
	
	@Override
	protected boolean excludeFromReport( Stock stock, boolean verbose ) {
		
//		if( stock.name.equals( "1000Mercis" )) {
//			
//			int i=0;
//			i++;
//		}

		if( stock.lastVE != null && stock.lastVE < 30.0 ) {
			// société trop petite			
			// System.out.println( String.format( "EXCLUDED : too small VE [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			return true;
		}
		
		if( stock.histoDIV != null && stock.histoDIV.size() < 5 ) {
			// société trop petite			
			System.out.println( String.format( "EXCLUDED : NO ENOUGH DIVIDENDS [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			return true;
		}		
		
		// il faut que 
		// soit le ratio VE/EBIT < 14 
		// soit le            PE < 13
		
		if( stock.avgPER == null && stock.ratioVeOverEBIT == null ) {
			
			System.out.println( String.format( "EXCLUDED : NO DATA {PER|EBIT} [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			return true;
		}

		// il faut au moins 1 aspect interessant sur l'action
		
		boolean interesting = false;
		
		if( stock.avgPER != null && (stock.avgPER <= 10.0) ) { // PER acceptable
			interesting = true;
			stock.rating++;
		} 
		if( stock.ratioVeOverEBIT != null && stock.ratioVeOverEBIT <= 10.0 ) { // VE/EBIT acceptable
			interesting = true;
			stock.rating++;
		}
		if( stock.rdtPerc != null && stock.rdtPerc >= 5.0 && stock.histoDIV.size() >= 8 ) { // versement régulier de dividende
			interesting = true;
			stock.rating++;
		}
		if( stock.ratioQuoteBV != null && stock.ratioQuoteBV <= 0.5 ) { // quote/BV acceptable
			interesting = true;
			stock.rating++;
		}	
		
		if( interesting == false ) {
			
			System.out.println( String.format( "EXCLUDED : OUT OF PREREQUISITED FONDAMENTAL [%s] zbSuffix=[%s]", stock.name, stock.zbSuffix ) );
			return true;
		}

		return false;
	}

	@Override
	void composeReport( XSSFWorkbook wb, HashMap<Integer,CellStyle> precisionStyle, ArrayList<Stock> selection ) throws Exception {
		
		CreationHelper ch = wb.getCreationHelper();
		
	    // create an empty work sheet
	    XSSFSheet reportSheet = wb.createSheet("report");
	    
	    // create an empty work sheet
	    XSSFSheet sourcesSheet = wb.createSheet("sources");
	    		    
	    // header row
	    reportSheet.createRow( 0 ); // [ 0 : first row
	    sourcesSheet.createRow( 0 ); // [ 0 : first row		
		
	    // prepare 1 row for each selected stock		
		for( int row = 0 ; row < selection.size() ; row++ ) {
		
	    	reportSheet.createRow( row + 1 );
	    	sourcesSheet.createRow( row + 1 );
		}
		
		System.out.println( String.format( "XSSFSheet row [ %d - %d ]", reportSheet.getFirstRowNum(),  reportSheet.getLastRowNum() ));
	    
	    // ********************* Compose Report Sheet ***********************

	    XSSFSheet sheet = reportSheet;

	    int column;
	    int iMax = selection.size();
	    	   
	    // elligible PEA
	    column = 0;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "PEA" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).withinPEALabel ); }

	    // Rating
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "RATING" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).rating ); }	    
	    
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
	    
//	    // Croissance EBITDA %
//	    column++;
//	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwEBITDA" );
//	    for( int i = 0 ; i < iMax ; i++ ) {
//	    	final Stock stock = selection.get(i);
//	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthEBITDA ).setCellStyle( createStyle( wb, stock, style -> {			
//			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
//			if( stock.growthEBITDA != null && stock.growthEBITDA > 5.0 ) {
//			    Font font = wb.createFont();
//			    font.setBold( true );
//			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
//			    font.setColor(IndexedColors.GREEN.getIndex());
//			    style.setFont(font);	    				
//			}
//		}));}
//	    
//	    // Croissance EBIT %
//	    column++;
//	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwEBIT" );
//	    for( int i = 0 ; i < iMax ; i++ ) {
//	    	final Stock stock = selection.get(i);
//	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthEBIT ).setCellStyle( createStyle( wb, stock, style -> {			
//			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
//			if( stock.growthEBIT != null && stock.growthEBIT > 5.0 ) {
//			    Font font = wb.createFont();
//			    font.setBold( true );
//			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
//			    font.setColor(IndexedColors.GREEN.getIndex());
//			    style.setFont(font);	    				
//			}
//		}));}
//	    
//	    // Croissance RN %
//	    column++;
//	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwRN" );
//	    for( int i = 0 ; i < iMax ; i++ ) {
//	    	final Stock stock = selection.get(i);
//	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthRN ).setCellStyle( createStyle( wb, stock, style -> {			
//			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
//			if( stock.growthRN != null && stock.growthRN > 5.0 ) {
//			    Font font = wb.createFont();
//			    font.setBold( true );
//			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
//			    font.setColor(IndexedColors.GREEN.getIndex());
//			    style.setFont(font);	    				
//			}
//		}));}
//
//	    // Croissance DIV %
//	    column++;
//	    sheet.getRow(0).createCell( column ).setCellValue( (String) "GrwDIV" );
//	    for( int i = 0 ; i < iMax ; i++ ) {
//	    	final Stock stock = selection.get(i);
//	    	createCell( sheet.getRow( i + 1 ), column, selection.get(i).growthDIV ).setCellStyle( createStyle( wb, stock, style -> {			
//			style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));			
//			if( stock.growthDIV != null && stock.growthDIV > 5.0 ) {
//			    Font font = wb.createFont();
//			    font.setBold( true );
//			    font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
//			    font.setColor(IndexedColors.GREEN.getIndex());
//			    style.setFont(font);	    				
//			}
//		}));}
	    
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

	    // sortie de calculs 
	    
	    // avg EBITDA - nb EBITDA
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgEBITDA (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgEBITDA ).setCellStyle( precisionStyle.get(-1)); }	    
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "EBITDA Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sizeEBITDA ); }		    
    
	    // avg EBIT - nb EBIT
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgEBIT (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgEBIT ).setCellStyle( precisionStyle.get(-1)); }
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "EBIT Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sizeEBIT ); }		    

	    // avg RN - nb RN
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avgRN (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgRN ).setCellStyle( precisionStyle.get(-1)); }
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "RN Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sizeRN ); }		    

	    
	    
	    // ---------------- Web Sites URL ...		    
	    
	    // Zone Bourse URL
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse URL" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).zbUrl ); }
				
	}
}
