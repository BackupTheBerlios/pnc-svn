package com.mathias.android.owanotify;

import android.util.Log;

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

	public static String getFullCalendarUrl(MSharedPreferences prefs){
		String url = prefs.getString(R.string.url_key, "");
		if(!url.endsWith("/")){
			url+="/";
		}
		String name = prefs.getString(R.string.name_key);
		String calendar = prefs.getString(R.string.calendar_key, "/Calendar/?Cmd=contents&View=Daily");
		return url+name+calendar;
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

}
