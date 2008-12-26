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
import android.text.Html;
import android.util.Log;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
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
			String desc = item != null && item.getDescription() != null ? item
					.getDescription() : "";
	        TextView description = (TextView) findViewById(R.id.description);
	        description.setText(Html.fromHtml(desc, Util.NULLIMAGEGETTER, null));

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
							mDbHandler.updateFeedItem(item.getId(),
									ACastDbAdapter.FEEDITEM_BOOKMARK, binder
											.getCurrentPosition());
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
						mDbHandler.updateFeedItem(item.getId(),
								ACastDbAdapter.FEEDITEM_BOOKMARK, binder
										.getCurrentPosition());
						binder.stop();
						finish();
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
					dur += "/"+Util.convertDuration(binder.getDuration());
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
		Log.d(TAG, "onPause");
		super.onPause();
		if(binder != null){
			unbindService(this);
			binder = null;
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy: isFinishing="+isFinishing());
		mDbHandler.close();
		mDbHandler = null;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		try {
			if(binder != null){
				if(!binder.isPlaying()){
					if(item.isDownloaded()){
						binder.initItem(item.getId(), item.getMp3file(), false);
					}else{
						binder.initItem(item.getId(), item.getMp3uri(), true);
					}
					binder.setCurrentPosition(item.getBookmark());
					binder.play();
					playpause.setImageResource(R.drawable.pause);
					//playpause.setImageResource(android.R.drawable.ic_media_pause);
				}else{
					playpause.setImageResource(R.drawable.play);
					//playpause.setImageResource(android.R.drawable.ic_media_play);
				}
				seekbar.setMax(binder.getDuration());
			}
		} catch (Exception e) {
			Log.e(TAG, item.getMp3file(), e);
			Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+name);
		binder = IMediaService.Stub.asInterface(service);
		try {
			if(item == null){
				long id = binder.getExternalId();
				item = mDbHandler.fetchFeedItem(id);
				TextView title = (TextView) findViewById(R.id.title);
				title.setText(item.getTitle());
			}
			if(item == null){
				return;
			}
			binder.registerCallback(mCallback);
			if (binder.isPlaying()
					&& ((item.isDownloaded() && binder.getLocator().equals(
							item.getMp3file())) || (!item.isDownloaded() && binder
							.getLocator().equals(item.getMp3uri())))) {
				Log.d(TAG, "Already playing: " + item.getMp3uri());
			} else {
				mDbHandler.updateFeedItem(binder.getExternalId(),
						ACastDbAdapter.FEEDITEM_BOOKMARK, binder
								.getCurrentPosition());
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
			seekbar.setMax(binder.getDuration());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

    private IMediaServiceCallback mCallback = new IMediaServiceCallback.Stub() {
		@Override
		public void onCompletion() throws RemoteException {
			if (item != null && mDbHandler != null) {
				int currentPosition = binder.getCurrentPosition();
				if (currentPosition + 100 >= binder.getDuration()) {
					item.setCompleted(true);
					item.setBookmark(0);
				}else{
					item.setBookmark(currentPosition);
				}
				mDbHandler.updateFeedItem(item);
			}
			Player.this.finish();
		}
	};

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected: "+name);
	}

}
