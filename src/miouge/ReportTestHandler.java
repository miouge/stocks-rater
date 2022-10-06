package miouge;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.stockRater.beans.Context;
import com.github.stockRater.beans.GetApi;
import com.github.stockRater.beans.Stock;
import com.github.stockRater.beans.TargetServer;
import com.github.stockRater.handlers.ZbFondamentalHandlerB;
import com.github.stockRater.handlers.ZbSearchHandler;

public class ReportTestHandler extends ReportGeneric {

	public ReportTestHandler(Context context) {
		super(context);
	}

	public void fetchDataZb() throws Exception {
		
		// ---------------- ZONE BOURSE -------------------
		
		System.out.println( "fetch data from ZONE BOURSE ..." );
				
		TargetServer zoneBourse = new TargetServer();
		zoneBourse.setBaseUrl( "https://www.zonebourse.com" );
		
		System.out.println( "	search ZbSuffix ..." );
		
		// https://www.zonebourse.com/recherche/instruments/?aComposeInputSearch=s_FR		
		// retrieve Zone Bourse custom company urlSuffix from ISIN code
		// la reponse est une liste ou l'on prend l'action coté a paris
		this.stocks.forEach( stock -> {	
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/recherche/instruments/?aComposeInputSearch=s_%S", stock.isin );
				theApi.stock = stock;
				theApi.handler = new ZbSearchHandler();
				theApi.handler.cacheSubFolder = "/cache/zb-searched";
			});			
			api.perform( zoneBourse, false );
		});
		
		this.stocks.forEach( stock -> {	
			
			if( stock.toIgnore == true ) { return; }
			if( stock.withinPEA == false ) { return; }
			if( stock.zbSuffix == null ) { return; }
			
			if( stock.mnemo.equals("VCT") == false ) { return; } else { System.out.println( "mnemo=" + stock.mnemo ); }
			
			GetApi api = new GetApi( this.context, theApi -> {
				
				theApi.urlSuffix = String.format( "/cours/action/%s/fondamentaux/", stock.zbSuffix );
				theApi.stock = stock;
				theApi.handler = new ZbFondamentalHandlerB();
				theApi.handler.cacheSubFolder = "/cache/zb-fondamentaux";
				theApi.charset = StandardCharsets.ISO_8859_1;
			});			
			api.perform( zoneBourse, true );
		});
	}	
	
	public void fetchData() throws Exception {
		
		fetchDataZb();
	}

	@Override
	void compute(Stock stock) {
	}

	@Override
	boolean excludeFromReport(Stock stock, boolean verbose) {
		return false;
	}		

	@Override
	void composeReport(XSSFWorkbook wb, HashMap<Integer, CellStyle> precisionStyle, ArrayList<Stock> selection)	throws Exception {		
	}
}
