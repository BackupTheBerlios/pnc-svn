package com.mathias.android.acast.common;

import java.util.HashMap;
import java.util.Map;

import com.mathias.android.acast.ACastDbAdapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {

	private static final BitmapCache instance = new BitmapCache();
	
	private final Map<Object, Bitmap> map = new HashMap<Object, Bitmap>();
	
	private BitmapCache(){
	}

	public static BitmapCache instance(){
		return instance;
	}

	public Bitmap get(String path){
		Bitmap res = map.get(path);
		if(res == null){
			res = BitmapFactory.decodeFile(path);
			map.put(path, res);
		}
		return res;
	}

	public Bitmap get(long feedId, ACastDbAdapter dbAdapter){
		Bitmap res = map.get(feedId);
		if(res == null){
			String path = dbAdapter.fetchFeedIcon(feedId);
			res = BitmapFactory.decodeFile(path);
			map.put(path, res);
			map.put(feedId, res);
		}
		return res;
	}

}
