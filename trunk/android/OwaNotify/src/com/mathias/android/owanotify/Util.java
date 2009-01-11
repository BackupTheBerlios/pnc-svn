package com.mathias.android.owanotify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class Util {

	private static final String TAG = Util.class.getSimpleName();
	
	public static String downloadFile(long externalid, String src,
			ProgressListener listener, String username, String password)
			throws Exception {
		if(src == null){
			throw new Exception("src is null!");
		}
		InputStream input = null;
		try {
			Log.d(TAG, "Connecting to: "+src);
			DefaultHttpClient client = new DefaultHttpClient();

	        client.getCredentialsProvider().setCredentials(
	                new AuthScope(AuthScope.ANY),
	                new NTCredentials(username, password, null, null));

			HttpResponse response = client.execute(new HttpGet(src));
			StringBuilder sb = new StringBuilder();
			input = response.getEntity().getContent();
			boolean cont = (listener != null ? listener.continueDownload(externalid) : true);
			while(cont){
				byte[] buffer = new byte[8192];
				int c = input.read(buffer);
				if(c == -1){
					break;
				}
				sb.append(new String(buffer, 0, c));
				if(listener != null){
					listener.progressDiff(externalid, c);
				}
			}
			return sb.toString();
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
		}
	}

	public interface ProgressListener{
		void progressDiff(long externalid, long size);
		boolean continueDownload(long externalid);
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

	public static String buildString(Object ... objs){
		StringBuilder sb = new StringBuilder();
		for (Object o : objs) {
			sb.append(o);
		}
		return sb.toString();
	}

	/**
	 * @param time EU format 19:30-20:00 
	 * @return hour
	 */
	public static int getHour(String time) {
		if(time != null){
			int ixd = time.indexOf(':');
			int ret = Integer.parseInt(time.substring(0, ixd));
			Log.d(TAG, "getHour: "+ret);
			return ret;
		}
		return -1;
	}

	/**
	 * @param time EU format 19:30-20:00 
	 * @return hour
	 */
	public static int getMinute(String time) {
		if(time != null){
			int start = indexAfter(time, 0, ":");
			int end = indexBefore(time, start, "-");
			int ret = Integer.parseInt(time.substring(start, end));
			Log.d(TAG, "getMinute: "+ret);
			return ret;
		}
		return -1;
	}

}
