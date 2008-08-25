package com.mathias.drawutils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class AbstractConfiguration {

	private Properties prop;
	
	private String filename;
	
	public AbstractConfiguration(String filename) {
		this.filename = filename;
		prop = new Properties();
		if(!loadPropertiesFile(filename)){
			if(!loadPropertiesResource("/"+filename)){
				//TODO exit?
			}
		}
	}

	private boolean loadPropertiesResource(String propfile){
		URL propurl = AbstractConfiguration.class.getResource(propfile);
		try {
			if(propurl == null){
				throw new FileNotFoundException("Could not get resource: "+propfile);
			}
			prop.load(new FileInputStream(propurl.getFile()));
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: "+e.getMessage());
		}
		return false;
	}
	
	private boolean loadPropertiesFile(String propfile){
		try {
			prop.load(new FileInputStream(propfile));
			return true;
		} catch (FileNotFoundException e) {
			//System.out.println("FileNotFoundException: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: "+e.getMessage());
		}
		return false;
	}
	
	public String get(String key){
		return prop.getProperty(key);
	}

	public Object set(String key, String value){
		return prop.setProperty(key, value);
	}

	public String get(String key, String def){
		return prop.getProperty(key, def);
	}

	public boolean getBoolean(String key, boolean def){
		return Boolean.parseBoolean(prop.getProperty(key, ""+def));
	}

	public int getInt(String key, int def){
		return Integer.parseInt(prop.getProperty(key, ""+def));
	}

	public Object remove(String key){
		return prop.remove(key);
	}

	public void store(){
		if(!storePropertiesFile(filename)){
			if(!storePropertiesResource("/"+filename)){
				//TODO?
			}
		}
	}

	private boolean storePropertiesResource(String propfile){
		URL propurl = AbstractConfiguration.class.getResource(propfile);
		try {
			if(propurl == null){
				throw new FileNotFoundException("Could not get resource: "+propfile);
			}
			prop.store(new FileOutputStream(propurl.getFile()), null);
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: "+e.getMessage());
		}
		return false;
	}

	private boolean storePropertiesFile(String file){
		try {
			prop.store(new FileOutputStream(file), null);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
