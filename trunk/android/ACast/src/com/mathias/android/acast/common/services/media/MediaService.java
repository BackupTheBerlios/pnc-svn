package com.mathias.android.acast.common.services.media;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.Constants;
import com.mathias.android.acast.Player;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;
import com.mathias.android.acast.podcast.Settings.SettingEnum;

public class MediaService extends Service {
	
	private static final String TAG = MediaService.class.getSimpleName();
	
	private static final int UPDATE_DELAY = 1000;

	private MediaPlayer mp;

    private NotificationManager mNM;
    
    private WorkerThread thread;
    
	private ACastDbAdapter mDbHandler;
	
    final RemoteCallbackList<IMediaServiceCallback> mCallbacks = new RemoteCallbackList<IMediaServiceCallback>();

	private LinkedList<FeedItem> queue = new LinkedList<FeedItem>();
	
	private FeedItem currentItem;
	
	private BroadcastReceiver receiver;

	private long lastPress = 0;
	
	private PositionThread posThread;

    @Override
	public void onCreate() {

    	thread = new WorkerThread();
    	thread.start();

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		mDbHandler = new ACastDbAdapter(this);
		mDbHandler.open();
		
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "MEDIA_BUTTON pressed");
				long newPress = System.currentTimeMillis();
				if(newPress < lastPress + 500){
					try {
						if(binder != null && mp != null){
							if(binder.isPlaying()) {
								binder.pause();
							}else{
								binder.play();
							}
						}
					} catch (RemoteException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
				lastPress = newPress;
			}
		};
		registerReceiver(receiver, new IntentFilter("android.intent.action.MEDIA_BUTTON"));

