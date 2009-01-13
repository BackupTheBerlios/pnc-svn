package com.mathias.android.acast.common.services.update;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.Constants;
import com.mathias.android.acast.FeedList;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.RssUtil;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings.SettingEnum;

public class UpdateService extends Service {

	private static final String TAG = UpdateService.class.getSimpleName();
	
    private NotificationManager mNM;
    
    private WorkerThread thread;
    
	private ACastDbAdapter mDbHelper;
	
    private PowerManager.WakeLock mPWL;

    final RemoteCallbackList<IUpdateServiceCallback> mCallbacks = new RemoteCallbackList<IUpdateServiceCallback>();

	@Override
	public void onCreate() {
    	thread = new WorkerThread();
    	thread.start();

    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mPWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"ACast Partial Wake Lock");

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		mCallbacks.kill();
		mDbHelper.close();
		mDbHelper = null;
	}
	
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

	private void showUpdatingNotification() {
		Log.d(TAG, "showUpdatingNotification()");

		Notification notification = new Notification(R.drawable.not_updating,
				"Updating feeds", System.currentTimeMillis());

		Intent i = new Intent(this, FeedList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, "Updating", "Updating all feeds...", contentIntent);

		mNM.notify(Constants.NOTIFICATION_UPDATING_ID, notification);
	}

	private void showUpdateCompleteNotification() {
		Log.d(TAG, "showUpdateCompleteNotification()");

		Notification notification = new Notification(
				R.drawable.not_updatecomplete, "Refresh done", System
						.currentTimeMillis());

		Intent i = new Intent(this, FeedList.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, "Refresh done", "All feed has been updated", contentIntent);

		mNM.notify(Constants.NOTIFICATION_UPDATECOMPLETE_ID, notification);
	}

    private class WorkerThread extends Thread {

		public Handler handler;
		
		public void updateAll(){
			handler.post(new Runnable(){
				@Override
				public void run() {
					mPWL.acquire();
					showUpdatingNotification();
					List<Feed> feeds = mDbHelper.fetchAllFeeds();
					for (Feed feed : feeds) {
						long rowId = feed.id;
						final String title = feed.title;
						Log.d(TAG, "Parsing "+title);
						Map<Feed, List<FeedItem>> result;
						try {
							result = new RssUtil().parse(feed.uri);
							mDbHelper.updateFeed(rowId, result);
						} catch (Throwable e) {
							broadcastUpdateItemException(feed.title, e.getMessage());
							Log.e(TAG, e.getMessage(), e);
						}
					}
					mDbHelper.setSetting(SettingEnum.LASTFULLUPDATE, new Date());
					Log.d(TAG, "Done");
					mNM.cancel(Constants.NOTIFICATION_UPDATING_ID);
					showUpdateCompleteNotification();
					broadcastUpdateAllCompleted();
					mPWL.release();
				}
			});
		}
		
		public void addFeed(final String url){
			handler.post(new Runnable(){
				@Override
				public void run() {
					String title = null;
					try {
						Map<Feed, List<FeedItem>> result = new RssUtil().parse(url);
						Feed resfeed = result.keySet().toArray(new Feed[0])[0];
						title = resfeed.title;
						mDbHelper.createFeed(resfeed, result.get(resfeed));
						broadcastUpdateItemCompleted(title);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						broadcastUpdateItemException(title, e.getMessage());
					}
				}
			});
		}
		
		public void updateFeed(final long id){
			handler.post(new Runnable(){
				@Override
				public void run() {
					String title = null;
					try {
						Feed feed = mDbHelper.fetchFeed(id);
						title = feed.title;
						Log.d(TAG, "Parsing "+title);
						Map<Feed, List<FeedItem>> result = new RssUtil().parse(feed.uri);
						mDbHelper.updateFeed(feed.id, result);
						Log.d(TAG, "Done");
						broadcastUpdateItemCompleted(title);
					} catch (Exception e) {
						broadcastUpdateItemException(title, e.getMessage());
						Log.e(TAG, e.getMessage(), e);
					}
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

}
