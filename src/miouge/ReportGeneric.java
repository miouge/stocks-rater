package miouge;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miouge.beans.Context;
import miouge.beans.Stock;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public abstract class ReportGeneric {

	Context context;
	
	protected ArrayList<Stock> stocks = new ArrayList<Stock>();
	protected TreeMap< String, Stock > stocksByIsin = new TreeMap< String, Stock >();
	
	@SuppressWarnings("unused")
	private ReportGeneric() {}
	
	public ReportGeneric(Context context) {
		super();
		this.context = context;
	}

	// base definition
		
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
				
				//stock.countryCode=fields[idx++];
				//stock.countryCode = stock.isin.substring(0, 2);
				
				stock.name       = fields[idx++];
				stock.mnemo      = fields[idx++]; // also named "ticker"
				stock.zbSuffix   = fields[idx++];
				stock.yahooSymbol= fields[idx++];
								
				if( fields[idx].length() > 0 ) {stock.withinPEA=Boolean.parseBoolean( fields[idx] ); }; idx++;
				if( fields[idx].length() > 0 ) {stock.toIgnore=Boolean.parseBoolean( fields[idx]); }; idx++;
				if( fields[idx].length() > 0 ) {
					Boolean owned = Boolean.parseBoolean( fields[idx]);
					if( owned ) { stock.portfolio = 1; }
				}; idx++;
				
				// System.out.println( stock.mnemo + " PEA=" + stock.withinPEA + "/ Ignore=" + stock.toIgnore );
			});
		}
		
		this.stocks.forEach( stock -> {

			if( stock.withinPEA == null ) {
				stock.withinPEA = false;
			}
			if( stock.toIgnore == null ) {
				stock.toIgnore = false;
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
	
	// computation

	abstract void compute( Stock stock );
	
	public void computeAll() throws Exception { 
		
		for( Stock stock : this.stocks ) {
			
			compute( stock );
		}		
		
		// now sort stock list by stock name
		this.stocks.sort( (st1, st2 ) -> {
			return st1.name.compareTo( st2.name );
		});
	}	

	// report generation
	
	protected CellStyle createStyle( XSSFWorkbook wb, Stock stock, Consumer<CellStyle> consumer ) throws Exception {

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

	protected void setBackgroundColor( CellStyle style, HSSFColorPredefined color ) {
		
		// HSSFColor.HSSFColorPredefined.BLACK.getIndex()
	    style.setFillForegroundColor( color.getIndex() );
	    style.setFillPattern( FillPatternType.SOLID_FOREGROUND );
	}
		
	protected XSSFCell createCell( XSSFRow row, int column, Object content ) {
		
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
		
	abstract void composeReport( XSSFWorkbook wb, HashMap<Integer,CellStyle> precisionStyle, ArrayList<Stock> selection ) throws Exception;
	
	abstract boolean excludeFromReport( Stock stock, boolean verbose );
	
	public void outputReport( String reportPrefixName ) throws Exception {
		
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
	        
		    // and compose the selection (only withinPEA and not to be ignored)

		    ArrayList<Stock> selection = new ArrayList<Stock>(); 
		    
		    for( int i = 0 ; i < this.stocks.size() ; i++ ) {
		    	
		    	if( this.stocks.get(i).withinPEA == null || this.stocks.get(i).withinPEA == false ) {
		    		continue;
		    	}
		    	if( this.stocks.get(i).toIgnore != null && this.stocks.get(i).toIgnore == true ) {
		    		continue;
		    	}
		    	if( this.excludeFromReport( this.stocks.get(i), false ) == true ) {
		    		continue;
		    	}
		    	selection.add( this.stocks.get(i) );
		    }
		    
		    System.out.println( String.format( "selection is about %d stock(s)", selection.size()));		    
	        
	        this.composeReport( wb, precisionStyle, selection );
		    
		    // write file
		    
			String reportXLS = this.context.rootFolder + "/" + "output/" + reportPrefixName;
		    
	        try( FileOutputStream outputStream = new FileOutputStream( reportXLS ) ) {

	            wb.write(outputStream);
	        }

	        System.out.println( String.format( "report generation for %d stock(s) : OK", selection.size()));
	    }	    
	}
}
