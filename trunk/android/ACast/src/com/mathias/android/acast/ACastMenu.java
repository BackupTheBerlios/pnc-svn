package com.mathias.android.acast;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class ACastMenu extends Activity {

	private static final String TAG = ACastMenu.class.getSimpleName();
	
	private static final int SETTINGS_ID = 0;

	private ACastDbAdapter mDbHelper;

	private IMediaService mediaBinder;
	
	private ServiceConnection mediaServiceConn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.menu);
		
		setTitle("Menu");

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		Intent i = new Intent(this, MediaService.class);
		startService(i);
		mediaServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				mediaBinder = IMediaService.Stub.asInterface(service);
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
		((Button) findViewById(R.id.library))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, FeedList.class);
						startActivity(i);
					}
				});

		// PLAYLIST
		((Button) findViewById(R.id.playlist))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, PlayList.class);
						startActivity(i);
					}
				});

		// DOWNLOADED
		((Button) findViewById(R.id.downloaded))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, DownloadedList.class);
						startActivity(i);
					}
				});

		// DOWNLOADQUEUE
		((Button) findViewById(R.id.queue))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, DownloadQueueList.class);
						startActivity(i);
					}
				});

		// SEARCH
		((Button) findViewById(R.id.search))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, FeedAdd.class);
						startActivity(i);
					}
				});

		// PLAYER
		((Button) findViewById(R.id.player))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(ACastMenu.this, Player.class);
						startActivity(i);
					}
				});

		// RESUME
		ImageButton resume = (ImageButton) findViewById(R.id.resume);
		resume.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String lastid = mDbHelper.getSetting(Settings.SettingEnum.LASTFEEDITEMID);
				if(lastid != null){
					FeedItem item = mDbHelper.fetchFeedItem(Long.parseLong(lastid));
					ACastUtil.resumeItem(mediaBinder, item);
					Intent i = new Intent(ACastMenu.this, Player.class);
					startActivity(i);
				}
			}
		});
		String lastid = mDbHelper.getSetting(Settings.SettingEnum.LASTFEEDITEMID);
		if(lastid != null){
			FeedItem item = mDbHelper.fetchFeedItem(Long.parseLong(lastid));
			if(item != null){
				TextView resumetitle = (TextView) findViewById(R.id.resumetitle);
				resumetitle.setText(item.getTitle());
				TextView resumedesc = (TextView) findViewById(R.id.resumedesc);
				resumedesc.setText(item.getAuthor());
			}else{
				Log.w(TAG, "No resume item for lastid="+lastid);
			}
		}

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
			Intent i = new Intent(this, SettingsEdit.class);
			startActivity(i);
		}
		//return super.onOptionsItemSelected(item);
		return true;
	}

}
