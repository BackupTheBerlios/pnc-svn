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

	private Boolean tracking = false;

	private FeedItem item;

	private TextView duration;

	private IMediaService binder;

	private SeekBar seekbar;

	private ImageButton playpause;

	private ACastDbAdapter mDbHandler;
	
	private int itemDuration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate "+getTaskId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		
		mDbHandler = new ACastDbAdapter(this);
		mDbHandler.open();

		item = (FeedItem) (savedInstanceState != null ? savedInstanceState
				.getSerializable(ACast.FEEDITEM) : null);
		if (item == null) {
			Bundle extras = getIntent().getExtras();
			item = (FeedItem) (extras != null ? extras.getSerializable(ACast.FEEDITEM)
					: null);
		}

		TextView title = (TextView) findViewById(R.id.title);

		duration = (TextView) findViewById(R.id.duration);

		if(item != null){
	        title.setText(item.getTitle());
		}else{
			title.setText("No item!");
		}

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
					if(binder != null){
						if(binder.isPlaying()){
							binder.pause();
							playpause.setImageResource(R.drawable.play);
							//playpause.setImageResource(android.R.drawable.ic_media_play);
						}else{
							binder.play();
							playpause.setImageResource(R.drawable.pause);
							//playpause.setImageResource(android.R.drawable.ic_media_pause);
						}
					}
				}catch(Exception e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		// STOP
		ImageButton stop = (ImageButton) findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(binder != null){
						binder.stop();
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
					if(binder != null){
						binder.seek(SEEK_REW);
					}
				}catch(Exception e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		// FORWARD
		ImageButton forward = (ImageButton) findViewById(R.id.forward);
		forward.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(binder != null){
						binder.seek(SEEK_FOR);
					}
				}catch(Exception e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
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
					if(binder != null){
						binder.setCurrentPosition(seekBar.getProgress());
					}
				}catch(Exception e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
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
		Util.showDialog(this, Util.fromHtmlNoImages(item.getTitle()), Util
				.fromHtmlNoImages(item.getDescription()));
		return true;
	}

	private Handler progressHandler = new Handler(){
		@Override
		public void handleMessage(Message not_used) {
			try {
				int pos;
				if(!tracking && binder != null){
					pos = binder.getCurrentPosition();
					seekbar.setProgress(pos);
				}else{
					pos = seekbar.getProgress();
				}
				String dur = Util.convertDuration(pos);
				if(binder != null){
					dur += "/"+Util.convertDuration(itemDuration);
					duration.setText(dur);
				}
			}catch(Exception e){
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
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
		if(binder != null){
			unbindService(this);
			binder = null;
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
			if(binder != null){
				if(!binder.isPlaying()){
					playpause.setImageResource(R.drawable.play);
					//playpause.setImageResource(android.R.drawable.ic_media_play);
				}else{
					playpause.setImageResource(R.drawable.pause);
					//playpause.setImageResource(android.R.drawable.ic_media_pause);
				}
				itemDuration = binder.getDuration();
				seekbar.setMax(itemDuration);
			}else{
				Log.w(TAG, "binder is null");
			}
		} catch (Exception e) {
			Log.e(TAG, item.getMp3file(), e);
			Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+getTaskId()+" "+name);
		binder = IMediaService.Stub.asInterface(service);
		try {
			if(item == null){
				long id = binder.getExternalId();
				item = mDbHandler.fetchFeedItem(id);
			}
			if(item == null){
				return;
			}
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(item.getTitle());

			binder.registerCallback(mCallback);

			if (binder.isPlaying() && binder.getExternalId() == item.getId()) {
				Log.d(TAG, "Already playing: " + item.getMp3uri());
			} else {
				if (item.isDownloaded()) {
					binder.initItem(item.getId(), item.getMp3file(),
							false);
				} else {
					binder.initItem(item.getId(), item.getMp3uri(),
							true);
				}
				binder.setCurrentPosition(item.getBookmark());
				binder.play();
			}
			playpause.setImageResource(R.drawable.pause);
			//playpause.setImageResource(android.R.drawable.ic_media_pause);
			itemDuration = binder.getDuration();
			seekbar.setMax(itemDuration);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

    private IMediaServiceCallback mCallback = new IMediaServiceCallback.Stub() {
		@Override
		public void onCompletion() throws RemoteException {
			Player.this.finish();
			//TODO 5: delete file on complete depending on settings
		}
	};

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected: "+getTaskId()+" "+name);
	}

}
