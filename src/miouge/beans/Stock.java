package miouge.beans;

import java.util.ArrayList;
import java.util.TreeMap;

public class Stock {
	
	// loaded from liste.csv (in the same order)
	public String isin;
	//public String countryCode; 		  // FR	
	public String mnemo;
	public Boolean withinPEA;
	public String withinPEALabel; // "PEA" ou null
	public Boolean toIgnore;
	public Boolean inPortfolio;
	public String name;
	
	public Long effectif;
			
	// abc bourse
	public String abcSuffix; // "ALLECp""
	public Long   abcSharesCount;
	
	// trading sat
	public String tradingSatUrl;
	public String tsSuffix; // "/engie-FR0010208488/societe.html"
	public Long   tsSharesCount;
	
	// zonebourse
	public String zbUrl;
	public String zbSuffix; // TELEPERFORMANCE-SE-4709
	
	// boursorama
	public String bmaSuffix; // "1rPMALT"
	public Long   bmaSharesCount;
	
	// yahoo
	public String yahooSymbol; // "VIRP.PA"
	
	// AlphaVantage
	public String aphaSymbol;

	// sources data from websites 
		
	public Double sharesCount; 	      // nombre de titres (source ZB)
	public Double lastQuote; 	      // last quotation (source ZB)
		
	// CR
	
	public ArrayList<Double> histoEBITDA; // M€ (source ZB)
	public Integer sizeEBITDA;	
	public ArrayList<Double> histoEBIT;  // M€ (source ZB)
	public Integer sizeEBIT;
	public ArrayList<Double> histoRN; // M€ (source ZB)
	public Integer sizeRN;
	
	// Bilan	

	public Double lastVE;
	public Double BVPS; // book value per share
		
	// Dividends

	public ArrayList<Double> histoDIV; // € (source ZB)
	public Integer sizeDIV;
	
	public TreeMap<Long,Event> events = new TreeMap<Long,Event>(); // epoch event object
	public Integer eventCount;
	
	// computed ratio

	public Double avgEBITDA;
	public Double growthEBITDA; // en %
	public Double avgEBIT;
	public Double growthEBIT; // en %
	public Double avgRN;
	public Double growthRN; // en %
	public Double avgBNA;
	public Double avgDIV;
	public Double growthDIV; // en %
	
	public Double rdtPerc; 		// rendement en %
	public Double payoutPerc;   // payout %

	public Double avgPER;
	public Double ratioQuoteBV;       // ratio Cours / BVPS
	public Double ratioVeOverEBIT;    // ratio  VE / EBIT	( eg 8.5 )

	public Double dfn;   	 	      // dette financiere nette en M€ (si < 0 trésorie nette))
	public Double ratioDfnOverEBITDA; // ratio DFN / EBITDA
	public Double netCashPS;          // ratio net cash per share

	public Overrides overrides = new Overrides();
	
	public Double rating = 0.0;
	public String ratingTxt = "";
	
//  public Double capitalization;     // en M€ (Nb de titres * last quotation)	
//  public Double debtRatio; // ratio d'endettement en %		
//	public Double previousQuote1; 	  // previous quotation point	
//	public Double progressionVsQuote1;
	
}
