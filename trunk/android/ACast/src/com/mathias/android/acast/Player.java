package com.mathias.android.acast;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class Player extends Activity {

	private static final String TAG = Player.class.getSimpleName();

	private static final int VOLUME_MAX = 15;

	private static final int SEEK_INC = 30000;

	private static final long UPDATE_DELAY = 300;

	private Boolean tracking = false;

	private MediaPlayer mp;

	private ACastDbAdapter mDbHelper;

	private FeedItem item;

	private Settings settings;

	private TextView duration;

	private boolean active = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		TextView title = (TextView) findViewById(R.id.title);

		duration = (TextView) findViewById(R.id.duration);

		item = (FeedItem) (savedInstanceState != null ? savedInstanceState
				.getSerializable(ACast.FEEDITEM) : null);
		if (item == null) {
			Bundle extras = getIntent().getExtras();
			item = (FeedItem) (extras != null ? extras.getSerializable(ACast.FEEDITEM)
					: null);
			if(item == null){
				title.setText("No item!");
				return;
			}
		}
		title.setText(item.getTitle());

		settings = mDbHelper.fetchSettings();
		if(settings == null){
			settings = new Settings(VOLUME_MAX, item.getId());
		}

		String file = item.getMp3file();
		if(file == null){
			String uri = item.getMp3uri().replace(' ', '+');
			mp = MediaPlayer.create(this, Uri.parse(uri));
			if(mp == null){
				Util.showDialog(this, "Could not create media player for: "+uri);
				return;
			}
			mp.start();
		}else{
			mp = new MediaPlayer();
			try {
//				File f = new File(getFilesDir()+File.separator+file);
				File f = new File(file);
				if(!f.exists()){
					Util.showDialog(this, "File does not exist: "+file);
					return;
				}
				mp.setDataSource(this, Uri.fromFile(f));
				mp.prepare();
				mp.start();
			} catch (IllegalArgumentException e) {
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(this, e.getMessage()+": "+file);
			} catch (IllegalStateException e) {
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(this, e.getMessage()+": "+file);
			} catch (IOException e) {
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(this, e.getMessage()+": "+file);
			}
		}

		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
			}
		});
//		mp.setVolume(settings.getVolume(), settings.getVolume());
		mp.seekTo(item.getBookmark());

		ImageButton play = (ImageButton) findViewById(R.id.play);
		play.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.start();
			}
		});

		ImageButton pause = (ImageButton) findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.pause();
			}
		});

		ImageButton stop = (ImageButton) findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.stop();
				try {
					mp.prepare();
				} catch (IllegalStateException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (IOException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		ImageButton rewind = (ImageButton) findViewById(R.id.rewind);
		rewind.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.seekTo(mp.getCurrentPosition()-SEEK_INC);
			}
		});

		ImageButton forward = (ImageButton) findViewById(R.id.forward);
		forward.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.seekTo(mp.getCurrentPosition()+SEEK_INC);
			}
		});

		final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
		seekbar.setMax(mp.getDuration());
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
				mp.seekTo(seekBar.getProgress());
			}
		});

		Thread progressThread = new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						sleep(UPDATE_DELAY);
						if(active){
							Message message = new Message();
							if(!tracking){
								seekbar.setProgress(mp.getCurrentPosition());
								message.obj = mp.getCurrentPosition();
							}else{
								message.obj = seekbar.getProgress();
							}
							durationHandler.sendMessage(message);
						}
					} catch (InterruptedException e) {
						Log.d(TAG, e.getMessage(), e);
					}
				}
			}
		};
//		progressThread.setDaemon(true);
		progressThread.start();
	}
	
	private Handler durationHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			String dur = Util.convertDuration((Integer)msg.obj);
			dur += "/"+Util.convertDuration(mp.getDuration());
			duration.setText(dur);
		}
	};
	
	// state methods:

	@Override
	protected void onPause() {
		super.onPause(); // called after onSaveInstanceState, calls onStop
		saveState();
	}
	
	@Override
	protected void onStop() {
		super.onStop(); //called after onPause
//		mDbHelper.close();
	}

	@Override
	protected void onResume() {
		super.onResume(); // called after onCreate
		active = true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//saveState(); onPause is called after this
	}
	
	private void saveState(){
		active = false;
		item.setBookmark(mp.getCurrentPosition());
		if(mp.getCurrentPosition()+100 >= mp.getDuration()){
			item.setCompleted(true);
			mDbHelper.updateFeedItemCompleted(item.getId(), item
					.isCompleted());
		}
		mp.reset();
		mDbHelper.updateFeedItemBookmark(item.getId(), item.getBookmark());
		settings.setLastFeedItemId(item.getId());
		mDbHelper.updateSettings(settings);
	}

}
