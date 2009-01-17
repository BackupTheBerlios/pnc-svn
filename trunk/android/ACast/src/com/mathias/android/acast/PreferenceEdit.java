package com.mathias.android.acast;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferenceEdit extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		ACastUtil.customTitle(this, "Settings", R.layout.settings_edit);

		// Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
	}

}
