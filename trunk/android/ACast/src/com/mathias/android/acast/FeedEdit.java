package com.mathias.android.acast;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class FeedEdit extends Activity {

	private EditText mTitleText;
	private EditText mUrlText;
	private Long mRowId = null;

	private ACastDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_edit);
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		mTitleText = (EditText) findViewById(R.id.title);
		mUrlText = (EditText) findViewById(R.id.url);
		Button confirm = (Button) findViewById(R.id.confirm);
		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(ACast.KEY) : null;
//		if (mRowId == null) {
//			Bundle extras = getIntent().getExtras();
//			mRowId = extras != null ? extras.getLong(ACast.KEY)
//					: null;
//		}

		populateFields();

		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor feed = mDbHelper.fetchFeed(mRowId);
			startManagingCursor(feed);
			mTitleText.setText(feed.getString(feed
					.getColumnIndexOrThrow(ACastDbAdapter.FEED_TITLE)));
			mUrlText.setText(feed.getString(feed
					.getColumnIndexOrThrow(ACastDbAdapter.FEED_URI)));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ACast.KEY, mRowId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private void saveState() {
		String title = mTitleText.getText().toString();
		String url = mUrlText.getText().toString();

		if (mRowId == null) {
			long id = mDbHelper.createFeed(title, url);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateFeed(mRowId, title, url);
		}
	}

}
