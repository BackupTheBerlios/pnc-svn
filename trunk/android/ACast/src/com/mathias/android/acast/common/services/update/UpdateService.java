package com.mathias.android.acast.common.services.update;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.Constants;
import com.mathias.android.acast.DatabaseException;
import com.mathias.android.acast.FeedList;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.RssUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.wifi.IWifiService;
import com.mathias.android.acast.common.services.wifi.IWifiServiceCallback;
import com.mathias.android.acast.common.services.wifi.WifiService;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class UpdateService extends Service implements ServiceConnection {

	private static final String TAG = UpdateService.class.getSimpleName();
	
    private NotificationManager mNM;
    
    private WorkerThread thread;
    
	private ACastDbAdapter mDbHelper;
	
    private PowerManager.WakeLock mPWL;

    private final RemoteCallbackList<IUpdateServiceCallback> mCallbacks = new RemoteCallbackList<IUpdateServiceCallback>();

	private SharedPreferences prefs;

	private boolean wifiAvailable = false;

	private IWifiService wifiBinder;
	
	private boolean onlyWifiUpdate;

	@Override
	public void onCreate() {
    	thread = new WorkerThread();
    	thread.start();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);
		readSettings(null);

    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mPWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"ACast Partial Wake Lock");

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		Intent i = new Intent(this, WifiService.class);
		bindService(i, this, BIND_AUTO_CREATE);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		int sched = intent.getIntExtra(Constants.ALARM, 0);
		Log.d(TAG, "onStart sched="+sched);
		if(sched != 0){
			if(binder != null){
				try {
					binder.updateAll();
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if(wifiBinder != null){
			unbindService(this);
		}
		mCallbacks.kill();
		mDbHelper.close();
		mDbHelper = null;
	}
	
	private void readSettings(String key){
		if(key == null || getString(R.string.ONLYWIFIUPDATE_key).equals(key)){
			onlyWifiUpdate = prefs.getBoolean(getString(R.string.ONLYWIFIUPDATE_key), false);
			Log.d(TAG, "onCreate onlyWifiUpdate="+onlyWifiUpdate);
		}
	}

	private OnSharedPreferenceChangeListener prefsListener = new OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			readSettings(key);
		}
	};

    private final IUpdateService.Stub binder = new IUpdateService.Stub() {
		@Override
		public void addFeed(String url) throws RemoteException {
			thread.addFeed(url);
		}
		@Override
		public void updateFeed(long id) throws RemoteException {
			thread.updateFeed(id);
		}
		@Override
		public void updateAll() throws RemoteException {
			thread.updateAll();
		}
		@Override
		public void registerCallback(IUpdateServiceCallback cb)
				throws RemoteException {
			mCallbacks.register(cb);
		}
		@Override
		public void unregisterCallback(IUpdateServiceCallback cb)
				throws RemoteException {
			mCallbacks.unregister(cb);
		}
    };

	private void broadcastUpdateAllCompleted(){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onUpdateAllCompleted();
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void broadcastUpdateItemCompleted(String title){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onUpdateItemCompleted(title);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void broadcastUpdateItemException(String title, String error){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onUpdateItemException(title, error);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void showUpdatingNotification(String msg, int max, int progress) {
		Log.d(TAG, "showUpdatingNotification()");

		Notification notification = new Notification(R.drawable.not_updating,
				"Updating feeds", System.currentTimeMillis());

		Intent i = new Intent(this, FeedList.class);
		notification.contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.not_progress);
		contentView.setImageViewResource(R.id.icon, R.drawable.not_updating);
		contentView.setTextViewText(R.id.text, msg);
		contentView.setProgressBar(R.id.progress, max, progress, false);
		notification.contentView = contentView;

		mNM.notify(Constants.NOTIFICATION_UPDATESERVICE_ID, notification);
	}

	private void showUpdateCompleteNotification() {
		Log.d(TAG, "showUpdateCompleteNotification()");

		Notification notification = new Notification(
				R.drawable.not_updatecomplete, "Refresh done", System
						.currentTimeMillis());

		Intent i = new Intent(this, FeedList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, "Refresh done", "All feed has been updated", contentIntent);

		notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;

		mNM.notify(Constants.NOTIFICATION_UPDATESERVICE_ID, notification);
	}

    private class WorkerThread extends Thread {

		public Handler handler;
		
		public void updateAll(){
			if(!wifiAvailable && onlyWifiUpdate){
				Util.showToastLong(UpdateService.this, "Update without WiFi disabled!");
				return;
			}
			handler.post(new Runnable(){
				@Override
				public void run() {
					mPWL.acquire();
					List<Feed> feeds = mDbHelper.fetchAllFeeds();
					int size = feeds.size();
					for (int i = 0; i < size; i++) {
						if(!wifiAvailable && onlyWifiUpdate){
							Util.showToastLong(UpdateService.this, "Update without WiFi disabled!");
							break;
						}
						Feed feed = feeds.get(i);
						long rowId = feed.id;
						final String title = feed.title;
						Log.d(TAG, "Parsing "+title);
						showUpdatingNotification("Parsing "+title, size, i);
						Map<Feed, List<FeedItem>> result;
						try {
							result = new RssUtil().parse(feed.uri);
							mDbHelper.updateFeed(rowId, result);
						} catch (Throwable e) {
							broadcastUpdateItemException(feed.title, e.getMessage());
							Log.e(TAG, e.getMessage(), e);
					        Util.showToastShort(UpdateService.this, "Could not update "+
					        		title+" "+e.getMessage());
						}
					}
					try {
						mDbHelper.setSetting(Settings.LASTFULLUPDATE, new Date());
					} catch (DatabaseException e) {
						Log.e(TAG, e.getMessage(), e);
					}
					Log.d(TAG, "Done");
					mNM.cancel(Constants.NOTIFICATION_UPDATESERVICE_ID);
					showUpdateCompleteNotification();
					broadcastUpdateAllCompleted();
			        Util.showToastShort(UpdateService.this, "Update complete");
					mPWL.release();
				}
			});
		}
		
		public void addFeed(final String url){
			if(!wifiAvailable && onlyWifiUpdate){
				Util.showToastLong(UpdateService.this, "Update without WiFi disabled!");
				return;
			}
			handler.post(new Runnable(){
				@Override
				public void run() {
					showUpdatingNotification("Parsing "+new File(url).getName(), 1, 0);
					String title = null;
					try {
						Map<Feed, List<FeedItem>> result = new RssUtil().parse(url);
						Feed resfeed = result.keySet().toArray(new Feed[0])[0];
						title = resfeed.title;
						mDbHelper.createFeed(resfeed, result.get(resfeed));
						broadcastUpdateItemCompleted(title);
				        Util.showToastShort(UpdateService.this, "Added "+title);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						broadcastUpdateItemException(title, e.getMessage());
				        Util.showToastShort(UpdateService.this,
								"Could not add " + title + " "+ e.getMessage());
					}
					mNM.cancel(Constants.NOTIFICATION_UPDATESERVICE_ID);
				}
			});
		}
		
		public void updateFeed(final long id){
			if(!wifiAvailable && onlyWifiUpdate){
				Util.showToastLong(UpdateService.this, "Update without WiFi disabled!");
				return;
			}
			handler.post(new Runnable(){
				@Override
				public void run() {
					String title = null;
					try {
						Feed feed = mDbHelper.fetchFeed(id);
						title = feed.title;
						showUpdatingNotification("Parsing "+title, 1, 0);
						Log.d(TAG, "Parsing "+title);
						Map<Feed, List<FeedItem>> result = new RssUtil().parse(feed.uri);
						mDbHelper.updateFeed(feed.id, result);
						Log.d(TAG, "Done");
						broadcastUpdateItemCompleted(title);
				        Util.showToastShort(UpdateService.this, "Updated "+title);
					} catch (Exception e) {
						broadcastUpdateItemException(title, e.getMessage());
						Log.e(TAG, e.getMessage(), e);
				        Util.showToastShort(UpdateService.this, "Could not update " + 
				        		title + " " + e.getMessage());
					}
					mNM.cancel(Constants.NOTIFICATION_UPDATESERVICE_ID);
				}
			});
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
		}
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
			wifiAvailable = connected;
		}
	};

}
