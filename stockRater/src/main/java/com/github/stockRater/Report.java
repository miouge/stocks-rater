package com.github.stockRater;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.stockRater.beans.Context;
import com.github.stockRater.beans.GetApi;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TargetServer;
import com.github.stockRater.handlers.TSGetCompanyPageUrl;
import com.github.stockRater.handlers.TSParseFinancialData;
import com.github.stockRater.handlers.TSParseSociete;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public class Report {

	Context context;
	
	@SuppressWarnings("unused")
	private Report() {}
	
	public Report(Context context) {
		super();
		this.context = context;
	}

	public ArrayList<Stock> stocks = new ArrayList<Stock>();
	
	public void loadCsvData( String csvStockFile ) throws FileNotFoundException, IOException, CsvException {
		
		CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build(); // custom separator
	
		String stocksCSV  = context.rootFolder + "/data/" + csvStockFile;
		
		try( CSVReader reader = new CSVReaderBuilder( new FileReader(stocksCSV))
				.withCSVParser(csvParser) // custom CSV
				.withSkipLines(1) // skip the first line (header info)
				.build()
		) {

			List<String[]> lines = reader.readAll();
			lines.forEach( fields -> {

				Stock stock = new Stock();
				stock.isin =fields[0];
				stock.name = fields[1];
				stock.mnemo = fields[2];
				stock.countryCode = fields[0].substring(0, 2);
				this.stocks.add( stock );
			});
		}
		System.out.println( String.format( "%d stock definitions loaded", this.stocks.size() ));
	}
	
	public void compute( Stock stock ) {
		
		System.out.println( String.format( "compute for stock <%s> ...", stock.name ));
		
//		if( stock.name.equals("Spir Communication")) {
//			
//			int i = 5;
//		}
		
		if( stock.histoRNPG != null && stock.histoRNPG.size() > 0 ) {
			
			for( int i = 0 ; i < stock.histoRNPG.size() ; i++ ) {			
				System.out.println( String.format( "histoRNPG [%d] = %d K€ ", i, stock.histoRNPG.get(i) ));
			}
			
			stock.avgRNPG = stock.histoRNPG.stream().mapToLong(Long::longValue).average().getAsDouble();
			System.out.println( String.format( "avg RNPDG = %.2f K€ ", stock.avgRNPG ));
		}
		
		if( stock.lastQuote != null && stock.sharesCount != null && stock.avgRNPG != null && stock.avgRNPG > 0 ) {

			Double avgEarningPerShare = ( stock.avgRNPG * 1000.0 ) / stock.sharesCount;
			if( avgEarningPerShare > 0 ) {
				stock.avg5yPER = stock.lastQuote / avgEarningPerShare;
			}
		}
		
		if( stock.lastQuote != null && stock.sharesCount != null && stock.capitauxPropres != null ) {

			Double cpPerShare = ( stock.capitauxPropres * 1000000.0 ) / stock.sharesCount;
			if( cpPerShare > 0 ) {
				stock.ratioQuoteBV = stock.lastQuote / cpPerShare;
			}
		}		
	}	
	
	public void fetchStockData( Stock stock ) throws Exception {
		
		TargetServer target = new TargetServer();
		target.setBaseUrl( "https://www.tradingsat.com" );
		//target.authenticate();
		
		// retrieve Trading Sat Company Page from ISIN code
		{	
			GetApi api = new GetApi( theApi -> {
				
				theApi.urlSuffix = String.format( "/async/json/instrument-search.php?term=%s", stock.isin );
				theApi.stock = stock;
				theApi.handler = new TSGetCompanyPageUrl();
				theApi.handler.cacheFolder =  this.context.rootFolder + "/cache/ts-searched";
			});			
			api.perform( target );
		}
		
		// https://www.tradingsat.com/vicat-FR0000031775/societe.html
		{
			GetApi api = new GetApi( theApi -> {
				
				theApi.urlSuffix = stock.tradingSatUrlSuffix + "societe.html";
				theApi.stock = stock;
				theApi.handler = new TSParseSociete();
				theApi.handler.cacheFolder =  this.context.rootFolder + "/cache/ts-societe";
			});			
			api.perform( target );
		}

		// https://www.tradingsat.com/vicat-FR0000031775/donnees-financieres.html
		{
			GetApi api = new GetApi( theApi -> {
				
				theApi.urlSuffix = stock.tradingSatUrlSuffix + "donnees-financieres.html";
				theApi.stock = stock;
				theApi.handler = new TSParseFinancialData();
				theApi.handler.cacheFolder =  this.context.rootFolder + "/cache/ts-donnees-financieres";
			});			
			api.perform( target );
		}
		
		this.compute(stock);
	}
	
	public void fetchData() throws Exception {
    
	    for( int i = 0 ; i < this.stocks.size() ; i++ ) {
	    	
	    	Stock stock = this.stocks.get( i );
	    	// if( stock.getCountryCode().equals( "FR" ) == false ) { continue; }
	    	
	    	fetchStockData( stock );
	    }
	}
	
	private void createCell( XSSFRow row, int column, Object content ) {
		
		XSSFCell cell = row.createCell(column);
		
		if( content == null ) {
			
			cell.setBlank();
			
		}
		else if( content instanceof String ) {
			
			cell.setCellValue( (String) content );
		} 
		else if(  content instanceof Long  ) {
			
			cell.setCellValue( (Long) content );
		}
		else if(  content instanceof Double  ) {
			
			cell.setCellValue( (Double) content );
		}
	}
			
	public void output() throws IOException {
			
	    // create 1 empty workbook
	    try( XSSFWorkbook wb = new XSSFWorkbook()) {
	    	
	    	CreationHelper ch = wb.getCreationHelper();
	    	
	    	// create the style for all the date
	    	CellStyle cellDateStyle = wb.createCellStyle();
	        cellDateStyle.setDataFormat( ch.createDataFormat().getFormat("yyyy/mm/dd hh:mm:ss") );
	        
	        // create predefined style to go along with some data precision if needed
	        HashMap<Integer,CellStyle> precisionStyle = new HashMap<Integer,CellStyle>();
	        
	        { // could also use the 'Double Brace Initialization' 
	        	CellStyle style = wb.createCellStyle();
	        	style.setDataFormat( ch.createDataFormat().getFormat("#0.0000"));
	        	precisionStyle.put( -4, style );
	        }
	        {
	        	CellStyle style = wb.createCellStyle();
	        	style.setDataFormat( ch.createDataFormat().getFormat("#0.000"));
	        	precisionStyle.put( -3, style );
	        }
	        {
	        	CellStyle style = wb.createCellStyle();
	        	style.setDataFormat( ch.createDataFormat().getFormat("#0.00"));
	        	precisionStyle.put( -2, style );
	        }
	        {
	        	CellStyle style = wb.createCellStyle();
	        	style.setDataFormat( ch.createDataFormat().getFormat("#0.0"));
	        	precisionStyle.put( -1, style );
	        }
	    		        
		    // create an empty work sheet
		    XSSFSheet sheet = wb.createSheet("data");
		    
		    // header row
		    sheet.createRow( 0 ); // [ 0 : first row

		    // 1 row for each stock
		    for( int i = 0 ; i < this.stocks.size() ; i++ ) {
		    	
		    	sheet.createRow( i + 1 ); // [ 0 : first row
		    }
		    
		    int column;
		    int iMax = this.stocks.size();
		    
		    // NAME 		    
		    column = 0;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Name" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).name ); }
		    
		    // ISIN
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "ISIN" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).isin ); }		        		    
		    
		    // MNEMO		    
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Mnemo" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).mnemo ); }

		    // Nombre d'actions
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Nombre d'actions" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).sharesCount ); }
		    
		    // capitalisation
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Capitalisation (M€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).capitalisation ); }		    
		    
		    // capitaux propres		    
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Capitaux propres (M€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).capitauxPropres ); }
		    
		    // avgRNPG		    
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "5y-Avg RNPG (K€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).avgRNPG ); }
		    
		    // Ratio d'endettement
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Endettement %" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).endettement ); }
		    
		    // Ratio Book Value
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Book value ratio" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).ratioQuoteBV ); }

		    // 5 years avg PER
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "5y-Avg PER" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).avg5yPER ); }

		    
		    // trading Sat URL
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "TradingSat URL" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).tradingSatUrl ); }

		    // trading Sat URL
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "TradingSat Suffix" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, this.stocks.get(i).tradingSatUrlSuffix ); }		    
		    
		    
		    // write file
		    
			String reportXLS = this.context.rootFolder + "/" + "output/report.xlsx";
		    
	        try( FileOutputStream outputStream = new FileOutputStream( reportXLS ) ) {
	        	
	            wb.write(outputStream);
	        }
	    }
	}
}
