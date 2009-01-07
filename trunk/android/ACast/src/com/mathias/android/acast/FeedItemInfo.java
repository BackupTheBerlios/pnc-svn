package com.mathias.android.acast;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.FeedItem;

public class FeedItemInfo extends Activity {

	private static final String TAG = FeedItemInfo.class.getSimpleName();
	
	private FeedItem item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "Information", R.layout.feeditem_info);

		item = (FeedItem) (savedInstanceState != null ? savedInstanceState
				.getSerializable(Constants.FEEDITEM) : null);
		if (item == null) {
			Bundle extras = getIntent().getExtras();
			item = (FeedItem) (extras != null ? extras.getSerializable(Constants.FEEDITEM)
					: null);
		}
		
		if(item == null){
			Log.e(TAG, "No feed item found!");
			return;
		}

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(item.title);

		TextView uri = (TextView) findViewById(R.id.uri);
		uri.setText(item.mp3uri);

		int bookmarkVal = item.bookmark;
		TextView bookmark = (TextView) findViewById(R.id.bookmark);
		if(bookmarkVal > 0){
			bookmark.setText(getString(R.string.bookmark)+Util.convertDuration(bookmarkVal));
		}else{
			bookmark.setVisibility(View.GONE);
		}

		String linkVal = item.link;
		TextView link = (TextView) findViewById(R.id.link);
		if(linkVal != null && !linkVal.equalsIgnoreCase(item.mp3uri)){
			link.setText(linkVal);
		}else{
			link.setVisibility(View.GONE);
		}

		String pubdateVal = new Date(item.pubdate).toString();
		TextView pubdate = (TextView) findViewById(R.id.pubdate);
		if(item.pubdate != 0){
			pubdate.setText(pubdateVal);
		}else{
			pubdate.setVisibility(View.GONE);
		}

		String categoryVal = item.category;
		TextView category = (TextView) findViewById(R.id.category);
		if(categoryVal != null){
			category.setText(categoryVal);
		}else{
			category.setVisibility(View.GONE);
		}

		if(item.mp3file != null){
			long fileSize = new File(item.mp3file).length();
			String sizeVal = "Size: "+fileSize+"/"+item.size;
			TextView size = (TextView) findViewById(R.id.size);
			size.setText(sizeVal);
		}

		String descVal = item.description;
        TextView description = (TextView) findViewById(R.id.description);
        if(descVal != null){
            description.setText(Util.fromHtmlNoImages(descVal));
        }else{
            description.setText("No description...");
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(0, 0, 0, R.string.gotolink);
		item.setIcon(android.R.drawable.ic_menu_set_as);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem mitem) {
		if(item != null && item.link != null){
			Util.openBrowser(this, item.link);
			return true;
		}
		return super.onMenuItemSelected(featureId, mitem);
	}

}
