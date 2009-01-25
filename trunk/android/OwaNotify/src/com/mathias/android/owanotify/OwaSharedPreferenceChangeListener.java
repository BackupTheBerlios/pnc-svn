package com.mathias.android.owanotify;

import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import com.mathias.android.owanotify.common.MSharedPreferences;

public class OwaSharedPreferenceChangeListener implements OnSharedPreferenceChangeListener {
	
	private static final String TAG = OwaSharedPreferenceChangeListener.class.getSimpleName();
	
	private Context cxt;
	
	private MSharedPreferences prefs;
	
	public OwaSharedPreferenceChangeListener(Context cxt, MSharedPreferences prefs){
		this.cxt = cxt;
        this.prefs = prefs;
	}

	@Override
	public void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, "onSharedPreferenceChanged: "+key);
		if(cxt.getString(R.string.frequency_key).equals(key)){
			String schedup = prefs.getString(R.string.frequency_key, "0");
			int minutes = Integer.parseInt(schedup);

			PendingIntent alarmSender = PendingIntent.getService(cxt,
	                0, new Intent(cxt, OwaService.class), 0);

			AlarmManager am = (AlarmManager)cxt.getSystemService(Context.ALARM_SERVICE);
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
		}else if(cxt.getString(R.string.internalviewer_key).equals(key)){
			NotificationManager nm = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(OwaService.NOTIFICATION_INBOX_ID);
			nm.cancel(OwaService.NOTIFICATION_CALENDAR_ID);
		}
	}

}