		posThread = new PositionThread();
		posThread.start();
	}
    
    private class PositionThread extends Thread {
    	
    	private final String TAG = PositionThread.class.getSimpleName();

    	private int currentPosition = 0;

		@Override
		public void run() {
			while(true){
				try {
					Log.d(TAG, "Sleeping for "+UPDATE_DELAY+" ms");
					sleep(UPDATE_DELAY);
					int newpos = (mp != null ? mp.getCurrentPosition() : 0);
					if(newpos == currentPosition){
						Log.d(TAG, "suspending current pos thread!");
						sleep(Long.MAX_VALUE);
					}
					currentPosition = newpos;
				} catch (InterruptedException e) {
				}
			}
		}
    }
    
    private class WorkerThread extends Thread {

		public Handler handler;
		
		public void play(){
			Log.d(TAG, "play() resuming current pos thread!");
			posThread.interrupt();
			showNotification();
			handler.post(new Runnable(){
				@Override
				public void run() {
					if (mp != null) {
						if (!mp.isPlaying()) {
							Log.d(TAG, "mp.start()");
							mp.start();
						}
					}else{
						// reinit mp
						if (currentItem == null && queue != null
								&& !queue.isEmpty()) {
							currentItem = queue.remove();
						}
						if(currentItem != null){
							try {
								initItem(currentItem);
								broadcastTrackCompleted();
							} catch (Exception e) {
								Log.d(TAG, e.getMessage());
							}
						}else{
							try {
								String lastId = mDbHandler.getSetting(SettingEnum.LASTFEEDITEMID);
								FeedItem item = mDbHandler.fetchFeedItem(Long.parseLong(lastId));
								if(item != null){
									initItem(item);
								}
								broadcastTrackCompleted();
							} catch (Exception e) {
								Log.d(TAG, e.getMessage());
							}
						}
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

	private int getCurrentPositionSync(){
		return posThread.currentPosition;
		//return (mp != null ? mp.getCurrentPosition() : 0);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy() mp="+mp);
		unregisterReceiver(receiver);
		stopMediaPlayer();
		mCallbacks.kill();
		mDbHandler.close();
		mDbHandler = null;
	}
	
    private final IMediaService.Stub binder = new IMediaService.Stub() {

		@Override
		public long getId() throws RemoteException {
			if(currentItem != null){
				return currentItem.id;
			}else if(!queue.isEmpty()){
				currentItem = queue.remove();
			}
			return (currentItem != null ? currentItem.id : -1);
		}
		@Override
		public void pause() throws RemoteException {
			Log.d(TAG, "pause()");
			bookmark(false);
	        mNM.cancel(Constants.NOTIFICATION_MEDIASERVICE_ID);
			if(mp != null){
				Log.d(TAG, "mp.pause()");
				mp.pause();
			}
		}
		@Override
		public void play() throws RemoteException {
			Log.d(TAG, "play()");
			thread.play();
		}
		@Override
		public void seek(int msec) throws RemoteException {
			Log.d(TAG, "seek(msec) mp="+mp);
			if(mp != null){
				mp.seekTo(getCurrentPositionSync()+msec);
			}
		}
		@Override
		public void stop() throws RemoteException {
			Log.d(TAG, "stop() mp="+mp);
			bookmark(false);
			stopMediaPlayer();
			stopSelf();
		}
		@Override
		public int getCurrentPosition() throws RemoteException {
			return getCurrentPositionSync();
		}
		@Override
		public int getDuration() throws RemoteException {
			Log.d(TAG, "getDuration() mp="+mp);
			return (mp != null ? mp.getDuration() : 0);
		}
		@Override
		public void setCurrentPosition(int position) throws RemoteException {
			Log.d(TAG, "setCurrentPosition(position) mp="+mp);
			if(mp != null){
				mp.seekTo(position);
			}
		}
		@Override
		public void queue(long id) throws RemoteException {
			if(id != -1){
				FeedItem item = mDbHandler.fetchFeedItem(id);
				if(item != null){
					queue.offer(item);
				}
			}
		}
		@Override
		public void initItem(long newid) throws RemoteException {
			FeedItem item = mDbHandler.fetchFeedItem(newid);
			MediaService.this.initItem(item);
		}
		@Override
		public boolean isPlaying() throws RemoteException {
			Log.d(TAG, "isPlaying() mp="+mp);
			if(mp != null){
				return mp.isPlaying();
			}
			return false;
		}
		@Override
		public void registerCallback(IMediaServiceCallback cb)
				throws RemoteException {
			Log.d(TAG, "registerCallback() cb="+cb);
            if (cb != null){
				mCallbacks.register(cb);
            }
		}
		@Override
		public void unregisterCallback(IMediaServiceCallback cb)
				throws RemoteException {
			Log.d(TAG, "unregisterCallback() cb="+cb);
            if (cb != null) {
            	mCallbacks.unregister(cb);
            }
		}
		@Override
		public List getQueue() throws RemoteException {
			List<Long> ret = new ArrayList<Long>(queue.size());
			for (Iterator<FeedItem> it = queue.iterator(); it.hasNext(); ) {
				ret.add(it.next().id);
			}
			return ret;
		}
		@Override
		public void clearQueue() throws RemoteException {
			queue.clear();
		}
		@Override
		public void clearQueueItem(long id) throws RemoteException {
			Iterator<FeedItem> it = queue.iterator();
			for (;it.hasNext();) {
				FeedItem next = it.next();
				if(next.id == id){
					it.remove();
					break;
				}
			}
		}
		@Override
		public void next() throws RemoteException {
			if(!queue.isEmpty()){
				FeedItem item = queue.remove();
				if(item != null){
					try {
						MediaService.this.initItem(item);
						broadcastTrackCompleted();
					} catch (Exception e) {
						Log.d(TAG, e.getMessage());
					}
				}
			}
		}
		@Override
		public void playQueueItem(long id) throws RemoteException {
			while(!queue.isEmpty()){
				FeedItem item = queue.remove();
				if(item != null && item.id == id){
					try {
						MediaService.this.initItem(item);
						broadcastTrackCompleted();
					} catch (Exception e) {
						Log.d(TAG, e.getMessage());
					}
					break;
				}
			}
		}
	};
	
	private void bookmark(boolean completed){
		try {
			long oldid = binder.getId();
			if(oldid != -1 && mp != null){
				int currpos = getCurrentPositionSync();
				int dur = mp.getDuration();
				Log.d(TAG, "Storing bookmark: "+currpos+"/"+dur+" for: "+oldid);
				if (completed && currpos + 100 >= dur) {
					mDbHandler.updateFeedItem(oldid,
							ACastDbAdapter.FEEDITEM_COMPLETED, true);
					mDbHandler.updateFeedItem(oldid,
							ACastDbAdapter.FEEDITEM_BOOKMARK, 0);
				}else{
					mDbHandler.updateFeedItem(oldid,
							ACastDbAdapter.FEEDITEM_BOOKMARK, currpos);
				}
			}else{
				Log.d(TAG, "bookmark: mp == null or id == -1");
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void initItem(FeedItem item) throws RemoteException {
		if(item == null){
			Log.e(TAG, "initItem: item is null");
			return;
		}
		Log.d(TAG, "initItem, id="+item.id);

		bookmark(false);

		Log.d(TAG, "Storing last feed item: "+item.id+" title="+item.title);
		mDbHandler.setSetting(Settings.SettingEnum.LASTFEEDITEMID, item.id);

		currentItem = item;

		if(mp != null){
			mp.reset();
			mp.release();
			mp = null;
		}
		if(item != null){
			if(!item.downloaded){
				String uri = item.mp3uri.replace(' ', '+');
				Log.d(TAG, "Initializing from URI: "+uri);
				try {
					Util.isRedirect(uri);
				} catch (Exception e) {
					String err = e.getMessage();
					Log.e(TAG, err);
					showErrorNotification(err);
					return;
				}
				mp = MediaPlayer.create(MediaService.this, Uri.parse(uri));
				if(mp == null){
					String err = "Could not create media player for: "+uri;
					Log.e(TAG, err);
					showErrorNotification(err);
					return;
				}
			}else{
				String locator = item.mp3file;
				Log.d(TAG, "Initializing from file: "+locator);
				mp = new MediaPlayer();
				try {
					File f = new File(locator);
					if(!f.exists()){
						String err = "File does not exist: "+locator;
						Log.e(TAG, err);
						showErrorNotification(err);
						return;
					}
					mp.setDataSource(MediaService.this, Uri.fromFile(f));
					mp.prepare();
				} catch (Exception e) {
					Log.e(TAG, locator, e);
					showErrorNotification(e.getMessage());
					return;
				}
			}
			mp.seekTo(item.bookmark);
		}

		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mediaplayer) {
				try {
					bookmark(true);
					if(queue.isEmpty()){
						mNM.cancel(Constants.NOTIFICATION_MEDIASERVICE_ID);
						broadcastTrackCompleted();
						broadcastPlaylistCompleted();
						stopSelf();
					}else{
						currentItem = queue.remove();
						initItem(currentItem);
						broadcastTrackCompleted();
					}
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		});

		thread.play();
		showNotification();
	}

	private void broadcastTrackCompleted(){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onTrackCompleted();
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void broadcastPlaylistCompleted(){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onPlaylistCompleted();
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

	private void stopMediaPlayer(){
        mNM.cancel(Constants.NOTIFICATION_MEDIASERVICE_ID);
		if(mp != null){
			mp.reset();
			mp.release();
			mp = null;
		}
	}

	private void showNotification() {
		Log.d(TAG, "showNotification()");
		String filename = "";
		try {
			long id = binder.getId();
			if(id >= 0){
				FeedItem item = mDbHandler.fetchFeedItem(id);
				if(item != null && item.mp3uri != null){
					filename = new File(item.mp3uri).getName();
				}
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		String ticker = filename;
		CharSequence title = getText(R.string.playing);
		String text = filename;

		Notification notification = new Notification(R.drawable.not_play, ticker,
				System.currentTimeMillis());

		Intent i = new Intent(this, Player.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, title, text, contentIntent);

		mNM.notify(Constants.NOTIFICATION_MEDIASERVICE_ID, notification);
	}

	private void showErrorNotification(String error) {
		Log.d(TAG, "showErrorNotification()");

		Notification notification = new Notification(R.drawable.not_play, "Exception",
				System.currentTimeMillis());

		Intent i = new Intent(this, Player.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, "Error", error, contentIntent);

		mNM.notify(Constants.NOTIFICATION_MEDIASERVICE_ID, notification);
	}

}
