package com.mathias.android.acast.podcast;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FeedItem implements Serializable {

	private long id;

	private long feedId;

	private String title;
	
	private String mp3uri;

	private String mp3file;
	
	private long size;

	private int bookmark;
	
	private boolean completed;
	
	private boolean downloaded;
	
	private String link;
	
	private String pubdate;
	
	private String category;
	
	private String author;
	
	private String comments;
	
	private String description;
	
	public FeedItem(){
	}

	public FeedItem(long id, long feedId, String title, String mp3uri,
			String mp3file, long size, int bookmark, boolean completed,
			boolean downloaded, String link, String pubdate, String category,
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

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getBookmark() {
		return bookmark;
	}

	public void setBookmark(int bookmark) {
		this.bookmark = bookmark;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPubdate() {
		return pubdate;
	}

	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
