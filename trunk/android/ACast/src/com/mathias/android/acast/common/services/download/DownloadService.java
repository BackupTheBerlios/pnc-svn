package com.mathias.android.acast.common.services.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.Constants;
import com.mathias.android.acast.DownloadQueueList;
import com.mathias.android.acast.DownloadedList;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.Util.ProgressListener;
import com.mathias.android.acast.podcast.Settings.SettingEnum;

/**
 * start service and bind interface and callback
 * service will terminate by it self when file is downloaded
 * sends notification when download is complete, no intent/activity will be started
 */
public class DownloadService extends Service {

	private static final String TAG = DownloadService.class.getSimpleName();
	
	public static final String EXTERNALID = "EXTERNALID";
	
	public static final String SRCURI = "SRCURI";
	
	public static final String DESTFILE = "DESTFILE";

	private RemoteCallbackList<IDownloadServiceCallback> mCallbacks = new RemoteCallbackList<IDownloadServiceCallback>();
	
    private NotificationManager mNM;
    
    private PowerManager.WakeLock mPWL;

	private ACastDbAdapter mDbHelper;
	
	private WorkerThread thread;
	
	private LinkedBlockingQueue<DownloadItem> queue = new LinkedBlockingQueue<DownloadItem>();
	
	private boolean wifiConnected = false;
	
