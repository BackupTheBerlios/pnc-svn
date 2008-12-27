package com.mathias.android.acast;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.FeedItem;

public class FeedItemInfo extends Activity {

	private static final String TAG = FeedItemInfo.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feeditem_info);

		FeedItem item = (FeedItem) (savedInstanceState != null ? savedInstanceState
				.getSerializable(ACast.FEEDITEM) : null);
		if (item == null) {
			Bundle extras = getIntent().getExtras();
			item = (FeedItem) (extras != null ? extras.getSerializable(ACast.FEEDITEM)
					: null);
		}
		
		if(item == null){
			Log.e(TAG, "No feed item found!");
			return;
		}

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(item.getTitle());

		TextView uri = (TextView) findViewById(R.id.uri);
		uri.setText(item.getMp3uri());

		int bookmarkVal = item.getBookmark();
		TextView bookmark = (TextView) findViewById(R.id.bookmark);
		if(bookmarkVal > 0){
			bookmark.setText(getString(R.string.bookmark)+Util.convertDuration(bookmarkVal));
		}else{
			bookmark.setVisibility(View.GONE);
		}

		String linkVal = item.getLink();
		TextView link = (TextView) findViewById(R.id.link);
		if(linkVal != null && !linkVal.equalsIgnoreCase(item.getMp3uri())){
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

		long fileSize = new File(item.getMp3file()).length();
		String sizeVal = "Size: "+fileSize+"/"+item.getSize();
		TextView size = (TextView) findViewById(R.id.size);
		size.setText(sizeVal);

		String descVal = item.getDescription();
        TextView description = (TextView) findViewById(R.id.description);
        if(descVal != null){
            description.setText(Html.fromHtml(descVal, Util.NULLIMAGEGETTER, null));
        }else{
            description.setText("No description...");
        }
	}

}
