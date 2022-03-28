package com.github.stockRater;

import com.github.stockRater.beans.Context;

public class StocksRaterMain 
{
	public static void main( String[] args )
	{

		try {
			
			Context context = new Context();
			
			boolean generateReportA = false;
			boolean generateReportB = true;
			
			if( generateReportA ) {
				
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
				report.outputReport( "reportA.xlsx" );
			}
			
			if( generateReportB ) {
				
				ReportB reportB = new ReportB( context );
				
				// load the stocks reference CSV file
				reportB.loadCsvData( "stocks-input.csv" );
				
				// load the portfolio CSV file
				// report.loadPortfolio( "portfolio.csv" );
	
				// retrieve needed data asking some websites
				reportB.fetchData();
	
				// compute ratio
				reportB.computeAll();
					
				// generate report
				reportB.outputReport( "reportB.xlsx" );
			}			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			System.err.println( "report generation end ... on ERROR !" );
		}
	}
}
