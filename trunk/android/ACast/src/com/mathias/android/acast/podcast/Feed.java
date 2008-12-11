package com.mathias.android.acast.podcast;

import java.util.ArrayList;
import java.util.List;

public class Feed {
	
	private long id;

	private String title;
	
	private String uri;
	
	private List<FeedItem> items = new ArrayList<FeedItem>();

	public Feed(){
	}

	public Feed(long id, String title, String uri) {
		this.id = id;
		this.title = title;
		this.uri = uri;
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void addItem(FeedItem item){
		items.add(item);
	}

	public List<FeedItem> getItems(){
		return items;
	}

}
