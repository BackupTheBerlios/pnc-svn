package com.mathias.clocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;

public class GenericLogManager {

	public static void init() {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream(new File("log2.properties")));
			LogManager.getLogManager().getLogger(GenericLogManager.class.getName()).info("lakjslkjaskdjksaj");
		} catch(FileNotFoundException e){
			try {
				LogManager.getLogManager().readConfiguration(GenericLogManager.class.getClass().getResourceAsStream("/log2.properties"));
				LogManager.getLogManager().getLogger(GenericLogManager.class.getName()).info("lakjslkjaskdjksaj");
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
