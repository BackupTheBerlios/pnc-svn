package com.mathias.clocks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class Configuration {
	
	public static final List<String> TIMEZONES = Arrays.asList(TimeZone.getAvailableIDs());

	private static Configuration theOne = new Configuration();
	
	private Properties prop;
	
	private Configuration() {
		prop = new Properties();
		if(!loadPropertiesFile("clocks.properties")){
			if(!loadPropertiesResource("/clocks.properties")){
				//new ExitAction().actionPerformed(null);
			}
		}
	}
	
	private boolean loadPropertiesResource(String propfile){
		URL propurl = Configuration.class.getResource(propfile);
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
	
	public static String get(String key){
		return theOne.prop.getProperty(key);
	}

	public static Object set(String key, String value){
		return theOne.prop.setProperty(key, value);
	}

	public static String get(String key, String def){
		return theOne.prop.getProperty(key, def);
	}

	public static boolean getBoolean(String key, boolean def){
		return Boolean.parseBoolean(theOne.prop.getProperty(key, ""+def));
	}

	public static int getInt(String key, int def){
		return Integer.parseInt(theOne.prop.getProperty(key, ""+def));
	}

	public static List<Clock> getClocks(){
		List<Clock> clocks = new ArrayList<Clock>();
		for (int i = 0; i < 10; i++) {
			String c = get("clock"+(i+1));
			if(c != null){
				StringTokenizer st = new StringTokenizer(c, ",");
				if(st.countTokens() != 2){
					System.out.println("Wrong amount of tokens!");
				}else{
					String name = st.nextToken().trim();
					String timeZone = st.nextToken().trim();
					boolean found = false;
					for (String tz : TIMEZONES) {
						//System.out.println(tz);
						if(tz.toUpperCase().indexOf(timeZone.toUpperCase()) != -1){
							clocks.add(new Clock(name, TimeZone.getTimeZone(tz)));
							found = true;
							break;
						}
					}
					if(!found){
						System.out.println("Could not find time zone: "+timeZone+" for "+name);
						clocks.add(new Clock(name, null));
					}
				}
			}
		}
		return clocks;
	}

	public static void store(){
		if(!theOne.storePropertiesFile("clocks.properties")){
			if(!theOne.storePropertiesResource("/clocks.properties")){
			}
		}
	}

	private boolean storePropertiesResource(String propfile){
		URL propurl = Configuration.class.getResource(propfile);
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
			theOne.prop.store(new FileOutputStream(file), null);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
