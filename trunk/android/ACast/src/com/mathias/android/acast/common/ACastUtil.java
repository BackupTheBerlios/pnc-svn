package com.mathias.android.acast.common;

import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.mathias.android.acast.R;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.podcast.FeedItem;

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
			if (mediaBinder != null
					&& (!mediaBinder.isPlaying() || mediaBinder.getId() != item
							.getId())) {
				playItem(mediaBinder, item);
			}else{
				Log.d(TAG, "isPlaying or mediaBinder == null");
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
			Log.d(TAG, "initItem: "+item.getId());
			mediaBinder.initItem(item.getId());
			mediaBinder.setCurrentPosition(item.getBookmark());
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

		boolean found = false;
		for (FeedItem item : items) {
			if(found){
				Log.d(TAG, "queue: "+item.getId());
				try {
					mediaBinder.queue(item.getId());
				} catch (Exception e) {
					String msg = e.getMessage();
					Log.e(TAG, (msg != null ? msg : e.toString()), e);
				}
			}else if(afterid == item.getId()){
				found = true;
			}
		}
	}

}
