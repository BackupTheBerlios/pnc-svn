package com.mathias.android.owanotify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.mathias.android.owanotify.beans.CalendarItem;
import com.mathias.android.owanotify.beans.MailItem;
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

	public static List<MailItem> parseInbox(String str, int timezoneadj, OwaNotifyDbAdapter dbHelper) {

		List<MailItem> items = new ArrayList<MailItem>();

		int start = 0;
		int end = 0;
		while (true) {
			/*
			 * /exchweb/img/?.gif
			 * view-mark
			 * view-importance
			 * view-document
			 * view-flag
			 * view-paperclip
			 * sort-d
			 * imp-high
			 * icon-mtgreq
			 * icon-msg-unread
			 * icon-msg-read
			 * icon-msg-reply
			 */
			String readimage = null;
			do{
				start = Util.indexAfter(str, end, "exchweb/img/");
				end = Util.indexBefore(str, start, ".gif");
				if(start == -1 || end == -1) {
					Log.e(TAG, "Could not parse email list. Last image="+readimage);
					return items;
				}
				readimage = str.substring(start, end);
			} while (!"icon-mtgreq".equals(readimage)
					&& !"icon-msg-unread".equals(readimage)
					&& !"icon-msg-read".equals(readimage)
					&& !"icon-recall".equals(readimage)
					&& !"icon-msg-forward".equals(readimage)
					&& !"icon-msg-reply".equals(readimage));
			boolean read = true;
			if ("icon-mtgreq".equals(readimage)
					|| "icon-msg-unread".equals(readimage)) {
				read = false;
			}

			if(true) {
			//if(!onlynew || !read) {
				// from
				start = Util.indexAfter(str, end, "<A", "<A", "<A", "<FONT size=\"2\" color=black>");
				end = Util.indexBefore(str, start, "</FONT>");
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
				start = Util.indexAfter(str, end, "<FONT size=\"2\" color=black>");
				end = Util.indexBefore(str, start, "</FONT>");
				if(start == -1 || end == -1){
					break;
				}
				String subject = str.substring(start, end);

				// date
				start = Util.indexAfter(str, end, "<FONT size=\"2\" color=black>");
				end = Util.indexBefore(str, start, "</FONT>");
				if(start == -1 || end == -1){
					break;
				}
				String date = str.substring(start, end);

				if(isBold(from)){
					from = removeBold(from);
					subject = removeBold(subject);
					date = removeBold(date);
					read = false;
				}

				MailItem item = new MailItem(url, from, subject, OwaUtil
						.parseDate(date, timezoneadj).getTime(), null, read);
				items.add(item);
				long id = dbHelper.createMail(item);
				if(id == -1){
					MailItem fetched = dbHelper.fetchMail(item.url);
					item.id = fetched.id;
					dbHelper.updateMail(item);
				}
			}
		}
		return items;
	}
	
	private static boolean isBold(String inp){
		return inp.contains("<b>");
	}

	private static String removeBold(String inp){
		inp = inp.replaceAll("<b>", "");
		inp = inp.replaceAll("</b>", "");
		return inp;
	}

	public static List<CalendarItem> parseCalendar(String str, int timezoneadj) {

		List<CalendarItem> items = new ArrayList<CalendarItem>();

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
			end = Util.indexBefore(str, start, "\"");
			if(start == -1 || end == -1){
				break;
			}
			String timeTitle = str.substring(start, end);
			
			//link
			start = Util.indexAfter(str, end, "href=\"");
			end = Util.indexBefore(str, start, "\"");
			if(start == -1 || end == -1){
				break;
			}
			String url = str.substring(start, end);
			
			// title and location
			start = Util.indexAfter(str, end, "style=\"text-decoration:none\">");
			end = Util.indexBefore(str, start, "<");
			if(start == -1 || end == -1){
				break;
			}
			while('<' == str.charAt(start)){
				start = Util.indexAfter(str, end, ">");
				end = Util.indexBefore(str, start, "<");
				if(start == -1 || end == -1){
					break;
				}
			}
			String titleLocation = str.substring(start, end);

			int i = timeTitle.indexOf(' ');
			if(i != -1){
				String title = timeTitle.substring(i,
						timeTitle.length());
				String time = timeTitle.substring(0, i);
				int[] timeA = OwaUtil.parseTime(time, timezoneadj);
				Date d = OwaUtil.parseDate(date, timezoneadj);
				MailItem item = new MailItem(url, "", title, d.getTime(), null, false);
				items.add(new CalendarItem(item, timeA[0], timeA[1], titleLocation));
			}
		}
		return items;
	}

}
