package com.mathias.android.owanotify;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.mathias.android.owanotify.OwaParser.OwaCalendarItem;
import com.mathias.android.owanotify.OwaParser.OwaInboxItem;
import com.mathias.android.owanotify.common.MSharedPreferences;
import com.mathias.android.owanotify.common.Util;

public class OwaUtil {

	private static final String TAG = OwaUtil.class.getSimpleName();
	
	public static String getFullUrl(MSharedPreferences prefs, String path){
		String url = prefs.getString(R.string.url_key, "");
		if(!url.endsWith("/")){
			url+="/";
		}
		String name = prefs.getString(R.string.name_key);
		if(!path.startsWith("/")){
			path = "/" + path;
		}
		return url+name+path;
	}

	public static String getFullInboxUrl(MSharedPreferences prefs){
		String url = prefs.getString(R.string.url_key, "");
		if(!url.endsWith("/")){
			url+="/";
		}
		String name = prefs.getString(R.string.name_key);
		String inbox = prefs.getString(R.string.inbox_key, "/Inbox/?Cmd=contents");
		return url+name+inbox;
	}

	public static String getFullCalendarUrl(MSharedPreferences prefs, Date date){
		String url = prefs.getString(R.string.url_key, "");
		if(!url.endsWith("/")){
			url+="/";
		}
		String name = prefs.getString(R.string.name_key);
		String calendar = prefs.getString(R.string.calendar_key, "/Calendar/?Cmd=contents&View=Daily");
		return url+name+calendar+"&m="+(1+date.getMonth())+"&d="+(1+date.getDay())+"&y="+(1900+date.getYear());
	}

	public static String fetchContent(MSharedPreferences prefs, String url){
		String text = null;
		String itemurl = OwaUtil.getFullUrl(prefs, url);
		String username = prefs.getString(R.string.username_key);
		String password = prefs.getString(R.string.password_key);
		if(itemurl != null && username != null && password != null){
			try {
				String str = Util.downloadFile(0, itemurl, null, username, password);
				text = OwaParser.parseEmail(str);
				if(text == null){
					text = "No content found";
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return text;
	}

	public static List<OwaInboxItem> fetchInboxNew(MSharedPreferences prefs){
		try {
			String username = prefs.getString(R.string.username_key);
			String password = prefs.getString(R.string.password_key);
			String inboxurl = getFullInboxUrl(prefs);
	        String sadd = prefs.getString(R.string.timezoneadj_key, "0");
	        int timezoneadj = Integer.parseInt(sadd);
			if(username != null && password != null && inboxurl != null){
				String inb = Util.downloadFile(0, inboxurl, null, username, password);
				return OwaParser.parseInbox(inb, true, timezoneadj);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}

	public static List<OwaCalendarItem> fetchCalendar(MSharedPreferences prefs){
		try {
			String username = prefs.getString(R.string.username_key);
			String password = prefs.getString(R.string.password_key);
			String calendarurl = getFullCalendarUrl(prefs, new Date());
	        String sadd = prefs.getString(R.string.timezoneadj_key, "0");
	        int timezoneadj = Integer.parseInt(sadd);
			if(username != null && password != null && calendarurl != null){
				String cal = Util.downloadFile(0, calendarurl, null, username, password);
				return OwaParser.parseCalendar(cal, timezoneadj);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}
	
	private final static Pattern date = Pattern.compile("\\D*(\\d{4})\\D(\\d{2})\\D(\\d{2}) (\\d{1,2})\\D(\\d{2})");

	public static Date parseDate(String in, int houradd){
		Matcher m = date.matcher(in);
		if(m.matches()){
			int year = Integer.parseInt(m.group(1));
			int month = Integer.parseInt(m.group(2));
			int date = Integer.parseInt(m.group(3));
			int hourOfDay = Integer.parseInt(m.group(4));
			int minute = Integer.parseInt(m.group(5));
			GregorianCalendar gc = new GregorianCalendar(year, month, date, hourOfDay, minute);
			gc.add(Calendar.HOUR_OF_DAY, houradd);
			return gc.getTime();
		}
		return null;
	}

	private final static Pattern time = Pattern.compile("(\\d{2})\\D(\\d{2})\\D(\\d{2})\\D(\\d{2})");

	public static int[] parseTime(String in, int houradd){
		Matcher m = time.matcher(in);
		if(m.matches()){
			int h1 = Integer.parseInt(m.group(1))+houradd;
			if(h1 < 0){
				h1+=12;
			}else if(h1 > 24){
				h1-=12;
			}
			int m1 = Integer.parseInt(m.group(2));
			int h2 = Integer.parseInt(m.group(3))+houradd;
			if(h2 < 0){
				h2+=12;
			}else if(h2 > 24){
				h2-=12;
			}
			int m2 = Integer.parseInt(m.group(4));
			return new int[]{h1*60+m1, h2*60+m2};
		}
		return null;
	}
	
	public static String buildTime(int startmin, int stopmin){
		int h1 = startmin/60;
		int m1 = startmin%60;
		int h2 = stopmin/60;
		int m2 = stopmin%60;
		return String.format("%d:%02d-%d:%02d", h1, m1, h2, m2);
	}

}
