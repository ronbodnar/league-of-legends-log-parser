/*
 *
 */
package org.mron.logparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.mron.logparser.impl.Champion;
import org.mron.logparser.impl.Game;
import org.mron.logparser.impl.GameSummoner;
import org.mron.logparser.impl.Summoner;

import com.google.gson.Gson;

/**
 * 
 * @author Ron
 *
 */
public class StatisticAPI {

	private int winCount;
	private int lossCount;
	private int abandonCount;
	private int unknownResultCount;
	private int blueWinCount;
	private int blueLossCount;
	private int purpleWinCount;
	private int purpleLossCount;
	private int executionCount;
	private int playCount;
	private int deathCount;
	private int skinCount;

	private int winStreakCount;
	private int lossStreakCount;
	private int longestWinStreak;
	private int longestLossStreak;

	private long totalGameTime;
	private long longestGameTime;
	private long shortestGameTime = Long.MAX_VALUE;

	private String firstGameDate;

	private Map<String, Game> games = new HashMap<String, Game>();
	private Map<String, Integer> deaths = new HashMap<String, Integer>();
	private Map<String, Integer> regions = new HashMap<String, Integer>(); // used to automatically detect dominant region for accurate match history links
	private Map<String, Summoner> summoners = new HashMap<String, Summoner>();

	public void processGames(List<Game> gameData) {
		for (int i = 0; i < gameData.size(); i++) {
			Game game = gameData.get(i);
			if (game == null) {
				continue;
			}
			String id = game.getID();
			if (id.equals("?")) {
				continue;
			}
			if (game.getEndTime() == 0) {
				game.setResult("A");
			}
			Game masterGame = games.get(id);
			if (masterGame == null) {
				games.put(game.getID(), game);
			} else {
				long currentLogStart = game.getStartTime();
				long originalLogStart = masterGame.getStartTime();

				long originalLogEnd = masterGame.getEndTime();
				long currentLogEnd = game.getEndTime();
				
				boolean reversed = originalLogStart > currentLogStart;
				double difference = (reversed ? (originalLogStart - currentLogStart) : (currentLogStart - originalLogStart));
				double gameEnd = difference + (reversed ? originalLogEnd : currentLogEnd);
				
				masterGame.setEndTime(Math.round(gameEnd));
				masterGame.setResult(game.getResult());
				masterGame.addKillers(game.getKillers());
			}
		}
		for (Map.Entry<String, Game> entry : games.entrySet()) {
			Game game = entry.getValue();
			
			if (game == null) {
				continue;
			}
			if (!game.getRegion().equals("?")) {
				boolean exists = regions.containsKey(game.getRegion());
				if (exists) {
					regions.put(game.getRegion(), regions.get(game.getRegion()) + 1);
				} else {
					regions.put(game.getRegion(), 1);
				}
			}
			playCount++;
			processDateAndTime(game);
			processSummoners(game);
			processResults(game);
			processDeaths(game);
		}
	}

	public void processResults(Game game) {
		GameSummoner owner = getGameOwner(game.getID());
		if (owner == null) {
			return;
		}
		switch (game.getResult()) {
			case "W":
				winCount++;
				/*
				 * Win streak
				 */
				winStreakCount++;
				if (lossStreakCount > longestLossStreak) {
					longestLossStreak = lossStreakCount;
				}
				lossStreakCount = 0;
				/*
				 * Owner stats
				 */
				if (owner.getTeam().equals("blue")) {
					blueWinCount++;
				} else if (owner.getTeam().equals("purple")) {
					purpleWinCount++;
				}
				break;

			case "L":
				lossCount++;
				/*
				 * Loss streak
				 */
				lossStreakCount++;
				if (winStreakCount > longestWinStreak) {
					longestWinStreak = winStreakCount;
				}
				winStreakCount = 0;
				/*
				 * Owner stats
				 */
				if (owner.getTeam().equals("blue")) {
					blueLossCount++;
				} else if (owner.getTeam().equals("purple")) {
					purpleLossCount++;
				}
				break;

			case "A":
				abandonCount++;
				break;

			case "?":
				unknownResultCount++;
				break;
		}
	}

