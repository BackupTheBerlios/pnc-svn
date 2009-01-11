package com.mathias.android.owanotify;

import java.util.ArrayList;
import java.util.List;

public abstract class OwaParser {

	private static final String TAG = OwaParser.class.getSimpleName();

	private OwaParser() {
	}

	public static List<OwaInboxItem> parseInboxNew(String str) {

		List<OwaInboxItem> items = new ArrayList<OwaInboxItem>();

		int start = 0;
		int end = 0;
		while (true) {
			// from
			start = Util.indexAfter(str, end, "icon-msg-unread.gif", "<A", "<A", "<A", "<FONT size=\"2\" color=black><b>");
			end = Util.indexBefore(str, start, "</b>");
			if(start == -1 || end == -1){
				break;
			}
			String from = str.substring(start, end);

			// subject
			start = Util.indexAfter(str, end, "<FONT size=\"2\" color=black><b>");
			end = Util.indexBefore(str, start, "</b>");
			if(start == -1 || end == -1){
				break;
			}
			String subject = str.substring(start, end);

			// date
			start = Util.indexAfter(str, end, "<FONT size=\"2\" color=black><b>");
			end = Util.indexBefore(str, start, "</b>");
			if(start == -1 || end == -1){
				break;
			}
			String date = str.substring(start, end);

			items.add(new OwaInboxItem(from, subject, date));
		}
		return items;
	}

	public static class OwaInboxItem {
		public boolean unread;
		public String from;
		public String subject;
		public String date;

		public OwaInboxItem(String from, String subject, String date) {
			this.from = from;
			this.subject = subject;
			this.date = date;
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
