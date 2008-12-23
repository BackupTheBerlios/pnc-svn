package com.mathias.android.acast.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.util.Log;

public abstract class Util {

	private static final String TAG = Util.class.getSimpleName();
	
	public static final ImageGetter NULLIMAGEGETTER = new ImageGetter(){
		@Override
		public Drawable getDrawable(String source) {
			return new Drawable(){
				@Override
				public void draw(Canvas canvas) {}
				@Override
				public int getOpacity() { return 0; }
				@Override
				public void setAlpha(int alpha) {}
				@Override
				public void setColorFilter(ColorFilter cf) {}
			};
		}
    };

	private Util() {
	}

	public static void showDialog(Context cxt, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		builder.setTitle(cxt.getPackageName());
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showDialog(Context cxt, String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(true);
		builder.show();
	}
	
	public interface ProgressListener{
		void progressDiff(long externalid, long size);
		boolean continueDownload(long externalid);
	}

	public static void downloadFile(long externalid, String src,
			File dest, ProgressListener listener) throws Exception {
		if(src == null || dest == null){
			throw new Exception("src or dest null!");
		}
		File dir = dest.getParentFile();
		if(!dir.exists()){
			if(!dir.mkdirs()){
				Log.e(TAG, "Could not create dirs: "+dir.getAbsolutePath());
				throw new Exception("Could not create dirs: "+dir.getAbsolutePath());
			}
		}
		if(!dest.exists() && !dest.createNewFile()){
			Log.e(TAG, "Could not create file: "+dest.getAbsolutePath());
			throw new Exception("Could not create file: "+dest.getAbsolutePath());
		}
		InputStream input = null;
		FileOutputStream output = null;
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpGet(src));
			input = response.getEntity().getContent();
			output = new FileOutputStream(dest);
//			input = new InputStreamReader(response.getEntity().getContent(), "UTF8");
//			output = cxt.openFileOutput(dest, Context.MODE_WORLD_READABLE);
			boolean cont = (listener != null ? listener.continueDownload(externalid) : true);
			while(cont){
				byte[] buffer = new byte[8192];
				int c = input.read(buffer);
				if(c == -1){
					break;
				}
				output.write(buffer, 0, c);
				if(listener != null){
					listener.progressDiff(externalid, c);
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception(e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception(e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception(e.getMessage());
		}finally{
			if(input != null){
				try {
					input.close();
				} catch (IOException e) {
				}
			}
			if(output != null){
				try {
					output.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static String convertDuration(long millis){
		long totalseconds = millis/1000;
		int seconds = (int) (totalseconds % 60);
		int minutes = (int) ((totalseconds/60) % 60);
		int hours = (int) ((totalseconds/3600) % 24);
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public static void closeCursor(Cursor c){
		if(c != null){
			c.close();
			c = null;
		}
	}

}
