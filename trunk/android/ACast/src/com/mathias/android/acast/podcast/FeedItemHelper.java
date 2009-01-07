package com.mathias.android.acast.podcast;

import java.util.List;

public abstract class FeedItemHelper {

	private FeedItemHelper(){
	}
	
	public static FeedItem getByTitle(List<FeedItem> items, String title){
		for (FeedItem item : items) {
			if(title.equals(item.title)){
				return item;
			}
		}
		return null;
	}

	public static FeedItem getById(List<FeedItem> items, long id){
		for (FeedItem item : items) {
			if(id == item.id){
				return item;
			}
		}
		return null;
	}

}
