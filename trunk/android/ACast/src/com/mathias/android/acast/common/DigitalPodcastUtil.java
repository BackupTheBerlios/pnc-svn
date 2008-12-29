package com.mathias.android.acast.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public abstract class DigitalPodcastUtil {

	private static final String TAG = DigitalPodcastUtil.class.getSimpleName();

	private static final String URI = "http://www.digitalpodcast.com/search.php?opt=0&keyword=";

	private DigitalPodcastUtil() {
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
			String str = sb.toString();
			int start = 0;
			int end = 0;
			while (true) {
				// title
				start = Util.indexAfter(str, end, "<!-- begin title -->", "width=", ">", "\n", "       ");
				end = Util.indexBefore(str, start, "&nbsp;");
				if(start == -1 || end == -1){
					break;
				}
				String title = str.substring(start, end);

				// description
				start = Util.indexAfter(str, end, "<!-- begin row box description -->", "\n", "         ");
				end = Util.indexBefore(str, start, "</td>");
				if(start == -1 || end == -1){
					break;
				}
				String description = str.substring(start, end);

				// url
				start = Util.indexAfter(str, end, "<!-- begin podcast button -->", "href=\"");
				end = Util.indexBefore(str, start, "\"");
				if(start == -1 || end == -1){
					break;
				}
				String rssuri = str.substring(start, end);

				items.add(new SearchItem(title, description, rssuri));
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
