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
import java.util.TreeMap;

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
import com.github.stockRater.handlers.AbcEventsAndQuoteHandler;
import com.github.stockRater.handlers.AbcAugCapitalEventsHandler;
import com.github.stockRater.handlers.AbcDividendEventsHandler;
import com.github.stockRater.handlers.AbcDivisionEventsHandler;
import com.github.stockRater.handlers.AbcSearchHandler;
import com.github.stockRater.handlers.AbcSocieteHandler;
import com.github.stockRater.handlers.BmaConsensusHandler;
import com.github.stockRater.handlers.BmaSearchHandler;
import com.github.stockRater.handlers.BmaSocieteHandler;
import com.github.stockRater.handlers.TSFinancialDataHandler;
import com.github.stockRater.handlers.TSSearchIsinHandler;
import com.github.stockRater.handlers.TSSocieteHandler;
import com.github.stockRater.handlers.YahooSearchHandler;
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
	public TreeMap< String, Stock > stocksByIsin = new TreeMap< String, Stock >();

	public void importNewIsinCsv() throws FileNotFoundException, IOException, CsvException {
		
		CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build(); // custom separator

		ArrayList<String> filesToImport = new ArrayList<>();
		
		filesToImport.add( context.rootFolder + "/data/1-alterNextAllShare.csv" );
		filesToImport.add( context.rootFolder + "/data/2-cacpme.csv" );
		filesToImport.add( context.rootFolder + "/data/3-cacsmall.csv" );
		filesToImport.add( context.rootFolder + "/data/4-mid-and-small.csv" );
		filesToImport.add( context.rootFolder + "/data/5-euronext-access.csv" );
		filesToImport.add( context.rootFolder + "/data/6-euronext-growth.csv" );
		
		for( String fileToImport : filesToImport ) {
		
			int initialSize = this.stocks.size();
			
			try( CSVReader reader = new CSVReaderBuilder( new FileReader(fileToImport))
					.withCSVParser(csvParser) // custom CSV
					.build()
			) {
				List<String[]> lines = reader.readAll();
				lines.forEach( fields -> {
	
					// Header line & ISIN expected to be in the fist column
					String otherIsin = fields[0];
					
					if( otherIsin.length() != 12 ) { return; } // IsinCode = 12 characters length
					
					String countryCode = otherIsin.substring(0, 2);
					
					if( countryCode == "GB" ) { return; }
					if( countryCode == "US" ) { return; }
					
					if( this.stocksByIsin.get(otherIsin) == null ) {
	
						// if not already existing
						
						Stock stock = new Stock();
						stock.isin = otherIsin; 
						stock.countryCode = stock.isin.substring(0, 2);
						this.stocks.add( stock );
						this.stocksByIsin.put(stock.isin, stock);
					}
				});
			}
			
			int importedCount = this.stocks.size() - initialSize;
			if( importedCount > 0 ) {
				System.out.println( String.format( "%d stock definitions imported from %s", importedCount, fileToImport ));
			}
		}
	}
	
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

				int idx = 0;
				
				String isin = fields[idx++];
				if( isin.length() != 12 ) { return; } // IsinCode = 12 characters length

				Stock stock = new Stock();
				stock.isin = isin;
				
				this.stocks.add( stock );
				stocksByIsin.put(stock.isin, stock);
				
				stock.countryCode=fields[idx++];
				//stock.countryCode = stock.isin.substring(0, 2);
				
				stock.mnemo      = fields[idx++]; // also named "ticker"
				stock.aphaSymbol = fields[idx++];
				stock.name       = fields[idx++];
				
				if( fields[idx].length() > 0 ) {stock.withinPEA=Boolean.parseBoolean( fields[idx] ); }; idx++;
				if( fields[idx].length() > 0 ) {stock.toIgnore=Boolean.parseBoolean( fields[idx]); }; idx++;
				
				// System.out.println( stock.mnemo + " PEA=" + stock.withinPEA + "/ Ignore=" + stock.toIgnore );
				
				stock.commentOnIgnore=fields[idx++];
 				stock.activity=fields[idx++];
				
