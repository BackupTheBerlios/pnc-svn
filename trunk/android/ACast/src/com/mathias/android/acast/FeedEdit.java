package com.mathias.android.acast;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.rss.RssUtil;

public class FeedEdit extends Activity {

	private EditText mUrlText;
	private Long mRowId = null;

	private ACastDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_edit);
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		mUrlText = (EditText) findViewById(R.id.url);
		Button confirm = (Button) findViewById(R.id.add);
		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(ACast.KEY) : null;
//		if (mRowId == null) {
//			Bundle extras = getIntent().getExtras();
//			mRowId = extras != null ? extras.getLong(ACast.KEY)
//					: null;
//		}

		if (mRowId != null) {
			Feed feed = mDbHelper.fetchFeed(mRowId);
			mUrlText.setText(feed.getUri());
		}

		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = mUrlText.getText().toString();
				Feed feed = new RssUtil().parse(uri);
				mDbHelper.createFeed(feed);
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ACast.KEY, mRowId);
	}

}
