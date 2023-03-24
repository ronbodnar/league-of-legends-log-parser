/*
 *
 */
package org.mron.logparser.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author Ron
 *
 */
public class Champion {

	private String name;

	private int playCount;

	private int wins;
	private int losses;
	private int abandons;
	private int unknownResults;

	private int deathCount;
	
	private long timePlaying;

	private Map<Integer, Integer> skinData;

	public Champion(String name) {
		this.name = name;
		this.skinData = new LinkedHashMap<Integer, Integer>();
	}

	public void increaseSkinUse(int id, int amount) {
		boolean exists = skinData.containsKey(id);
		if (exists) {
			skinData.put(id, skinData.get(id) + amount);
		} else {
			skinData.put(id, amount);
		}
	}

	public String getName() {
		return name;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public void increaseWins(int amount) {
		wins += amount;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public void increaseLosses(int amount) {
		losses += amount;
	}

	public int getAbandons() {
		return abandons;
	}

	public void setAbandons(int abandons) {
		this.abandons = abandons;
	}

	public void increaseAbandons(int amount) {
		abandons += amount;
	}

	public int getUnknownResults() {
		return unknownResults;
	}

	public void setUnknownResults(int unknownResults) {
		this.unknownResults = unknownResults;
	}

	public void increaseUnknownResults(int amount) {
		unknownResults += amount;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	public void increasePlayCount(int amount) {
		playCount += amount;
	}

	public int getDeathCount() {
		return deathCount;
	}

	public void setDeathCount(int deathCount) {
		this.deathCount = deathCount;
	}

	public void increaseDeathCount(int amount) {
		deathCount += amount;
	}
	
	public long getTimePlaying() {
		return timePlaying;
	}
	
	public void increaseTimePlaying(long amount) {
		timePlaying += amount;
	}

	public Map<Integer, Integer> getSkinData() {
		return skinData;
	}

}