/*				TODO : move on specific file ini file override 

				
				
				if( fields[idx].length() > 0 ) { stock.initShareCount=Long.parseLong(fields[idx]); }; idx++;
				if( fields[idx].length() > 0 ) { stock.offsetRNPG=Long.parseLong(fields[idx]); }; idx++;
				if( fields[idx].length() > 0 ) { stock.offsetFCFW=Long.parseLong(fields[idx]); }; idx++;
				if( fields[idx].length() > 0 ) { stock.offsetDividends=Double.parseDouble(fields[idx]); }; idx++;
*/				
			});
		}
		
		this.stocks.forEach( stock -> {
			if( stock.withinPEA == null ) {
				stock.withinPEA = false;
			}
			if( stock.toIgnore == null ) {
				stock.toIgnore = false;
			}
			if( stock.withTTF == null ) {
				stock.withTTF = false;
			}			
		});
		
		System.out.println( String.format( "%d stock definitions loaded", this.stocks.size() ));
		
		// this.importNewIsinCsv();
	}
	
	private void setCsvCell( String[] fieldsOfLine, int columnIdx, Object content ) {
		
		if( content == null ) {
			
			fieldsOfLine[ columnIdx ] = "";
			
		}
		else {
			
			fieldsOfLine[ columnIdx ] = content.toString();
		}
	}	
	
	public void flushCsvData() throws IOException {
		
		int COLUMN_NB = 9; 
		
		String stocksCSV  = context.rootFolder + "/data/" + "stocks-out.csv";
		
		List<String[]> stringArray = new ArrayList<String[]>();
		
		String[] header = new String[COLUMN_NB];
		stringArray.add(header);
		int column = 0;
		header[column] = "ISIN";column++;
		header[column] = "Country";column++;
		
		header[column] = "Mnemo";column++;
		header[column] = "alphaSymbol";column++;
		header[column] = "Name";column++;
		
		header[column] = "WithinPEA";column++;
		header[column] = "ToIgnore";column++;


		header[column] = "Comments";column++;
		header[column] = "Activity";column++;

		for( Stock stock : this.stocks ) {
			
			String[] array = new String[COLUMN_NB];
			stringArray.add(array);
			column = 0;			
			setCsvCell( array, column++, stock.isin );
			setCsvCell( array, column++, stock.countryCode );
			
			setCsvCell( array, column++, stock.mnemo );
			setCsvCell( array, column++, stock.aphaSymbol );
			setCsvCell( array, column++, stock.name );
			
			setCsvCell( array, column++, stock.withinPEA );
			setCsvCell( array, column++, stock.toIgnore );
			
			setCsvCell( array, column++, stock.commentOnIgnore );
			setCsvCell( array, column++, stock.activity );
		}
		
	     CSVWriter writer = new CSVWriter(new FileWriter(stocksCSV), ';', '\u0000', '\\', "\n" );
	     
	     writer.writeAll(stringArray);
	     writer.close();
	     
	     System.out.println( String.format( "flush stock definitions Csv for %d stock(s) : OK", stocks.size()));
	}	
	
	public void unused( Stock stock ) throws Exception {
		
		// TODO : https://live.euronext.com/fr/product/equities/FR0000031775-XPAR
		// pour la cotation et le min / max sur les 52 derniere semaines
		
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
		
//		TargetServer alphavantage = new TargetServer();
//		alphavantage.setBaseUrl( "https://www.alphavantage.co" );
		
		
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
	
	public void fetchData() throws Exception {

		TargetServer abcBourse = new TargetServer();
		abcBourse.setBaseUrl( "https://www.abcbourse.com" );

		TargetServer tradingSat = new TargetServer();
		tradingSat.setBaseUrl( "https://www.tradingsat.com" );

		TargetServer zoneBourse = new TargetServer();
		zoneBourse.setBaseUrl( "https://www.zonebourse.com" );
		
 		TargetServer boursorama = new TargetServer();
		boursorama.setBaseUrl( "https://www.boursorama.com" );
			
		// ---------------- ABC BOURSE -------------------		
		
		// https://www.abcbourse.com/marches/symbol_retrieve/<ISIN>
		// -> https://www.abcbourse.com/cotation/VCTp
		// retrieve ABC Bourse  custom company urlSuffix from ISIN code
		// retrieve Mnemo if not already known
		// retrieve Name if not already known
		// retrieve withinPEA if not already known
		// retrieve withTTF flag
		// TODO : could retrieve les extremes de quotation de 1 semaine à 10ans !
		this.stocks.forEach( stock -> {
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/marches/symbol_retrieve/%s", stock.isin );
				theApi.stock = stock;
				theApi.handler = new AbcSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/abc-searched";
			});			
			api.perform( abcBourse );
		});
		
		// https://www.abcbourse.com/analyses/chiffres/CNPp
		//  
		// retrieve the share count
		// retrieve the last 5 Debt Ratio %
		// TODO : last 5 RNPG
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.abcSuffix == null ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/analyses/chiffres/%s", stock.abcSuffix );
				theApi.stock = stock;
				theApi.handler = new AbcSocieteHandler();
				theApi.handler.cacheSubFolder = "/cache/abc-societe";
			});			
			api.perform( abcBourse );
		});
		
		// https://www.abcbourse.com/marches/events/VCTp
		// retrieve 
		// last quotation
		
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.abcSuffix == null ) { return; }
			
			if( stock.abcSuffix != null ) {	
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/marches/events/%s", stock.abcSuffix );
					theApi.stock = stock;
					theApi.handler = new AbcEventsAndQuoteHandler();
					theApi.handler.cacheSubFolder = "/cache/abc-eventsAndQuote";
				});			
				api.perform( abcBourse );
			}
		});

		// https://www.abcbourse.com/api/general/GetEventsFiltered?typeEv=DVD&symbolid=FR0000031775p
		// retrieve last dividends events

		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.abcSuffix == null ) { return; }
			
			if( stock.abcSuffix != null ) {	
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/api/general/GetEventsFiltered?typeEv=DVD&symbolid=%sp", stock.isin );
					theApi.stock = stock;
					theApi.handler = new AbcDividendEventsHandler();
					theApi.handler.cacheSubFolder = "/cache/abc-dividends";
				});			
				api.perform( abcBourse );
			}
		});

		// https://www.abcbourse.com/api/general/GetEventsFiltered?typeEv=DVD&symbolid=FR0000031775p
		// retrieve last division events (regroupement est une division < 1 cf FR0013506730 Vallourec) 

		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.abcSuffix == null ) { return; }
			
			if( stock.abcSuffix != null ) {	
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/api/general/GetEventsFiltered?typeEv=DIV&symbolid=%sp", stock.isin );
					theApi.stock = stock;
					theApi.handler = new AbcDivisionEventsHandler();
					theApi.handler.cacheSubFolder = "/cache/abc-divisions";
				});			
				api.perform( abcBourse );
			}
		});		
		
		// Augmentation de capital AK
		
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.abcSuffix == null ) { return; }
			
			if( stock.abcSuffix != null ) {	
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/api/general/GetEventsFiltered?typeEv=AK&symbolid=%sp", stock.isin );
					theApi.stock = stock;
					theApi.handler = new AbcAugCapitalEventsHandler();
					theApi.handler.cacheSubFolder = "/cache/abc-aug-capital";
				});			
				api.perform( abcBourse );
			}
		});		
		
		// ---------------- TRADING SAT -------------------		
		
		// retrieve Trading Sat custom company urlSuffix from ISIN code (response is json like)
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			
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
		// retrieve ratio d'endettement %
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			
			if( stock.tsSuffix != null ) {
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = stock.tsSuffix + "societe.html";
					theApi.stock = stock;
					theApi.handler = new TSSocieteHandler();
					theApi.handler.cacheSubFolder = "/cache/ts-societe";
				});			
				api.perform( tradingSat );
			}
		});
		
		// https://www.tradingsat.com/vicat-FR0000031775/donnees-financieres.html
		// retrieve last 5 years RNPG (K€)
		// last 5 years CP (K€), le dernier etant le plus récent
		
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.tsSuffix == null ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = stock.tsSuffix + "donnees-financieres.html";
				theApi.stock = stock;
				theApi.handler = new TSFinancialDataHandler();
				theApi.handler.cacheSubFolder = "/cache/ts-donnees-financieres";
			});			
			api.perform( tradingSat );
		});

		// ---------------- ZONE BOURSE -------------------
		
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
				theApi.handler = new ZbFondamentalHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-fondamentaux";
				theApi.charset = StandardCharsets.ISO_8859_1;
			});			
			api.perform( zoneBourse );
		});
		
		// ---------------- BOURSORAMA -------------------
		
		// https://www.boursorama.com/recherche/ajax?query=fr0000031775		
		// retrieve Boursorama custom company urlSuffix from ISIN code
		
		this.stocks.forEach( stock -> {	
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/recherche/ajax?query=%s", stock.isin.toLowerCase() );
				theApi.stock = stock;
				theApi.handler = new BmaSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/bma-searched";
			});			
			api.perform( boursorama );
		});
		
		
		// https://www.boursorama.com/cours/societe/profil/1rPVCT/

		this.stocks.forEach( stock -> {

			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.bmaSuffix == null ) { return; }

			GetApi api = new GetApi( this.context, theApi -> {

				theApi.urlSuffix = String.format( "/cours/societe/profil/%s/", stock.bmaSuffix );
				theApi.stock = stock;
				theApi.handler = new BmaSocieteHandler();
				theApi.handler.cacheSubFolder = "/cache/bma-societe";
			});
			api.perform( boursorama );
		});
		
		// https://www.boursorama.com/cours/consensus/1rPLTA/
		// DFN enc
		/*
		this.stocks.forEach( stock -> {

			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.bmaSuffix == null ) { return; }

			GetApi api = new GetApi( this.context, theApi -> {

				theApi.urlSuffix = String.format( "/cours/consensus/%s/", stock.bmaSuffix );
				theApi.stock = stock;
				theApi.handler = new BmaConsensusHandler();
				theApi.handler.cacheSubFolder = "/cache/bma-consensus";
			});
			api.perform( boursorama );
		});	
		*/

		// cotation a une date donnée
		
		// https://www.boursorama.com/_formulaire-periode/?symbol=1rPADP&historic_search%5BstartDate%5D=12%2F08%2F2020&historic_search%5Bduration%5D=1M&historic_search%5Bperiod%5D=1
		// https://www.abcbourse.com/download/valeur/CNPp POST / application/x-www-form-urlencoded
		
