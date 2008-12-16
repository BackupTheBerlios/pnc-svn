package com.mathias.android.acast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.mathias.android.acast.common.ChoiceSimpleAdapter;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.rss.RssUtil;

public class FeedItemList extends ListActivity {

	private static final String TAG = FeedItemList.class.getSimpleName();

	// used to fetch data out of map for SimpleAdapter
	private static final String ICON = "ICON";
	private static final String FEEDITEM = "FEEDITEM";

	private static final int PLAY_ID = Menu.FIRST;
	private static final int DOWNLOAD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;

	private Long mFeedId;

	private ACastDbAdapter mDbHelper;
	
	private ChoiceSimpleAdapter adapter;
	
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
			List<Map<String, Object>> arr = buildArray(feed);
			adapter = new ChoiceSimpleAdapter(
					this, 
					arr,
					android.R.layout.activity_list_item,
					new String[] {FEEDITEM, ICON}, 
					new int[] { android.R.id.text1, android.R.id.icon },
					"title");
			setListAdapter(adapter);
		}
	}
	
	private List<Map<String, Object>> buildArray(Feed feed){
		List<FeedItem> items = feed.getItems();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(
				items.size());
		for (FeedItem item : items) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FEEDITEM, item);
			if(item.getMp3file() != null){
				if(item.isCompleted()){
					if(item.getBookmark() > 0){
						map.put(ICON, R.drawable.downloaded_done_bm);
					}else{
						map.put(ICON, R.drawable.downloaded_done);
					}
				}else{
					if(item.getBookmark() > 0){
						map.put(ICON, R.drawable.downloaded_bm);
					}else{
						map.put(ICON, R.drawable.downloaded);
					}
				}
			}else{
				if(item.isCompleted()){
					if(item.getBookmark() > 0){
						map.put(ICON, R.drawable.notdownloaded_done_bm);
					}else{
						map.put(ICON, R.drawable.notdownloaded_done);
					}
				}else{
					if(item.getBookmark() > 0){
						map.put(ICON, R.drawable.notdownloaded_bm);
					}else{
						map.put(ICON, R.drawable.notdownloaded);
					}
				}
			}
			list.add(map);
		}
		return list;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ACast.KEY, mFeedId);
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
			long id = ((FeedItem)adapter.getItem(pos).get(FEEDITEM)).getId();
			playItem(id);
			return true;
		}else if(pos >= 0 && DOWNLOAD_ID == item.getItemId()){
			long id = ((FeedItem)adapter.getItem(pos).get(FEEDITEM)).getId();
			downloadItem(id);
			return true;
		}else if(pos >= 0 && DELETE_ID == item.getItemId()){
			long id = ((FeedItem)adapter.getItem(pos).get(FEEDITEM)).getId();
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
				Util.downloadFile(cxt, uri, new File(file), this);
				item.setMp3file(file);
				mDbHelper.updateFeedItem(item);
				downloadHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				Log.e(TAG, item.getMp3file(), e);
				Message msg = new Message();
				msg.obj = e;
				downloadHandler.sendMessage(msg);
			}
		}
		
		public String getFilename(){
			String file = File.separator + "sdcard" + File.separator + "acast"
					+ File.separator + new File(item.getMp3uri()).getName();
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
			populateFields();
			if(msg != null && msg.obj != null){
				if(msg.obj instanceof Exception){
					Util.showDialog(FeedItemList.this, "Problem during download: "+((Exception)msg.obj).getMessage());
				}else{
					Util.showDialog(FeedItemList.this, "Problem during download!");
				}
			}
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
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setButton2("Cancel", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				download.stop();
				new File(download.getFilename()).delete();
			}
		});
		pd.setMax((int)item.getSize());
		pd.show();

	}

	private void deleteItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		if(item != null && item.getMp3file() != null){
			new File(item.getMp3file()).delete();
			item.setMp3file(null);
			mDbHelper.updateFeedItem(item);
			populateFields();
		}
	}

	private void refreshFeed(){
		Feed feed = mDbHelper.fetchFeed(mFeedId);
		feed = new RssUtil().parse(feed.getUri());
		mDbHelper.updateFeed(mFeedId, feed);
		populateFields();
	}

}
