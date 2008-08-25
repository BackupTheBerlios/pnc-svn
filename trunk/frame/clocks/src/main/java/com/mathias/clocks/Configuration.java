package com.mathias.clocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.mathias.drawutils.AbstractConfiguration;

public class Configuration extends AbstractConfiguration {

	/*
	##clock1-clock10, Name, TimeZone
	clock1=Stockholm, CET
	clock2=Atlanta, US/Eastern
	clock3=Dehli, IST
	#clock4=Yahoo, YahooTimeZone
	##none, right, left, top, bottom
	location=left
	#autohide=false
	#overlap=5
	#hidden=false
	#ontop=true
	#seconds=true
	#font=Arial
	#fontsize
	#systray=false
	 */

	public static final List<String> TIMEZONES = Arrays.asList(TimeZone.getAvailableIDs());
	
	public Configuration(String filename) {
		super(filename);
	}

	public List<Clock> getClocks(){
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

	public boolean getAlwaysOnTop(){
		return getBoolean("ontop", true);
	}
	
	public void setAlwaysOnTop(boolean ontop){
		set("ontop", ""+ontop);
	}

	public int getFontSize() {
		return getInt("fontsize", 20);
	}
	
	public void setFontSize(Object size){
		set("fontsize", ""+size);
	}

	public String getFont(){
		return get("font", "Arial");
	}
	
	public void setFont(Object font){
		set("font", ""+font);
	}
	
	public boolean getSystray(){
		return getBoolean("systray", true);
	}
	
	public void setSystray(boolean systray){
		set("systray", ""+systray);
	}

	public String getLocation(){
		return get("location", "left");
	}
	
	public void setLocation(Object location){
		set("location", ""+location);
	}

	public boolean getSeconds(){
		return getBoolean("seconds", false);
	}
	
	public void setSeconds(Object seconds){
		set("seconds", ""+seconds);
	}
	
	public boolean getAutohide(){
		return getBoolean("autohide", true);
	}
	
	public void setAutohide(boolean autohide){
		set("autohide", ""+autohide);
	}

	public boolean getHidden(){
		return getBoolean("hidden", false);
	}
	
	public void setHidden(boolean hidden){
		set("hidden", ""+hidden);
	}

	public int getOverlap(){
		return getInt("overlap", 1);
	}
	
	public void setOverlap(Object overlap){
		set("overlap", ""+overlap);
	}
}
