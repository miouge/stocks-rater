package miouge.handlers;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import miouge.beans.Stock;
import miouge.beans.jsonMapping.AlphaMatch;
import miouge.beans.jsonMapping.AlphaSearched;

public class AlphaSearchHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return "Alpha-" + stock.isin + ".json";		
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {

		boolean success = false;
		
		// System.out.println( String.format( "customProcess for %s ...", stock.name ));		
		
		ObjectMapper mapper = new ObjectMapper();
			
		mapper.enable( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY );
		mapper.disable( DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES );
		mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
			
		AlphaSearched item = mapper.readValue( response.toString(), AlphaSearched.class);
		//List<AlphaSearched> objs =  mapper.readValue(response.toString(), new TypeReference<List<TSSearchedIsin>>() {});
		
		if( item == null ) {
			return false;
		}
		
		ArrayList<AlphaMatch> bestMatches = item.getBestMatches();
		
		if( bestMatches == null ) {
			return false;
		}
		
		ArrayList<AlphaMatch> parisEquities = new ArrayList<AlphaMatch>(); 
		
		for( AlphaMatch match : bestMatches ) {
			
			if( match.getRegion().equals("Paris") == false ) { continue; }
			if( match.getType().equals("Equity") == false ) { continue; }
			
			// System.out.println( String.format( "match : symbol founded <%s> {%s} searching for name <%s> ...", match.getSymbol(), match.getName(), stock.name ));
			parisEquities.add(match);
		}
		
		if( parisEquities.size() == 1 ) {
			
			AlphaMatch match = parisEquities.get(0); 			
			// only one match consider that's OK
			System.out.println( String.format( "using match symbol <%s> {%s} for name <%s> ...", match.getSymbol(), match.getName(), stock.name ));
			stock.aphaSymbol = match.getSymbol();
		}
		else {

			// do it a second times just for trace
			for( AlphaMatch match : bestMatches ) {
				
				if( match.getRegion().equals("Paris") == false ) { continue; }
				if( match.getType().equals("Equity") == false ) { continue; }				
				System.out.println( String.format( "multiple matches : symbol founded <%s> {%s} searching for name <%s> ...", match.getSymbol(), match.getName(), stock.name ));
			}			
		}
		
		return success;
	}
}