//		POST /download/valeur/CNPp HTTP/1.1
//		Host: www.abcbourse.com
//		Cache-Control: no-cache
//		Content-Type: application/x-www-form-urlencoded
//
//		dateFrom=2020-08-12&dateTo=2020-08-12&sFormat=x&typeData=isin&__RequestVerificationToken=CfDJ8D7QL4yWkXhGoO_8O46EbPdCgMzyjslOuZ6f5rfXaeqzk5PEBgIM2LTn4ZgRtwwfdfd3MWcMNZQB-n_NaasBJzvfLRDa5WFGXLwaGCOLXFG-DDGC1PE1_9FFNp0IDQgYLXrqoV1osZTMzeRdeK72tmQ
		
//      https://fr.finance.yahoo.com/quote/VCT.PA/history?period1=1597190400&period2=1597276800&interval=1d&filter=history&frequency=1d&includeAdjustedClose=true
		
//      https://fr.finance.yahoo.com/quote/VCT.PA/history?period1=1597190400&period2=1597276800&interval=1d&filter=history&frequency=1d&includeAdjustedClose=false		
		
		
		TargetServer yahoo1 = new TargetServer();
		yahoo1.setBaseUrl( "https://query2.finance.yahoo.com" );
			
		// finance/search?q=FR0000120222&lang=en-US&region=US&quotesCount=6&newsCount=4&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&newsQueryId=news_cie_vespa&enableCb=true&enableNavLinks=true&enableEnhancedTrivialQuery=true			
		// https://query2.finance.yahoo.com/v1/finance/search?q=FR0000031577&lang=en-US&region=US&quotesCount=6&newsCount=0&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&enableCb=true&enableEnhancedTrivialQuery=true
		// retrieve Yahoo Finance custom company urlSuffix from ISIN code
		
		this.stocks.forEach( stock -> {

			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }

			GetApi api = new GetApi( this.context, theApi -> {

				theApi.urlSuffix = String.format( "/v1/finance/search?q=%s&lang=en-US&region=US&quotesCount=6&newsCount=0&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&enableCb=true&enableEnhancedTrivialQuery=true", stock.isin );
				theApi.stock = stock;
				theApi.handler = new YahooSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/yahoo-searched";
			});
			yahoo1.purgeCookieStore();
			api.perform( yahoo1 );
		});		
		
	}
	
	public void compute( Stock stock ) {
		
		//System.out.println( String.format( "compute for stock <%s> ...", stock.name ));
		
//		if( stock.name.equals("Spir Communication")) {
//			
//			int i = 5;
//		}
		
		ArrayList<Double> ratings = new ArrayList<Double>();
				
		// figure out the correct (worse) share count
		
		if( stock.shareCounts.size() > 0 ) {
		
			// stock.sharesCount = (long) shareCounts.stream().mapToLong(Long::longValue).average().getAsDouble();
			stock.sharesCount = stock.shareCounts.stream().mapToLong(Long::longValue).max().getAsLong();
		}		
		
		// Capitalization (K€)
		
		if( stock.lastQuote != null && stock.sharesCount != null ) {
			
			// en M€
			stock.capitalization = ( stock.lastQuote * stock.sharesCount ) / 1000000.0;
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
				
				//                            M€                         K€
				stock.ratioQuoteBV = stock.capitalization / (double)stock.capitauxPropres;
				stock.ratioQuoteBV *= 1000.0;

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
			// System.out.println( String.format( "avg RNPDG = %.2f K€ (avg of %d values)", stock.avgRNPG, stock.histoRNPG.size()));
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
		
		// ratio d'endettement % (vient de TS ou du dernier % historique d'ABC)
		if( stock.debtRatio == null && stock.histoDebtRatio != null && stock.histoDebtRatio.size() > 0  ) {

			stock.debtRatio = stock.histoDebtRatio.get(stock.histoDebtRatio.size()-1);
		}
		
		stock.eventCount = stock.events.size();
		
//		if( ratings.size() > 0 ) {
//			stock.rating = ratings.stream().mapToDouble( i -> i ).average().getAsDouble();
//		}
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
	
	private XSSFCell createCell( XSSFRow row, int column, Object content ) {
		
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
		
		return cell;
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
	        
            // cellStyle.setAlignment(HorizontalAlignment.LEFT);
            // cellStyle.setAlignment(HorizontalAlignment.CENTER);		    
		    
		    /*
		    final Font font = sheet.getWorkbook ().createFont ();
		    font.setFontName ( "Arial" );
		    font.setBoldweight ( Font.BOLDWEIGHT_BOLD );
		    font.setColor ( HSSFColor.WHITE.index );

		    final CellStyle style = sheet.getWorkbook ().createCellStyle ();
		    style.setFont ( font );
		    style.setFillForegroundColor ( HSSFColor.BLACK.index );
		    style.setFillPattern ( PatternFormatting.SOLID_FOREGROUND );

		    final HSSFRow row = sheet.createRow ( 0 );

		    for ( int i = 0; i < columns.size (); i++ )
		    {
		        final Field field = columns.get ( i );

		        final HSSFCell cell = row.createCell ( i );
		        cell.setCellValue ( field.getHeader () );
		        cell.setCellStyle ( style );
		    }		    
		}	
			*/
	        
	        
	    		        
		    // create an empty work sheet
		    XSSFSheet reportSheet = wb.createSheet("report");
		    // create an empty work sheet
		    XSSFSheet sourcesSheet = wb.createSheet("sources");
		    		    
		    // header row
		    reportSheet.createRow( 0 ); // [ 0 : first row
		    sourcesSheet.createRow( 0 ); // [ 0 : first row

		    // prepare 1 row for each selected stock
		    // and compose the selection (only PEA elligible)

		    ArrayList<Stock> selection = new ArrayList<Stock>(); 
		    
		    for( int i = 0, row = 0 ; i < this.stocks.size() ; i++ ) {
		    	
		    	if( this.stocks.get(i).withinPEA == null || this.stocks.get(i).withinPEA == false ) {continue;}
		    	if( this.stocks.get(i).toIgnore != null && this.stocks.get(i).toIgnore == true ) {continue;}
		    	
		    	reportSheet.createRow( row + 1 ); // [ 0 : first row
		    	sourcesSheet.createRow( row + 1 ); // [ 0 : first row
		    	selection.add( this.stocks.get(i) );
		    	row++;
		    }
		    
		    int column;
		    int iMax = selection.size();
		    
		    // NAME 		    
		    column = 0;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Name" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).name ); }
		    
		    // ISIN
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "ISIN" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).isin ); }
		    
		    // with TTF
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "TTF" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).withTTF ); }		    

		    // MNEMO		    
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Mnemo" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).mnemo ); }
		    
		    // MNEMO		    
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Effectif" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).effectif ); }


		    // lastQuote		    
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Last Quote" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).lastQuote ); }
		    
		    // capitalisation
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Capitalization (M€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).capitalization ).setCellStyle( precisionStyle.get(-1)); }
		    		    
		    // capitaux propres		    
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Capitaux propres (M€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).capitauxPropres ); }

		    // Dette financiere nette		    
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "DFN (M€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).dfn ); }		    
		    
		    // avgRNPG		    
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "5y-Avg RNPG (K€)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).avgRNPG ); }
		    
		    // Ratio d'endettement
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Endettement %" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).debtRatio ); }
		    
		    // Ratio Book Value
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Book value ratio" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).ratioQuoteBV ).setCellStyle( precisionStyle.get(-2)); }

		    // 5 years avg PER
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "5y-Avg PER" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).avg5yPER ).setCellStyle( precisionStyle.get(-1)); }

		    // VE / EBIT
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "VE/EBIT" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).ratioVeOverEBIT ).setCellStyle( precisionStyle.get(-1)); }
		    
		    // Custom Rating
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Investment Rating" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).rating ); }
 
		    // ---------------- Web Sites URL ...
		    
		    // trading Sat URL
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "TradingSat URL" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).tradingSatUrl ); }
		    
		    // Zone Bourse URL
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse URL" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( reportSheet.getRow( i + 1 ), column, selection.get(i).zbUrl ); }

		    // Sources SHEET
		    
		    // NAME 		    
		    column = 0;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Name" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).name ); }
		    
		    // ISIN
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "ISIN" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).isin ); }
		    	    
		    // MNEMO		    
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Mnemo" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).mnemo ); }

		    // ---------------- Web Sites Suffix and URL ...

		    // ABC Bourse Suffix
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "ABC Bourse Suffix" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).abcSuffix ); }

		    // trading Sat Suffix
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "TradingSat Suffix" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).tsSuffix ); }
		    
		    // Zone Bourse Suffix
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Zone Bourse Suffix" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).zbSuffix ); }
		    
		    // Boursorama Suffix
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Boursorama Suffix" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).bmaSuffix ); }
		    		    
		    // Yahoo Suffix
