package com.mathias.android.owanotify.beans;


public class CalendarItem extends MailItem {
	public int startmin;
	public int stopmin;
	public String location;

	public CalendarItem() {	
	}

	public CalendarItem(MailItem item, int startmin, int stopmin,
			String location) {
		super(item);
		this.startmin = startmin;
		this.stopmin = stopmin;
		this.location = location;
	}

}
