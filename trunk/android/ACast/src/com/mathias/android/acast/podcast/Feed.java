package com.mathias.android.acast.podcast;

import java.util.ArrayList;
import java.util.List;

public class Feed {
	
	private long id;

	private String title;
	
	private String uri;
	
	private String icon;
	
	private String description;
	
	private List<FeedItem> items = new ArrayList<FeedItem>();

	public Feed(){
	}

	public Feed(long id, String title, String uri, String icon, String description) {
		this.id = id;
		this.title = title;
		this.uri = uri;
		this.icon = icon;
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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
