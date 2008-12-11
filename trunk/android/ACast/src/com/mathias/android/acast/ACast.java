package com.mathias.android.acast;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.rss.RssUtil;

public class ACast extends ListActivity {
	
	public static final String KEY = "keyyyy";

	private static final String TAG = ACast.class.getSimpleName();

	private static final int INSERT_ID = Menu.FIRST;
	private static final int UPDATE_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;

	private ACastDbAdapter mDbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feeds_list);
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		fillData();
	}

	private void fillData() {
		Cursor c = mDbHelper.fetchAllFeeds();
		startManagingCursor(c);

		String[] from = new String[] { ACastDbAdapter.FEED_TITLE };
		int[] to = new int[] { R.id.text1 };

		SimpleCursorAdapter feeds = new SimpleCursorAdapter(this,
				R.layout.feed_row, c, from, to);
		setListAdapter(feeds);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.addfeed);
		menu.add(0, UPDATE_ID, 0, R.string.editfeed);
		menu.add(0, DELETE_ID, 0, R.string.removefeed);
		menu.add(0, REFRESH_ID, 0, R.string.refreshfeeds);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			createFeed();
			return true;
		case UPDATE_ID:
			editFeed(getListView().getSelectedItemId());
			return true;
		case DELETE_ID:
			deleteFeed(getListView().getSelectedItemId());
			return true;
		case REFRESH_ID:
			refreshFeeds();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void createFeed() {
		Intent i = new Intent(this, FeedEdit.class);
		startActivityForResult(i, 0);
	}

	private void editFeed(long id) {
		Intent i = new Intent(this, FeedEdit.class);
		i.putExtra(KEY, id);
		startActivityForResult(i, 0);
	}

	private void deleteFeed(long id) {
		mDbHelper.deleteFeed(getListView().getSelectedItemId());
		fillData();
	}
	
	private void refreshFeeds(){
		Cursor c = mDbHelper.fetchAllFeeds();
		if(!c.moveToFirst()){
			Log.w(TAG, "No feeds!");
			return;
		}
		do{
			long rowId = c.getLong(c.getColumnIndexOrThrow(ACastDbAdapter.FEED_ID));
			String title = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEED_TITLE));
			String uri = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEED_URI));
			Log.d(TAG, "Title: "+title+" Uri: "+uri);
			Feed feed = new RssUtil().parse(uri);
			mDbHelper.updateFeed(rowId, feed);
		}while(c.moveToNext());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, FeedList.class);
		i.putExtra(KEY, id);
		startActivityForResult(i, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillData();
	}

}
