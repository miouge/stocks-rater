package com.github.stockRater;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import com.github.stockRater.handlers.AbcSearchHandler;
import com.github.stockRater.handlers.AbcSocieteHandler;
import com.github.stockRater.handlers.TSFinancialDataHandler;
import com.github.stockRater.handlers.TSSearchIsinHandler;
import com.github.stockRater.handlers.TSSocieteHandler;
import com.github.stockRater.handlers.ZbFondamentalHandler;
import com.github.stockRater.handlers.ZbSearchHandler;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
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
				this.stocks.add( stock );
				int idx = 0;
				
				stock.isin=fields[idx++];				
				stock.name=fields[idx++];
				stock.mnemo=fields[idx++]; // also name of "ticker"
				
				stock.countryCode = stock.isin.substring(0, 2);
				if( fields.length <= 3 ) { return; } // when loading file from ABC bourse
				
				if( fields[idx].length() > 0 ) {stock.withinPEA=Boolean.parseBoolean( fields[idx] ); }; idx++;
				if( fields[idx].length() > 0 ) {stock.toIgnore=Boolean.parseBoolean(fields[idx]); }; idx++;
				
				stock.aphaSymbol = fields[idx++];
				
				if( fields[idx].length() > 0 ) { stock.initShareCount=Long.parseLong(fields[idx]); }; idx++;
				if( fields[idx].length() > 0 ) { stock.offsetRNPG=Long.parseLong(fields[idx]); }; idx++;
				if( fields[idx].length() > 0 ) { stock.offsetFCFW=Long.parseLong(fields[idx]); }; idx++;
				if( fields[idx].length() > 0 ) { stock.offsetDividends=Double.parseDouble(fields[idx]); }; idx++;
				
				stock.activity=fields[idx++];				
				stock.commentOnIgnore=fields[idx++];	
				stock.commentOnOffsets=fields[idx++];
			});
		}
		System.out.println( String.format( "%d stock definitions loaded", this.stocks.size() ));
	}

	public void flushCsvData() throws IOException {
		
		int COLUMN_NB = 13; 
		
		String stocksCSV  = context.rootFolder + "/data/" + "stocks-out.csv";
		
		List<String[]> stringArray = new ArrayList<String[]>();
		
		String[] header = new String[COLUMN_NB];
		stringArray.add(header);
		int column = 0;
		header[column] = "ISIN";column++;
		header[column] = "Name";column++;
		header[column] = "Mnemo";column++;
		
		header[column] = "WithinPEA";column++;
		header[column] = "ToIgnore";column++;
		header[column] = "alphaSymbol";column++;
		
		// overrides
		header[column] = "initShareCount";column++;
		header[column] = "OffsetRNPG";column++;
		header[column] = "OffsetFCFW";column++;
		header[column] = "OffsetDiv";column++;
		
		header[column] = "Activity";column++;
		header[column] = "Comment On Ignore";column++;		
		header[column] = "Comment On Offset";column++;						

		for( Stock stock : this.stocks ) {
			
			String[] array = new String[COLUMN_NB];
			stringArray.add(array);
			column = 0;			
			setCsvCell( array, column++, stock.isin );
			setCsvCell( array, column++, stock.name );
			setCsvCell( array, column++, stock.mnemo );	
			
			setCsvCell( array, column++, stock.withinPEA );
			setCsvCell( array, column++, stock.toIgnore );
			setCsvCell( array, column++, stock.aphaSymbol );
			
			setCsvCell( array, column++, stock.initShareCount );
			setCsvCell( array, column++, stock.offsetRNPG );
			setCsvCell( array, column++, stock.offsetFCFW );	
			setCsvCell( array, column++, stock.offsetDividends );
			
			setCsvCell( array, column++, stock.activity );
			setCsvCell( array, column++, stock.commentOnIgnore );
			setCsvCell( array, column++, stock.commentOnOffsets );
		}
		
	     CSVWriter writer = new CSVWriter(new FileWriter(stocksCSV), ';', '\u0000', '\\', "\n" );
	     
	     writer.writeAll(stringArray);
	     writer.close();
	}	
	
	public void fetchStockData( Stock stock ) throws Exception {
		
/*
		TargetServer yahoo = new TargetServer();
		yahoo.setBaseUrl( "https://" );
			
		// https://query2.finance.yahoo.com/v1/finance/search?q=FR0000120222&lang=en-US&region=US&quotesCount=6&newsCount=4&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&newsQueryId=news_cie_vespa&enableCb=true&enableNavLinks=true&enableEnhancedTrivialQuery=true			
		// https://query2.finance.yahoo.com/v1/finance/search?q=FR0000031577&lang=en-US&region=US&quotesCount=6&newsCount=0&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&enableCb=true&enableEnhancedTrivialQuery=true
		// retrieve Yahoo Finance custom company urlSuffix from ISIN code
		{	
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "query2.finance.yahoo.com/v1/finance/search?q=%s&lang=en-US&region=US&quotesCount=6&newsCount=0&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&enableCb=true&enableEnhancedTrivialQuery=true", stock.isin );
				theApi.stock = stock;
				theApi.handler = new YahooSearchIsinHandler();
				theApi.handler.cacheSubFolder = "/cache/yahoo-searched";
			});			
			//yahoo.purgeCookieStore();
			api.perform( yahoo );
		}
		
		// https://fr.finance.yahoo.com/quote/VCT.PA/cash-flow		
		// retrieve Free Cash Flow
		
		{	
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "fr.finance.yahoo.com/quote/%s/cash-flow", stock.yahooSuffix );
				theApi.stock = stock;
				theApi.handler = new YahooParseCashFlowHandler();
				theApi.handler.cacheSubFolder = "/cache/yahoo-cashflow";
			});			
			//yahoo.purgeCookieStore();
			api.perform( yahoo );
		}
*/		
/*		
		// https://www.boursorama.com/recherche/ajax?query=fr0000031775		
		// retrieve Boursorama custom company urlSuffix from ISIN code
		{	
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/recherche/ajax?query=%s", stock.isin.toLowerCase() );
				theApi.stock = stock;
				theApi.handler = new BoursoramaParseSearchedHandler();
				theApi.handler.cacheSubFolder = "/cache/bma-searched";
			});			
			api.perform( boursorama );
		}
		
		// https://www.boursorama.com/cours/societe/profil/1rPVCT/
		
		if( stock.bmaSuffix != null ){	
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/cours/societe/profil/%s/", stock.bmaSuffix );
				theApi.stock = stock;
				theApi.handler = new BoursoramaParseSocieteHandler();
				theApi.handler.cacheSubFolder = "/cache/bma-societe";
			});			
			api.perform( boursorama );
		}		
*/		
	}
	
	public void fetchData() throws Exception {


		TargetServer tradingSat = new TargetServer();
		tradingSat.setBaseUrl( "https://www.tradingsat.com" );
		
		TargetServer abcBourse = new TargetServer();
		abcBourse.setBaseUrl( "https://www.abcbourse.com" );

		TargetServer zoneBourse = new TargetServer();
		zoneBourse.setBaseUrl( "https://www.zonebourse.com" );		

		TargetServer boursorama = new TargetServer();
		boursorama.setBaseUrl( "https://www.boursorama.com" );
		
		TargetServer alphavantage = new TargetServer();
		alphavantage.setBaseUrl( "https://www.alphavantage.co" );
		
		// TODO : https://live.euronext.com/fr/product/equities/FR0000031775-XPAR
		// pour la cotation et le min / max sur les 52 derniere semaines
		
		// https://www.abcbourse.com/marches/symbol_retrieve/<ISIN>
		// retrieve ABC Bourse  custom company urlSuffix from ISIN code
		this.stocks.forEach( stock -> {
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/marches/symbol_retrieve/%s", stock.isin );
					theApi.stock = stock;
					theApi.handler = new AbcSearchHandler();
					theApi.handler.cacheSubFolder = "/cache/abc-searched";
				});			
				api.perform( abcBourse );
		});
		
		// https://www.abcbourse.com/analyses/chiffres/CNPp
		// retrieve 
		// within PEA 
		// the share count
		// last 5 debt ratio
		this.stocks.forEach( stock -> {
			if( stock.abcSuffix != null ) {	
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/analyses/chiffres/%s", stock.abcSuffix );
					theApi.stock = stock;
					theApi.handler = new AbcSocieteHandler();
					theApi.handler.cacheSubFolder = "/cache/abc-societe";
				});			
				api.perform( abcBourse );
			}
		});
				
		// retrieve Trading Sat custom company urlSuffix from ISIN code
		this.stocks.forEach( stock -> {	
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/async/json/instrument-search.php?term=%s", stock.isin );
				theApi.stock = stock;
				theApi.handler = new TSSearchIsinHandler();
				theApi.handler.cacheSubFolder = "/cache/ts-searched";
			});			
			api.perform( tradingSat );
		});
		
		// https://www.tradingsat.com/vicat-FR0000031775/societe.html
		// retrieve action count
		this.stocks.forEach( stock -> {	
			if( stock.tradingSatUrlSuffix != null ) {
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = stock.tradingSatUrlSuffix + "societe.html";
					theApi.stock = stock;
					theApi.handler = new TSSocieteHandler();
					theApi.handler.cacheSubFolder = "/cache/ts-societe";
				});			
				api.perform( tradingSat );
			}
		});
		
		// https://www.tradingsat.com/vicat-FR0000031775/donnees-financieres.html
		// retrieve financial data
		// last 5 years RNPG
		
		this.stocks.forEach( stock -> {	
			if( stock.tradingSatUrlSuffix != null ) {
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = stock.tradingSatUrlSuffix + "donnees-financieres.html";
					theApi.stock = stock;
					theApi.handler = new TSFinancialDataHandler();
					theApi.handler.cacheSubFolder = "/cache/ts-donnees-financieres";
				});			
				api.perform( tradingSat );
			}
		});
		
		// https://www.zonebourse.com/recherche/instruments/?aComposeInputSearch=s_FR		

		// retrieve Zone Bourse custom company urlSuffix from ISIN code
		this.stocks.forEach( stock -> {	
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/recherche/instruments/?aComposeInputSearch=s_%S", stock.isin );
				theApi.stock = stock;
				theApi.handler = new ZbSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-searched";
			});			
			api.perform( zoneBourse );
		});
		
		// https://www.zonebourse.com/cours/action/VICAT-5009/fondamentaux/

		// retrieve Zone Bourse fondamentaux
		// ebit (résultat d'exploitation)
		// dette nette ou trésorerie nette
		// free-cash flow		
		this.stocks.forEach( stock -> {	
			if( stock.zbSuffix != null ) {
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/cours/action/%s/fondamentaux/", stock.zbSuffix );
					theApi.stock = stock;
					theApi.handler = new ZbFondamentalHandler();
					theApi.handler.cacheSubFolder = "/cache/zb-fondamentaux";
					theApi.charset = StandardCharsets.ISO_8859_1;
				});			
				api.perform( zoneBourse );
			}
		});
		
		// retrieve Alphavantage custom company symbol from name searched
		// https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=Exel&apikey=48SR4TNP9V41IEGQhttps://www.google.com		
		// TODO seulement pour les actions PEA
		// seulement si un seul match est pour Paris
		// le stocker dans le CSV d'origine
		
