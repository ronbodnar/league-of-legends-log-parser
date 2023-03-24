/*
 *
 */
package org.mron.logparser;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mron.logparser.impl.Game;
import org.mron.logparser.impl.GameSummoner;

/**
 * 
 * @author Ron
 *
 */
public class ParserAPI {

	private int mapID;
	private int ownerID;

	private long endTime;
	private long startTime;

	private boolean spectator;

	private String gameID;
	private String gameDate;
	private String region;
	private String gameResult;
	private String gameMutator;

	private GameSummoner gameOwner;

	private ArrayList<Game> games = new ArrayList<Game>();

	private ArrayList<String> killers = new ArrayList<String>();
	private ArrayList<GameSummoner> gameSummoners = new ArrayList<GameSummoner>();

	/*
	 * Log line data
	 */
	private String mapLine;
	private String netUIDLine;
	private String logStartLine;
	private String gameInformationLine;
	private String exitCodeLine;
	private String gameMutatorLine;

	private boolean containsBotAI;
	private boolean oldSummonerFormat;

	private ArrayList<String> deathResults = new ArrayList<String>();
	private ArrayList<String> summonerResults = new ArrayList<String>();

	public void parseGameResults(String[] files) {
		for (int i = 0; i < files.length; i++) {
			String[] lines = files[i].split("\n");
			for (int a = 0; a < lines.length; a++) {
				String[] split = lines[a].split("\\|");
				if (split.length < 2 || lines[a].length() < 1) {
					continue;
				}
				String message = split[1];
				double secondsElapsed = split[0].length() <= 0 ? 0.0 : Double.parseDouble(split[0]);

				if (message.contains("PKT_World_SendGameNumber")) {
					gameInformationLine = message;
				}
				if (message.contains("level zip file:")) {
					mapLine = message;
				}
				if (message.contains("netUID:")) {
					netUIDLine = message;
				}
				if (message.contains("exit_code")) {
					exitCodeLine = message;
				}
				if (message.contains("The Killer was:")) {
					deathResults.add(message);
				}
				if (message.contains("GameMode Mutator")) {
					gameMutatorLine = message;
				}
				if (message.contains("PKT_StartGame")) {
					startTime = Math.round(secondsElapsed);
				}
				if (message.contains("End game message processed")) {
					endTime = Math.round(secondsElapsed);
				}
				if (message.contains("created for") || message.contains("Spawning champion")) {
					summonerResults.add(message);
					oldSummonerFormat = message.contains("created for");
				}
				if (lines[a].contains("Log started at") || message.contains("Logging started at")) {
					logStartLine = message;
				}
			}

			parseLogStart();
			parseResult();
			parseDeathData();
			parseGameMutator();
			parseMapID();
			parseGameInformation();
			parseOwnerID();
			parseGameSummoners();

			if (!spectator) {
				String[] killerArray = killers.toArray(new String[killers.size()]);

				GameSummoner[] gameSummonerArray = gameSummoners.toArray(new GameSummoner[gameSummoners.size()]);

				games.add(new Game(gameID, gameDate, gameMutator, gameOwner, startTime, endTime, mapID, gameResult, ownerID, region, containsBotAI, killerArray, gameSummonerArray));
			}
			reset();
		}
	}

	public void parseLogStart() {
		if (logStartLine == null) {
			gameDate = "?";
			return;
		}
		logStartLine = logStartLine.replaceAll("--- Log started at ", "");
		logStartLine = logStartLine.replaceAll("Logging started at ", "");

		boolean newFormat = logStartLine.substring(logStartLine.length() - 4, logStartLine.length() - 3).equals(".");

		String newPattern = "yyyy-MM-dd'T'H:mm:ss.SSS";
		String oldPattern = "EEE MMM dd H:mm:ss yyyy";

		DateTimeFormatter formatter = DateTimeFormat.forPattern(newFormat ? newPattern : oldPattern);
		DateTime dateTime = formatter.parseDateTime(logStartLine);

		gameDate = dateTime.toString("MM/d/yyy H:mm");
	}

	public void parseResult() {
		if (exitCodeLine == null) {
			gameResult = "?";
			return;
		}
		exitCodeLine = exitCodeLine.replaceAll("\"", "");
		exitCodeLine = exitCodeLine.replaceAll("\\{", "");
		exitCodeLine = exitCodeLine.replaceAll("\\}", "");
		String[] colon = exitCodeLine.split("\\:");
		if (colon.length < 4) {
			gameResult = "?";
			return;
		}
		String[] underscore = colon[3].split("_");
		if (underscore.length < 2) {
			gameResult = "?";
			return;
		}
		String result = underscore[1];
		if (result == null) {
			gameResult = "?";
		} else {
			if (result.equals("WIN")) {
				gameResult = "W";
			} else if (result.equals("LOSE")) {
				gameResult = "L";
			} else if (result.equals("ABANDON")) {
				gameResult = "A";
			} else {
				gameResult = "?";
			}
		}
	}

