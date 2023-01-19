package miouge.beans;

import java.util.ArrayList;
import java.util.TreeMap;

public class Stock {
	
	// loaded from liste.csv (in the same order)
	public String isin;
	public String mnemo;
	public Boolean withinPEA;
	public Boolean toIgnore;
	public String name;
	public String activity;	
	public String comment;
	public Integer portfolio = 0; // current nb of stock in portfolio

	public String withinPEALabel; // "PEA" ou null

	public Long effectif;
	public Overrides overrides = new Overrides();
		
	// abc bourse suffix
	public String abcSuffix; // "ALLECp""
	public Long   abcSharesCount;
	
	// trading sat
	public String tradingSatUrl;
	public String tsSuffix; // "/engie-FR0010208488/societe.html"
	public Long   tsSharesCount;
	
	// zonebourse
	public String zbUrl;
	public String zbSuffix; // TELEPERFORMANCE-SE-4709
	//public Long   zbSharesCount; // TODO
	
	// boursorama
	public String bmaSuffix; // "1rPMALT"
	public Long   bmaSharesCount;
	
	// yahoo
	public String yahooSymbol; // "VIRP.PA"
	
	// AlphaVantage
	public String aphaSymbol;

	// aggregated data from all websites
	
	public String countryCode; 		  // FR
	
	public ArrayList<Long> shareCounts = new ArrayList<Long>(); // différentes récupération sur les websites
	public Long sharesCount; 	      // nombre de titres (max)
	
	public Double lastQuote; 	      // last quotation (from AbcEventsAndQuoteHandler)
	public Double capitalization;     // en M€ (Nb de titres * last quotation) 

	public Double previousQuote1; 	  // previous quotation point	
	public Double progressionVsQuote1;
	
	// Bilan	
	public Long capitauxPropres;
	public Double dfnZb;  // dette financiere nette en M€ (si < 0 trésorie nette)
	public Double dfnBma; // dette financiere nette en M€ (si < 0 trésorie nette
	public Double dfn;    // dette financiere nette en M€ (si < 0 trésorie nette))
	
	// Compte de resultat
	public ArrayList<Long> histoCA; // K€
	public Double avgCA;
	
	public ArrayList<Long> histoRNPG; // K€
	public ArrayList<Double> histoRN; // K€
	public Double avgRNPG;
	
	public ArrayList<Long> histoCP; // K€
	
	public ArrayList<Double> histoEBIT;
	public Double avgEBIT;
	
	public ArrayList<Double> histoDebtRatio; // ratio d'endettement en %
	public Double debtRatio;                 // en %	
	
	public ArrayList<Double> histoNetDebt; // Dette nette // difficile de connaitre le calage ?
	public ArrayList<Double> histoNetTres; // Trésorerie Nette
	
	public ArrayList<Double> histoVE; // Valeur d'entreprise (Capitalization + Dette financiere nette) en M€ (from Zb)
	public Double lastVE;                    // last Valeur d'entreprise si histoVE > 0;
	public Double soulteVE; // en M€ uantité à ajouter a la capitalization pour avoir la valeur d'entreprise
	
	// Dividends
	
	public TreeMap<Long,Event> events = new TreeMap<Long,Event>(); // epoch event object
	public Integer eventCount;
	
	// computed ratio
	
	public Double avg5yPER;
	public Double ratioQuoteBV;    // ratio Cours / capitaux propres part du groupe par action (eg 0.8)
	public Double ratioVeOverEBIT; // ratio  VE / EBIT	( eg 8.5 )
	//public Double rating;          // Personal based rating (from 0 to 100+)
}
