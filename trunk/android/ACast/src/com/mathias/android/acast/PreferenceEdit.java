package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.mathias.android.acast.podcast.Settings;

public class PreferenceEdit extends PreferenceActivity {

	private static final String TAG = PreferenceEdit.class.getSimpleName();

	private static final List<SettingsItem> items = new ArrayList<SettingsItem>();
	
	static {
		items.add(new SettingsItem(Settings.ONLYWIFIDOWNLOAD, "Only Wifi download", "Only automatic download through Wifi"));
		items.add(new SettingsItem(Settings.ONLYWIFISTREAM, "Only Wifi stream", "Only stream audio through Wifi"));
		items.add(new SettingsItem(Settings.AUTODELETE, "Auto delete", "Downloaded items are automatically deleted during feed refresh"));
		items.add(new SettingsItem(Settings.AUTOREFRESH, "Auto refresh", "Auto refresh all feeds at spec time (hourly, daily, week, month)"));
		items.add(new SettingsItem(Settings.AUTODOWNLOAD, "Auto download", "Download all feeds during auto refresh"));
		items.add(new SettingsItem(Settings.RESUMEPARTLYDOWNLOADED, "Resume partly downloaded", "Resume partly downloaded files"));
		items.add(new SettingsItem(Settings.AUTODELETECOMPLETED, "Auto delete after played", "Auto delete on completion played"));
		items.add(new SettingsItem(Settings.OPENINFORMATION, "Open information", "Open information as default instead of media player."));
		items.add(new SettingsItem(Settings.AUTOPLAYNEXT, "Auto queue next", "Auto queue next feed items after a feed item is completed."));
		items.add(new SettingsItem(Settings.AUTOPLAYNEXT_DOWNLOADED, "Only auto queue downloaded", "Only auto queue downloaded feed items."));
		items.add(new SettingsItem(Settings.AUTOPLAYNEXT_COMPLETED, "Only auto queue completed", "Only auto queue downloaded feed items."));
		items.add(new SettingsItem(Settings.PLAYERINLANDSCAPE, "Player in landscape mode", "Always show player in landscape mode."));
		Collections.sort(items);
	}

	private ACastDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		ACastUtil.customTitle(this, "Settings", R.layout.settings_edit);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		// Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		Log.d(TAG, "onPreferenceTreeClick()");
		// TODO Auto-generated method stub
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}

	private static class SettingsItem implements Comparable<SettingsItem> {
		Settings type;
		private String title;
		private String description;

		public SettingsItem(Settings type, String title, String description){
			this.type = type;
			this.title = title;
			this.description = description;
		}

		@Override
		public int compareTo(SettingsItem arg0) {
			return arg0.title.compareTo(title);
		}
	}

}
