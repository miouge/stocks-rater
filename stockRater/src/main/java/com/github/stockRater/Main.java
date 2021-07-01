package com.github.stockRater;

import com.github.stockRater.beans.AnswerHandler;
import com.github.stockRater.beans.GetApi;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TargetServer;

public class Main 
{
    public static void main( String[] args )
    {
    	// https://www.tradingsat.com/engie-FR0010208488/societe.html
    	
    	
		TargetServer target = new TargetServer();
		//target.setBaseUrl( "http://127.0.0.1:8080" );
		target.setBaseUrl( "https://www.tradingsat.com" );
    
		//target.authenticate();
    	
		Stock stock = new Stock( "engie", "FR0010208488" ); 
		GetApi api = new GetApi("/engie-FR0010208488/societe.html");
		api.setUrlSuffix( String.format( "/%s-%s/societe.html", stock.getName(), stock.getIsin()));
		AnswerHandler handler = new AnswerHandler();
				
		try {
						
			api.perform( target, null, stock, handler );
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    	
    	
        System.out.println( "complete ... !" );
    }
}
