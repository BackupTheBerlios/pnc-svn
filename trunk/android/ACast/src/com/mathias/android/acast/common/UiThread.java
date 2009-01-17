package com.mathias.android.acast.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class UiThread extends Thread {

	private Handler handler;
	
	private Context cxt;
	
	public UiThread(Context cxt){
		this.cxt = cxt;
	}
	
	public void showToastLong(final String text){
		handler.post(new Runnable(){
			@Override
			public void run() {
				Util.showToastLong(cxt, text);
			}
		});
	}
	public void showToastShort(final String text){
		handler.post(new Runnable(){
			@Override
			public void run() {
				Util.showToastShort(cxt, text);
			}
		});
	}
	@Override
	public void run() {
		Looper.prepare();
		handler = new Handler();
		Looper.loop();
	}

}
