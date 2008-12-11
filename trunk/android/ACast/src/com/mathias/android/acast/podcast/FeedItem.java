package com.mathias.android.acast.podcast;

public class FeedItem {

	private long id;

	private long feedId;

	private String title;
	
	private String mp3uri;

	private String mp3file;
	
	public FeedItem(){
	}

	public FeedItem(long id, long feedId, String title, String mp3uri, String mp3file) {
		this.id = id;
		this.feedId = feedId;
		this.title = title;
		this.mp3uri = mp3uri;
		this.mp3file = mp3file;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMp3uri() {
		return mp3uri;
	}

	public void setMp3uri(String mp3uri) {
		this.mp3uri = mp3uri;
	}

	public String getMp3file() {
		return mp3file;
	}

	public void setMp3file(String mp3file) {
		this.mp3file = mp3file;
	}

	public long getFeedId() {
		return feedId;
	}

	public void setFeedId(long feedId) {
		this.feedId = feedId;
	}

}
