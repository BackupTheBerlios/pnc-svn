package com.mathias.android.owanotify.common;

import java.util.Map;

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

	public int getInt(int resid){
		return prefs.getInt(cxt.getString(resid), 0);
	}

	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener){
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener){
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public Map<String, ?> getAll(){
		return prefs.getAll();
	}

	public SharedPreferences getPrefs(){
		return prefs;
	}

}
