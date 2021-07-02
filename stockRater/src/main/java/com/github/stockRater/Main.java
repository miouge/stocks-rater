package com.github.stockRater;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.stockRater.beans.AnswerHandler;
import com.github.stockRater.beans.GetApi;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TargetServer;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public class Main 
{
	static ArrayList<Stock> stocks = new ArrayList<Stock>(); 
		
	private static void loadCSVData() throws FileNotFoundException, IOException, CsvException {

		String listeCSV = "D:/GIT/MISC/SRT/stockRater/data/liste.csv";
		
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
				stocks.add( stock );
			});
		}
	}
	
    public static void main( String[] args )
    {

		try {
			
	    	loadCSVData();
	    	
	    	System.out.print( String.format( "loaded %d stocks", stocks.size() ));
	    	
	    	
	    	// https://www.tradingsat.com/engie-FR0010208488/societe.html
			TargetServer target = new TargetServer();
			//target.setBaseUrl( "http://127.0.0.1:8080" );
			target.setBaseUrl( "https://www.tradingsat.com" );
	    
			//target.authenticate();
	    	
			//Stock stock = new Stock( "engie", "FR0010208488" );
				
			/*
			GetApi api = new GetApi( stock, (theStock, theApi) -> {			
				theApi.setUrlSuffix( String.format( "/%s-%s/societe.html", theStock.getName(), theStock.getIsin()));		
			});
			
			AnswerHandler handler = new AnswerHandler();
						
			api.perform( target, null, stock, handler );
			
			*/
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    	
    	
        System.out.println( "complete ... !" );
    }
}
