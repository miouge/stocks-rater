package com.github.stockRater.beans;

import java.util.ArrayList;

public class Stock {
	
	// loaded from liste.csv (in the same order)	
	public String isin;		
	public String mnemo;
	public Boolean withinPEA;
	public Boolean toIgnore;
	public String name;
	public String activity;
	
	// manual overrides of fix of exceptionnals events
	public Long initShareCount;
	public Long offsetRNPG;
	public Long offsetFCFW;
	public Double offsetDividends;
	public String commentOnIgnore;	
	public String commentOnOffsets;
	
	// trading sat
	public String tradingSatUrl;
	public String tradingSatUrlSuffix; // "/engie-FR0010208488/societe.html"
	public Long sharesCountTS;
	
	// abc bourse suffix
	public String abcSuffix; // "ALLECp""
	public Long sharesCountABC;
	
	// boursorama
	public String bmaSuffix; // "1rPMALT"
	
	// yahoo
	public String yahooSuffix; // "VIRP.PA"
	
	// AlphaVantage
	public String aphaSymbol;
	
	// zonebourse
	public String zbSuffix; // TELEPERFORMANCE-SE-4709	

	public String countryCode; 		// FR
	public Long sharesCount; 	      // nombre de titres
	public Long capitalization;       // en K€
	public Double endettement;        // en %
	public Double lastQuote; 	      // last quotation
	
	// Bilan
	
	public Long capitauxPropres;
	
	// Compte de resultat	
	
	public ArrayList<Long> histoRNPG;
	public ArrayList<Long> histoCP;
	public ArrayList<Double> histoEBIT;
	public Double avgRNPG;
	public Double avg5yPER;
	public Double ratioQuoteBV; // ration cours / capitaux propres par action

	// Dividende
	
	
	// rating
	public Double rating; // our personnal based rating 
	
}
