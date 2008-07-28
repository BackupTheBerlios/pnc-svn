package com.mathias.clocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.mathias.drawutils.AbstractConfiguration;

public class Configuration {

	public static final List<String> TIMEZONES = Arrays.asList(TimeZone.getAvailableIDs());

	public static List<Clock> getClocks(){
		List<Clock> clocks = new ArrayList<Clock>();
		for (int i = 0; i < 10; i++) {
			String c = AbstractConfiguration.get("clock"+(i+1));
			if(c != null){
				StringTokenizer st = new StringTokenizer(c, ",");
				if(st.countTokens() != 2){
					System.out.println("Wrong amount of tokens!");
				}else{
					String name = st.nextToken().trim();
					String timeZone = st.nextToken().trim();
					boolean found = false;
					for (String tz : TIMEZONES) {
						if(tz.toUpperCase().equals(timeZone.toUpperCase())){
							clocks.add(new Clock((i+1), name, TimeZone.getTimeZone(tz)));
							found = true;
							break;
						}
					}
					if(!found){
						for (String tz : TIMEZONES) {
							if(tz.toUpperCase().indexOf(timeZone.toUpperCase()) != -1){
								clocks.add(new Clock((i+1), name, TimeZone.getTimeZone(tz)));
								break;
							}
						}
						System.out.println("Could not find time zone: "+timeZone+" for "+name);
						clocks.add(new Clock((i+1), name, null));
					}
				}
			}
		}
		return clocks;
	}

}
