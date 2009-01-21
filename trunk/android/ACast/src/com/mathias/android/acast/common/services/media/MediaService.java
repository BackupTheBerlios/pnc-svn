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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.Constants;
import com.mathias.android.acast.DatabaseException;
import com.mathias.android.acast.Player;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.wifi.IWifiService;
import com.mathias.android.acast.common.services.wifi.IWifiServiceCallback;
import com.mathias.android.acast.common.services.wifi.WifiService;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class MediaService extends Service implements ServiceConnection {
	
	private static final String TAG = MediaService.class.getSimpleName();

	private MediaPlayer mp;

    private NotificationManager mNM;
    
    private WorkerThread thread;
    
	private ACastDbAdapter mDbHandler;
	
    final RemoteCallbackList<IMediaServiceCallback> mCallbacks = new RemoteCallbackList<IMediaServiceCallback>();

	private LinkedList<FeedItem> queue = new LinkedList<FeedItem>();
	
	private FeedItem currentItem;
	
	private BroadcastReceiver receiver;

	private SharedPreferences prefs;
	
	private LocalSettings settings = new LocalSettings();
	
	private boolean wifiAvailable = false;

	private IWifiService wifiBinder;

    @Override
	public void onCreate() {

    	thread = new WorkerThread();
    	thread.start();

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);
		readSettings();

		mDbHandler = new ACastDbAdapter(this);
		mDbHandler.open();
		
		receiver = new BroadcastReceiver() {
			private long lastPress = 0;

			@Override
			public void onReceive(Context context, Intent intent) {
				if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
					Log.d(TAG, "MEDIA_BUTTON pressed");
					long newPress = System.currentTimeMillis();
					if(newPress < lastPress + 500){
						try {
							if(binder != null && mp != null){
								if(binder.isPlaying()) {
									binder.pause();
								}else{
//									binder.play();
								}
							}
						} catch (RemoteException e) {
							Log.e(TAG, e.getMessage(), e);
						}
					}
					lastPress = newPress;
				}else if(Intent.ACTION_ANSWER.equals(intent.getAction())) {
					try {
						if(binder != null && mp != null){
							if(binder.isPlaying()) {
								binder.pause();
							}
						}
					} catch (RemoteException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}else if(Intent.ACTION_CALL_BUTTON.equals(intent.getAction())) {
					try {
						if(binder != null && mp != null){
							if(binder.isPlaying()) {
								binder.pause();
							}
						}
					} catch (RemoteException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		};
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_ANSWER));
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CALL_BUTTON));
		
		Intent i = new Intent(this, WifiService.class);
		bindService(i, this, BIND_AUTO_CREATE);

		try {
			String lastId = mDbHandler.getSetting(Settings.LASTFEEDITEMID);
			if(lastId != null){
				FeedItem item = mDbHandler.fetchFeedItem(Long.parseLong(lastId));
				if(item != null && item.downloaded){
					initItem(item, false, false);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
    
	private OnSharedPreferenceChangeListener prefsListener = new OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			boolean t = settings.onlyWifiStream;
			readSettings();
			if(!wifiAvailable && settings.onlyWifiStream && !t){
				if(binder != null){
					try {
						binder.stop();
					} catch (RemoteException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		}
	};

	private void readSettings(){
		settings.onlyWifiStream = prefs.getBoolean(getString(R.string.ONLYWIFIDOWNLOAD_key), false);
		Log.d(TAG, "listener onlyWifiDownload="+settings.onlyWifiStream);
		settings.autoDeleteAfterPlayed = prefs.getBoolean(getString(R.string.AUTODELETECOMPLETED_key), false);
		Log.d(TAG, "listener autoDeleteAfterPlayed="+settings.autoDeleteAfterPlayed);
	}

	private class WorkerThread extends Thread {

		public Handler handler;
		
		public void play(){
			Log.d(TAG, "play() resuming current pos thread!");
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
//						if (currentItem == null && queue != null
//								&& !queue.isEmpty()) {
//							currentItem = queue.remove();
//						}
						if(currentItem != null){
							try {
								initItem(currentItem, false, true);
//								broadcastTrackCompleted();
							} catch (Exception e) {
								Log.d(TAG, e.getMessage());
							}
						}else{
							try {
								String lastId = mDbHandler.getSetting(Settings.LASTFEEDITEMID);
								FeedItem item = mDbHandler.fetchFeedItem(Long.parseLong(lastId));
								if(item != null){
									initItem(item, false, true);
								}
//								broadcastTrackCompleted();
							} catch (Exception e) {
								Log.d(TAG, e.getMessage());
							}
						}
					}
					Log.d(TAG, "playin: "+(currentItem != null ? currentItem.title : "ci=null"));
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
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy() mp="+mp);
		prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
		unregisterReceiver(receiver);
		if(wifiBinder != null){
			unbindService(this);
		}
		stopMediaPlayer();
		mCallbacks.kill();
		mDbHandler.close();
		mDbHandler = null;
	}
	
    private final IMediaService.Stub binder = new IMediaService.Stub() {

		@Override
		public long getId() throws RemoteException {
			Log.d(TAG, "getId mp="+mp);
			if(currentItem != null){
				Log.d(TAG, "getId: "+currentItem.title);
				return currentItem.id;
			}else{
				int lastId = mDbHandler.getSettingInt(Settings.LASTFEEDITEMID);
				if(lastId != -1){
					currentItem = mDbHandler.fetchFeedItem(lastId);
				}
			}
			long id = (currentItem != null ? currentItem.id : -1);
			Log.d(TAG, "getId: "+(currentItem != null ? currentItem.title : "ci=null"));
			return id;
		}
		@Override
		public void pause() throws RemoteException {
			Log.d(TAG, "pause mp="+mp);
	        mNM.cancel(Constants.NOTIFICATION_MEDIASERVICE_ID);
			if(mp != null){
				Log.d(TAG, "mp.pause()");
				mp.pause();
			}
			bookmark(false);
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
				int pos = getCurrentPosition()+msec;
				Log.d(TAG, "seek(msec) pos="+pos);
				mp.seekTo(pos);
			}
		}
		@Override
		public void stop() throws RemoteException {
			Log.d(TAG, "stop() mp="+mp);
	        mNM.cancel(Constants.NOTIFICATION_MEDIASERVICE_ID);
			if(mp != null){
				mp.pause();
			}
			bookmark(false);
			stopSelf();
		}
		@Override
		public int getCurrentPosition() throws RemoteException {
			int pos = (mp != null ? mp.getCurrentPosition() : 0);
			Log.d(TAG, "getCurrentPosition pos="+pos+" mp="+mp);
			return pos;
		}
		@Override
		public int getDuration() throws RemoteException {
			int dur = (mp != null ? mp.getDuration() : 0);
			Log.d(TAG, "getDuration() dur="+dur+" mp="+mp);
			return dur;
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
			Log.d(TAG, "queue id="+id+" mp="+mp);
			if(id != -1){
				FeedItem item = mDbHandler.fetchFeedItem(id);
				if(item != null){
					queue.offer(item);
				}
			}
		}
		@Override
		public void initItem(long newid) throws RemoteException {
			Log.d(TAG, "initItem mp="+mp);
			FeedItem item = mDbHandler.fetchFeedItem(newid);
			MediaService.this.initItem(item, false, true);
		}
		@Override
		public boolean isPlaying() throws RemoteException {
			boolean res = false;
			if(mp != null){
				res = mp.isPlaying();
			}
			Log.d(TAG, "isPlaying="+res+" mp="+mp);
			return res;
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
			Log.d(TAG, "getQueue mp="+mp);
			List<Long> ret = new ArrayList<Long>(queue.size());
			for (Iterator<FeedItem> it = queue.iterator(); it.hasNext(); ) {
				ret.add(it.next().id);
			}
			return ret;
		}
		@Override
		public void clearQueue() throws RemoteException {
			Log.d(TAG, "clearQueue mp="+mp);
			queue.clear();
		}
		@Override
		public void clearQueueItem(long id) throws RemoteException {
			Log.d(TAG, "clearQueueItem mp="+mp);
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
			Log.d(TAG, "next mp="+mp);
			if(!queue.isEmpty()){
				FeedItem item = queue.remove();
				if(item != null){
					try {
						MediaService.this.initItem(item, false, true);
						broadcastTrackCompleted();
					} catch (Exception e) {
						Log.d(TAG, e.getMessage());
					}
				}
			}
		}
		@Override
		public void playQueueItem(long id) throws RemoteException {
			Log.d(TAG, "playQueueItem mp="+mp);
			while(!queue.isEmpty()){
				FeedItem item = queue.remove();
				if(item != null && item.id == id){
					try {
						MediaService.this.initItem(item, false, true);
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
		Log.d(TAG, "bookmark completed="+completed+" mp="+mp);
		try {
			long oldid = binder.getId();
			if(oldid != -1 && mp != null){
				int currpos = binder.getCurrentPosition();
				int dur = mp.getDuration();
				Log.d(TAG, "Storing bookmark: "+currpos+"/"+dur+" for: "+oldid);
				if (completed && currpos + 100 >= dur) {
					mDbHandler.updateFeedItem(oldid,
							ACastDbAdapter.FEEDITEM_COMPLETED, true);
					mDbHandler.updateFeedItem(oldid,
							ACastDbAdapter.FEEDITEM_BOOKMARK, 0);
					if(settings.autoDeleteAfterPlayed) {
						FeedItem item = mDbHandler.fetchFeedItem(oldid);
						if(item != null && item.downloaded){
							new File(item.mp3file).delete();
							item.downloaded = false;
							mDbHandler.updateFeedItem(item);
						}
					}
				}else{
					mDbHandler.updateFeedItem(oldid,
							ACastDbAdapter.FEEDITEM_BOOKMARK, currpos);
				}
			}else{
				Log.d(TAG, "bookmark: mp == null or id == -1");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void initItem(FeedItem item, boolean lastCompleted, boolean play) throws RemoteException {
		if(item == null){
			Log.e(TAG, "initItem: item is null");
			return;
		}
		if(settings.onlyWifiStream && !wifiAvailable){
			Log.e(TAG, "Streaming over non-WiFi disabled!");
			Util.showToastLong(this, "Streaming over non-WiFi disabled!");
			return;
		}
		Log.d(TAG, "initItem, id=" + item.id + " lastCompleted="
				+ lastCompleted + " mp=" + mp);

		bookmark(lastCompleted);

		Log.d(TAG, "Storing last feed item: "+item.id+" title="+item.title);
		try {
			mDbHandler.setSetting(Settings.LASTFEEDITEMID, item.id);
		} catch (DatabaseException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		currentItem = item;

		stopMediaPlayer();

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
				File l = new File(locator);
				if(!l.exists() || l.length() == 0){
					String err = "File is corrupt: "+locator;
					Log.e(TAG, err);
					showErrorNotification(err);
					return;
				}
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
			mp.setOnCompletionListener(completionListener);

			mp.setOnBufferingUpdateListener(mpListener);
			mp.setOnErrorListener(mpListener);
			mp.setOnPreparedListener(mpListener);
			mp.setOnSeekCompleteListener(mpListener);
			
			mp.seekTo(item.bookmark);

			if(play){
				thread.play();
			}
		}
	}
	
	private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener(){
		@Override
		public void onCompletion(MediaPlayer mediaplayer) {
			Log.d(TAG, "onCompletion mp="+mp);
			try {
				if(!queue.isEmpty()){
					initItem(queue.remove(), true, true);
					broadcastTrackCompleted();
				}else{
					mNM.cancel(Constants.NOTIFICATION_MEDIASERVICE_ID);
					broadcastTrackCompleted();
					broadcastPlaylistCompleted();
					stopSelf();
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	};

	private void broadcastTrackCompleted() {
		Log.d(TAG, "broadcastTrackCompleted mp="+mp);
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

	private void broadcastPlaylistCompleted() {
		Log.d(TAG, "broadcastPlaylistCompleted mp="+mp);
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

	private void stopMediaPlayer() {
		Log.d(TAG, "stopMediaPlayer mp="+mp);
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

	private static class LocalSettings {
		boolean onlyWifiStream = false;
		boolean autoDeleteAfterPlayed = false;
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
			if(!connected && settings.onlyWifiStream){
				if(binder != null){
					try {
						binder.stop();
					} catch (RemoteException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		}
	};

	private MediaPlayerListener mpListener = new MediaPlayerListener();

	private static class MediaPlayerListener implements OnBufferingUpdateListener, OnErrorListener, OnPreparedListener, OnSeekCompleteListener {

		private static final String TAG = MediaPlayerListener.class.getSimpleName();
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			Log.d(TAG, "onBufferingUpdate: percent="+percent);
		}
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.d(TAG, "onError: what="+what+" extra="+extra);
			return false;
		}
		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.d(TAG, "onPrepared: mp="+mp);
		}
		@Override
		public void onSeekComplete(MediaPlayer mp) {
			Log.d(TAG, "onSeekComplete: mp="+mp);
		}
	}
	
}
