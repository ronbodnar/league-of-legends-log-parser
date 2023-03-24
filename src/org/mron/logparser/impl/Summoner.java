/*
 *
 */
package org.mron.logparser.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ron
 *
 */
public class Summoner {

	private String name;

	private int wins;
	private int losses;
	private int abandons;
	private int unknownResults; // used when the game is abandoned ONLY
	
	private boolean botAI;

	private int appearances;

	private List<Champion> champions = new ArrayList<Champion>();

	public Summoner(String name, boolean botAI) {
		this.name = name;
		this.botAI = botAI;
	}

	public void addChampion(Champion newChampion, int skinID) {
		if (newChampion == null) {
			return;
		}
		Champion champion = getChampionForName(newChampion.getName());
		if (champion == null) {
			champions.add(newChampion);
		} else {
			champion.setWins(newChampion.getWins());
			champion.setLosses(newChampion.getLosses());
			champion.setAbandons(newChampion.getAbandons());
			champion.setUnknownResults(newChampion.getUnknownResults());
			champion.setDeathCount(newChampion.getDeathCount());
			champion.setPlayCount(newChampion.getPlayCount());
		}
	}

	public Champion getChampionForName(String name) {
		for (int i = 0; i < champions.size(); i++) {
			if (champions.get(i) == null) {
				continue;
			}
			if (champions.get(i).getName().equals(name)) {
				return champions.get(i);
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}
	
	public boolean isBotAI() {
		return botAI;
	}

	public int getWins() {
		return wins;
	}

	public void increaseWins() {
		wins++;
	}

	public int getLosses() {
		return losses;
	}

	public void increaseLosses() {
		losses++;
	}

	public int getAbandons() {
		return abandons;
	}

	public void increaseAbandons() {
		abandons++;
	}

	public int getUnknownResults() {
		return unknownResults;
	}

	public void increaseUnknownResults() {
		unknownResults++;
	}

	public List<Champion> getChampions() {
		return champions;
	}

	public int getAppearances() {
		return appearances;
	}

	public void increaseAppearances() {
		appearances++;
	}

	@Override
	public String toString() {
		return "Summoner: [name=" + name + ", wins=" + wins + ", losses=" + losses + ", abandons=" + abandons + ", appearances=" + appearances + ", champions=" + champions.size() + "]";
	}

}