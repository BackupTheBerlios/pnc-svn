package com.mathias.android.acast;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.mathias.android.acast.common.ChoiceArrayAdapter;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.rss.RssUtil;

public class FeedItemList extends ListActivity {

	private static final String TAG = FeedItemList.class.getSimpleName();

	private static final int PLAY_ID = Menu.FIRST;
	private static final int DOWNLOAD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;

	private Long mFeedId;

	private ACastDbAdapter mDbHelper;
	
	private ChoiceArrayAdapter<FeedItem> adapter;
	
	private ProgressDialog pd;
	
	private Integer currPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feeditem_list);
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		mFeedId = savedInstanceState != null ? savedInstanceState
				.getLong(ACast.KEY) : null;
		if (mFeedId == null) {
			Bundle extras = getIntent().getExtras();
			mFeedId = extras != null ? extras.getLong(ACast.KEY)
					: null;
		}
		populateFields();
	}

	private void populateFields() {
		if (mFeedId != null) {
			Feed feed = mDbHelper.fetchFeed(mFeedId);
			adapter = new ChoiceArrayAdapter<FeedItem>(this,
					R.layout.feed_row, R.id.text1, feed.getItems(), "title");
			setListAdapter(adapter);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ACast.KEY, mFeedId);
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, PLAY_ID, 0, R.string.playitem);
		menu.add(0, DOWNLOAD_ID, 0, R.string.downloaditem);
		menu.add(0, DELETE_ID, 0, R.string.deleteitem);
		menu.add(0, REFRESH_ID, 0, R.string.refreshfeed);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		currPos = position;
		openOptionsMenu();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int pos = getSelectedItemPosition();
		if(currPos != null){
			pos = currPos;
			currPos = null;
		}
		if(pos >= 0 && PLAY_ID == item.getItemId()){
			long id = adapter.getItem(pos).getId();
			playItem(id);
			return true;
		}else if(pos >= 0 && DOWNLOAD_ID == item.getItemId()){
			long id = adapter.getItem(pos).getId();
			downloadItem(id);
			return true;
		}else if(pos >= 0 && DELETE_ID == item.getItemId()){
			long id = adapter.getItem(pos).getId();
			deleteItem(id);
			return true;
		}else if(REFRESH_ID == item.getItemId()){
			refreshFeed();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void playItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		Intent i = new Intent(this, Player.class);
		i.putExtra(ACast.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private class Download extends Thread implements Util.ProgressListener {
		
		private FeedItem item;
		
		private Context cxt;
		
		public Download(Context cxt, FeedItem item){
			setDaemon(false);
			this.cxt = cxt;
			this.item = item;
		}
		
		@Override
		public void run() {
			String uri = item.getMp3uri().replace(' ', '+');
			String file = getFilename();
			try {
				Util.downloadFile(cxt, uri, file, null);
				item.setMp3file(file);
				mDbHelper.updateFeedItem(item);
			} catch (Exception e) {
				Log.e(TAG, item.getMp3file(), e);
			}
			downloadHandler.sendEmptyMessage(0);
		}
		
		public String getFilename(){
			String file = item.getTitle();
			if(!file.endsWith(".mp3")){
				file += ".mp3";
			}
			return file;
		}

		@Override
		public void progressDiff(long diff) {
			pd.incrementProgressBy((int)diff);
		}
	}

	private Handler downloadHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
		}
	};

	private void downloadItem(long id){
		final FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		String file = item.getMp3file();
		if(file != null){
			Util.showDialog(this, "Already downloaded: "+file);
			return;
		}

		final Download download = new Download(this, item);
		download.start();

		pd = new ProgressDialog(this);
		pd.setTitle("Downloading");
		pd.setMessage(item.getMp3uri());
		pd.setButton("Background", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pd.dismiss();
			}
		});
		pd.setButton2("Cancel", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				download.stop();
				deleteFile(download.getFilename());
			}
		});
		pd.setMax((int)item.getSize());
		pd.show();

	}

	private void deleteItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		if(item != null && item.getMp3file() != null){
			deleteFile(item.getMp3file());
			item.setMp3file(null);
			mDbHelper.updateFeedItem(item);
		}
	}

	private void refreshFeed(){
		Feed feed = mDbHelper.fetchFeed(mFeedId);
		feed = new RssUtil().parse(feed.getUri());
		mDbHelper.updateFeed(mFeedId, feed);
		populateFields();
	}

}
