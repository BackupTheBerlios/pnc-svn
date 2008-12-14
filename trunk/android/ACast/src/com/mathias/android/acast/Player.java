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
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class Player extends Activity {

	private static final String TAG = Player.class.getSimpleName();
	
	private static final int VOLUME_INC = 1;
	
	private static final int VOLUME_MAX = 15;
	
	private static final int SEEK_INC = 30000;
	
	private static final long UPDATE_DELAY = 300;

	private Boolean tracking = false;

	private MediaPlayer mp;

	private ACastDbAdapter mDbHelper;

	private FeedItem item;

	private Settings settings;
	
	private TextView duration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		settings = mDbHelper.fetchSettings();
		if(settings == null){
			settings = new Settings(VOLUME_MAX);
		}

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
				File f = new File(getFilesDir()+File.separator+file);
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
		
		mp.setVolume(settings.getVolume(), settings.getVolume());
		mp.seekTo(item.getBookmark());

		Button play = (Button) findViewById(R.id.play);
		play.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.start();
			}
		});

		Button pause = (Button) findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.pause();
			}
		});

		Button reset = (Button) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.reset();
			}
		});

		Button stop = (Button) findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.stop();
			}
		});

		Button rewind = (Button) findViewById(R.id.rewind);
		rewind.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.seekTo(mp.getCurrentPosition()-SEEK_INC);
			}
		});

		Button forward = (Button) findViewById(R.id.forward);
		forward.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mp.seekTo(mp.getCurrentPosition()+SEEK_INC);
			}
		});

		Button volup = (Button) findViewById(R.id.volup);
		volup.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				int volume = settings.getVolume();
				volume += VOLUME_INC;
				if(volume > VOLUME_MAX){
					volume = VOLUME_MAX;
				}
				mp.setVolume(volume, volume);
				settings.setVolume(volume);
			}
		});

		Button voldown = (Button) findViewById(R.id.voldown);
		voldown.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				int volume = settings.getVolume();
				volume -= VOLUME_INC;
				if(volume < 0){
					volume = 0;
				}
				mp.setVolume(volume, volume);
				settings.setVolume(volume);
			}
		});

		final SeekBar bar = (SeekBar) findViewById(R.id.seekbar);
		bar.setMax(mp.getDuration());
		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
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
		
		new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						sleep(UPDATE_DELAY);
						Message message = new Message();
						if(!tracking){
							bar.setProgress(mp.getCurrentPosition());
							message.obj = mp.getCurrentPosition();
						}else{
							message.obj = bar.getProgress();
						}
						durationHandler.sendMessage(message);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();

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
		super.onPause();
		saveState();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	private void saveState(){
		item.setBookmark(mp.getCurrentPosition());
		mp.reset();
		mDbHelper.updateFeedItem(item);
		mDbHelper.updateSettings(settings);
	}

}
