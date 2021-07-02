package com.github.stockRater;

public class Main 
{
	public static void main( String[] args )
    {

		try {
			
			Report report = new Report();
			
			// load the stocks CSV reference
			report.loadCSVData();
			System.out.print( String.format( "loaded %d stocks\n", report.stocks.size() ));
						
			// ask some websites for key figures
			report.fetchData();
			
			// generate report
			report.output();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    	
    	
        System.out.println( "complete ... !" );
    }
}
