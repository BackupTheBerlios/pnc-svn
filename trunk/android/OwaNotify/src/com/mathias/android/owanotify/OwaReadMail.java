package com.mathias.android.owanotify;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mathias.android.owanotify.OwaParser.OwaInboxItem;
import com.mathias.android.owanotify.common.MSharedPreferences;

public class OwaReadMail extends Activity {

	public static final String EMAIL = "EMAIL";
	
	private static final int OPENINBROWSER_ID = Menu.FIRST +0;
	
	private MSharedPreferences prefs;

	private OwaInboxItem item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.reademail);

        prefs = new MSharedPreferences(this);

		item = (OwaInboxItem) (savedInstanceState != null ? savedInstanceState
				.getSerializable(EMAIL)
				: null);

		if (item == null) {
			Bundle extras = getIntent().getExtras();
			item = (OwaInboxItem) (extras != null ? extras
					.getSerializable(EMAIL) : null);
		}

		if (item != null) {
			TextView from = (TextView) findViewById(R.id.from);
			from.setText(item.from);
			TextView subject = (TextView) findViewById(R.id.subject);
			subject.setText(item.subject);
			TextView date = (TextView) findViewById(R.id.date);
			date.setText(item.date);
			TextView text = (TextView) findViewById(R.id.text);
			text.setText(Html.fromHtml(item.text, null, null));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.NONE, OPENINBROWSER_ID, Menu.NONE, "Open in browser");
		item.setIcon(android.R.drawable.ic_menu_set_as);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mitem) {
		if(OPENINBROWSER_ID == mitem.getItemId()){
    		String fullurl = OwaUtil.getFullUrl(prefs, item.url);
    		startActivity(new Intent("android.intent.action.VIEW", Uri.parse(fullurl)));			
			return true;
		}
		return super.onOptionsItemSelected(mitem);
	}

}
