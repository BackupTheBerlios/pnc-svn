package com.mathias.android.acast;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.podcast.FeedItem;

public class Player extends Activity implements ServiceConnection {

	private static final String TAG = Player.class.getSimpleName();

	private static final int SEEK_REW = -30000;

	private static final int SEEK_FOR = 30000;

	private static final long UPDATE_DELAY = 1000;

	private Boolean tracking = false;

	private TextView duration;

	private IMediaService mediaBinder;

	private SeekBar seekbar;

	private ImageButton playpause;

	private ACastDbAdapter mDbHandler;
	
	private int itemDuration;

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
					if(mediaBinder != null){
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
			Util.showDialog(this, Util.fromHtmlNoImages(item.getTitle()), Util
					.fromHtmlNoImages(item.getDescription()));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showDialog(this, e.getMessage());
		}
		return true;
	}

	private Handler progressHandler = new Handler(){
		@Override
		public void handleMessage(Message not_used) {
			try {
				int pos;
				if(!tracking && mediaBinder != null){
					pos = mediaBinder.getCurrentPosition();
					seekbar.setProgress(pos);
				}else{
					pos = seekbar.getProgress();
				}
				String dur = Util.convertDuration(pos);
				if(mediaBinder != null){
					dur += "/"+Util.convertDuration(itemDuration);
					duration.setText(dur);
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage(), e);
				Util.showDialog(Player.this, e.getMessage());
			}
			sendEmptyMessageDelayed(0, UPDATE_DELAY);
		}
	};

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause "+getTaskId());
		super.onPause();
	}
	
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
		try {
			if(mediaBinder != null){
				if(!mediaBinder.isPlaying()){
					playpause.setImageResource(R.drawable.play);
					//playpause.setImageResource(android.R.drawable.ic_media_play);
				}else{
					playpause.setImageResource(R.drawable.pause);
					//playpause.setImageResource(android.R.drawable.ic_media_pause);
				}
				itemDuration = mediaBinder.getDuration();
				seekbar.setMax(itemDuration);
			}else{
				Log.w(TAG, "binder is null");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showDialog(Player.this, e.getMessage());
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+getTaskId()+" "+name);
		mediaBinder = IMediaService.Stub.asInterface(service);
		try {
			long id = mediaBinder.getId();
			FeedItem item = mDbHandler.fetchFeedItem(id);

			TextView title = (TextView) findViewById(R.id.title);
			if(item == null){
				title.setText("No item!");
				return;
			}
			title.setText(item.getTitle());

			if (mediaBinder.isPlaying()) {
				Log.d(TAG, "Already playing: " + item.getMp3uri());
				playpause.setImageResource(R.drawable.pause);
				//playpause.setImageResource(android.R.drawable.ic_media_pause);
			} else {
				playpause.setImageResource(R.drawable.play);
				//playpause.setImageResource(android.R.drawable.ic_media_play);
			}
			itemDuration = mediaBinder.getDuration();
			seekbar.setMax(itemDuration);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}


	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected: "+getTaskId()+" "+name);
	}

}
