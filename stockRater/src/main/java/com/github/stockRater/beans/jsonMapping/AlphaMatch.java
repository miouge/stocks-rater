package com.github.stockRater.beans.jsonMapping;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaMatch {

	@JsonProperty("1. symbol")
	String symbol;
	
	@JsonProperty("2. name")
	String name;
	
	@JsonProperty("3. type")
	String type;
	
	@JsonProperty("4. region")
	String Region;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRegion() {
		return Region;
	}

	public void setRegion(String region) {
		Region = region;
	}
}
