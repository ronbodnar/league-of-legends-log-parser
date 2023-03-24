/*
 *
 */
package org.mron.logparser.impl;

/**
 * 
 * @author Ron
 *
 */
public class GameSummoner {

	private String team;

	private int skinID;
	private int clientID;

	private String entityType;
	private String summonerName;
	private String championName;

	public GameSummoner(String team, int skinID, int clientID, String entityType, String summonerName, String championName) {
		this.team = team;
		this.skinID = skinID;
		this.clientID = clientID;
		this.entityType = entityType;
		this.summonerName = summonerName;
		this.championName = championName;
	}

	public String getTeam() {
		return team;
	}

	public int getSkinID() {
		return skinID;
	}

	public int getClientID() {
		return clientID;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getSummonerName() {
		return summonerName;
	}

	public String getChampionName() {
		return championName;
	}

	public String toString() {
		return getTeam() + " - " + getSkinID() + " - " + getClientID() + " - " + getEntityType() + " - " + getSummonerName() + " = " + getChampionName();
	}

}