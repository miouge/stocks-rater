package com.github.stockRater;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
//import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.stockRater.beans.GetApi;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TargetServer;
import com.github.stockRater.handlers.TSParseFundamentals;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public class Report {

	String listeCSV = "D:/GIT/MISC/SRT/stockRater/data/liste.csv";
	String reportXLS = "D:/GIT/MISC/SRT/stockRater/data/report.xlsx";
	
	public ArrayList<Stock> stocks = new ArrayList<Stock>();
	
	public void loadCSVData() throws FileNotFoundException, IOException, CsvException {
		
		CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build(); // custom separator
		
		try( CSVReader reader = new CSVReaderBuilder( new FileReader(listeCSV))
				.withCSVParser(csvParser) // custom CSV
				.withSkipLines(1) // skip the first line, header info
				.build()
		) {

			List<String[]> lines = reader.readAll();
			lines.forEach( fields -> {

				Stock stock = new Stock();
				stock.setIsin( fields[0] );
				stock.setName( fields[1] );
				stock.setMnemo( fields[2] );
				stock.setCountryCode( fields[0].substring(0, 2) );
				this.stocks.add( stock );
			});
		}
	}
	
	public void fetchStockData( Stock stock ) throws Exception {
		
		TargetServer target = new TargetServer();
		target.setBaseUrl( "https://www.tradingsat.com" );
		//target.authenticate();
		
    	// https://www.tradingsat.com/engie-FR0010208488/societe.html
		
		GetApi api = new GetApi( theApi -> {
			
			theApi.urlSuffix = String.format( "/%s-%s/societe.html",
					stock.getName(),
					stock.getIsin());
			
			theApi.stock = stock;
			theApi.handler = new TSParseFundamentals();
		});
			
		api.perform( target );
	}
	
	
	public void fetchData() throws Exception {
    
	    for( int i = 0 ; i < this.stocks.size() ; i++ ) {
	    	
	    	fetchStockData( this.stocks.get( i ) );
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
		    for( int i = 0 ; i < iMax ; i++ ) { sheet.getRow( i + 1 ).createCell( column ).setCellValue( (String) this.stocks.get(i).getName() ); }
		    
		    // ISIN
		    column = 1;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "ISIN" );
		    for( int i = 0 ; i < iMax ; i++ ) { sheet.getRow( i + 1 ).createCell( column ).setCellValue( (String) this.stocks.get(i).getIsin() ); }
		    		    
		    // MNEMO		    
		    column = 2;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Mnemo" );
		    for( int i = 0 ; i < iMax ; i++ ) { sheet.getRow( i + 1 ).createCell( column ).setCellValue( (String) this.stocks.get(i).getMnemo() ); }
		    
		    
		    // write file
		    
	        try( FileOutputStream outputStream = new FileOutputStream( reportXLS ) ) {
	        	
	            wb.write(outputStream);
	        }
	    }
	}
}