	public void parseDeathData() {
		for (int i = 0; i < deathResults.size(); i++) {
			killers.add(deathResults.get(i).replaceAll("The Killer was: ", ""));
		}
	}

	public void parseGameMutator() {
		if (gameMutatorLine == null) {
			gameMutator = "?";
			return;
		}
		gameMutatorLine = gameMutatorLine.replaceAll("\\.", "");
		gameMutatorLine = gameMutatorLine.replaceAll("Processing GameMode Mutator = ", "");
		gameMutatorLine = gameMutatorLine.trim();

		gameMutator = gameMutatorLine;
	}

	public void parseMapID() {
		if (mapLine == null) {
			mapID = -1;
			return;
		}
		mapLine = mapLine.replaceAll("Adding level zip file: Map", "");
		mapLine = mapLine.replaceAll(".zip", "");

		try {
			mapID = Integer.parseInt(mapLine);
		} catch (NumberFormatException exception) {
			mapID = -1;
		}
	}

	public void parseGameInformation() {
		if (gameInformationLine == null) {
			gameID = "?";
			region = "?";
			return;
		}
		gameInformationLine = gameInformationLine.replaceAll("Receiving PKT_World_SendGameNumber, GameID: ", "");
		gameInformationLine = gameInformationLine.replaceAll(", PlatformID: ", "|");

		String[] split = gameInformationLine.split("\\|");
		if (split.length < 1) {
			gameID = "?";
		} else {
			gameID = split[0].replaceFirst("^0+(?!$)", "").toUpperCase();
		}
		if (split.length < 2) {
			region = "?";
		} else {
			region = split[1];
		}
	}

	public void parseOwnerID() {
		if (netUIDLine == null) {
			ownerID = -1;
			return;
		}
		netUIDLine = netUIDLine.replaceAll("netUID: ", "");
		String[] split = netUIDLine.split(" ");
		if (split.length < 1) {
			ownerID = -1;
		} else {
			if (split[0].startsWith("f")) {
				spectator = true;
			} else {
				try {
					ownerID = Integer.parseInt(split[0]);
				} catch (NumberFormatException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	public void parseGameSummoners() {
		String[][] newReplacements = { { "Spawning champion ", "" }, { " with skinID ", "|" }, { " on team ", "|" }, { " for clientID ", "|" }, { " and summonername ", "|" }, { "(is HUMAN PLAYER)", "" }, { "(is BOT AI)", "" }, { "(", "" }, { ")", "" } };
		String[][] oldReplacements = { { "Hero ", "" }, { "(", "|" }, { ") created for ", "|" } };
		for (int i = 0; i < summonerResults.size(); i++) {
			String result = summonerResults.get(i);
			if (oldSummonerFormat) {
				for (int a = 0; a < oldReplacements.length; a++) {
					result = result.replace(oldReplacements[a][0], oldReplacements[a][1]);
				}
			} else {
				for (int a = 0; a < newReplacements.length; a++) {
					result = result.replace(newReplacements[a][0], newReplacements[a][1]);
				}
			}
			String[] split = result.split("\\|");
			String team = "?";
			int skinID = -1;
			int clientID = -1;
			String entityType = "?";
			String championName = "?";
			String summonerName = "?";
			if (oldSummonerFormat) {
				skinID = Integer.parseInt(split[1]);
				championName = split[0];
				summonerName = split[2];
			} else {
				team = split[2].equals("100") ? "blue" : "purple";
				skinID = Integer.parseInt(split[1]);
				clientID = Integer.parseInt(split[3]);
				championName = split[0];
				summonerName = split[4].trim();
				entityType = clientID == -1 ? "botAI" : "human";
			}
			GameSummoner summoner = new GameSummoner(team, skinID, clientID, entityType, summonerName, championName);
			if (clientID == ownerID) {
				gameOwner = summoner;
			}
			if (entityType.equals("botAI")) {
				containsBotAI = true;
			}
			gameSummoners.add(summoner);
		}
	}

	public void reset() {
		mapID = -1;
		ownerID = -1;
		endTime = 0L;
		startTime = 0L;
		spectator = false;
		gameID = "?";
		region = "?";
		gameResult = "?";
		gameMutator = "?";
		gameDate = "?";
		gameOwner = null;
		killers.clear();
		gameSummoners.clear();

		mapLine = null;
		netUIDLine = null;
		logStartLine = null;
		gameInformationLine = null;
		exitCodeLine = null;
		gameMutatorLine = null;
		oldSummonerFormat = false;
		containsBotAI = false;
		deathResults.clear();
		summonerResults.clear();
	}

	public ArrayList<Game> getGames() {
		return games;
	}

}