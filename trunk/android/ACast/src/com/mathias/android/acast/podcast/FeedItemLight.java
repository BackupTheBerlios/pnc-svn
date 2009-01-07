package com.mathias.android.acast.podcast;

public class FeedItemLight {

	public int bookmark;

	public boolean completed;

	public boolean downloaded;
	
	public long pubdate;

	public FeedItemLight(int bookmark, boolean completed, boolean downloaded, long pubdate) {
		this.bookmark = bookmark;
		this.completed = completed;
		this.downloaded = downloaded;
		this.pubdate = pubdate;
	}

}
