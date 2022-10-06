package miouge.beans.jsonMapping;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbcDividend {
	
	/*
    {
        "Item1": "03/05/2017",
        "Item2": "Détachement du dividende",
        "Item3": "Montant : 1.5€"
    }
    */	
	
	@JsonProperty("Item1")
	String timestamp;
	
	@JsonProperty("Item2")
	String label;
	
	@JsonProperty("Item3")
	String amount;
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
}
