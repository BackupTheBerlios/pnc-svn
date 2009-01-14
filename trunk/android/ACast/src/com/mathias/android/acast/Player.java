package com.mathias.android.acast;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.common.services.media.IMediaServiceCallback;
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.podcast.FeedItem;

public class Player extends Activity implements ServiceConnection {

	private static final String TAG = Player.class.getSimpleName();

	private static final int SEEK_REW = -30000;

	private static final int SEEK_FOR = 30000;

	private static final long UPDATE_DELAY = 1000;

	private boolean tracking = false;

	private TextView duration;

	private IMediaService mediaBinder;

	private SeekBar seekbar;

	private ImageButton playpause;

	private ACastDbAdapter mDbHandler;
	
	private int itemDuration;

	private TextView title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate "+getTaskId());
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "Player", R.layout.player);

		mDbHandler = new ACastDbAdapter(this);
		mDbHandler.open();

		duration = (TextView) findViewById(R.id.duration);

		// MEDIA SERVICE
		Intent i = new Intent(this, MediaService.class);
		startService(i);
		if(!bindService(i, this, BIND_AUTO_CREATE)){
			Log.w(TAG, "Could not connect to media service!");
			Util.showDialog(this, "Could not connect to media service!");
		}

		title = (TextView) findViewById(R.id.title);

		// PAUSE
		playpause = (ImageButton) findViewById(R.id.playpause);
		playpause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(mediaBinder != null){
						if(mediaBinder.isPlaying()){
							mediaBinder.pause();
							playpause.setImageResource(R.drawable.play);
							//playpause.setImageResource(android.R.drawable.ic_media_play);
						}else{
							mediaBinder.play();
							playpause.setImageResource(R.drawable.pause);
							//playpause.setImageResource(android.R.drawable.ic_media_pause);
						}
					}
				}catch(Exception e){
					Log.e(TAG, e.getMessage(), e);
					Util.showDialog(Player.this, e.getMessage());
				}
			}
		});

		// STOP
		ImageButton stop = (ImageButton) findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(mediaBinder != null){
						mediaBinder.stop();
						finish();
					}else{
						Log.d(TAG, "Stop clicked, binder is null");
					}
				}catch(Exception e){
					Log.e(TAG, e.getMessage(), e);
				}
			}
		});

		// REWIND
		ImageButton rewind = (ImageButton) findViewById(R.id.rewind);
		rewind.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(mediaBinder != null){
						mediaBinder.seek(SEEK_REW);
					}
				}catch(Exception e){
					Log.e(TAG, e.getMessage(), e);
					Util.showDialog(Player.this, e.getMessage());
				}
			}
		});

		// FORWARD
		ImageButton forward = (ImageButton) findViewById(R.id.forward);
		forward.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(mediaBinder != null){
						mediaBinder.seek(SEEK_FOR);
					}
				}catch(Exception e){
					Log.e(TAG, e.getMessage(), e);
					Util.showDialog(Player.this, e.getMessage());
				}
			}
		});

		// SEEKBAR
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				tracking = true;
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				tracking = false;
				try{
					if(mediaBinder != null) {
						mediaBinder.setCurrentPosition(seekBar.getProgress());
					}
				}catch(Exception e){
					Log.e(TAG, e.getMessage(), e);
					Util.showDialog(Player.this, e.getMessage());
				}
			}
		});

		// start progress handler loop
		progressHandler.sendEmptyMessageDelayed(0, UPDATE_DELAY);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(0, 0, 0, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem mitem) {
		try {
			FeedItem item = mDbHandler.fetchFeedItem(mediaBinder.getId());
			Util.showDialog(this, Util.fromHtmlNoImages(item.title), Util
					.fromHtmlNoImages(item.description));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showToastLong(this, e.getMessage());
		}
		return true;
	}

	private Handler progressHandler = new Handler(){
		@Override
		public void handleMessage(Message not_used) {
			try {
				if(mediaBinder != null){
					int pos;
					if(!tracking){
						pos = mediaBinder.getCurrentPosition();
						seekbar.setProgress(pos);
					}else{
						pos = seekbar.getProgress();
					}
					String dur = Util.convertDuration(pos);
					dur += "/"+Util.convertDuration(itemDuration);
					duration.setText(dur);
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage(), e);
				Util.showToastLong(Player.this, e.getMessage());
			}
			sendEmptyMessageDelayed(0, UPDATE_DELAY);
		}
	};

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop() finishing"+getTaskId());
		super.onStop();
		finish();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy: isFinishing="+isFinishing()+" taskid"+getTaskId());
		if(mediaBinder != null){
			unbindService(this);
			mediaBinder = null;
		}
		mDbHandler.close();
		mDbHandler = null;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume "+getTaskId());
		super.onResume();
		populateView();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+getTaskId()+" "+name);
		mediaBinder = IMediaService.Stub.asInterface(service);
		try {
			mediaBinder.registerCallback(mediaCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		populateView();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected: "+getTaskId()+" "+name);
		try {
			mediaBinder.unregisterCallback(mediaCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private final IMediaServiceCallback mediaCallback = new IMediaServiceCallback.Stub() {
		@Override
		public void onPlaylistCompleted() throws RemoteException {
			Log.d(TAG, "onPlaylistCompleted()");
		}
		@Override
		public void onTrackCompleted() throws RemoteException {
			Log.d(TAG, "onTrackCompleted()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					populateView();
				}
			});
		}
	};

	private void populateView(){
		try {
			if(mediaBinder != null){
				FeedItem item = null;
				long id = mediaBinder.getId();
				if(id != -1){
					item = mDbHandler.fetchFeedItem(id);
				}
				if(item == null){
					title.setText("No item!");
				}else{
					title.setText(item.title);
					
					if (mediaBinder.isPlaying()) {
						Log.d(TAG, "Already playing: " + item.mp3uri);
						playpause.setImageResource(R.drawable.pause);
						//playpause.setImageResource(android.R.drawable.ic_media_pause);
					} else {
						playpause.setImageResource(R.drawable.play);
						//playpause.setImageResource(android.R.drawable.ic_media_play);
					}
					int dur = mediaBinder.getDuration();
					if(dur > 0){
						itemDuration = dur;
						seekbar.setMax(itemDuration);
					}
				}
			}else{
				Log.w(TAG, "binder is null");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showToastLong(this, e.getMessage());
		}
	}

}
