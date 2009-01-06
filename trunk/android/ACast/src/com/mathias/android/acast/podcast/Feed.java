package com.mathias.android.acast.podcast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.util.Log;

@SuppressWarnings("serial")
public class Feed implements Serializable {
	
	private static final String TAG = Feed.class.getSimpleName();

	public static Comparator<Feed> BYDATE = new Comparator<Feed>() {
		@Override
		public int compare(Feed arg0, Feed arg1) {
			Date a0 = getPubdateAsDate(arg0);
			Date a1 = getPubdateAsDate(arg1);
			return (a0 != null && a1 != null ? a0.compareTo(a1) : 0);
		}
	};

	public static Comparator<Feed> BYTITLE = new Comparator<Feed>() {
		@Override
		public int compare(Feed arg0, Feed arg1) {
			return arg0.title.compareTo(arg1.title);
		}
	};
	
	public static Date getPubdateAsDate(Feed feed) {
		Date date = feed.getPubdateAsDate();
		if (date == null) {
			List<FeedItem> items = feed.getItems();
			Collections.sort(items, FeedItem.BYDATE);
			Collections.reverse(items);
			date = (items.size() > 0 ? items.get(0).getPubdateAsDate() : null);
		}
		return date;
	}

	private long id;

	private String title;
	
	private String uri;
	
	private String icon;
	
	private String link;
	
	private String pubdate;
	
	private String category;
	
	private String author;
	
	private String description;
	
	private List<FeedItem> items = new ArrayList<FeedItem>();

	public Feed(){
	}

	public Feed(long id, String title, String uri, String icon, String link,
			String pubdate, String category, String author, String description) {
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPubdate() {
		return pubdate;
	}

	public Date getPubdateAsDate() {
		Date date = null;
		try{
			if(pubdate != null){
				date = new Date(pubdate);
			}
		}catch(Exception e){
			Log.e(TAG, e.getMessage(), e);
		}
		return date;
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

}
