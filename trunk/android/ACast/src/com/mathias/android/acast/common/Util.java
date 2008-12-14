package com.mathias.android.acast.common;

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
import android.util.Log;

public abstract class Util {

	private static final String TAG = Util.class.getSimpleName();

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
		void progressDiff(long size);
	}
	
	public static void downloadFile(Context cxt, String src, String dest, ProgressListener listener) throws Exception {
		if(src == null || dest == null){
			throw new Exception("src or dest null!");
		}
		InputStream input = null;
		FileOutputStream output = null;
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpGet(src));
			input = response.getEntity().getContent();
			output = cxt.openFileOutput(dest, Context.MODE_WORLD_READABLE);
			while(true){
				byte[] buffer = new byte[8192];
				int c = input.read(buffer);
				if(c == -1){
					break;
				}
				output.write(buffer, 0, c);
				if(listener != null){
					listener.progressDiff(c);
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception("Download failed");
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception("Download failed");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception("Download failed");
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

}
