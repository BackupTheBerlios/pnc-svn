package com.mathias.android.owanotify;

import java.util.Date;

import com.mathias.android.owanotify.common.MSharedPreferences;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingEdit extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = SettingEdit.class.getSimpleName();
	
	private MSharedPreferences prefs;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        
        prefs = new MSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

	@Override
	protected void onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, "onSharedPreferenceChanged: "+key);
		if(getString(R.string.frequency_key).equals(key)){
			String schedup = prefs.getString(R.string.frequency_key, "0");
			int minutes = Integer.parseInt(schedup);

			PendingIntent alarmSender = PendingIntent.getService(this,
	                0, new Intent(this, OwaService.class), 0);

			AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			if(minutes > 0){
				long interval = minutes * 60000;
	            long firstTime = System.currentTimeMillis();
				Log.d(TAG, "setRepeating: "+new Date(firstTime+interval));
	            am.setRepeating(AlarmManager.RTC, firstTime+interval, interval,
						alarmSender);
			}else{
				//Cancel alarm
				Log.d(TAG, "Cancel alarm");
	            am.cancel(alarmSender);
			}
		}else if(getString(R.string.internalviewer_key).equals(key)){
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(OwaService.NOTIFICATION_INBOX_ID);
			nm.cancel(OwaService.NOTIFICATION_CALENDAR_ID);
		}
	}

}
