package com.mathias.clocks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class Configuration {
	
	private static final List<String> TIMEZONES = Arrays.asList(TimeZone.getAvailableIDs());

	private static Configuration theOne = new Configuration();
	
	private Properties prop;
	
	private Configuration() {
		prop = new Properties();
		String propfile = "/clocks.properties";
		URL propurl = Configuration.class.getResource(propfile);
		try {
			if(propurl == null){
				throw new FileNotFoundException("Could not get resource: "+propfile);
			}
			prop.load(new FileInputStream(propurl.getFile()));
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: "+e.getMessage());
		}
	}
	
	public static String get(String key){
		return theOne.prop.getProperty(key);
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
					if(!TIMEZONES.contains(timeZone)){
						System.out.println("Could not find time zone: "+timeZone+" for "+name);
					}else{
						clocks.add(new Clock(name, TimeZone.getTimeZone(timeZone)));
					}
				}
			}
		}
		return clocks;
	}

}
