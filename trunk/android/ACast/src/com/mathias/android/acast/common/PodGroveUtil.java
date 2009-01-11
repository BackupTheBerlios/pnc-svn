package com.mathias.android.acast.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public abstract class PodGroveUtil {

	private static final String TAG = PodGroveUtil.class.getSimpleName();

	private static final String KEY = "\"/main/subscribe?newFeed[rss]=";

	private static final String URI = "http://podgrove.com/search/get_results?commit=Search&search_term=";

	private PodGroveUtil() {
	}

	public static List<SearchItem> parse(String searchstr) {
		Log.d(TAG, "Searching for: "+searchstr);

		List<SearchItem> items = new ArrayList<SearchItem>();

		String uri = URI+searchstr;
		InputStream inp = null;
		try {
			URLConnection conn = new URL(uri).openConnection();
			inp = conn.getInputStream();
			int c;
			byte[] buffer = new byte[8192];
			StringBuilder sb = new StringBuilder();
			while ((c = inp.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, c));
			}
			int i = 0;
			while ((i = sb.indexOf(KEY, i)) != -1) {
				// find rss uri
				String rssuri = null;
				int end = sb.indexOf("\"", i+KEY.length());
				if (end != -1) {
					rssuri = sb.substring(i + KEY.length(), end);
					i += KEY.length()+rssuri.length();
				}
				
				//find title, next href
				String rsstitle = null;
				int j = sb.indexOf("href", i);
				if(j != -1){
					int start = sb.indexOf(">", j);
					if(start != -1){
						end = sb.indexOf("<", start);
						rsstitle = sb.substring(start+1, end);
					}
				}
				items.add(new SearchItem(rsstitle, "", rssuri));
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			try {
				if (inp != null) {
					inp.close();
				}
			} catch (IOException ioe) {
			}
		}
		return items;
	}

}