//		    column++;
//		    sheet.getRow(0).createCell( column ).setCellValue( (String) "Yahoo Suffix" );
//		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sheet.getRow( i + 1 ), column, selection.get(i).yahooSuffix ); }
		    
		    // elligible PEA
		    column++;
		    reportSheet.getRow(0).createCell( column ).setCellValue( (String) "PEA" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).withinPEA ); }
		    
		    // Nombre d'actions
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Share Count (ABC)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).abcSharesCount ); }

		    // Nombre d'actions
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Share Count (TS)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).tsSharesCount ); }

		    // Nombre d'actions
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Share Count (BMA)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).bmaSharesCount ); }
		    
		    // Nombre d'actions
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Share Count (Max)" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).sharesCount ); }
		    
		    // Dividend Event count
		    column++;
		    sourcesSheet.getRow(0).createCell( column ).setCellValue( (String) "Dividend Ev Nb" );
		    for( int i = 0 ; i < iMax ; i++ ) { createCell( sourcesSheet.getRow( i + 1 ), column, selection.get(i).eventCount ); }		    
		    
		    
		    // write file
		    
			String reportXLS = this.context.rootFolder + "/" + "output/report.xlsx";
		    
	        try( FileOutputStream outputStream = new FileOutputStream( reportXLS ) ) {

	            wb.write(outputStream);
	        }

	        System.out.println( String.format( "report generation for %d stock(s) : OK", selection.size()));
	    }	    
	}
}
