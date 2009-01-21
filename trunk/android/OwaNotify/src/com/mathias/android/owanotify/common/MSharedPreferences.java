package com.mathias.android.owanotify.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class MSharedPreferences {
	
	private Context cxt;

	private SharedPreferences prefs;
	
	public MSharedPreferences(Context cxt){
		this.cxt = cxt;

		prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
	}

	public String getString(int resid){
		return prefs.getString(cxt.getString(resid), null);
	}

	public String getString(int resid, String defValue){
		return prefs.getString(cxt.getString(resid), defValue);
	}

	public boolean getBool(int resid){
		return prefs.getBoolean(cxt.getString(resid), false);
	}

	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener){
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

}
