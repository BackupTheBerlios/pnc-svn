package com.mathias.android.owanotify.beans;

import java.io.Serializable;

import com.mathias.android.owanotify.common.Util;

@SuppressWarnings("serial")
public class MailItem implements Serializable {
	public long id;
	public boolean read;
	public String url;
	public String sender;
	public String cc;
	public String to;
	public String subject;
	public long date;
	public String text;

	public MailItem(){
	}

	public MailItem(MailItem item){
		read = item.read;
		sender = item.sender;
		subject = item.subject;
		date = item.date;
		url = item.url;
		text = item.text;
	}

	public MailItem(String url, String from, String subject, long date,
			String text, boolean read) {
		this.read = read;
		this.sender = from;
		this.subject = subject;
		this.date = date;
		this.url = url;
		this.text = text;
	}

	@Override
	public String toString() {
		return Util.buildString("[from=", sender, "][read=", read, "][subject=",
				subject, "][date=", date, "][url=", url, "]");
	}

}
