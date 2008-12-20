package com.mathias.android.acast.common.services.download;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
			executor.execute(new DownloadTread(externalid, srcuri, destfile));
			Log.d(TAG, "executed: "+externalid +" "+srcuri +" "+destfile);
		}
	}
	
	private class DownloadTread implements Runnable {
		
		private long externalId;

		private String srcuri;

		private String destfile;
		
		public DownloadTread(long externalId, String srcuri, String destfile) {
			this.externalId = externalId;
			this.srcuri = srcuri;
			this.destfile = destfile;
		}

		@Override
		public void run() {
			Message msg = new Message();
			msg.arg1 = (int)externalId;
			try {
				Util.downloadFile(externalId, srcuri, new File(
						destfile), DownloadService.this);
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
				break;
			case 1:
				String s = msg.obj.toString();
				if(msg.obj instanceof Exception){
					s = ((Exception)msg.obj).getMessage();
				}
				broadcastException(msg.arg1, s);
				break;
			}
		}
	};
	
	private final IDownloadService.Stub binder = new IDownloadService.Stub(){
		@Override
		public void download(long externalid, String srcuri, String destfile)
				throws RemoteException {
			executor.execute(new DownloadTread(externalid, srcuri, destfile));
		}
		@Override
		public void cancelAndRemove() throws RemoteException {
			continueDownload = false;
			new File(lastDestFile).delete();
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
