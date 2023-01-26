package miouge.handlers;

import miouge.beans.Stock;

public class ZbResumeHandler extends ResponseHandlerTemplate {

	@Override
	public String getDumpFilename( Stock stock ) {
		
		return stock.mnemo + "-" + stock.isin + "-resume.html";	
	}

	@Override
	public boolean customProcess(Stock stock, StringBuilder response ) throws Exception {
		
		return true;
	}
}
