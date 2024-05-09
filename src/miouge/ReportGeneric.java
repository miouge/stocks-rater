package miouge;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import miouge.beans.Context;
import miouge.beans.ExclusionResume;
import miouge.beans.Stock;

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
		
		try( CSVReader reader = new CSVReaderBuilder( new FileReader(stocksCSV) )
				.withCSVParser(csvParser) // custom CSV
				.withSkipLines(1) // skip the first line (header info)
				.build()
		) {

			List<String[]> lines = reader.readAll();
			lines.forEach( fields -> {

				// for each line except the header
				
				int idx = 0;
				
				String isin = fields[idx++]; // reading of column ISIN
				if( isin.length() != 12 ) { return; } // IsinCode = 12 characters length

				Stock stock = new Stock();
				this.stocks.add( stock );
				
				stock.isin = isin;
				this.stocksByIsin.put( stock.isin, stock );
				
				//stock.countryCode=fields[idx++];
				//stock.countryCode = stock.isin.substring(0, 2);
				
				stock.name       = fields[idx++];
				stock.mnemo      = fields[idx++]; // also named "ticker"
				stock.zbSuffix   = fields[idx++];
				stock.yahooSymbol= fields[idx++];
				
				String ignoreUntil = fields[idx++]; // dd/MM/YYYY
				if( ignoreUntil.length() == 0 ) {
					stock.toIgnore = false;					
				}
				else {
					
					ignoreUntil += " 00:00:00";					
					long ignoreUntilEpoch = EpochTool.convertToEpoch( ignoreUntil, EpochTool.Format.STD_SLASH_FULL_FR, null );
					if( EpochTool.getNowEpoch() < ignoreUntilEpoch ) {
						stock.toIgnore = true;
					}					
				}
				
				if( fields[idx].length() > 0 ) {stock.withinPEA   = Boolean.parseBoolean( fields[idx] ); }; idx++;
				if( fields[idx].length() > 0 ) {stock.inPortfolio = Boolean.parseBoolean( fields[idx] ); }; idx++;
				
				if( stock.withinPEA   == null ) { stock.withinPEA   = false; }
				if( stock.toIgnore    == null ) { stock.toIgnore    = false; }
				if( stock.inPortfolio == null ) { stock.inPortfolio = false; }

				// System.out.println( stock.mnemo + " PEA=" + stock.withinPEA + "/ Ignore=" + stock.toIgnore );
			});
		}

		System.out.println( String.format( "Stock definitions loaded : %d", this.stocks.size() ));

		List<Stock> overrided = new ArrayList<Stock>();

		this.stocks.forEach( stock -> {

			if( stock.withinPEA ) {
				stock.withinPEALabel = "PEA";
			}
			
			if( stock.isin != null && stock.isin.length() > 0 && stock.mnemo != null && stock.mnemo.length() > 0 ) {

				// check presence of overrides ...
				String overrideFile = context.rootFolder + "/data/overrides/" + stock.mnemo + "-" + stock.isin + ".ini";
				Path path = Paths.get( overrideFile );
				
				// file exists and it is not a directory
				if( Files.exists(path) && !Files.isDirectory(path)) {
					
					// System.out.println(String.format( "loading override for %s-%s ...", stock.mnemo, stock.isin ));
					overrided.add( stock );
					
					// check presence of override for the SharesCount ...
					
					String value = Tools.getIniSetting( overrideFile, "General", "SharesCount", "" );
					if( value != null && value.length() > 0 ) {
						stock.overrides.sharesCount = Long.parseLong(value);
					}
				}
			}
		});

		
		System.out.println( String.format( "Overrides loaded : %d", overrided.size() ));
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
		header[column] = "ToIgnore";column++;
		header[column] = "WithinPEA";column++;
		header[column] = "InPortfolio";column++;
		
		for( Stock stock : this.stocks ) {
					
			String[] array = new String[COLUMN_NB];
			stringArray.add(array);
			column = 0;			
			setCsvCell( array, column++, stock.isin );			
			setCsvCell( array, column++, stock.name );
			setCsvCell( array, column++, stock.mnemo );
			setCsvCell( array, column++, stock.zbSuffix );
			setCsvCell( array, column++, stock.yahooSymbol );						
			setCsvCell( array, column++, stock.toIgnore );
			setCsvCell( array, column++, stock.withinPEA );
			setCsvCell( array, column++, stock.inPortfolio );			
		}
		
	     CSVWriter writer = new CSVWriter(new FileWriter(stocksCSV), ';', '\u0000', '\\', "\n" );
	     
	     writer.writeAll(stringArray);
	     writer.close();
	     
	     System.out.println( String.format( "flush stock definitions Csv for %d stock(s) : OK", stocks.size()));
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
    		
    		style.setAlignment( HorizontalAlignment.CENTER ); // centré par défaut
    		consumer.accept( style );
    	}
    	
