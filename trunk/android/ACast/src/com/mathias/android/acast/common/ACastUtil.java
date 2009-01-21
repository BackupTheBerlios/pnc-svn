package com.mathias.android.acast.common;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.mathias.android.acast.Player;
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

	public static void playQueueItem(Context cxt, IMediaService mediaBinder,
			FeedItem item, List<FeedItem> items) {
		try {
			if (item != null
					&& mediaBinder != null
					&& (!mediaBinder.isPlaying() || mediaBinder.getId() != item.id)) {
				mediaBinder.initItem(item.id);
				//Queue
				mediaBinder.clearQueue();
				boolean found = false;
				for (FeedItem fitem : items) {
					if(found){
						if(!fitem.completed && fitem.downloaded){
							Log.d(TAG, "queue: "+fitem.id);
							mediaBinder.queue(fitem.id);
						}
					}else if(item.id == fitem.id){
						found = true;
					}
				}
			} else {
				Log.d(TAG, "isPlaying or mediaBinder == null");
			}
		} catch (Exception e) {
			String msg = e.getMessage();
			Log.e(TAG, (msg != null ? msg : e.toString()), e);
		}
		Intent i = new Intent(cxt, Player.class);
		cxt.startActivity(i);
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
				return -1;
			}else{
				return 1;
			}
		}
		return 0;
	}

	public static int getStatusIcon(FeedItem item){
		if(item.size > 0 && item.downloaded){
			if(new File(item.mp3file).length() != item.size){
				return R.drawable.downloaded_partly;
			}
		}
        if(item.mp3uri == null){
        	if(item.completed){
	            return R.drawable.textonly_done;
        	}else{
	            return R.drawable.textonly;
        	}
        }else if(item.downloaded){
			if(item.completed){
				if(item.bookmark > 0){
		            return R.drawable.downloaded_done_bm;
				}else{
					return R.drawable.downloaded_done;
				}
			}else{
				if(item.bookmark > 0){
					return R.drawable.downloaded_bm;
				}else{
					return R.drawable.downloaded;
				}
			}
		}else{
			if(item.completed){
				if(item.bookmark > 0){
					return R.drawable.notdownloaded_done_bm;
				}else{
					return R.drawable.notdownloaded_done;
				}
			}else{
				if(item.bookmark > 0){
					return R.drawable.notdownloaded_bm;
				}else{
					return R.drawable.notdownloaded;
				}
			}
		}
        //return R.drawable.question;
	}

}
