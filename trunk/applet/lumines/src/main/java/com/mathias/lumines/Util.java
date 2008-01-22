package com.mathias.lumines;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Util {
	
	private Util(){
	}

	/**
	 * log all levels with the console handler for the logger
	 * @param name name of logger to add new console handler
	 */
	public static void addConsoleHandler(String name){
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		ch.setFormatter(new SimpleFormatter());
		Logger logger = Logger.getLogger(name);
		logger.addHandler(ch);
		logger.setUseParentHandlers(false);
	}

	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
