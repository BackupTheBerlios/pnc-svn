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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.Constants;
import com.mathias.android.acast.DownloadQueueList;
import com.mathias.android.acast.DownloadedList;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.UiThread;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.Util.DownloadException;
import com.mathias.android.acast.common.Util.ProgressListener;
import com.mathias.android.acast.common.services.wifi.IWifiService;
import com.mathias.android.acast.common.services.wifi.IWifiServiceCallback;
import com.mathias.android.acast.common.services.wifi.WifiService;

/**
 * start service and bind interface and callback
 * service will terminate by it self when file is downloaded
 * sends notification when download is complete, no intent/activity will be started
 */
public class DownloadService extends Service implements ServiceConnection {
	
	private static final String TAG = DownloadService.class.getSimpleName();
	
	public static final String EXTERNALID = "EXTERNALID";
	
	public static final String SRCURI = "SRCURI";
	
	public static final String DESTFILE = "DESTFILE";

	private RemoteCallbackList<IDownloadServiceCallback> mCallbacks = new RemoteCallbackList<IDownloadServiceCallback>();
	
    private NotificationManager mNM;
    
    private PowerManager.WakeLock mPWL;

	private ACastDbAdapter mDbHelper;
	
	private WorkerThread workThread;
	
	private UiThread uiThread;
	
	private LinkedBlockingQueue<DownloadItem> queue = new LinkedBlockingQueue<DownloadItem>();

	private SharedPreferences prefs;

	private boolean onlyWifiDownload = false;

	private boolean wifiAvailable = false;

	private IWifiService wifiBinder;

