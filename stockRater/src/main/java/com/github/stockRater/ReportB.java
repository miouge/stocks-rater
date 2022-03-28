package com.github.stockRater;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.stockRater.beans.Context;
import com.github.stockRater.beans.GetApi;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TargetServer;
import com.github.stockRater.handlers.ZbFondamentalHandler;
import com.github.stockRater.handlers.ZbFondamentalHandlerB;
import com.github.stockRater.handlers.ZbSearchHandler;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public class ReportB {

	Context context;	

	@SuppressWarnings("unused")
	private ReportB() {}
	
	public ReportB(Context context) {
		super();
		this.context = context;
	}
	
	public ArrayList<Stock> stocks = new ArrayList<Stock>();
	public TreeMap< String, Stock > stocksByIsin = new TreeMap< String, Stock >();	

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
				this.stocksByIsin.put(stock.isin, stock);
				
				stock.countryCode=fields[idx++];
				//stock.countryCode = stock.isin.substring(0, 2);
				
				stock.mnemo      = fields[idx++]; // also named "ticker"
				stock.yahooSymbol = fields[idx++];
				stock.name       = fields[idx++];
				
				if( fields[idx].length() > 0 ) {stock.withinPEA=Boolean.parseBoolean( fields[idx] ); }; idx++;
				if( fields[idx].length() > 0 ) {stock.toIgnore=Boolean.parseBoolean( fields[idx]); }; idx++;
				
				// System.out.println( stock.mnemo + " PEA=" + stock.withinPEA + "/ Ignore=" + stock.toIgnore );
				
				stock.comment=fields[idx++];
 				stock.activity=fields[idx++];
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
			if( stock.withinPEA ) {
				stock.withinPEALabel = "PEA";
			}
			
			if( stock.isin != null && stock.isin.length() > 0 && stock.mnemo != null && stock.mnemo.length() > 0 ) {

				// check presence of overrides ...
				String overrideFile = context.rootFolder + "/data/overrides/" + stock.mnemo + "-" + stock.isin + ".ini";				
				Path path = Paths.get( overrideFile );
				
				// file exists and it is not a directory
				if( Files.exists(path) && !Files.isDirectory(path)) {
					
					System.out.println(String.format( "loading override for %s-%s ...", stock.mnemo, stock.isin ));
					
					String value = Tools.getIniSetting(overrideFile, "General", "SharesCount", "");

					if( value != null && value.length() > 0 ) {
						stock.overrides.sharesCount = Long.parseLong(value);
					}
					/* TODO : move on specific file ini file override 
					if( fields[idx].length() > 0 ) { stock.initShareCount=Long.parseLong(fields[idx]); }; idx++;
					if( fields[idx].length() > 0 ) { stock.offsetRNPG=Long.parseLong(fields[idx]); }; idx++;
					if( fields[idx].length() > 0 ) { stock.offsetFCFW=Long.parseLong(fields[idx]); }; idx++;
					if( fields[idx].length() > 0 ) { stock.offsetDividends=Double.parseDouble(fields[idx]); }; idx++;
					*/				
				}
			}
		});

		System.out.println( String.format( "%d stock definitions loaded", this.stocks.size() ));

		// this.importNewIsinCsv();
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
		
	public void computeAll() throws Exception { 
		
		for( Stock stock : this.stocks ) {
			
			compute( stock );
		}		
		
		// now sort stock list by stock name
		this.stocks.sort( (st1, st2 ) -> {
			return st1.name.compareTo( st2.name );
		});
	}
	
	CellStyle createStyle( XSSFWorkbook wb, Stock stock, Consumer<CellStyle> consumer ) throws Exception {

    	final CellStyle style = wb.createCellStyle();

    	if( consumer != null ) {
    		consumer.accept( style );
    	}
    	
    	if( stock.portfolio > 0 ) {

    		final Font font = wb.createFont ();
    		font.setFontName( "Calibri" );
    		font.setItalic( true );
    		font.setBold( true );
    		style.setFont(font);
    	}

// cellStyle.setAlignment(HorizontalAlignment.LEFT);
// cellStyle.setAlignment(HorizontalAlignment.CENTER);
//    	HSSFColor.HSSFColorPredefined.
//    	AQUA 
//    	AUTOMATIC
//    	Special Default/Normal/Automatic color.
//    	BLACK 
//    	BLUE 
//    	BLUE_GREY 
//    	BRIGHT_GREEN 
//    	BROWN 
//    	CORAL 
//    	CORNFLOWER_BLUE 
//    	DARK_BLUE 
//    	DARK_GREEN 
//    	DARK_RED 
//    	DARK_TEAL 
//    	DARK_YELLOW 
//    	GOLD 
//    	GREEN 
//    	GREY_25_PERCENT 
//    	GREY_40_PERCENT 
//    	GREY_50_PERCENT 
//    	GREY_80_PERCENT 
//    	INDIGO 
//    	LAVENDER 
//    	LEMON_CHIFFON 
//    	LIGHT_BLUE 
//    	LIGHT_CORNFLOWER_BLUE 
//    	LIGHT_GREEN 
//    	LIGHT_ORANGE 
//    	LIGHT_TURQUOISE 
//    	LIGHT_YELLOW 
//    	LIME 
//    	MAROON 
//    	OLIVE_GREEN 
//    	ORANGE 
//    	ORCHID 
//    	PALE_BLUE 
//    	PINK 
//    	PLUM 
//    	RED 
//    	ROSE 
//    	ROYAL_BLUE 
//    	SEA_GREEN 
//    	SKY_BLUE 
//    	TAN 
//    	TEAL 
//    	TURQUOISE 
//    	VIOLET 
//    	WHITE 
//    	YELLOW     	

    	return style;
	}
		
	private void setBackgroundColor( CellStyle style, HSSFColorPredefined color ) {
		
		// HSSFColor.HSSFColorPredefined.BLACK.getIndex()
	    style.setFillForegroundColor( color.getIndex() );
	    style.setFillPattern( FillPatternType.SOLID_FOREGROUND );
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

	private boolean excludeFromReport( Stock stock, boolean verbose ) {
		
		return false;
	}
	
	public void outputReport( String reportFileName ) throws Exception {
			
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
		    XSSFSheet reportSheet = wb.createSheet("report");
		    // create an empty work sheet
		    XSSFSheet sourcesSheet = wb.createSheet("sources");
		    		    
		    // header row
		    reportSheet.createRow( 0 ); // [ 0 : first row
		    sourcesSheet.createRow( 0 ); // [ 0 : first row

		    // prepare 1 row for each selected stock
		    // and compose the selection (only withinPEA and not to be ignored)

		    ArrayList<Stock> selection = new ArrayList<Stock>(); 
		    
		    for( int i = 0, row = 0 ; i < this.stocks.size() ; i++ ) {
		    	
		    	if( this.stocks.get(i).withinPEA == null || this.stocks.get(i).withinPEA == false ) {
		    		continue;
		    	}
		    	if( this.stocks.get(i).toIgnore != null && this.stocks.get(i).toIgnore == true ) {
		    		continue;
		    	}
		    	if( this.excludeFromReport( this.stocks.get(i), false ) == true ) {
		    		continue;
		    	}
		    	
		    	reportSheet.createRow( row + 1 ); // [ 0 : first row
		    	sourcesSheet.createRow( row + 1 ); // [ 0 : first row
		    	selection.add( this.stocks.get(i) );
		    	row++;
		    }
		    
		    System.out.println( String.format( "selection is about %d stock(s)", selection.size()));		    
		    
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
		    
		    // write file
		    
			String reportXLS = this.context.rootFolder + "/" + "output/" + reportFileName;
		    
	        try( FileOutputStream outputStream = new FileOutputStream( reportXLS ) ) {

	            wb.write(outputStream);
	        }

	        System.out.println( String.format( "report generation for %d stock(s) : OK", selection.size()));
	    }	    
	}	
	
}
