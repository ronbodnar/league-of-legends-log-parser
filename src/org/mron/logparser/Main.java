/*
 *
 */
package org.mron.logparser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @author Ron
 *
 */
public class Main {

	private boolean debug;
	private boolean missionControl;

	private ParserAPI parserAPI;

	private StatisticAPI statisticAPI;

	public static void main(String[] arguments) {
		new Main(arguments);
	}

	public Main(String[] arguments) {
		if (arguments.length > 0) {
			for (String argument : arguments) {
				if (argument.equals("--debug")) {
					debug = true;
				}
				if (argument.equals("--mission-control")) {
					missionControl = true;
				}
			}
		}
		if (missionControl) {
			Scanner s = new Scanner(System.in);
			String ss = s.nextLine();
			if (ss.equals("go")) {
				doIt();
			}
			s.close();
		} else {
			doIt();
		}
	}

	public void doIt() {
		parserAPI = new ParserAPI();
		statisticAPI = new StatisticAPI();
		long start = System.currentTimeMillis();
		int gameCount = 0;
		String[] gameData = null;
		String fileData = getFileData();
		if (fileData != null) {
			String[] split = fileData.split("~");
			gameData = new String[split.length];
			for (int i = 0; i < split.length; i++) {
				split[i] = split[i].trim();
				gameData[i] = split[i];
			}
			gameCount = gameData.length;
			System.out.println(gameCount + " games detected");
			System.out.println("-----------------------");
		}
		//getResponse(gameData);
		System.out.println(getResponse(gameData));
		System.out.println();
		System.out.println("Log file parsing for " + statisticAPI.getGames().size() + " valid games (" + (gameCount - statisticAPI.getGames().size()) + " invalid) completed in " + (((System.currentTimeMillis() - start) * 1000.0) / 1000000) + " seconds");
	}

	public String getFileData() {
		BufferedReader reader = null;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			reader = new BufferedReader(new FileReader((debug ? "../" : "") + "logs/kori.txt"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			return stringBuilder.toString().trim();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}
		return null;
	}

	public String getResponse(String[] gameData) {
		parserAPI.parseGameResults(gameData);
		statisticAPI.processGames(parserAPI.getGames());

		return statisticAPI.getResponse();
	}

}