package com.github.stockRater.beans;

import java.util.ArrayList;

public class Stock {
	
	// loaded from liste.csv
	
	public String isin;	
	public String name;	
	public String mnemo;
	
	public String countryCode; // FR
	
	
	
	// trading sat
	public String tradingSatUrl;
	public String tradingSatUrlSuffix; // "/engie-FR0010208488/societe.html"	
		
	// nombre de titres
	public Long sharesCount;
	public Long capitalisation;
	public Double endettement = null; // en %
	
	// last quotation
	
	public Double lastQuote;
	
	// Bilan
	
	public Long capitauxPropres;
	
	// Compte de resultat	
	
	public ArrayList<Long> histoRNPG;
	public Double avgRNPG;
	public Double avg5yPER;
	public Double ratioQuoteBV; // ration cours / capitaux propres par action
	
		
	// Dividende	
}
