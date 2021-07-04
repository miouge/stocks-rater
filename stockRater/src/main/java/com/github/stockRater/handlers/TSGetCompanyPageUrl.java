package com.github.stockRater.handlers;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TSSearchedIsin;

public class TSGetCompanyPageUrl extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "TS-url-" + stock.isin + ".json";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response) throws Exception {

		boolean success = false;
		
		// System.out.println( String.format( "customProcess for %s ...", stock.name ));		
		
		ObjectMapper mapper = new ObjectMapper();
			
		mapper.enable( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY );
			
		//TSSearchedIsin item = mapper.readValue( response.toString(), TSSearchedIsin.class);			
		List<TSSearchedIsin> objs =  mapper.readValue(response.toString(), new TypeReference<List<TSSearchedIsin>>() {});
			
		if( objs.size() == 1 ) {
			
			TSSearchedIsin obj = objs.get(0);
			String link = obj.getLink(); // "///www.tradingsat.com/eurofins-scient-FR0014000MR3/"
			
			if( link.contains( "///www.tradingsat.com" ) ) {
				
				stock.tradingSatUrl = link.replace( "///", "" );
				stock.tradingSatUrlSuffix = link.replace( "///www.tradingsat.com", "" );				
				stock.lastQuote = Double.parseDouble( obj.getPrice());				
				success = true;
			}
			else {
				
				success = false;
			}
		}
		
		return success;
	}
}
