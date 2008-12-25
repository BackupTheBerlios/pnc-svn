package com.mathias.android.acast;

import java.security.GuardedObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;

public class FeedInfo extends Activity {

	private static final String TAG = FeedInfo.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_info);

		Feed item = (Feed) (savedInstanceState != null ? savedInstanceState
				.getSerializable(ACast.FEED) : null);
		if (item == null) {
			Bundle extras = getIntent().getExtras();
			item = (Feed) (extras != null ? extras.getSerializable(ACast.FEED)
					: null);
		}
		
		if(item == null){
			Log.e(TAG, "No feed found!");
			return;
		}

		String iconVal = item.getIcon();
		ImageView icon = (ImageView) findViewById(R.id.icon);
		if(iconVal != null){
			Bitmap bitmap = BitmapFactory.decodeFile(iconVal);
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
            description.setText(Html.fromHtml(descVal, Util.NULLIMAGEGETTER, null));
        }else{
            description.setText("No description...");
        }
	}

}