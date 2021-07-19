package com.github.stockRater.handlers;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stockRater.Tools;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.jsonMapping.AbcDividend;
import com.github.stockRater.beans.jsonMapping.DividendEvent;

public class AbcDivisionEventsHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + ".json";	
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response) throws Exception {

		boolean success = false;
		
		// System.out.println( String.format( "customProcess for %s ...", stock.name ));		
		
//		ObjectMapper mapper = new ObjectMapper();
//			
//		mapper.enable( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY );
//					
//		List<AbcDividend> objs =  mapper.readValue(response.toString(), new TypeReference<List<AbcDividend>>() {});
//
//		for( AbcDividend obj : objs) {
//						 			
//			Long epoch = Tools.convertToEpoch( obj.getTimestamp());
//			
//			DividendEvent dividend = new DividendEvent();
//			dividend.setEpoch( epoch );
//			dividend.setTimestamp(obj.getTimestamp());			
//			String amountStr = obj.getAmount().replace("Montant :", "" ).replace("€", "" ).replace(",", "." ).replace("(Solde)", "" ).replace("(Acompte)", "" ).trim();
//			dividend.setAmount(Double.parseDouble(amountStr));
//			stock.events.put(epoch, dividend);
//			// System.out.println( String.format( "[%s-%d] %s <%s> -> {%s}", obj.getTimestamp(), epoch, obj.getLabel(), obj.getAmount(), amountStr ));
//		}

		return true;
	}
}
