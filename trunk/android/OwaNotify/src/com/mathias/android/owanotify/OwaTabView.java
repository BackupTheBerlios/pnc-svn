package com.mathias.android.owanotify;

import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.mathias.android.owanotify.common.MSharedPreferences;

public class OwaTabView extends TabActivity {
	
	private static final String TAG = OwaTabView.class.getSimpleName();

	private static final int SETTINGS_ID = Menu.FIRST+0;

	private MSharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = new MSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("Mail").setIndicator("Mail")
				.setContent(
						new Intent(this, OwaMailView.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

		tabHost.addTab(tabHost.newTabSpec("Calendar").setIndicator("Calendar")
				.setContent(
						new Intent(this, OwaCalendarView.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, "Settings");
		item.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(SETTINGS_ID == item.getItemId()){
			Intent i = new Intent(this, SettingEdit.class);
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
			if(getString(R.string.frequency_key).equals(key)){
				String schedup = prefs.getString(R.string.frequency_key, "0");
				int minutes = Integer.parseInt(schedup);

				PendingIntent alarmSender = PendingIntent.getService(OwaTabView.this,
		                0, new Intent(OwaTabView.this, OwaService.class), 0);

				AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
				if(minutes > 0){
					long interval = minutes * 60000;
		            long firstTime = System.currentTimeMillis();
					Log.d(TAG, "setRepeating: "+new Date(firstTime+interval));
		            am = (AlarmManager)getSystemService(ALARM_SERVICE);
		            am.setRepeating(AlarmManager.RTC, firstTime+interval, interval,
							alarmSender);
				}else{
					//Cancel alarm
					Log.d(TAG, "Cancel alarm");
		            am.cancel(alarmSender);
				}
			}else if(getString(R.string.internalviewer_key).equals(key)){
				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				nm.cancel(OwaService.NOTIFICATION_INBOX_ID);
				nm.cancel(OwaService.NOTIFICATION_CALENDAR_ID);
			}
		}
	};

}
