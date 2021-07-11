package com.github.stockRater.beans.jsonMapping;

import java.util.ArrayList;

public class AlphaSearched {

	ArrayList<AlphaMatch> bestMatches;

	public ArrayList<AlphaMatch> getBestMatches() {
		return bestMatches;
	}

	public void setBestMatches(ArrayList<AlphaMatch> bestMatches) {
		this.bestMatches = bestMatches;
	}
}
