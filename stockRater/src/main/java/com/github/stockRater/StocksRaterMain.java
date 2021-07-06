package com.github.stockRater;

import com.github.stockRater.beans.Context;

public class StocksRaterMain 
{
	public static void main( String[] args )
	{

		try {
			
			Context context = new Context();
			
			Report report = new Report( context );
			
			// load the stocks reference CSV file
			report.loadCsvData( "stocks.csv " );
						
			// retrieve needed data asking some websites
			report.fetchData();

			// generate report
			report.output();
			
			 System.out.println( "report generation OK" );
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			System.err.println( "report generation end ... on ERROR !" );
		}
	}
}
