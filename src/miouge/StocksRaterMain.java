
package miouge;

import miouge.beans.Context;

public class StocksRaterMain 
{
	@SuppressWarnings("unused")
	private static void generateReportA( Context context ) throws Exception {
		
		Report report = new Report( context );
		
		// load the stocks reference CSV file
		report.loadCsvData( "stocks-input.csv" );
		
		// load the portfolio CSV file
		// report.loadPortfolio( "portfolio.csv" );

		// retrieve needed data asking some websites
		report.fetchData();

		// compute ratio
		report.computeAll();

		// generate CSV
		// report.flushCsvData( "stocks-output.csv" );
		
		// generate report
		report.outputReport( "reportA.xlsx" );
	}
	
	@SuppressWarnings("unused")
	private static void generateReportZB( Context context ) throws Exception {
		
		ReportZB reportZB = new ReportZB( context );
		
		// load the stocks reference CSV file
		reportZB.loadCsvData( "stocks-input-ZB.csv" );		
		// reportZB.loadCsvData( "stocks-input-ZB-onlyOwned.csv" );
		// reportZB.loadCsvData( "stocks-input-ZB-onlyVCT.csv" );
		
		// load the portfolio CSV file
		// report.loadPortfolio( "portfolio.csv" );

		// retrieve needed data asking some websites
		reportZB.fetchData();

		// compute ratio
		reportZB.computeAll();
		
		// generate CSV
		// reportZB.flushCsvData( "stocks-output-ZB.csv" );
			
		// generate report
		reportZB.outputReport( "reportZB.xlsx" );
	}
	
	public static void main( String[] args )
	{
		try {
			
			Context context = new Context();
			
			
			//testHandlers( context );
			//generateReportA( context );
			generateReportZB( context );
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			System.err.println( "report generation end ... on ERROR !" );
		}
	}
}