	public void processSummoners(Game game) {
		GameSummoner owner = getGameOwner(game.getID());
		for (int i = 0; i < game.getSummoners().length; i++) {
			GameSummoner gameSummoner = game.getSummoners()[i];
			Summoner summoner = summoners.get(gameSummoner.getSummonerName());
			if (summoner == null) {
				summoner = new Summoner(gameSummoner.getSummonerName(), gameSummoner.getEntityType().equals("botAI"));
				summoners.put(summoner.getName(), summoner);
			}
			Champion champion = summoner.getChampionForName(gameSummoner.getChampionName());
			if (champion == null) {
				champion = new Champion(gameSummoner.getChampionName());
			}
			boolean isTeammate = true;
			if (owner != null) {
				isTeammate = gameSummoner.getTeam().equals(owner.getTeam());
			}
			champion.increaseTimePlaying(game.getLength());
			champion.increasePlayCount(1);
			switch (game.getResult()) {
				case "W":
					if (isTeammate) {
						summoner.increaseWins();
						champion.increaseWins(1);
					} else {
						summoner.increaseLosses();
						champion.increaseLosses(1);
					}
					break;

				case "L":
					if (isTeammate) {
						summoner.increaseLosses();
						champion.increaseLosses(1);
					} else {
						summoner.increaseWins();
						champion.increaseWins(1);
					}
					break;

				case "A":
					if (owner != null) {
						if (!owner.getSummonerName().equals(summoner.getName())) {
							summoner.increaseUnknownResults();
						} else {
							summoner.increaseAbandons();
						}
					} else {
						summoner.increaseUnknownResults();
					}
					champion.increaseAbandons(1);
					break;

				case "?":
					summoner.increaseUnknownResults();
					champion.increaseUnknownResults(1);
					break;
			}
			if (owner != null && gameSummoner.getSummonerName().equals(owner.getSummonerName())) {
				champion.increaseDeathCount(game.getKillers().length);
				if (owner.getSkinID() > 0) {
					skinCount++;
				}
			}
			champion.increaseSkinUse(gameSummoner.getSkinID(), 1);
			summoner.increaseAppearances();
			summoner.addChampion(champion, gameSummoner.getSkinID());
		}
	}

	public void processDeaths(Game game) {
		for (int i = 0; i < game.getKillers().length; i++) {
			String killer = game.getKillers()[i];
			if (killer == null || killer.length() <= 0) {
				continue;
			}
			if (killer.equals("Tower") || killer.equals("Minion")) {
				executionCount++;
			} else {
				String championName = null;
				for (int a = 0; a < game.getSummoners().length; a++) {
					if (game.getSummoners()[a] == null) {
						continue;
					}
					if (game.getSummoners()[a].getSummonerName().equals(killer)) {
						championName = game.getSummoners()[a].getChampionName();
						break;
					}
				}
				if (championName != null) {
					boolean exists = deaths.containsKey(championName);
					deaths.put(championName, exists ? (deaths.get(championName) + 1) : 1);
				}
			}
			deathCount++;
		}
	}

	public void processDateAndTime(Game game) {
		totalGameTime += game.getLength();
		if (firstGameDate == null) {
			firstGameDate = game.getDate();
		} else {
			if (game.getDateTime().isBefore(DateTime.parse(firstGameDate, DateTimeFormat.forPattern("MM/d/yyyy H:mm")))) {
				firstGameDate = game.getDate();
			}
		}
		if ((game.getLength() < shortestGameTime) && game.getLength() > 0) {
			shortestGameTime = game.getLength();
		}
		if (game.getLength() > longestGameTime) {
			longestGameTime = game.getLength();
		}
	}

	public String getDominantRegion() {
		if (regions.size() == 1) {
			return regions.keySet().toArray(new String[regions.size()])[0];
		}
		int regionAmount = 0;
		String dominantRegion = "?";
		for (Map.Entry<String, Integer> entry : regions.entrySet()) {
			if (entry.getValue() > regionAmount) {
				regionAmount = entry.getValue();
				dominantRegion = entry.getKey();
			}
		}
		return dominantRegion;
	}

