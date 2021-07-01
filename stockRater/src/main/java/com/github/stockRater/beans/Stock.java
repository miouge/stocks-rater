package com.github.stockRater.beans;

public class Stock {
	
	private String name;
	private String isin;
	
	
	private long stocksCount;

	public Stock( String name, String isin ) {
		super();
		this.isin = isin;
		this.name = name;
	}

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

	public long getStocksCount() {
		return stocksCount;
	}

	public void setStocksCount(long stocksCount) {
		this.stocksCount = stocksCount;
	}
}
