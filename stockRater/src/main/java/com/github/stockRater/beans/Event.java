package com.github.stockRater.beans;

public class Event {
	
	Long epoch;
	String timestamp;
	
	public Long getEpoch() {
		return epoch;
	}
	public void setEpoch(Long epoch) {
		this.epoch = epoch;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
