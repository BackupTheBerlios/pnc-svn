package com.mathias.android.acast;

import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.common.services.update.UpdateService;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class ACastMenu extends Activity {

	private static final String TAG = ACastMenu.class.getSimpleName();
	
	private static final int SETTINGS_ID = Menu.FIRST+0;

	private ACastDbAdapter mDbHelper;

	private IMediaService mediaBinder;
	
	private ServiceConnection mediaServiceConn;
	
	private TextView resumetitle;

	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		//since indeterminate progress is used custom title is unavailable
		setContentView(R.layout.menu);
		setTitle("Menu");

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		resumetitle = (TextView) findViewById(R.id.resumetitle);

		Intent i = new Intent(this, MediaService.class);
		startService(i);
		mediaServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				mediaBinder = IMediaService.Stub.asInterface(service);
				populateView();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: "+name);
				mediaBinder = null;
			}
		};
		if(!bindService(i, mediaServiceConn, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start media service!");
		}

		// LIBRARY
		((ImageButton) findViewById(R.id.library))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, FeedList.class);
						startActivity(i);
					}
				});

		// PLAYLIST
		((ImageButton) findViewById(R.id.playlist))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, PlayList.class);
						startActivity(i);
					}
				});

		// DOWNLOADED
		((ImageButton) findViewById(R.id.downloaded))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, DownloadedList.class);
						startActivity(i);
					}
				});

		// DOWNLOADQUEUE
		((ImageButton) findViewById(R.id.queue))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, DownloadQueueList.class);
						startActivity(i);
					}
				});

		// SEARCH
		((ImageButton) findViewById(R.id.search))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, FeedAdd.class);
						startActivity(i);
					}
				});

		// PLAYER
		((ImageButton) findViewById(R.id.player))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, Player.class);
						startActivity(i);
					}
				});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		populateView();
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		if(mediaBinder != null){
			unbindService(mediaServiceConn);
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, R.string.settings);
		item.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(SETTINGS_ID == item.getItemId()){
			Intent i = new Intent(this, PreferenceEdit.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private OnSharedPreferenceChangeListener prefsListener = new OnSharedPreferenceChangeListener(){
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Log.d(TAG, "onSharedPreferenceChanged: "+key);
			String schedkey = getString(R.string.SCHEDULEDUPDATE_key);
			if(schedkey.equals(key)){
				String schedup = prefs.getString(schedkey, "0");
				int hours = Integer.parseInt(schedup);

				Intent i = new Intent(ACastMenu.this, UpdateService.class);
				i.putExtra(Constants.ALARM, 1);
				PendingIntent mAlarmSender = PendingIntent.getService(
						ACastMenu.this, 0, i, 0);

				AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
				if(hours > 0){
					long interval = hours * 3600000;
		            long firstTime = System.currentTimeMillis();
					Log.d(TAG, "setRepeating: "+new Date(firstTime+interval));
		            am = (AlarmManager)getSystemService(ALARM_SERVICE);
		            am.setRepeating(AlarmManager.RTC, firstTime+interval, interval,
							mAlarmSender);
				}else{
					//Cancel alarm
					Log.d(TAG, "Cancel alarm");
		            am.cancel(mAlarmSender);
				}
			}
		}
	};

	private void populateView(){
		FeedItem item = null;
		try {
			if(mediaBinder != null && mDbHelper != null){
				long id = mediaBinder.getId();
				if(id >= 0){
					item = mDbHelper.fetchFeedItem(id);
				}
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		if (item == null) {
			String lastid = mDbHelper
					.getSetting(Settings.LASTFEEDITEMID);
			if (lastid != null) {
				item = mDbHelper.fetchFeedItem(Long.parseLong(lastid));
			}
		}
		if (item != null) {
			resumetitle.setText(item.title);
		} else {
			Log.w(TAG, "No resume item...");
		}
	}

}
