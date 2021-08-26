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
			report.loadCsvData( "stocks-input.csv" );
			
			// load the portfolio CSV file
			report.loadPortfolio( "portfolio.csv" );

			// retrieve needed data asking some websites
			report.fetchData();

			// compute ratio
			report.computeAll();

			// generate CSV
			report.flushCsvData( "stocks-output.csv" );
			
			// generate report
			report.outputReport();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			System.err.println( "report generation end ... on ERROR !" );
		}
	}
}