//    	if( stock.inPortfolio ) {
//
//    		final Font font = wb.createFont ();
//    		font.setFontName( "Calibri" );
//    		font.setItalic( true );
//    		font.setBold( true );
//    		style.setFont(font);
//    	}

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
	
	protected boolean excludeFromReport( ExclusionResume resume, Stock stock, boolean verbose ) { return false; }
	
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
	        {
	        	CellStyle style = wb.createCellStyle();
	        	style.setDataFormat( ch.createDataFormat().getFormat("0"));
	        	precisionStyle.put( 0, style );
	        }
	        {
	        	CellStyle style = wb.createCellStyle();
	        	style.setDataFormat( ch.createDataFormat().getFormat("#,##0"));
	        	precisionStyle.put( 1000, style );
	        }
	        
		    // and compose the selection (only withinPEA and not to be ignored)

		    ArrayList<Stock> selection = new ArrayList<Stock>();
		    ExclusionResume resume = new ExclusionResume();

		    for( int i = 0 ; i < this.stocks.size() ; i++ ) {

		    	if( this.stocks.get(i).toIgnore != null && this.stocks.get(i).toIgnore == true ) {
		    		continue;
		    	}

		    	if( this.excludeFromReport( resume, this.stocks.get(i), false ) == true ) {
		    		continue;
		    	}
		    	selection.add( this.stocks.get(i) );
		    }

		    if( resume.fundamentalsUnavailable > 0 ) {
		    	System.out.println( String.format( "%d stock(s) excluded reason : fundamentals not available", resume.fundamentalsUnavailable ));
		    }		    
		    if( resume.tooSmallVE > 0 ) {
		    	System.out.println( String.format( "%d stock(s) excluded reason : too small VE", resume.tooSmallVE ));
		    }
		    if( resume.nonProfitable > 0 ) {
		    	System.out.println( String.format( "%d stock(s) excluded reason : non profitable", resume.nonProfitable ));
		    }		    
		    if( resume.operationalLoss > 0 ) {
		    	System.out.println( String.format( "%d stock(s) excluded reason : operational loss detected", resume.operationalLoss ));
		    }
		    if( resume.insufficientRating > 0 ) {
		    	System.out.println( String.format( "%d stock(s) excluded reason : insufficient rating", resume.insufficientRating ));
		    }
		    if( resume.fondamentalNotOK > 0 ) {
		    	System.out.println( String.format( "%d stock(s) excluded reason : fondamental not OK", resume.fondamentalNotOK ));
		    }

		    System.out.println( String.format( "output selection is about %d stock(s)", selection.size()));		    
	        
	        this.composeReport( wb, precisionStyle, selection );
		    
		    // write out file
		    
			String reportXLS = this.context.rootFolder + "/" + "output/" + reportPrefixName;
		    
	        try( FileOutputStream outputStream = new FileOutputStream( reportXLS ) ) {

	            wb.write(outputStream);
	        }

	        System.out.println( String.format( "report generation for %d stock(s) : OK", selection.size()));
	    }	    
	}
}
