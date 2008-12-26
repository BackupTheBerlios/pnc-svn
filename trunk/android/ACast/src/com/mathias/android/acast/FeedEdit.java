package com.mathias.android.acast;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.rss.RssUtil;

public class FeedEdit extends Activity {
	
	private static final String TAG = FeedEdit.class.getSimpleName();

	private ACastDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_edit);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		final EditText urlText = (EditText) findViewById(R.id.url);
		ImageButton confirm = (ImageButton) findViewById(R.id.add);

		urlText.setText("http://");

		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = urlText.getText().toString();
				try {
					Feed feed = new RssUtil().parse(uri);
					mDbHelper.createFeed(feed);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
					Util.showDialog(FeedEdit.this, e.getMessage());
				}
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}

}
