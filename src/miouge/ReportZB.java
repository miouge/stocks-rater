package miouge;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVWriter;

import miouge.beans.Context;
import miouge.beans.GetApi;
import miouge.beans.Stock;
import miouge.beans.TargetServer;
import miouge.handlers.ZbFondamentalHandlerB;

public class ReportZB extends ReportGeneric {

	public ReportZB(Context context) {
		super(context);
	}

	public void fetchDataZb() throws Exception {
		
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
		
		System.out.println( "	search ebit, VE ..." );		
		// https://www.zonebourse.com/cours/action/VICAT-5009/fondamentaux/
		// retrieve Zone Bourse fondamentaux		
		this.stocks.forEach( stock -> {
			
			// System.out.println( String.format( "[%s] stock.zbSuffix=[%s] ignore=%s", stock.name, stock.zbSuffix, stock.toIgnore.toString() ) );
			
			if( stock.toIgnore == true ) { return; }
			//if( stock.withinPEA == false ) { return; }
			if( stock.zbSuffix == null ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/cours/action/%s/fondamentaux/", stock.zbSuffix );
				theApi.stock = stock;
				theApi.stock.zbUrl = String.format("https://www.zonebourse.com/cours/action/%s/fondamentaux/", stock.zbSuffix );				
				theApi.handler = new ZbFondamentalHandlerB();
				theApi.handler.cacheSubFolder = "/cache/zb-fondamentaux";
				theApi.charset = StandardCharsets.ISO_8859_1;
			});			
			api.perform( zoneBourse, false );
		});
	}
	
	public void fetchData() throws Exception {
		
		fetchDataZb();
	}		

	@Override
	public void compute( Stock stock ) {
		
		//System.out.println( String.format( "compute for stock <%s> ...", stock.name ));

		if( stock.histoVE != null && stock.histoVE.size() > 0 ) {
			
			stock.lastVE = stock.histoVE.get( stock.histoVE.size() - 1 );
		}
		
		if( stock.lastVE != null && stock.histoEBIT != null && stock.histoEBIT.size() > 0 ) {
			
			stock.avgEBIT = stock.histoEBIT.stream().mapToDouble( i -> i ).average().getAsDouble();
			
			if( stock.avgEBIT >= 0 ) {				
				stock.ratioVeOverEBIT = stock.lastVE / stock.avgEBIT;
			}
		}
	}
			
	private void setCsvCell( String[] fieldsOfLine, int columnIdx, Object content ) {
		
		if( content == null ) {
			
			fieldsOfLine[ columnIdx ] = "";
			
		}
		else {
			
			if( content instanceof Boolean ) {
				
				if( (Boolean)content == false ) {
					fieldsOfLine[ columnIdx ] = ""; // output void for false boolean
					return;
				}
			}
			
			fieldsOfLine[ columnIdx ] = content.toString();
		}
	}
	
	public void flushCsvData( String csvStockFile ) throws IOException {
		
		int COLUMN_NB = 9; 
		
		String stocksCSV  = context.rootFolder + "/data/" + csvStockFile;
		
		List<String[]> stringArray = new ArrayList<String[]>();
		
		String[] header = new String[COLUMN_NB];
		stringArray.add(header);
		int column = 0;
		header[column] = "ISIN";column++;
		
		header[column] = "Name";column++;
		header[column] = "Mnemo";column++;
		header[column] = "zbSuffix";column++;
		header[column] = "yahooSymbol";column++;
		header[column] = "WithinPEA";column++;
		header[column] = "ToIgnore";column++;

		for( Stock stock : this.stocks ) {
					
			String[] array = new String[COLUMN_NB];
			stringArray.add(array);
			column = 0;			
			setCsvCell( array, column++, stock.isin );			
			setCsvCell( array, column++, stock.name );
			setCsvCell( array, column++, stock.mnemo );
			setCsvCell( array, column++, stock.zbSuffix );
			setCsvCell( array, column++, stock.yahooSymbol );						
			setCsvCell( array, column++, stock.withinPEA );
			setCsvCell( array, column++, stock.toIgnore );
		}
		
	     CSVWriter writer = new CSVWriter(new FileWriter(stocksCSV), ';', '\u0000', '\\', "\n" );
	     
	     writer.writeAll(stringArray);
	     writer.close();
	     
	     System.out.println( String.format( "flush stock definitions Csv for %d stock(s) : OK", stocks.size()));
	}	
	
	@Override
	protected boolean excludeFromReport( Stock stock, boolean verbose ) {
		
		if( stock.lastVE != null && stock.lastVE < 50.0 ) {
			// société trop petite
			if( verbose ) { System.out.println( String.format( "exclude %s : too small lastVE", stock.name )); }			
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
	    
	    // NAME 		    
	    column = 0;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Name" );
	    for( int i = 0 ; i < iMax ; i++ ) {
	    	final Stock stock = selection.get(i);	    	
	    	System.out.println( String.format( "stock <%s> <%s>", stock.isin, stock.name ));	    	    	
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

	    // lastVE
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "VE (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).lastVE ).setCellStyle( precisionStyle.get(-1)); }		    	    
	    
	    // avg EBIT
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "avg EBIT (M€)" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgEBIT ).setCellStyle( precisionStyle.get(-1)); }		    
	    
	    // nb EBIT
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "EBIT Nb" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).histoEBIT.size() ).setCellStyle( precisionStyle.get(0)); }		    
    
	    
	    // ---------------- Web Sites URL ...		    
	    
	    // Zone Bourse URL
	    column++;
	    sheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse URL" );
	    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).zbUrl ); }
				
	}	
}