	public String getResponse() {
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		responseMap.put("wins", getWinCount());
		responseMap.put("losses", getLossCount());
		responseMap.put("abandons", getAbandonCount());
		responseMap.put("unknownResults", getUnknownResultCount());
		responseMap.put("blueWins", getBlueWinCount());
		responseMap.put("blueLosses", getBlueLossCount());
		responseMap.put("purpleWins", getPurpleWinCount());
		responseMap.put("purpleLosses", getPurpleLossCount());
		responseMap.put("executions", getExecutionCount());
		responseMap.put("abandons", getAbandonCount());
		responseMap.put("playCount", getPlayCount());
		responseMap.put("deathCount", getDeathCount());
		responseMap.put("skinCount", getSkinCount());
		responseMap.put("dominantRegion", getDominantRegion());
		responseMap.put("totalGameTime", getTotalGameTime());
		responseMap.put("longestGameTime", getLongestGameTime());
		responseMap.put("shortestGameTime", getShortestGameTime());
		responseMap.put("firstGameDate", getFirstGameDate());
		responseMap.put("longestWinStreak", getLongestWinStreak());
		responseMap.put("longestLossStreak", getLongestLossStreak());
		responseMap.put("mostKilledBy", getMostKilledBy());
		responseMap.put("games", getFilteredGames());
		Map<String, List<Champion>> champs = new HashMap<String, List<Champion>>();
		List<Summoner> filteredSummoners = getFilteredSummoners();
		responseMap.put("summoners", filteredSummoners);
		for (int i = 0; i < filteredSummoners.size(); i++) {
			Summoner summoner = filteredSummoners.get(i);
			if (summoner == null || summoner.getName() == null) {
				continue;
			}
			champs.put(summoner.getName(), summoner.getChampions());
		}
		responseMap.put("champions", champs);
		
		return new Gson().toJson(responseMap);
	}

	public Map<String, Integer> getMostKilledBy() {
		Map.Entry<String, Integer> highestEntry = null;
		for (Map.Entry<String, Integer> entry : deaths.entrySet()) {
			if (highestEntry == null || entry.getValue().compareTo(highestEntry.getValue()) > 0) {
				highestEntry = entry;
			}
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(highestEntry.getKey(), highestEntry.getValue());

		return map;
	}

	public List<Game> getFilteredGames() {
		List<Game> filteredGames = new ArrayList<Game>();
		for (Map.Entry<String, Game> entry : games.entrySet()) {
			Game game = entry.getValue();
			if (game == null || game.getLength() <= 0 || game.getResult().equals("?")) {
				continue;
			}
			filteredGames.add(game);
		}
		return filteredGames;
	}

	public List<Summoner> getFilteredSummoners() {
		final int MAX_THRESHOLD = 100;
		int appearanceThreshold = (int) (playCount * .05);
		if (appearanceThreshold > MAX_THRESHOLD) {
			appearanceThreshold = MAX_THRESHOLD;
		}
		List<Summoner> filteredSummoners = new ArrayList<Summoner>();
		for (Map.Entry<String, Summoner> entry : summoners.entrySet()) {
			Summoner summoner = entry.getValue();
			if (summoner == null || summoner.getAppearances() <= appearanceThreshold || summoner.isBotAI()) {
				continue;
			}
			filteredSummoners.add(summoner);
		}
		return filteredSummoners;
	}

	public GameSummoner getGameOwner(String id) {
		for (Map.Entry<String, Game> entry : games.entrySet()) {
			Game game = entry.getValue();
			if (game == null) {
				continue;
			}
			if (game.getID().equals(id)) {
				return game.getOwner();
			}
		}
		return null;
	}

	public int getWinCount() {
		return winCount;
	}

	public int getLossCount() {
		return lossCount;
	}

	public int getBlueWinCount() {
		return blueWinCount;
	}

	public int getBlueLossCount() {
		return blueLossCount;
	}

	public int getPurpleWinCount() {
		return purpleWinCount;
	}

	public int getPurpleLossCount() {
		return purpleLossCount;
	}

	public int getExecutionCount() {
		return executionCount;
	}

	public int getAbandonCount() {
		return abandonCount;
	}

	public int getUnknownResultCount() {
		return unknownResultCount;
	}

	public int getPlayCount() {
		return playCount;
	}

	public int getDeathCount() {
		return deathCount;
	}

	public int getSkinCount() {
		return skinCount;
	}

	public long getTotalGameTime() {
		return totalGameTime;
	}

	public String getFirstGameDate() {
		return firstGameDate;
	}

	public long getLongestGameTime() {
		return longestGameTime;
	}

	public long getShortestGameTime() {
		return shortestGameTime;
	}

	public int getLongestWinStreak() {
		return longestWinStreak;
	}

	public int getLongestLossStreak() {
		return longestLossStreak;
	}

	public Map<String, Game> getGames() {
		return games;
	}

	public Map<String, Summoner> getSummoners() {
		return summoners;
	}

}