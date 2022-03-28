package com.github.stockRater;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.stockRater.beans.Context;
import com.github.stockRater.beans.GetApi;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TargetServer;
import com.github.stockRater.handlers.ZbFondamentalHandlerB;
import com.github.stockRater.handlers.ZbSearchHandler;

public class ReportB extends ReportGeneric {

	Context context;

	public ReportB(Context context) {
		super(context);
	}

	public void fetchDataZb() throws Exception {
		
		// ---------------- ZONE BOURSE -------------------
		
		System.out.println( "fetch data from ZONE BOURSE ..." );
				
		TargetServer zoneBourse = new TargetServer();
		zoneBourse.setBaseUrl( "https://www.zonebourse.com" );
		
		System.out.println( "	search ZbSuffix ..." );
		
		// https://www.zonebourse.com/recherche/instruments/?aComposeInputSearch=s_FR		
		// retrieve Zone Bourse custom company urlSuffix from ISIN code
		// la reponse est une liste ou l'on prend l'action coté a paris
		this.stocks.forEach( stock -> {	
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/recherche/instruments/?aComposeInputSearch=s_%S", stock.isin );
				theApi.stock = stock;
				theApi.handler = new ZbSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-searched";
			});			
			api.perform( zoneBourse );
		});
		
		System.out.println( "	search ebit, VE ..." );
		
		// https://www.zonebourse.com/cours/action/VICAT-5009/fondamentaux/

		// retrieve Zone Bourse fondamentaux
		// last 3 years EBIT (résultat d'exploitation)
		// last 3 years Valeur d'entreprise (résultat d'exploitation) (TODO normalement dépend de la cotation)
		// TODO : dette nette ou trésorerie nette
		// TODO : free-cash flow
		
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.zbSuffix == null ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/cours/action/%s/fondamentaux/", stock.zbSuffix );
				theApi.stock = stock;
				theApi.handler = new ZbFondamentalHandlerB();
				theApi.handler.cacheSubFolder = "/cache/zb-fondamentaux";
				theApi.charset = StandardCharsets.ISO_8859_1;
			});			
			api.perform( zoneBourse );
		});		
	}	
	
	public void fetchData() throws Exception {
		
		fetchDataZb();
	}		

	@Override
	public void compute( Stock stock ) {
		
		//System.out.println( String.format( "compute for stock <%s> ...", stock.name ));
		
//		if( stock.name.equals("Spir Communication")) {
//			
//			int i = 5;
//		}
		
		if( stock.histoVE != null && stock.histoEBIT != null && stock.histoVE.size() > 0 && stock.histoEBIT.size() > 0 ) {
			
			stock.avgEBIT = stock.histoEBIT.stream().mapToDouble( i -> i ).average().getAsDouble();
			
			if( stock.avgEBIT >= 0 ) {
				
				Double lastVE = stock.histoVE.get(stock.histoVE.size()-1);
				stock.ratioVeOverEBIT = lastVE / stock.avgEBIT;
			}
		}		
	}	
			
	@Override
	protected boolean excludeFromReport( Stock stock, boolean verbose ) {
		
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
		
	    	reportSheet.createRow( row + 1 ); // [ 0 : first row
	    	sourcesSheet.createRow( row + 1 ); // [ 0 : first row
	    	row++;			
		}
	    
	    // ********************* Compose Report Sheet ***********************
	    
	    XSSFSheet sheet = reportSheet;
	    
	    int column;
	    int iMax = selection.size();
	    
	    // NAME 		    
	    column = 0;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Name" );
	    for( int i = 0 ; i < iMax ; i++ ) {    	
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, stock.name ).setCellStyle( createStyle( wb, stock, style -> {
	    		if( stock.portfolio > 0 ) {
	    			setBackgroundColor( style, HSSFColor.HSSFColorPredefined.PALE_BLUE );
	    		}
	    }));}
	    
	    // ISIN
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "ISIN" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).isin ); }
	    
	    // MNEMO		    
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Mnemo" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).mnemo ); }
	    
	    // VE / EBIT
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "VE/EBIT" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);
	    	createCell( sheet.getRow( i + 1 ), column, stock.ratioVeOverEBIT ).setCellStyle( createStyle( wb, stock, style -> {
	    					
				style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));

				if( stock.ratioVeOverEBIT != null ) {
					if( stock.ratioVeOverEBIT < 8.0 ) {
						setBackgroundColor( style, HSSFColor.HSSFColorPredefined.LIGHT_GREEN );
					}
					if( stock.ratioVeOverEBIT > 12.0 ) {
						setBackgroundColor( style, HSSFColor.HSSFColorPredefined.LIGHT_ORANGE );
					}
				}
	    }));}
	     
	    // ---------------- Web Sites URL ...		    
	    
	    // Zone Bourse URL
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse URL" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).zbUrl ); }
				
	}	
}
