package com.mathias.android.owanotify;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class OwaTabView extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

}
