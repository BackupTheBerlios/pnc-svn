package com.mathias.android.acast;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;

public class FeedInfo extends Activity {

	private static final String TAG = FeedInfo.class.getSimpleName();
	
	private Feed item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "Information", R.layout.feed_info);

		item = (Feed) (savedInstanceState != null ? savedInstanceState
				.getSerializable(Constants.FEED) : null);
		if (item == null) {
			Bundle extras = getIntent().getExtras();
			item = (Feed) (extras != null ? extras.getSerializable(Constants.FEED)
					: null);
		}
		
		if(item == null){
			Log.e(TAG, "No feed found!");
			return;
		}

		String iconVal = item.getIcon();
		ImageView icon = (ImageView) findViewById(R.id.icon);
		if(iconVal != null){
			Bitmap bitmap = BitmapCache.instance().get(iconVal);
			icon.setImageBitmap(bitmap);
		}else{
			icon.setVisibility(View.GONE);
		}

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(item.getTitle());

		TextView uri = (TextView) findViewById(R.id.uri);
		uri.setText(item.getUri());

		String linkVal = item.getLink();
		TextView link = (TextView) findViewById(R.id.link);
		if(linkVal != null && !linkVal.equalsIgnoreCase(item.getUri())){
			link.setText(linkVal);
		}else{
			link.setVisibility(View.GONE);
		}

		String pubdateVal = item.getPubdate();
		TextView pubdate = (TextView) findViewById(R.id.pubdate);
		if(pubdateVal != null){
			pubdate.setText(pubdateVal);
		}else{
			pubdate.setVisibility(View.GONE);
		}

		String categoryVal = item.getCategory();
		TextView category = (TextView) findViewById(R.id.category);
		if(categoryVal != null){
			category.setText(categoryVal);
		}else{
			category.setVisibility(View.GONE);
		}

		String descVal = item.getDescription();
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
		if(item != null && item.getLink() != null){
			Util.openBrowser(this, item.getLink());
			return true;
		}
		return super.onMenuItemSelected(featureId, mitem);
	}

}