	private DownloadItem currentItem;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);
		readSettings();

		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mPWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"ACast Partial Wake Lock");

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		workThread = new WorkerThread();
		workThread.start();

		uiThread = new UiThread(this);
		uiThread.start();

		Intent i = new Intent(this, WifiService.class);
		bindService(i, this, BIND_AUTO_CREATE);
	}

	private void readSettings(){
		onlyWifiDownload = prefs.getBoolean(getString(R.string.ONLYWIFIDOWNLOAD_key), false);
		Log.d(TAG, "onCreate onlyWifiDownload="+onlyWifiDownload);
	}

	private class WorkerThread extends Thread implements ProgressListener {

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
			broadcastDownloadProgress(externalid, size);
		}

		@Override
		public boolean continueDownload(long externalid) {
			return currentItem != null;
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
			while(true){
				try{
					Log.d(TAG, "run onlyWifiDownload="+onlyWifiDownload);
					if(onlyWifiDownload && !wifiAvailable) {
						sleep(Long.MAX_VALUE);
					}else{
						currentItem = queue.take();
						mPWL.acquire();
						Log.d(TAG, "Got item from queue: "+currentItem);
						if(currentItem != null){
							int externalId = (int) currentItem.externalId;
							try {
								File f = new File(currentItem.destfile);
								showDownloadingNotification(f.getName());
								Log.d(TAG, "Downloading file: "+f.getName());
								Util.downloadFile(externalId, currentItem.srcuri, f, WorkerThread.this);
								Log.d(TAG, "Done downloading file: "+f.getName());
		
								if(mDbHelper != null){
									mDbHelper.updateFeedItem(externalId, ACastDbAdapter.FEEDITEM_DOWNLOADED, true);
								}

								broadcastDownloadCompleted(externalId);
							} catch (DownloadException e) {
								queue.clear();
								String ret = "Download failed: "+e.getMessage();
								Log.e(TAG, "broadcastDownloadException: "+e.getMessage(), e);
								broadcastDownloadException(externalId, ret);
								uiThread.showToastLong(ret);
							}
							mNM.cancel(Constants.NOTIFICATION_DOWNLOADING_ID);
						}
						if(queue.isEmpty()){
							showDownloadCompleteNotification(currentItem);
						}
						mPWL.release();
					}
				}catch(InterruptedException e){
					continue;
				} catch (Throwable e2) {
					String str = "Error: "+e2.getMessage();
					Log.e(TAG, str, e2);
					if(queue != null && !queue.isEmpty()){
						queue.clear();
					}
					mPWL.release();
					showDownloadExceptionNotification();
					broadcastDownloadException(-1, str);
					uiThread.showToastLong(str);
				}
			}
		}
	}
	
	private OnSharedPreferenceChangeListener prefsListener = new OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			boolean t = onlyWifiDownload;
			readSettings();
			if(!wifiAvailable && onlyWifiDownload && !t){
				currentItem = null;
			}
			workThread.interrupt();
		}
	};

	//
	private void broadcastDownloadCompleted(long externalid){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onCompleted(externalid);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void broadcastDownloadException(long externalid, String exception){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onException(externalid, exception);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void broadcastDownloadProgress(long externalid, long diff){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onProgress(externalid, diff);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private final IDownloadService.Stub binder = new IDownloadService.Stub(){
		@Override
		public void download(long externalid, String srcuri, String destfile)
				throws RemoteException {
			workThread.download(new DownloadItem(externalid, srcuri, destfile, 0));
		}
		@Override
		public void cancelAndRemoveCurrent() throws RemoteException {
			workThread.cancelAndRemoveCurrent();
		}
		@Override
		public void cancelAndRemove(long externalid) throws RemoteException {
			workThread.cancelAndRemove(externalid);
		}
		@Override
		public void cancelAndRemoveAll() throws RemoteException {
			workThread.cancelAndRemoveAll();
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
			return workThread.getQueue();
		}
		@Override
		public long getCurrentDownload() throws RemoteException {
			DownloadItem item = workThread.getCurrentDownload();
			if(item != null) {
				return item.externalId;
			}
			return Constants.INVALID_ID;
		}
		@Override
		public long getProgress() throws RemoteException {
			DownloadItem item = workThread.getCurrentDownload();
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
		prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
		if(wifiBinder != null){
			unbindService(this);
		}
		mCallbacks.kill();
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}

	private void showDownloadExceptionNotification() {
		Log.d(TAG, "showDownloadExceptionNotification()");
		String ticker = "Download error";
		CharSequence title = "Download error";
		String text = "Download error occured";

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, ticker, System
						.currentTimeMillis());

		Intent i = new Intent(this, DownloadedList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, title, text, contentIntent);

		mNM.notify(Constants.NOTIFICATION_DOWNLOADCOMPLETE_ID, notification);
	}

	private void showDownloadCompleteNotification(DownloadItem item) {
		Log.d(TAG, "showDownloadCompleteNotification()");
		String ticker = "Download complete";
		CharSequence title = "Download complete";
		String text = "Download complete";

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, ticker, System
						.currentTimeMillis());

		Intent i = new Intent(this, DownloadedList.class);
		i.putExtra(Constants.FEEDITEMID, (item != null ? item.externalId : Constants.INVALID_ID));

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, title, text, contentIntent);

		notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;

		mNM.notify(Constants.NOTIFICATION_DOWNLOADCOMPLETE_ID, notification);
	}

	private void showDownloadingNotification(String name) {
		Log.d(TAG, "showDownloadingNotification()");
		String ticker = "Downloading "+name;
		CharSequence title = "Downloading...";
		String text = "Downloading "+name;

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download, ticker, System
						.currentTimeMillis());

		Intent i = new Intent(this, DownloadQueueList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, title, text, contentIntent);

		mNM.notify(Constants.NOTIFICATION_DOWNLOADING_ID, notification);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		wifiBinder = IWifiService.Stub.asInterface(service);
		try {
			wifiAvailable = wifiBinder.isWifiAvailable();
			wifiBinder.registerCallback(wifiCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		try {
			wifiBinder.unregisterCallback(wifiCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		wifiBinder = null;
	}

	private final IWifiServiceCallback wifiCallback = new IWifiServiceCallback.Stub() {
		@Override
		public void onWifiStateChanged(boolean connected)
				throws RemoteException {
			workThread.interrupt();
		}
	};

}
