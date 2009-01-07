package com.mathias.android.acast.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.mathias.android.acast.R;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.FeedItemLight;

public abstract class ACastUtil {

	private static final String TAG = ACastUtil.class.getSimpleName();

	private ACastUtil(){}
	
	public static void customTitle(Activity cxt, String title, int layoutResID){
        cxt.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        cxt.setContentView(layoutResID);
        cxt.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        TextView right = (TextView) cxt.findViewById(R.id.right_text);
        right.setText(title);
	}

	public static void resumeItem(IMediaService mediaBinder, FeedItem item){
		try {
			if (item != null && mediaBinder != null
					&& (!mediaBinder.isPlaying() || mediaBinder.getId() != item
							.id)) {
				playItem(mediaBinder, item);
			}else{
				Log.d(TAG, "isPlaying or mediaBinder == null or item == null");
			}
		} catch (Exception e) {
			String msg = e.getMessage();
			Log.e(TAG, (msg != null ? msg : e.toString()), e);
		}
	}

	public static void playItem(IMediaService mediaBinder, FeedItem item){
		if(mediaBinder == null){
			Log.e(TAG, "binder is null. No connection to media service!");
			return;
		}

		try {
			Log.d(TAG, "initItem: "+item.id);
			mediaBinder.initItem(item.id);
			mediaBinder.setCurrentPosition(item.bookmark);
			mediaBinder.play();
		} catch (Exception e) {
			String msg = e.getMessage();
			Log.e(TAG, (msg != null ? msg : e.toString()), e);
		}
	}

	public static void queueItems(IMediaService mediaBinder, List<FeedItem> items, long afterid){
		if(mediaBinder == null){
			Log.e(TAG, "binder is null. No connection to media service!");
			return;
		}
		
		try {
			mediaBinder.clearQueue();
	
			boolean found = false;
			for (FeedItem item : items) {
				if(found){
					Log.d(TAG, "queue: "+item.id);
						mediaBinder.queue(item.id);
				}else if(afterid == item.id){
					found = true;
				}
			}
		} catch (Exception e) {
			String msg = e.getMessage();
			Log.e(TAG, (msg != null ? msg : e.toString()), e);
		}
	}

	public static boolean queueItem(IMediaService mediaBinder, FeedItem item){
		if(mediaBinder == null){
			Log.e(TAG, "binder is null. No connection to media service!");
			return false;
		}
		
		try {
			mediaBinder.queue(item.id);
			return true;
		} catch (Exception e) {
			String msg = e.getMessage();
			Log.e(TAG, (msg != null ? msg : e.toString()), e);
			return false;
		}
	}

	public static Comparator<Feed> FEED_BYDATE = new Comparator<Feed>() {
		@Override
		public int compare(Feed arg0, Feed arg1) {
			return compareLong(arg0.pubdate, arg1.pubdate);
		}
	};

	public static Comparator<Feed> FEED_BYTITLE = new Comparator<Feed>() {
		@Override
		public int compare(Feed arg0, Feed arg1) {
			return arg0.title.compareTo(arg1.title);
		}
	};
	
	public static Date getPubdateAsDate(Feed feed, List<FeedItem> items) {
		Date date = new Date(feed.pubdate);
		if (date == null) {
			Collections.sort(items, FEEDITEM_BYDATE);
			date = (items.size() > 0 ? new Date(items.get(0).pubdate) : null);
		}
		return date;
	}

	public static Comparator<FeedItem> FEEDITEM_BYDATE = new Comparator<FeedItem>() {
		@Override
		public int compare(FeedItem arg0, FeedItem arg1) {
			return compareLong(arg0.pubdate, arg1.pubdate);
		}
	};

	public static Comparator<FeedItemLight> FEEDITEMLIGHT_BYDATE = new Comparator<FeedItemLight>() {
		@Override
		public int compare(FeedItemLight arg0, FeedItemLight arg1) {
			return compareLong(arg0.pubdate, arg1.pubdate);
		}
	};

	public static Comparator<FeedItem> FEEDITEM_BYTITLE = new Comparator<FeedItem>() {
		@Override
		public int compare(FeedItem arg0, FeedItem arg1) {
			return arg0.title.compareTo(arg1.title);
		}
	};

	public static int compareLong(long a0, long a1){
		if(a0 != 0 && a1 != 0){
			if(a0 > a1){
				return 1;
			}else{
				return -1;
			}
		}
		return 0;
	}
}
