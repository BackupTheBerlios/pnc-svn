package com.mathias.android.acast.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.widget.Toast;

public abstract class Util {

	private static final String TAG = Util.class.getSimpleName();
	
	private static final int DOWNLOAD_PROGRESS_LIMIT = 400000;
	
	private static final ImageGetter NULLIMAGEGETTER = new ImageGetter(){
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

	public static void showDialog(Context cxt, CharSequence msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
//		builder.setTitle(cxt.getPackageName());
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showConfirmationDialog(Context cxt, CharSequence msg, OnClickListener oklistener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
//		builder.setTitle(cxt.getPackageName());
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, oklistener);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showDialog(Context cxt, CharSequence title, CharSequence msg) {
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
	
	@SuppressWarnings("serial")
	public static class DownloadException extends Exception {
		public DownloadException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static void downloadFile(long externalid, String src,
			File dest, ProgressListener listener) throws DownloadException {
		if(src == null || dest == null){
			throw new DownloadException("src or dest null!");
		}
		File dir = dest.getParentFile();
		if(!dir.exists()){
			if(!dir.mkdirs()){
				Log.e(TAG, "Could not create dirs: "+dir.getAbsolutePath());
				throw new DownloadException("Could not create dirs: "+dir.getAbsolutePath());
			}
		}
		InputStream input = null;
		FileOutputStream output = null;
		try {
			if(!dest.exists() && !dest.createNewFile()){
				Log.e(TAG, "Could not create file: "+dest.getAbsolutePath());
				throw new DownloadException("Could not create file: "+dest.getAbsolutePath());
			}
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpGet(src));
			input = response.getEntity().getContent();
			output = new FileOutputStream(dest);
//			input = new InputStreamReader(response.getEntity().getContent(), "UTF8");
//			output = cxt.openFileOutput(dest, Context.MODE_WORLD_READABLE);
			int progressSize = 0;
			boolean cont = (listener != null ? listener.continueDownload(externalid) : true);
			byte[] buffer = new byte[8192];
			while(cont){
				int c = input.read(buffer);
				if(c == -1){
					break;
				}
				output.write(buffer, 0, c);
				progressSize += c;
				if(listener != null && progressSize > DOWNLOAD_PROGRESS_LIMIT){
					listener.progressDiff(externalid, progressSize);
					progressSize = 0;
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new DownloadException(e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new DownloadException(e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new DownloadException(e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			throw new DownloadException(e.getMessage());
		}finally{
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

	public static String buildString(Object ... objs){
		StringBuilder sb = new StringBuilder();
		for (Object o : objs) {
			sb.append(o);
		}
		return sb.toString();
	}

	public static int indexAfter(String str, int from, String exp) {
		int i = str.indexOf(exp, from);
		if(i != -1){
			return i+exp.length();
		}
		return -1;
	}

	public static int indexAfter(String str, int from, String ... exp) {
		for (String s : exp) {
			from = str.indexOf(s, from);
			if(from == -1){
				return -1;
			}
			from += s.length();
		}
		return from;
	}

	public static int indexAfter(String str, int from, String exp, int count) {
		for (int i = 0; i < count; i++) {
			int ret = indexAfter(str, from, exp);
			if(ret == -1){
				return -1;
			}
			from = ret;
		}
		return from;
	}

	public static int indexBefore(String str, int from, String exp) {
		return str.indexOf(exp, from);
	}

	public static String escapeFilename(String filename){
		// Valid for NTFS? @  &  ^  '  ,  {  }  [  ]  $  =  !  -  #  (  )  %  +  ~  _  .
		// Invalid for FAT: space \  /  ?  :  "  *  <  >  | 
		filename = filename.replaceAll("\\|", "");
		filename = filename.replaceAll(":", "");
		// TODO 5: verify below...
		filename = filename.replaceAll("\\?", "");
		filename = filename.replaceAll("\\&", "");
		filename = filename.replaceAll("\\*", "");
		filename = filename.replaceAll("\\<", "");
		filename = filename.replaceAll("\\>", "");
		return filename;
	}

	public static boolean isEmpty(String str){
		return str == null || str.length() == 0;
	}

	public static void openBrowser(Context cxt, String url){
		Intent i = new Intent();
		i.setAction("android.intent.action.VIEW");
		i.setData(Uri.parse(url));
		cxt.startActivity(i);
	}
	
	public static void showToastLong(Context cxt, String text){
		Toast.makeText(cxt, text, Toast.LENGTH_LONG).show();
	}

	public static void showToastShort(Context cxt, String text){
		Toast.makeText(cxt, text, Toast.LENGTH_SHORT).show();
	}

	public static CharSequence fromHtmlNoImages(String source){
		return Html.fromHtml(source, NULLIMAGEGETTER, null);
	}

	public static boolean isRedirect(String uri) throws MalformedURLException, IOException{
		HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
		conn.connect();
		int res = conn.getResponseCode();
		conn.disconnect();
		Log.d(TAG, "response code: "+res);
		if((""+res).charAt(0) == '3'){
			return true;
		}
		return false;
	}

}
