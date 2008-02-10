package com.mathias.drawutils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Util {

	/**
	 * log all levels with the console handler for the logger
	 * @param name
	 */
	public static void addConsoleHandler(String name){
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		Logger logger = Logger.getLogger(name);
		logger.addHandler(ch);
		logger.setUseParentHandlers(false);
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

}
