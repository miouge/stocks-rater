package miouge.handlers;

import miouge.beans.Stock;

public class BmaConsensusHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + ".html";
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {
		
		PatternFinder pf;
		String data;
		
		// DFN : Dette financière nette
		
		pf = new PatternFinder( response, thePf -> {

			thePf.contextPatterns.add( "Dette financière nette" );
			thePf.outOfContextPattern = "Actif net par action";
			thePf.leftPattern = "u-text-left u-text-right u-ellipsis\">";
			thePf.rightPattern = "<br>"; // ou </td>
		}); 		
		data = pf.find().replace(" ", "").trim();

		if( data.equals("-") == false ) {
			stock.dfnBma = Double.parseDouble(data);
		}

		if( stock.dfnBma == null ) {
			pf = new PatternFinder( response, thePf -> {
	
				thePf.contextPatterns.add( "Dette financière nette" );
				thePf.outOfContextPattern = "Actif net par action";
				thePf.leftPattern = "u-text-left u-text-right u-ellipsis\">";
				thePf.rightPattern = "</td>"; 
			}); 		
			data = pf.find().replace(" ", "").trim();
	
			if( data.equals("-") == false ) {
				stock.dfnBma = Double.parseDouble(data);
			}
		}
		
		return true;
	}
}
