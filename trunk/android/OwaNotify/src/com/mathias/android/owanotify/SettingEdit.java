package com.mathias.android.owanotify;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingEdit extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

}
