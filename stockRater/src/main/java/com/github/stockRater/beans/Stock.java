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
	
	// manual overrides or fix of exceptional events
	public Long initShareCount;
	public Long offsetRNPG;
	public Long offsetFCFW;
	public Long offsetEBIT; // TODO
	public Double offsetDividends;
	public String commentOnIgnore;	
	public String commentOnOffsets;
	
	// trading sat
	public String tradingSatUrl;
	public String tradingSatUrlSuffix; // "/engie-FR0010208488/societe.html"
	public Long   tradingSatSharesCount;
	
	// abc bourse suffix
	public String abcUrl; // TODO
	public String abcSuffix; // "ALLECp""
	public Long   abcSharesCount;
	
	// zonebourse
	public String zbUrl;  // TODO
	public String zbSuffix; // TELEPERFORMANCE-SE-4709
	public Long   zbSharesCount; // TODO
	
	// boursorama
	public String bmaSuffix; // "1rPMALT"
	
	// yahoo
	public String yahooSuffix; // "VIRP.PA"
	
	// AlphaVantage
	public String aphaSymbol;

	// aggregated data from all websites
	
	public String countryCode; 		  // FR
	public Long sharesCount; 	      // nombre de titres
	public Long capitalization;       // en K€

	public Double lastQuote; 	      // last quotation
	
	// Bilan	
	public Long capitauxPropres;
	
	// Compte de resultat	
	
	public ArrayList<Long> histoRNPG;
	public Double avgRNPG;
	
	public ArrayList<Long> histoCP;
	
	public ArrayList<Double> histoEBIT;
	public Double avgEBIT;
	
	public ArrayList<Double> histoDebtRatio; // ratio d'endettement en %
	public Double debtRatio;                 // en %	
	
	public ArrayList<Double> histoNetDebt; // Dette nette
	public ArrayList<Double> histoNetTres; // Trésorerie Nette
	
	public ArrayList<Double> histoVE; // Valeur d'entreprise
	
	// Dividends
	
	// computed ratio
	
	public Double avg5yPER;
	public Double ratioQuoteBV;    // ratio Cours / capitaux propres part du groupe par action (eg 0.8)
	public Double ratioVeOverEBIT; // ratio  VE / EBIT	( eg 8.5 )
	public Double rating;          // Personal based rating (from 0 to 100+)
}
