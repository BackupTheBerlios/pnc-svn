package com.mathias.android.owanotify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.mathias.android.owanotify.common.Util;

public abstract class OwaParser {

	private static final String TAG = OwaParser.class.getSimpleName();

	private OwaParser() {
	}

	public static String parseEmail(String str) {

		String ret = null;

		int start = 0;
		int end = 0;
		while (true) {
			// from
			start = Util.indexAfter(str, end, "class=\"tblMsgBody\"", ">");
			end = Util.indexBefore(str, start, "</TABLE>");
			Log.d(TAG, "end="+end+" start="+start);
			if(start == -1 || end == -1) {
				break;
			}
			ret = str.substring(start, end);
		}
		return ret;
	}

	public static List<OwaInboxItem> parseInbox(String str, boolean onlynew) {

		List<OwaInboxItem> items = new ArrayList<OwaInboxItem>();

		int start = 0;
		int end = 0;
		while (true) {
			start = Util.indexAfter(str, end, "icon-msg-");
			end = Util.indexBefore(str, start, ".gif");
			if(start == -1 || end == -1){
				break;
			}
			boolean read = true;
			String readimage = str.substring(start, end);
			if("unread".equals(readimage)){
				read = false;
			}

			if(!onlynew || !read) {
				
				String readPrefix = "";
				String readSuffix = "";
				if(!read){
					readPrefix = "<b>";
					readSuffix = "</b>";
				}
				
				// from
				start = Util.indexAfter(str, end, "<A", "<A", "<A", "<FONT size=\"2\" color=black>"+readPrefix);
				end = Util.indexBefore(str, start, readSuffix+"</FONT>");
				if(start == -1 || end == -1){
					break;
				}
				String from = str.substring(start, end);

				// url
				start = Util.indexAfter(str, end, "<A href=\"");
				end = Util.indexBefore(str, start, "\">");
				if(start == -1 || end == -1){
					break;
				}
				String url = str.substring(start, end);

				// subject
				start = Util.indexAfter(str, end, "<FONT size=\"2\" color=black>"+readPrefix);
				end = Util.indexBefore(str, start, readSuffix+"</FONT>");
				if(start == -1 || end == -1){
					break;
				}
				String subject = str.substring(start, end);

				// date
				start = Util.indexAfter(str, end, "<FONT size=\"2\" color=black>"+readPrefix);
				end = Util.indexBefore(str, start, readSuffix+"</FONT>");
				if(start == -1 || end == -1){
					break;
				}
				String date = str.substring(start, end);

				items.add(new OwaInboxItem(url, from, subject, date, null, read));
			}
		}
		return items;
	}

	@SuppressWarnings("serial")
	public static class OwaInboxItem implements Serializable {
		public boolean read;
		public String url;
		public String from;
		public String subject;
		public String date;
		public String text;

		public OwaInboxItem(String url, String from, String subject, String date, String text, boolean read) {
			this.read = read;
			this.from = from;
			this.subject = subject;
			this.date = date;
			this.url = url;
			this.text = text;
		}
		
		@Override
		public String toString() {
			return Util.buildString("[from=", from, "][read=", read,
					"][subject=", subject, "][date=", date, "][url=", url, "]");
		}
	}

	public static List<OwaCalendarItem> parseCalendar(String str) {

		List<OwaCalendarItem> items = new ArrayList<OwaCalendarItem>();

		int start = 0;
		int end = 0;

		// date
		start = Util.indexAfter(str, end, "<TABLE class=\"tblHierarchy\"", "<TABLE class=\"calVwTbl\"", "<FONT size=3>");
		end = Util.indexBefore(str, start, "</FONT>");
		String date = null;
		if(start != -1 || end != -1){
			date = str.substring(start, end);
		}
		
		while (true) {
			// time and title
			start = Util.indexAfter(str, end, "<TD TITLE=\"");
			end = Util.indexBefore(str, start, "\" rowspan");
			if(start == -1 || end == -1){
				break;
			}
			String timeTitle = str.substring(start, end);
			
			// title and location
			start = Util.indexAfter(str, end, "style=\"text-decoration:none\">");
			end = Util.indexBefore(str, start, "</A");
			if(start == -1 || end == -1){
				break;
			}
			String titleLocation = str.substring(start, end);

			int i = timeTitle.indexOf(' ');
			if(i != -1){
				String title = timeTitle.substring(i,
						timeTitle.length());
				String time = timeTitle.substring(0, i);
				items.add(new OwaCalendarItem(title, date, time, titleLocation));
			}
		}
		return items;
	}

	public static class OwaCalendarItem {
		public String title;
		public String date;
		public String time;
		public String titleLocation;

		public OwaCalendarItem(String title, String date, String time, String titleLocation) {
			this.title = title;
			this.date = date;
			this.time = time;
			this.titleLocation = titleLocation;
		}
	}

}
