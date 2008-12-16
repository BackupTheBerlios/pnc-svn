package com.mathias.android.acast;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
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

public class Player extends Activity implements ServiceConnection {

	private static final String TAG = Player.class.getSimpleName();

	private static final int VOLUME_MAX = 15;

	private static final long UPDATE_DELAY = 300;

	private Boolean tracking = false;

	private ACastDbAdapter mDbHelper;

	private FeedItem item;

	private Settings settings;

	private TextView duration;

	private boolean active = false;
	
	private IMediaService binder;

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

//		Intent i = new Intent(Player.this, MediaService.class);
		Intent i = new Intent();
		i.setClassName(MediaService.class.getPackage().getName(),
				MediaService.class.getName());
//		i.putExtra(ACast.FEEDITEM, item);
//		startService(i);
		
//		if(!stopService(i)){
//			Util.showDialog(Player.this, "Could not stop media service!");
//		}
		if(!bindService(i, Player.this, BIND_AUTO_CREATE)){
			Util.showDialog(Player.this, "Could not connect to media service!");
		}

		ImageButton play = (ImageButton) findViewById(R.id.play);
		play.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(binder != null){
						binder.play();
					}
				}catch(DeadObjectException e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (RemoteException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		ImageButton pause = (ImageButton) findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(binder != null){
						binder.pause();
					}
				}catch(DeadObjectException e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (RemoteException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		ImageButton stop = (ImageButton) findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(binder != null){
						binder.stop();
					}
				}catch(DeadObjectException e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (RemoteException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		ImageButton rewind = (ImageButton) findViewById(R.id.rewind);
		rewind.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(binder != null){
						binder.rewind();
					}
				}catch(DeadObjectException e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (RemoteException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		ImageButton forward = (ImageButton) findViewById(R.id.forward);
		forward.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					if(binder != null){
						binder.forward();
					}
				}catch(DeadObjectException e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (RemoteException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		ImageButton eject = (ImageButton) findViewById(R.id.eject);
		eject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					int pid = binder.getPid();
					Process.killProcess(pid);
				}catch(DeadObjectException e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (RemoteException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
			}
		});

		final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
		try{
			if(binder != null){
				seekbar.setMax(binder.getDuration());
			}
		}catch(DeadObjectException e){
			Log.e(TAG, item.getMp3file(), e);
			Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
		} catch (RemoteException e) {
			Log.e(TAG, item.getMp3file(), e);
			Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
		}
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
				}catch(DeadObjectException e){
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				} catch (RemoteException e) {
					Log.e(TAG, item.getMp3file(), e);
					Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
				}
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
							if(!tracking && binder != null){
								int pos = binder.getCurrentPosition();
								seekbar.setProgress(pos);
								message.obj = pos;
							}else{
								message.obj = seekbar.getProgress();
							}
							durationHandler.sendMessage(message);
						}
					} catch (InterruptedException e) {
						Log.d(TAG, e.getMessage(), e);
					}catch(DeadObjectException e){
						Log.d(TAG, e.getMessage(), e);
					} catch (RemoteException e) {
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
			try{
				if(binder != null){
					dur += "/"+Util.convertDuration(binder.getDuration());
				}
			}catch(DeadObjectException e){
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
			} catch (RemoteException e) {
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
			}
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
	
	private void saveState() {
		active = false;
		settings.setLastFeedItemId(item.getId());
		mDbHelper.updateSettings(settings);
		unbindService(this);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.e(TAG, "onServiceConnected: "+name);
		binder = IMediaService.Stub.asInterface(service);
		try {
			binder.playFeedItem((int)item.getId());
		}catch(DeadObjectException e){
			Log.e(TAG, item.getMp3file(), e);
			Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
		} catch (RemoteException e) {
			Log.e(TAG, item.getMp3file(), e);
			Util.showDialog(Player.this, e.getMessage()+": "+item.getMp3uri());
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.e(TAG, "onServiceDisconnected: "+name);
		binder = null;
	}

}
