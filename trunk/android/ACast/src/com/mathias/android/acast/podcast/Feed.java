package com.mathias.android.acast.podcast;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Feed implements Serializable {

	public long id;

	public String title;
	
	public String uri;
	
	public String icon;
	
	public String link;
	
	public long pubdate;
	
	public String category;
	
	public String author;

	public String description;
	
	public Feed(){
	}

	public Feed(long id, String title, String uri, String icon, String link,
			long pubdate, String category, String author, String description) {
		this.id = id;
		this.title = title;
		this.uri = uri;
		this.icon = icon;
		this.link = link;
		this.pubdate = pubdate;
		this.category = category;
		this.author = author;
		this.description = description;
	}

}