	private boolean waitingForWifi = false; 

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mPWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"ACast Partial Wake Lock");

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		thread = new WorkerThread();
		thread.start();
		
		new WifiConnectionThread().start();
	}
	
	private class WifiConnectionThread extends Thread {
		@Override
		public void run() {
			while(true){
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				WifiInfo info = wifiManager.getConnectionInfo();
				boolean tempval = info != null && info.getSSID() != null;
				if(waitingForWifi && tempval != wifiConnected){
					thread.interrupt();
				}
				wifiConnected = tempval;

				DownloadService.sleep(10000);
			}
		}
	}

	private class WorkerThread extends Thread implements ProgressListener {

		private DownloadItem currentItem;

		public void download(DownloadItem item){
			Log.d(TAG, "Queing: "+item.destfile);
			queue.offer(item);
		}
		
		/**
		 * Cancel and remove current downloading item if exists and start next
		 * item.
		 */
		public void cancelAndRemoveCurrent(){
			if(currentItem != null){
				new File(currentItem.destfile).delete();
				currentItem = null;
			}
		}
		
		public void cancelAndRemove(long externalid){
			for (Iterator<DownloadItem> it = queue.iterator(); it.hasNext();) {
				DownloadItem item = it.next();
				if(item.externalId == externalid){
					new File(item.destfile).delete();
					it.remove();
					break;
				}
			}
		}
		
		public void cancelAndRemoveAll(){
			for (Iterator<DownloadItem> it = queue.iterator(); it.hasNext();) {
				new File(it.next().destfile).delete();
				it.remove();
			}
			cancelAndRemoveCurrent();
		}
		
		public List<DownloadItem> getQueue(){
			List<DownloadItem> res = new ArrayList<DownloadItem>();
			for (Iterator<DownloadItem> it = queue.iterator(); it.hasNext();) {
				res.add(it.next());
			}
			return res;
		}
		
		public DownloadItem getCurrentDownload(){
			return currentItem;
		}

		@Override
		public void progressDiff(long externalid, long size) {
			if(currentItem != null && currentItem.externalId == externalid){
				currentItem.progress = currentItem.progress+size;
			}
	        final int N = mCallbacks.beginBroadcast();
			for (int i = 0; i < N; i++) {
				try {
					mCallbacks.getBroadcastItem(i).onProgress(externalid, size);
				} catch (RemoteException e) {
				}
			}
			mCallbacks.finishBroadcast();		
		}

		@Override
		public boolean continueDownload(long externalid) {
			return currentItem != null;
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
			while(true){
				boolean onlyWifiDownload = Boolean.parseBoolean(mDbHelper
						.getSetting(SettingEnum.ONLYWIFIDOWNLOAD));
				if(onlyWifiDownload && !wifiConnected){
					waitingForWifi = true;
					DownloadService.sleep(Long.MAX_VALUE);
					waitingForWifi = false;
					continue;
				}
				try {
					currentItem = queue.take();
					Log.d(TAG, "Got item from queue: "+currentItem);
					if(currentItem != null){
						final int externalId = (int) currentItem.externalId;
						try {
							mPWL.acquire();
							showDownloadingNotification();
							File f = new File(currentItem.destfile);
							Log.d(TAG, "Downloading file: "+f.getName());
							Util.downloadFile(externalId, currentItem.srcuri, f, WorkerThread.this);
							Log.d(TAG, "Done downloading file: "+f.getName());
	
							if(mDbHelper != null){
								mDbHelper.updateFeedItem(externalId, ACastDbAdapter.FEEDITEM_DOWNLOADED, true);
							}

							final int N = mCallbacks.beginBroadcast();
							for (int i = 0; i < N; i++) {
								try {
									mCallbacks.getBroadcastItem(i)
											.onCompleted(externalId);
								} catch (RemoteException e) {
								}
							}
							mCallbacks.finishBroadcast();
							mPWL.release();
						} catch (Exception e) {
							mPWL.release();
							String ret = "Exception: "+e.getMessage();
							Log.e(TAG, ret);
					        final int N = mCallbacks.beginBroadcast();
							for (int i = 0; i < N; i++) {
								try {
									mCallbacks.getBroadcastItem(i).onException(
											externalId, ret);
								} catch (RemoteException e1) {
								}
							}
							mCallbacks.finishBroadcast();		
						}
						mNM.cancel(Constants.NOTIFICATION_DOWNLOADING_ID);
					}
					if(queue.isEmpty()){
						showDownloadCompleteNotification();
					}
				} catch (Throwable e2) {
					Log.e(TAG, e2.getMessage(), e2);
				}
			}
		}
	}

	private final IDownloadService.Stub binder = new IDownloadService.Stub(){
		@Override
		public void download(long externalid, String srcuri, String destfile)
				throws RemoteException {
			thread.download(new DownloadItem(externalid, srcuri, destfile, 0));
		}
		@Override
		public void cancelAndRemoveCurrent() throws RemoteException {
			thread.cancelAndRemoveCurrent();
		}
		@Override
		public void cancelAndRemove(long externalid) throws RemoteException {
			thread.cancelAndRemove(externalid);
		}
		@Override
		public void cancelAndRemoveAll() throws RemoteException {
			thread.cancelAndRemoveAll();
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
		//TODO 5: Parcable?
		@SuppressWarnings("unchecked")
		@Override
		public List getDownloads() throws RemoteException {
			return thread.getQueue();
		}
		@Override
		public long getCurrentDownload() throws RemoteException {
			DownloadItem item = thread.getCurrentDownload();
			if(item != null) {
				return item.externalId;
			}
			return -1;
		}
		@Override
		public long getProgress() throws RemoteException {
			DownloadItem item = thread.getCurrentDownload();
			return (item != null ? item.progress : 0);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		mCallbacks.kill();
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}

	private void showDownloadCompleteNotification() {
		Log.d(TAG, "showDownloadCompleteNotification()");
		String ticker = "Download complete";
		CharSequence title = "Download complete";
		String text = "Download complete";

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, ticker, System
						.currentTimeMillis());

		Intent i = new Intent(this, DownloadedList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, title, text, contentIntent);

		mNM.notify(Constants.NOTIFICATION_DOWNLOADCOMPLETE_ID, notification);
	}

	private void showDownloadingNotification() {
		Log.d(TAG, "showDownloadingNotification()");
		String ticker = "Downloading...";
		CharSequence title = "Downloading "+new File(thread.currentItem.srcuri).getName();
		String text = "Downloading...";

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download, ticker, System
						.currentTimeMillis());

		Intent i = new Intent(this, DownloadQueueList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, title, text, contentIntent);

		mNM.notify(Constants.NOTIFICATION_DOWNLOADING_ID, notification);
	}

	private static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}

}