//		this.stocks.forEach( stock -> {
//			
//			if( stock.withinPEA == null ) return;
//			if( stock.withinPEA == false ) return;
//			if( stock.aphaSymbol != null && stock.aphaSymbol.length() > 0 ) return;
//			
//			String[] parts = stock.name.split( " " );
//			if( parts.length < 1 ) {
//				return;
//			}
//			GetApi api = new GetApi( this.context, theApi -> {
//				
//				theApi.parameters = new TreeMap();
//				theApi.parameters.put("function", "SYMBOL_SEARCH");
//				theApi.parameters.put("apikey", "48SR4TNP9V41IEGQ");
//				theApi.parameters.put("keywords", parts[0] );
//				theApi.urlSuffix = String.format( "/query" );
//				theApi.stock = stock;
//				theApi.handler = new AlphaSearchSymbolHandler();
//				theApi.handler.cacheSubFolder = "/cache/alpha-searched";
//			});			
//			api.perform( alphavantage );
//			if( api.useCache == false ) { 
//				try {
//					Thread.sleep(  12000 );
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
	}
	
	public void compute( Stock stock ) {
		
		//System.out.println( String.format( "compute for stock <%s> ...", stock.name ));
		
//		if( stock.name.equals("Spir Communication")) {
//			
//			int i = 5;
//		}
		
		ArrayList<Double> ratings = new ArrayList<Double>();
				
		// figure out the correct share count
		ArrayList<Long> shareCounts = new ArrayList<Long>();
		if( stock.abcSharesCount != null ) {
			shareCounts.add( stock.abcSharesCount );
		}
		if( stock.tradingSatSharesCount != null ) {
			shareCounts.add( stock.tradingSatSharesCount );
		}
		if( shareCounts.size() > 0 ) {
		
			// stock.sharesCount = (long) shareCounts.stream().mapToLong(Long::longValue).average().getAsDouble();
			stock.sharesCount = shareCounts.stream().mapToLong(Long::longValue).max().getAsLong();
		}		
		
		// Capitalization (K€)
		
		if( stock.lastQuote != null && stock.sharesCount != null ) {
			
			stock.capitalization = (long)((stock.lastQuote * stock.sharesCount) / 1000.0);
		}

		// Capitaux propres
		if( stock.histoCP != null ) {
			stock.histoCP.forEach( cp -> {
				if( cp != null ) {
					stock.capitauxPropres = cp;	 // keep only the last one (the more recent)
				}
			});
		}
						
		if( stock.capitalization != null && stock.capitauxPropres != null ) {

			if( stock.capitauxPropres > 0 ) {
				
				stock.ratioQuoteBV = (double)stock.capitalization / (double)stock.capitauxPropres;

				Double rating = 50.0 / stock.ratioQuoteBV;
				// if ratioQuoteBV = 0.5 => rating = 100
				// if ratioQuoteBV = 1   => rating =  50
				// if ratioQuoteBV = 2   => rating =  25
				// if ratioQuoteBV = 3   => rating =  16				
				ratings.add( rating );
			}
		}
		
		if( stock.histoRNPG != null && stock.histoRNPG.size() > 0 ) {
			
			for( int i = 0 ; i < stock.histoRNPG.size() ; i++ ) {			
				//System.out.println( String.format( "histoRNPG [%d] = %d K€ ", i, stock.histoRNPG.get(i) ));
			}
			
			stock.avgRNPG = stock.histoRNPG.stream().mapToLong(Long::longValue).average().getAsDouble();
			//System.out.println( String.format( "avg RNPDG = %.2f K€ ", stock.avgRNPG ));
		}
		
		if( stock.histoEBIT != null && stock.histoEBIT.size() > 0 ) {
			
			for( int i = 0 ; i < stock.histoEBIT.size() ; i++ ) {			
				//System.out.println( String.format( "histoEBIT [%d] = %d K€ ", i, stock.histoEBIT.get(i) ));
			}
			
			stock.avgEBIT = stock.histoEBIT.stream().mapToDouble( i -> i ).average().getAsDouble();
			//System.out.println( String.format( "avg EBIT = %.2f M€ ", stock.avgEBIT ));
		}		

		if( stock.lastQuote != null && stock.sharesCount != null && stock.avgRNPG != null && stock.avgRNPG > 0 ) {

			Double avgEarningPerShare = ( stock.avgRNPG * 1000.0 ) / stock.sharesCount;
			if( avgEarningPerShare > 0 && stock.lastQuote > 0 ) {
				stock.avg5yPER = stock.lastQuote / avgEarningPerShare;
				
				// 0-100 
				// 100 ; resultat de 11% (PE de 9)
				// 0   : resultat de  5%
				Double a = 1666.0;
				Double b = 83.30;
				Double rating = ( avgEarningPerShare / stock.lastQuote ) * a + b;
				ratings.add( rating );
			}
		}
		
		if( stock.histoVE != null && stock.histoVE.size() > 0 && stock.avgEBIT != null ) {
			
			if( stock.avgEBIT >= 0 ) {
				
				Double lastVE = stock.histoVE.get(stock.histoVE.size()-1);
				stock.ratioVeOverEBIT = lastVE / stock.avgEBIT; 
			}
		}
		
		if( stock.histoDebtRatio != null && stock.histoDebtRatio.size() > 0  ) {

			stock.debtRatio = stock.histoDebtRatio.get(stock.histoDebtRatio.size()-1);
		}
		
		
		if( ratings.size() > 0 ) {
			stock.rating = ratings.stream().mapToDouble( i -> i ).average().getAsDouble();
		}
	}	
	
	public void computeAll() throws Exception { 
		
		for( Stock stock : this.stocks ) {
			
			compute( stock );
		}		
		
		// now sort stock list by stock name 		
		this.stocks.sort( (st1, st2 ) -> {						
			return st1.name.compareTo( st2.name );
		});
	}
	
	private void createCell( XSSFRow row, int column, Object content ) {
		
		XSSFCell cell = row.createCell(column);
		
		if( content == null ) {
			
			cell.setBlank();
			
		}
		else if( content instanceof Boolean ) {
			
			cell.setCellValue( (Boolean) content );
		}		
		else if( content instanceof String ) {
			
			cell.setCellValue( (String) content );
		}
		else if(  content instanceof Integer  ) {
			
			cell.setCellValue( (Integer) content );
		}		
		else if(  content instanceof Long  ) {
			
			cell.setCellValue( (Long) content );
		}
		else if(  content instanceof Double  ) {
			
			cell.setCellValue( (Double) content );
		}
	}

	private void setCsvCell( String[] fieldsOfLine, int columnIdx, Object content ) {
		
		if( content == null ) {
			
			fieldsOfLine[ columnIdx ] = "";
			
		}
		else {
			
			fieldsOfLine[ columnIdx ] = content.toString();
		}
	}
	
	public void outputReport() throws IOException {
			
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
		    
		    ArrayList<Stock> selection = new ArrayList<Stock>(); 
		    
		    for( int i = 0, row = 0 ; i < this.stocks.size() ; i++ ) {
		    	
		    	if( this.stocks.get(i).withinPEA == null || this.stocks.get(i).withinPEA == false ) {continue;}
		    	if( this.stocks.get(i).toIgnore != null && this.stocks.get(i).toIgnore == true ) {continue;}
		    	
		    	sheet.createRow( row + 1 ); // [ 0 : first row
		    	selection.add( this.stocks.get(i) );
		    	row++;
		    }
		    
		    int column;
		    int iMax = selection.size();
		    
		    // NAME 		    
		    column = 0;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Name" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).name ); }
		    
		    // ISIN
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "ISIN" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).isin ); }
		    
		    // elligible PEA
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "PEA" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).withinPEA ); }
		    
		    // MNEMO		    
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Mnemo" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).mnemo ); }

		    // Nombre d'actions
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Share Count" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sharesCount ); }
		    
		    // Nombre d'actions
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Share Count (ABC)" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sharesCountABC ); }

		    // Nombre d'actions
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Share Count (TS)" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).sharesCountTS ); }
		    
		    // lastQuote		    
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Last Quote" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).lastQuote ); }
		    
		    // capitalisation
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Capitalization (K€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).capitalization ); }
		    
		    // capitaux propres		    
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Capitaux propres (M€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).capitauxPropres ); }
		    
		    // avgRNPG		    
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "5y-Avg RNPG (K€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avgRNPG ); }
		    
		    // Ratio d'endettement
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Endettement %" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).debtRatio ); }
		    
		    // Ratio Book Value
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Book value ratio" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratioQuoteBV ); }

		    // 5 years avg PER
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "5y-Avg PER" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).avg5yPER ); }

		    // VE / EBIT
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "VE/EBIT" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).ratioVeOverEBIT ); }
		    
		    // Custom Rating
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Investment Rating" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).rating ); }
 
		    // ---------------- Web Sites Suffix and URL ...

		    // ABC Bourse Suffix
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "ABC Bourse Suffix" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).abcSuffix ); }

		    // trading Sat Suffix
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "TradingSat Suffix" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).tradingSatUrlSuffix ); }
		    
		    // trading Sat URL
		    column++;
		    sheet.getRow(0).createCell( column ).setCellValue( (String) "TradingSat URL" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).tradingSatUrl ); }
		    
		    
		    
		    // Yahoo Suffix
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Yahoo Suffix" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).yahooSuffix ); }

		    		    
		    // Zone Bourse Suffix
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse Suffix" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).zbSuffix ); }		    
		    
		    // Boursorama Suffix
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Boursorama Suffix" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).bmaSuffix ); }
//		    

		    // write file
		    
			String reportXLS = this.context.rootFolder + "/" + "output/report.xlsx";
		    
	        try( FileOutputStream outputStream = new FileOutputStream( reportXLS ) ) {

	            wb.write(outputStream);
	        }

	        System.out.println( String.format( "report generation for %d stock(s) : OK", selection.size()));
	    }
	}
}
