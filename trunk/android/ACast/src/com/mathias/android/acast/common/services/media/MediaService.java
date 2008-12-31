package com.mathias.android.acast.common.services.media;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.Player;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.Util;

public class MediaService extends Service {
	
	private static final String TAG = MediaService.class.getSimpleName();

	private static final int NOTIFICATION_ID = R.string.playing;

	private MediaPlayer mp;

    private NotificationManager mNM;
    
    private WorkerThread thread;
    
	private ACastDbAdapter mDbHandler;
	
    final RemoteCallbackList<IMediaServiceCallback> mCallbacks = new RemoteCallbackList<IMediaServiceCallback>();

    @Override
	public void onCreate() {

    	thread = new WorkerThread();
    	thread.start();
    	
    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		mDbHandler = new ACastDbAdapter(this);
		mDbHandler.open();
	}
    
    private class WorkerThread extends Thread {
    	
		public Handler handler;
		
		public void play(){
			showNotification();
			handler.sendEmptyMessage(0);
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
			Looper.prepare();
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					if(mp != null && !mp.isPlaying()){
						Log.d(TAG, "mp.start()");
    					mp.start();
					}
				}
			};
			Looper.loop();
		}
    }

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart" );
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    private final IMediaService.Stub binder = new IMediaService.Stub() {
    	
    	private long externalid;
    	
    	private String locator;
    	
    	private boolean stream;

		@Override
		public void pause() throws RemoteException {
			Log.d(TAG, "pause()");
			mDbHandler.updateFeedItem(externalid,
					ACastDbAdapter.FEEDITEM_BOOKMARK, getCurrentPosition());
	        mNM.cancel(NOTIFICATION_ID);
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
				mp.seekTo(mp.getCurrentPosition()+msec);
			}
		}
		@Override
		public void stop() throws RemoteException {
			Log.d(TAG, "stop() mp="+mp);
			mDbHandler.updateFeedItem(externalid,
					ACastDbAdapter.FEEDITEM_BOOKMARK, getCurrentPosition());
			stopMediaPlayer();
			stopSelf();
		}
		@Override
		public int getCurrentPosition() throws RemoteException {
			//Log.d(TAG, "getCurrentPosition() mp="+mp);
			if(mp == null){
				return 0;
			}
			return mp.getCurrentPosition();
		}
		@Override
		public int getDuration() throws RemoteException {
			Log.d(TAG, "getDuration() mp="+mp);
			if(mp == null){
				return 0;
			}
			return mp.getDuration();
		}
		@Override
		public void setCurrentPosition(int position) throws RemoteException {
			Log.d(TAG, "setCurrentPosition(position) mp="+mp);
			if(mp != null){
				mp.seekTo(position);
			}
		}
		@Override
		public void initItem(final long newexternalid, String locator, boolean stream) throws RemoteException {
			Log.d(TAG, "initItem, locator="+locator);

			if(locator == null){
				return;
			}

			mDbHandler.updateFeedItem(this.externalid,
					ACastDbAdapter.FEEDITEM_BOOKMARK, getCurrentPosition());

			this.externalid = newexternalid;
			this.locator = locator;
			this.stream = stream;
			
			if(mp != null){
				mp.reset();
				mp.release();
				mp = null;
			}
			if(stream){
				String uri = locator.replace(' ', '+');
				Log.d(TAG, "Initializing from URI: "+uri);
				mp = MediaPlayer.create(MediaService.this, Uri.parse(uri));
				if(mp == null){
					Util.showDialog(MediaService.this, "Could not create media player for: "+uri);
					return;
				}
			}else{
				Log.d(TAG, "Initializing from file: "+locator);
				mp = new MediaPlayer();
				try {
					File f = new File(locator);
					if(!f.exists()){
						Util.showDialog(MediaService.this, "File does not exist: "+locator);
						return;
					}
					mp.setDataSource(MediaService.this, Uri.fromFile(f));
					mp.prepare();
				} catch (Exception e) {
					Log.e(TAG, locator, e);
					Util.showDialog(MediaService.this, e.getMessage()+": "+locator);
				}
			}

			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer mediaplayer) {
					if (externalid >= 0) {
						int currentPosition = mp.getCurrentPosition();
						if (currentPosition + 100 >= mp.getDuration()) {
							mDbHandler.updateFeedItem(externalid, ACastDbAdapter.FEEDITEM_COMPLETED, true);
							mDbHandler.updateFeedItem(externalid, ACastDbAdapter.FEEDITEM_BOOKMARK, 0);
						}else{
							mDbHandler.updateFeedItem(externalid, ACastDbAdapter.FEEDITEM_BOOKMARK, currentPosition);
						}
					}
					broadcastOnCompletion();
			        mNM.cancel(NOTIFICATION_ID);
					stopSelf();
				}
			});
			showNotification();
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
		public long getExternalId() throws RemoteException {
			return externalid;
		}
		@Override
		public String getLocator() throws RemoteException {
			return locator;
		}
		@Override
		public boolean isStreming() throws RemoteException {
			return stream;
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
	};
	
	private void broadcastOnCompletion(){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onCompletion();
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();		
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy() mp="+mp);
		stopMediaPlayer();
		mCallbacks.kill();
		mDbHandler.close();
		mDbHandler = null;
	}
	
	private void stopMediaPlayer(){
        mNM.cancel(NOTIFICATION_ID);
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
			String locator = binder.getLocator();
			if(locator != null){
				filename = new File(locator).getName();
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		String ticker = filename;
		CharSequence title = getText(R.string.playing);
		String text = filename;

		Notification notification = new Notification(R.drawable.icon, ticker,
				System.currentTimeMillis());

		Intent i = new Intent(this, Player.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, title, text, contentIntent);

		mNM.notify(NOTIFICATION_ID, notification);
	}

}
