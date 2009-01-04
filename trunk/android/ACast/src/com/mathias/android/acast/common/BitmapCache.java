package com.mathias.android.acast.common;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {

	private static final BitmapCache instance = new BitmapCache();
	
	private final Map<String, Bitmap> map = new HashMap<String, Bitmap>();
	
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

}
