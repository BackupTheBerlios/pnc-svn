package com.mathias.android.acast.common.services.download;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.Util.ProgressListener;

/**
 * start service and bind interface and callback
 * service will terminate by it self when file is downloaded
 * sends notification when download is complete, no intent/activity will be started
 */
public class DownloadService extends Service implements ProgressListener {

	private static final String TAG = DownloadService.class.getSimpleName();
	
	public static final String EXTERNALID = "EXTERNALID";
	
	public static final String SRCURI = "SRCURI";
	
	public static final String DESTFILE = "DESTFILE";

	private RemoteCallbackList<IDownloadServiceCallback> mCallbacks = new RemoteCallbackList<IDownloadServiceCallback>();
	
	private ExecutorService executor;
	
	private boolean continueDownload = true;
	
	private String lastDestFile;
	
	private Map<Long, DownloadItem> items = new HashMap<Long, DownloadItem>();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart" );
		
		Bundle extras = intent.getExtras();
		if(extras != null){
			Long externalid = (Long)extras.get(EXTERNALID);
			String srcuri = (String)extras.get(SRCURI);
			String destfile = lastDestFile = (String)extras.get(DESTFILE);
			if(externalid == null || srcuri == null || destfile == null){
				throw new RuntimeException("Parmaterer null: "+externalid +" "+srcuri +" "+destfile);
			}
			DownloadItem item = new DownloadItem(externalid, srcuri, destfile, 0);
			items.put(externalid, item);
			executor.execute(new DownloadTread(item));
			Log.d(TAG, "executed: "+externalid +" "+srcuri +" "+destfile);
		}
	}
	
	private class DownloadTread implements Runnable {

		private DownloadItem item;

		public DownloadTread(DownloadItem item) {
			this.item = item;
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
			Message msg = new Message();
			msg.arg1 = (int)item.getExternalId();
			try {
				File f = new File(item.getDestfile());
				Log.d(TAG, "Downloading file: "+f.getName());
				Util.downloadFile(item.getExternalId(), item.getSrcuri(), f, DownloadService.this);
				Log.d(TAG, "Done downloading file: "+f.getName());
				msg.what = 0;
				handler.sendMessage(msg);
			} catch (Exception e) {
				msg.what = 1;
				msg.obj = e;
				handler.sendMessage(msg);
			}
		}
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				broadcastCompleted(msg.arg1);
				DownloadItem remove = items.remove(msg.arg1);
				String r = (remove != null ? remove.getDestfile() : "remove is null");
				Log.d(TAG, "broadcastCompleted: "+r);
				break;
			case 1:
				String s = msg.obj.toString();
				if(msg.obj instanceof Exception){
					s = ((Exception)msg.obj).getMessage();
				}
				broadcastException(msg.arg1, s);
				remove = items.remove(msg.arg1);
				r = (remove != null ? remove.getDestfile() : "remove is null");
				Log.d(TAG, "broadcastException: "+r);
				break;
			}
		}
	};
	
	private final IDownloadService.Stub binder = new IDownloadService.Stub(){
		@Override
		public void download(long externalid, String srcuri, String destfile)
				throws RemoteException {
			Log.d(TAG, "Queing: "+destfile);
			DownloadItem item = new DownloadItem(externalid, srcuri, destfile, 0);
			items.put(externalid, item);
			executor.execute(new DownloadTread(item));
		}
		@Override
		public void cancelAndRemoveCurrent() throws RemoteException {
			continueDownload = false;
			new File(lastDestFile).delete();
			Iterator<DownloadItem> it = items.values().iterator();
			while(it.hasNext()){
				DownloadItem item = it.next();
				if(item.getDestfile().equalsIgnoreCase(lastDestFile)){
					it.remove();
					break;
				}
			}
		}
		@Override
		public void cancelAndRemove(long externalid) throws RemoteException {
			DownloadItem item = items.get(externalid);
			String destfile = item.getDestfile();
			if(lastDestFile.equalsIgnoreCase(destfile)){
				continueDownload = false;
			}
			new File(destfile).delete();
			items.remove(externalid);
		}
		@Override
		public void cancelAndRemoveAll() throws RemoteException {
			continueDownload = false;
			new File(lastDestFile).delete();
			Iterator<DownloadItem> it = items.values().iterator();
			while(it.hasNext()){
				DownloadItem item = it.next();
				new File(item.getDestfile()).delete();
				it.remove();
			}
		}
		@Override
		public void registerCallback(IDownloadServiceCallback cb)
				throws RemoteException {
			mCallbacks.register(cb);
		}
		@Override
		public void unregisterCallback(IDownloadServiceCallback cb)
				throws RemoteException {
			mCallbacks.unregister(cb);
		}
		@Override
		public List getDownloads() throws RemoteException {
			return new ArrayList<DownloadItem>(items.values());
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void progressDiff(long externalid, long size) {
		DownloadItem item = items.get(externalid);
		if(item != null){
			item.setProgress(item.getProgress()+size);
		}
		broadcastDiff(externalid, size);
	}

	@Override
	public boolean continueDownload(long externalid) {
		return continueDownload;
	}

	private void broadcastCompleted(int externalid){
//		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		nm.notify(0, new Notification(R.drawable.icon, "Download complete", 0));

        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onCompleted(externalid);
			} catch (RemoteException e) {
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void broadcastException(int externalid, String exception){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onException(externalid, exception);
			} catch (RemoteException e) {
			}
		}
		mCallbacks.finishBroadcast();		
	}

	private void broadcastDiff(long externalid, long diff){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onProgress(externalid, diff);
			} catch (RemoteException e) {
			}
		}
		mCallbacks.finishBroadcast();		
	}

}
