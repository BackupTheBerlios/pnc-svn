package com.mathias.android.acast;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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

		if(item.getBookmark() > 0){
			TextView bookmark = (TextView) findViewById(R.id.bookmark);
			bookmark.setText(getString(R.string.bookmark)+Util.convertDuration(item.getBookmark()));
		}

		String desc = item != null && item.getDescription() != null ? item
				.getDescription() : "";
        TextView description = (TextView) findViewById(R.id.description);
        description.setText(Html.fromHtml(desc, Util.NULLIMAGEGETTER, null));
	}

}
