package miouge;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import miouge.beans.Context;
import miouge.beans.ExclusionResume;
import miouge.beans.GetApi;
import miouge.beans.Stock;
import miouge.beans.TargetServer;
import miouge.handlers.AbcAugCapitalEventsHandler;
import miouge.handlers.AbcDividendEventsHandler;
import miouge.handlers.AbcDivisionEventsHandler;
import miouge.handlers.AbcEventsAndQuoteHandler;
import miouge.handlers.AbcSearchHandler;
import miouge.handlers.AbcSocieteHandler;
import miouge.handlers.BmaSearchHandler;
import miouge.handlers.BmaSocieteHandler;
import miouge.handlers.TSFinancialDataHandler;
import miouge.handlers.TSSearchIsinHandler;
import miouge.handlers.TSSocieteHandler;
import miouge.handlers.YahooHistoQuoteHandler;
import miouge.handlers.YahooSearchHandler;
import miouge.handlers.ZbFondamentalHandler;
import miouge.handlers.ZbSearchHandler;

public class Report extends ReportGeneric {

	public Report(Context context) {
		super(context);
	}

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
						//stock.countryCode = stock.isin.substring(0, 2);
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
		
//				waitMs( 12000 );
//			}
//		});		
		
	}
		
	public void fetchDataAbc() throws Exception {
		
		// ---------------- ABC BOURSE -------------------
	
		System.out.println( "fetch data from ABC BOURSE ..." );

		TargetServer abcBourse = new TargetServer();
		abcBourse.setBaseUrl( "https://www.abcbourse.com" );
		
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
			api.perform( abcBourse, false );
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
			api.perform( abcBourse, false );
		});
		
		// https://www.abcbourse.com/marches/events/VCTp
		// retrieve
		// the last quotation
		
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.abcSuffix == null ) { return; }
			
			if( stock.abcSuffix != null ) {	
				GetApi api = new GetApi( this.context, theApi -> {
					
					theApi.urlSuffix = String.format( "/marches/events/%s", stock.abcSuffix );
					theApi.stock = stock;
					theApi.handler = new AbcEventsAndQuoteHandler();
					theApi.handler.cacheSubFolder = "/cache/abc-last-quotation";
				});			
				api.perform( abcBourse, false );
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
				api.perform( abcBourse, false );
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
				api.perform( abcBourse, false );
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
				api.perform( abcBourse, false );
			}
		});
		
		// https://www.abcbourse.com/download/valeur/CNPp POST / application/x-www-form-urlencoded
		
