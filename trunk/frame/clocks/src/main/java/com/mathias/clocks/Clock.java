package com.mathias.clocks;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Clock {
	private int index;
	private String name;
	private GregorianCalendar gc;
	private TimeZone timeZone;

	public Clock(int index, String name, TimeZone timeZone) {
		this.index = index;
		this.name = name;
		this.timeZone = timeZone;
		gc = new GregorianCalendar();
		gc.setTimeZone(timeZone);
	}

	public String getTime(boolean seconds){
		if(timeZone == null){
			return "N/A";
		}
		gc.setTime(new Date());
		int h = gc.get(Calendar.HOUR_OF_DAY);
		int m = gc.get(Calendar.MINUTE);
		if(seconds){
			int s = gc.get(Calendar.SECOND);
			return String.format("%02d:%02d:%02d", h, m, s);
		}else{
			return String.format("%02d:%02d", h, m);
		}
	}

	public String getName(){
		return name;
	}
	
	public TimeZone getTimeZone(){
		return timeZone;
	}

	public int getIndex(){
		return index;
	}

}
