package miouge;

import com.github.stockRater.beans.Context;

public class StocksRaterMain 
{
	@SuppressWarnings("unused")
	private static void testHandlers( Context context ) throws Exception {
		
		ReportTestHandler report = new ReportTestHandler( context );
		
		// load the stocks reference CSV file
		report.loadCsvData( "stocks-input.csv" );
			
		// retrieve needed data asking some websites
		report.fetchData();
	}
	
	@SuppressWarnings("unused")
	private static void generateReportA( Context context ) throws Exception {
		
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
	
	@SuppressWarnings("unused")
	private static void generateReportB( Context context ) throws Exception {
		
		ReportB reportB = new ReportB( context );
		
		// load the stocks reference CSV file
		reportB.loadCsvData( "stocks-input.csv" );
		
		// load the portfolio CSV file
		// report.loadPortfolio( "portfolio.csv" );

		// retrieve needed data asking some websites
		reportB.fetchData();

		// compute ratio
		//reportB.computeAll();
			
		// generate report
		//reportB.outputReport( "reportB.xlsx" );
	}
	
	public static void main( String[] args )
	{
		try {
			
			Context context = new Context();
			
			
			//testHandlers( context );
			//generateReportA( context );
			generateReportB( context );
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			System.err.println( "report generation end ... on ERROR !" );
		}
	}
}
