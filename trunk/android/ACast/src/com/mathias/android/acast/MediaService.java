package com.mathias.android.acast;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.FeedItem;

public class MediaService extends Service {
	
	private static final String TAG = MediaService.class.getSimpleName();

	private static final int SEEK_INC = 30000;

	private MediaPlayer mp;

	private FeedItem item;
	
	private ACastDbAdapter mDbHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart" );

		Bundle extras = intent.getExtras();
		item = (FeedItem) (extras != null ? extras.getSerializable(ACast.FEEDITEM)
				: null);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    private final IMediaService.Stub binder = new IMediaService.Stub() {
		@Override
		public void forward() throws RemoteException {
			if(mp != null){
				mp.seekTo(mp.getCurrentPosition()+SEEK_INC);
			}
		}
		@Override
		public void pause() throws RemoteException {
			if(mp != null){
				mp.pause();
			}
		}
		@Override
		public void play() throws RemoteException {
			if(mp != null){
				mp.start();
			}
		}
		@Override
		public void rewind() throws RemoteException {
			if(mp != null){
				mp.seekTo(mp.getCurrentPosition()-SEEK_INC);
			}
		}
		@Override
		public void stop() throws RemoteException {
			if(mp != null){
				mp.stop();
				try {
					mp.prepare();
				} catch (IllegalStateException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(MediaService.this, e.getMessage()+": "+item.getMp3uri());
				} catch (IOException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(MediaService.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
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
		public void reset() throws RemoteException {
			if(mp != null){
				mp.reset();
			}
		}
		@Override
		public void setCurrentPosition(int position) throws RemoteException {
			if(mp != null){
				mp.seekTo(position);
			}
		}
		@Override
		public void playFeedItem(int id) throws RemoteException {
			if(mp != null){
				mp.stop();
				mp.reset();
				mp.release();
				mp = null;
			}
			item = mDbHelper.fetchFeedItem(id);
			String file = item.getMp3file();
			if(file == null){
				String uri = item.getMp3uri().replace(' ', '+');
				Log.d(TAG, "Playing from URI: "+uri);
				mp = MediaPlayer.create(MediaService.this, Uri.parse(uri));
				if(mp == null){
					Util.showDialog(MediaService.this, "Could not create media player for: "+uri);
					return;
				}
				mp.start();
			}else{
				Log.d(TAG, "Playing from file: "+file);
				mp = new MediaPlayer();
				try {
//					File f = new File(getFilesDir()+File.separator+file);
					File f = new File(file);
					if(!f.exists()){
						Util.showDialog(MediaService.this, "File does not exist: "+file);
						return;
					}
					mp.setDataSource(MediaService.this, Uri.fromFile(f));
					mp.prepare();
					mp.start();
				} catch (IllegalArgumentException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(MediaService.this, e.getMessage()+": "+file);
				} catch (IllegalStateException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(MediaService.this, e.getMessage()+": "+file);
				} catch (IOException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(MediaService.this, e.getMessage()+": "+file);
				}
			}

			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer mp) {
					int currentPosition = mp.getCurrentPosition();
					item.setBookmark(currentPosition);
					if(currentPosition+100 >= mp.getDuration()){
						item.setCompleted(true);
						mDbHelper.updateFeedItemCompleted(item.getId(), item
								.isCompleted());
					}
					mp.reset();
					mDbHelper.updateFeedItemBookmark(item.getId(), item.getBookmark());
				}
			});
			mp.seekTo(item.getBookmark());
		}
		@Override
		public int getPid() throws RemoteException {
			return Process.myPid();
		}
	};

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.e(TAG, "onRebind");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "onUnbind");
		return true;
	}

}
