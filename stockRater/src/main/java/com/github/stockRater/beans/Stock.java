package com.github.stockRater.beans;

public class Stock {
	
	private String isin;
	private String name;	
	private String mnemo;
	
	private String countryCode; // FR
		
	// nombre de titres
	private long stocksCount;

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMnemo() {
		return mnemo;
	}

	public void setMnemo(String mnemo) {
		this.mnemo = mnemo;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public long getStocksCount() {
		return stocksCount;
	}

	public void setStocksCount(long stocksCount) {
		this.stocksCount = stocksCount;
	}
}
