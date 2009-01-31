package com.mathias.android.owanotify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mathias.android.owanotify.OwaParser.OwaInboxItem;
import com.mathias.android.owanotify.common.MSharedPreferences;

public class OwaReadMail extends Activity {

	public static final String EMAIL = "EMAIL";
	
	private static final int OPENINBROWSER_ID = Menu.FIRST +0;
	private static final int TOGGLEBG_ID = Menu.FIRST +1;
	
	private MSharedPreferences prefs;

	private OwaInboxItem item;
	
	private ScrollView scroll;
	
	private int bgc = Color.BLACK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.reademail);

		scroll = (ScrollView) findViewById(R.id.scroll);

		prefs = new MSharedPreferences(this);
		
		if(prefs.getBool(R.string.defaultwhitebg_key)){
			bgc = Color.WHITE;
			scroll.setBackgroundColor(bgc);
		}

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
		item = menu.add(Menu.NONE, TOGGLEBG_ID, Menu.NONE, "Toggle background");
		item.setIcon(android.R.drawable.ic_menu_revert);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mitem) {
		if(OPENINBROWSER_ID == mitem.getItemId()){
    		String fullurl = OwaUtil.getFullUrl(prefs, item.url);
    		startActivity(new Intent("android.intent.action.VIEW", Uri.parse(fullurl)));			
			return true;
		}else if(TOGGLEBG_ID == mitem.getItemId()){
			if(bgc == Color.BLACK){
				bgc = Color.WHITE;
			}else{
				bgc = Color.BLACK;
			}
			scroll.setBackgroundColor(bgc);
		}
		return super.onOptionsItemSelected(mitem);
	}

}
