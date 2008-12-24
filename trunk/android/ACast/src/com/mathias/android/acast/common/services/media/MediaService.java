package com.mathias.android.acast.common.services.media;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.acast.Player;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.Util;

public class MediaService extends Service {
	
	private static final String TAG = MediaService.class.getSimpleName();

	private MediaPlayer mp;

    private NotificationManager mNM;
    
    final RemoteCallbackList<IMediaServiceCallback> mCallbacks = new RemoteCallbackList<IMediaServiceCallback>();
    
    private class MediaPlayerThread extends Thread {
		@Override
		public void run() {
			mp.start();
		}
	}

    @Override
	public void onCreate() {
    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
		showNotification();

		// setForeground(true);
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
	        mNM.cancel(R.string.remote_service_started);
			if(mp != null){
				mp.pause();
			}
		}
		@Override
		public void play() throws RemoteException {
			showNotification();
			if(mp != null){
				new MediaPlayerThread().start();
			}
		}
		@Override
		public void seek(int msec) throws RemoteException {
			if(mp != null){
				mp.seekTo(mp.getCurrentPosition()+msec);
			}
		}
		@Override
		public void stop() throws RemoteException {
	        mNM.cancel(R.string.remote_service_started);
			stopSelf();
		}
		@Override
		public int getCurrentPosition() throws RemoteException {
			if(mp == null){
				return 0;
			}
			return mp.getCurrentPosition();
		}
		@Override
		public int getDuration() throws RemoteException {
			if(mp == null){
				return 0;
			}
			return mp.getDuration();
		}
		@Override
		public void setCurrentPosition(int position) throws RemoteException {
			if(mp != null){
				mp.seekTo(position);
			}
		}
		@Override
		public void playItem(long externalid, String locator, boolean stream) throws RemoteException {
			this.externalid = externalid;
			this.locator = locator;
			this.stream = stream;
			
			if(mp != null){
				mp.reset();
				mp.release();
				mp = null;
			}
			if(stream){
				String uri = locator.replace(' ', '+');
				Log.d(TAG, "Playing from URI: "+uri);
				mp = MediaPlayer.create(MediaService.this, Uri.parse(uri));
				if(mp == null){
					Util.showDialog(MediaService.this, "Could not create media player for: "+uri);
					return;
				}
				new MediaPlayerThread().start();
			}else{
				Log.d(TAG, "Playing from file: "+locator);
				mp = new MediaPlayer();
				try {
					File f = new File(locator);
					if(!f.exists()){
						Util.showDialog(MediaService.this, "File does not exist: "+locator);
						return;
					}
					mp.setDataSource(MediaService.this, Uri.fromFile(f));
					mp.prepare();
					new MediaPlayerThread().start();
				} catch (Exception e) {
					Log.e(TAG, locator, e);
					Util.showDialog(MediaService.this, e.getMessage()+": "+locator);
				}
			}

			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer mediaplayer) {
					broadcastOnCompletion();
//					if(mediaplayer != null){
//						mediaplayer.release();
//						mediaplayer = null;
//					}
					stopSelf();
				}
			});
			showNotification();
		}
		@Override
		public boolean isPlaying() throws RemoteException {
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
            if (cb != null){
				mCallbacks.register(cb);
            }
		}
		@Override
		public void unregisterCallback(IMediaServiceCallback cb)
				throws RemoteException {
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
		Log.e(TAG, "onDestroy");
        mNM.cancel(R.string.remote_service_started);
		if(mp != null){
			mp.release();
			mp = null;
		}
		mCallbacks.kill();
	}

	private void showNotification() {
		Notification notification = new Notification(R.drawable.downloaded,
				getText(R.string.remote_service_started), System
						.currentTimeMillis());

		Intent i = new Intent(this, Player.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this,
				getText(R.string.remote_service_started),
				getText(R.string.remote_service_started), contentIntent);

		mNM.notify(R.string.remote_service_started, notification);
	}

}
