package com.mathias.android.acast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(PLAY_ID == item.getItemId()){
			int pos = getSelectedItemPosition();
			if(pos >= 0){
				long id = adapter.getItem(pos).getId();
				playItem(id);
			}
			return true;
		}else if(DOWNLOAD_ID == item.getItemId()){
			int pos = getSelectedItemPosition();
			if(pos >= 0){
				long id = adapter.getItem(pos).getId();
				downloadItem(id);
			}
			return true;
		}else if(DELETE_ID == item.getItemId()){
			int pos = getSelectedItemPosition();
			if(pos >= 0){
				long id = adapter.getItem(pos).getId();
				deleteItem(id);
			}
			return true;
		}else if(REFRESH_ID == item.getItemId()){
			refreshFeed();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void playItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		String file = item.getMp3file();
		String uri = item.getMp3uri().replace(' ', '+');
		if(file == null){
			MediaPlayer mp = MediaPlayer.create(this, Uri.parse(uri));
			if(mp == null){
				Util.showDialog(this, "Could not create media player for: "+uri);
				return;
			}
			mp.start();
		}else{
			MediaPlayer mp = new MediaPlayer();
			try {
				mp.setDataSource(file);
				//mp.setDisplay();
//				mp.prepare();
				mp.start();
			} catch (IllegalArgumentException e) {
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(this, e.getMessage()+": "+uri);
			} catch (IllegalStateException e) {
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(this, e.getMessage()+": "+uri);
			} catch (IOException e) {
				Log.e(TAG, item.getMp3file(), e);
				Util.showDialog(this, e.getMessage()+": "+uri);
			}
		}
	}

	private void downloadItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		String file = item.getMp3file();
		String uri = item.getMp3uri().replace(' ', '+');
		if(file != null){
			Util.showDialog(this, "Already downloaded: "+file);
			return;
		}
		file = item.getTitle();
		InputStream input = null;
		FileOutputStream output = null;
		
		ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("Downloading: "+file);
		progress.setMax(100);
		progress.show();
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpGet(uri));
			input = response.getEntity().getContent();
			output = openFileOutput(item.getTitle(), MODE_WORLD_READABLE);
			while(true){
				byte[] buffer = new byte[8192];
				int c = input.read(buffer);
				if(c == -1){
					break;
				}
				output.write(buffer, 0, c);
				progress.incrementProgressBy(5);
			}
			item.setMp3file(file);
			mDbHelper.updateFeedItem(item);
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}finally{
			if(input != null){
				try {
					input.close();
				} catch (IOException e) {
				}
			}
			if(output != null){
				try {
					output.close();
				} catch (IOException e) {
				}
			}
		}
		//progress.dismiss();
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
