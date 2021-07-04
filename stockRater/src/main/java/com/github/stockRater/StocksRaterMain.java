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
			report.loadCsvData();			
						
			// ask some websites for key figures
			report.fetchData();
			
			// generate report
			report.output();
			
	        System.out.println( "complete ... OK !" );
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
	        System.err.println( "complete ... on ERROR !" );
		}    	
    }
}
