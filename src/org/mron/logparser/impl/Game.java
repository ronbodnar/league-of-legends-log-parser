/*
 *
 */
package org.mron.logparser.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * 
 * @author Ron
 *
 */
public class Game {

	private String id;
	private String date;
	private String mode;
	private String result;
	private String region;

	private int mapID;
	private int ownerID;

	private long length;
	private long endTime;
	private long startTime;
	
	private boolean botAIGame;

	private GameSummoner owner;

	private String[] killers;
	private GameSummoner[] summoners;

	public Game(String id, String date, String mode, GameSummoner owner, long startTime, long endTime, int mapID, String result, int ownerID, String region, boolean botAIGame, String[] killers, GameSummoner[] summoners) {
		this.id = id;
		this.date = date;
		this.mode = mode;
		this.owner = owner;
		this.result = result;
		this.endTime = endTime;
		this.ownerID = ownerID;
		this.region = region;
		this.killers = killers;
		this.botAIGame = botAIGame;
		this.summoners = summoners;
		this.startTime = startTime;
		this.length = (endTime - startTime);
	}

	public void addKillers(String[] additionalKillers) {
		int originalLength = this.killers.length;
		int additionalLength = additionalKillers.length;

		String[] newKillers = new String[originalLength + additionalLength];

		System.arraycopy(this.killers, 0, newKillers, 0, originalLength);
		System.arraycopy(additionalKillers, 0, newKillers, originalLength, additionalLength);

		this.killers = newKillers;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getID() {
		return id;
	}

	public String getMode() {
		return mode;
	}

	public String getResult() {
		return result;
	}

	public String getRegion() {
		return region;
	}

	public int getMapID() {
		return mapID;
	}

	public int getOwnerID() {
		return ownerID;
	}

	public long getLength() {
		return length;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getStartTime() {
		return startTime;
	}
	
	public boolean isBotAIGame() {
		return botAIGame;
	}

	public DateTime getDateTime() {
		return DateTime.parse(date, DateTimeFormat.forPattern("MM/d/yyy H:mm"));
	}

	public String getDate() {
		return date;
	}

	public GameSummoner getOwner() {
		return owner;
	}

	public String[] getKillers() {
		return killers;
	}

	public GameSummoner[] getSummoners() {
		return summoners;
	}

	@Override
	public String toString() {
		return "Game (id=" + id + ", date=" + date + ", mode=" + mode + ", owner=" + (owner == null ? "N/A" : owner.getSummonerName()) + ", length=" + getLength() + ", startTime=" + startTime + ", endTime=" + endTime + ", mapID=" + mapID + ", result=" + result + ", ownerID=" + ownerID + ", platform=" + region + ", killers=" + killers.length + ", summoners=" + summoners.length + ")";
	}

}