//		POST /download/valeur/CNPp HTTP/1.1
//		Host: www.abcbourse.com
//		Cache-Control: no-cache
//		Content-Type: application/x-www-form-urlencoded
//
//		dateFrom=2020-08-12&dateTo=2020-08-12&sFormat=x&typeData=isin&__RequestVerificationToken=CfDJ8D7QL4yWkXhGoO_8O46EbPdCgMzyjslOuZ6f5rfXaeqzk5PEBgIM2LTn4ZgRtwwfdfd3MWcMNZQB-n_NaasBJzvfLRDa5WFGXLwaGCOLXFG-DDGC1PE1_9FFNp0IDQgYLXrqoV1osZTMzeRdeK72tmQ
		
	}
	
	public void fetchDataTs() throws Exception {
	
		// ---------------- TRADING SAT -------------------
		
		System.out.println( "fetch data from TRADING SAT ..." );
		
		TargetServer tradingSat = new TargetServer();
		tradingSat.setBaseUrl( "https://www.tradingsat.com" );
		
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
			api.perform( tradingSat, false );
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
				api.perform( tradingSat, false );
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
			api.perform( tradingSat, false );
		});		
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
			api.perform( zoneBourse, false );
		});

		/*
		System.out.println( "	search dfn ..." );
		
		// https://www.zonebourse.com/cours/action/CAISSE-REGIONALE-DE-CREDI-5701/
		// TODO : Dette nette 2020 ou Trésorie nette 2020 => calcul de la Valeur d'entreprise		

		//pas mal d'erreur de parsing pour le moment
		 
		this.stocks.forEach( stock -> {
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.zbSuffix == null ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/cours/action/%s/", stock.zbSuffix );
				theApi.stock = stock;
				theApi.handler = new ZbResumeHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-resume";
				theApi.charset = StandardCharsets.ISO_8859_1;
			});			
			api.perform( zoneBourse );
		});
		*/
		
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
				theApi.handler = new ZbFondamentalHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-fondamentaux";
				theApi.charset = StandardCharsets.ISO_8859_1;
			});			
			api.perform( zoneBourse, false );
		});
		
	}

	public void fetchDataBma() throws Exception {
		
		// ---------------- BOURSORAMA -------------------
		
		System.out.println( "fetch data from BOURSORAMA ..." );
		
 		TargetServer boursorama = new TargetServer();
		boursorama.setBaseUrl( "https://www.boursorama.com" );		
		
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
			api.perform( boursorama, false );
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
			api.perform( boursorama, false );
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
	}

	public void fetchDataYahoo() throws Exception {
	
		// ---------------- YAHOO -------------------
		
		System.out.println( "fetch data from YAHOO ..." );
		
		// cotation a une date donnée
		
		TargetServer yahoo2 = new TargetServer();
		yahoo2.setBaseUrl( "https://query2.finance.yahoo.com" );
			
		// finance/search?q=FR0000120222&lang=en-US&region=US&quotesCount=6&newsCount=4&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&newsQueryId=news_cie_vespa&enableCb=true&enableNavLinks=true&enableEnhancedTrivialQuery=true			
		// https://query2.finance.yahoo.com/v1/finance/search?q=FR0000031577&lang=en-US&region=US&quotesCount=6&newsCount=0&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&enableCb=true&enableEnhancedTrivialQuery=true
		// retrieve Yahoo Finance custom company urlSuffix from ISIN code
		
		this.stocks.forEach( stock -> {

			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.yahooSymbol != null && stock.yahooSymbol.length() > 4 ) { return; }

			// System.out.println( String.format( "missing yahoo symbol for %s %s ", stock.name, stock.isin ));
			
			GetApi api = new GetApi( this.context, theApi -> {

				theApi.urlSuffix = String.format( "/v1/finance/search?q=%s&lang=en-US&region=US&quotesCount=6&newsCount=0&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&enableCb=true&enableEnhancedTrivialQuery=true", stock.isin );
				theApi.stock = stock;
				theApi.handler = new YahooSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/yahoo-searched";
				theApi.onlyUseCache = true;
			});
			
			yahoo2.purgeCookieStore();
			api.perform( yahoo2, false );
			
			if( api.cacheLoaded == false ) {
				Tools.waitMs( 1000 );
			}
		});

		// https://query2.finance.yahoo.com/v7/finance/options/VCT.PA => OK TODO : dépouiller les informations présentes		

		// https://query2.finance.yahoo.com/v8/finance/chart/VCT.PA ca repond
		
		// https://query2.finance.yahoo.com/v8/finance/chart/VCT.PA?period1=1597190400&period2=1597276800&interval=1d&filter=history&frequency=1d ca repond 1 donnée
		
		// https://query1.finance.yahoo.com/v7/finance/quote?lang=en-US&region=US&corsDomain=finance.yahoo.com&symbols=FB
				
		Long epoch1 = Tools.convertToEpoch( "2020/01/20", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"), ZoneId.of("UTC")); // date de la cotation demandée		
		Long epoch2 = epoch1 + 86400;		
				
		this.stocks.forEach( stock -> {

			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.yahooSymbol == null ) { return; }

			GetApi api = new GetApi( this.context, theApi -> {

				theApi.urlSuffix = String.format( "/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d&filter=history&frequency=1d", stock.yahooSymbol, epoch1, epoch2 );
				theApi.stock = stock;
				theApi.handler = new YahooHistoQuoteHandler();				
				theApi.handler.cacheSubFolder = "/cache/yahoo-histo-quote-2020-01-20";
				theApi.onlyUseCache = true;
			});
			api.debug = true;
			api.perform( yahoo2, false );
		});	
		
		/*
		  
https://query2.finance.yahoo.com/v10/finance/quoteSummary/VCT.PA?modules=financialData		  
		  
https://query1.finance.yahoo.com/v7/finance/quote?lang=en-US&region=US&corsDomain=finance.yahoo.com&symbols=FB		  
https://query2.finance.yahoo.com/v10/finance/quoteSummary/NVDA?modules=incomeStatementHistoryQuarterly
https://query2.finance.yahoo.com/v10/finance/quoteSummary/NVDA?modules=financialData
https://github.com/pilwon/node-yahoo-finance/blob/master/docs/quote.md
		
let modules = [
	'assetProfile', 'balanceSheetHistory', 'balanceSheetHistoryQuarterly', 'calendarEvents',
	'cashflowStatementHistory', 'cashflowStatementHistoryQuarterly', 'defaultKeyStatistics', 'earnings',
	'earningsHistory', 'earningsTrend', 'financialData', 'fundOwnership', 'incomeStatementHistory',
	'incomeStatementHistoryQuarterly', 'indexTrend', 'industryTrend', 'insiderHolders', 'insiderTransactions',
	'institutionOwnership', 'majorDirectHolders', 'majorHoldersBreakdown', 'netSharePurchaseActivity', 'price', 'quoteType',
	'recommendationTrend', 'secFilings', 'sectorTrend', 'summaryDetail', 'summaryProfile', 'symbol', 'upgradeDowngradeHistory',
	'fundProfile', 'topHoldings', 'fundPerformance',
]		
		
		
		*/		
	}
	
	public void fetchData() throws Exception {
		
		fetchDataAbc();	
		fetchDataTs();
		fetchDataZb();
		fetchDataBma();
		fetchDataYahoo();

		// TODO : voir https://www.reuters.com/companies/api/getFetchCompanyKeyMetrics/goog.oq
	}
		
	@Override
	public void compute( Stock stock )	{}	

	@Override
	protected boolean excludeFromReport( ExclusionResume resume, Stock stock, boolean verbose ) { return false; }
	
	@Override
	protected void composeReport( XSSFWorkbook wb, HashMap<Integer, CellStyle> precisionStyle, ArrayList<Stock> selection ) throws Exception {}
}
