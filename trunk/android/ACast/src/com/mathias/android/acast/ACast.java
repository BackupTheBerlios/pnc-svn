package com.mathias.android.acast;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.acast.common.ChoiceArrayAdapter;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;
import com.mathias.android.acast.rss.RssUtil;

public class ACast extends ListActivity {
	
	public static final String KEY = "key";
	public static final String FEEDITEM = "feeditem";

//	private static final String TAG = ACast.class.getSimpleName();

	private static final int INSERT_ID = Menu.FIRST;
	private static final int UPDATE_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;

	private ACastDbAdapter mDbHelper;
	
	private ChoiceArrayAdapter<Feed> adapter;
	
	private Settings settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_list);
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		fillData();

		Button resume = (Button) findViewById(R.id.resume);
		resume.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(settings != null){
					Long lastFeedItemId = settings.getLastFeedItemId();
					if(lastFeedItemId != null){
						FeedItem item = mDbHelper.fetchFeedItem(lastFeedItemId);
						Intent i = new Intent(ACast.this, Player.class);
						i.putExtra(ACast.FEEDITEM, item);
						startActivity(i);
					}
				}
			}
		});
	}

	private void fillData() {
		List<Feed> names = mDbHelper.fetchAllFeedsLight();
		adapter = new ChoiceArrayAdapter<Feed>(this,
				R.layout.feed_row, R.id.text1, names, "title");
		setListAdapter(adapter);

		settings = mDbHelper.fetchSettings();
		if(settings != null && settings.getLastFeedItemId() != null){
			TextView resumetitle = (TextView) findViewById(R.id.resumetitle);
			FeedItem item = mDbHelper.fetchFeedItem(settings.getLastFeedItemId());
			resumetitle.setText(item.getTitle());
		}
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, FeedItemList.class);
		i.putExtra(KEY, adapter.getItem(position).getId());
		startActivityForResult(i, 0);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(INSERT_ID == item.getItemId()){
			createFeed();
			return true;
		}else if(UPDATE_ID == item.getItemId()){
			int pos = getSelectedItemPosition();
			if(pos >= 0){
				long feedId = adapter.getItem(pos).getId();
				editFeed(feedId);
			}
			return true;
		}else if(DELETE_ID == item.getItemId()){
			int pos = getSelectedItemPosition();
			if(pos >= 0){
				long feedId = adapter.getItem(pos).getId();
				deleteFeed(feedId);
			}
			return true;
		}else if(REFRESH_ID == item.getItemId()){
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
		mDbHelper.deleteFeed(id);
		fillData();
	}
	
	private void refreshFeeds(){
		List<Feed> feeds = mDbHelper.fetchAllFeedsLight();
		for (Feed feed : feeds) {
			long rowId = feed.getId();
			String uri = feed.getUri();
			feed = new RssUtil().parse(uri);
			mDbHelper.updateFeed(rowId, feed);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillData();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
//		mDbHelper.close();
	}

}
