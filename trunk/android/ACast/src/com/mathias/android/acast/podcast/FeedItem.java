package com.mathias.android.acast.podcast;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FeedItem implements Serializable {

	public long id;

	public long feedId;

	public String title;
	
	public String mp3uri;

	public String mp3file;
	
	public long size;

	public int bookmark;
	
	public boolean completed;
	
	public boolean downloaded;
	
	public String link;
	
	public long pubdate;
	
	public String category;
	
	public String author;
	
	public String comments;
	
	public String description;
	
	public FeedItem(){
	}

	public FeedItem(long id, long feedId, String title, String mp3uri,
			String mp3file, long size, int bookmark, boolean completed,
			boolean downloaded, String link, long pubdate, String category,
			String author, String comments, String description) {
		this.id = id;
		this.feedId = feedId;
		this.title = title;
		this.mp3uri = mp3uri;
		this.mp3file = mp3file;
		this.size = size;
		this.bookmark = bookmark;
		this.completed = completed;
		this.downloaded = downloaded;
		this.link = link;
		this.pubdate = pubdate;
		this.category = category;
		this.author = author;
		this.comments = comments;
		this.description = description;
	}

}
