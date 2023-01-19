package miouge.beans.jsonMapping;

import miouge.beans.Event;

public class DividendEvent extends Event {

	Double amount; // as mentionned by the event
	Double ajusted; // "actualized" value considering the share division
	
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Double getAjusted() {
		return ajusted;
	}
	public void setAjusted(Double ajusted) {
		this.ajusted = ajusted;
	}
}
