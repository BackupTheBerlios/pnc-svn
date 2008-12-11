package com.mathias.android.acast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.rss.RssUtil;

public class FeedList extends ListActivity {

	private static final int PLAY_ID = Menu.FIRST;
	private static final int DOWNLOAD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;

	private Long mRowId;

	private ACastDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_list);
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(ACast.KEY) : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(ACast.KEY)
					: null;
		}
		populateFields();
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor c = mDbHelper.fetchFeedItems(mRowId);
			startManagingCursor(c);

			String[] from = new String[] { ACastDbAdapter.FEEDITEM_TITLE };
			int[] to = new int[] { R.id.text1 };

			SimpleCursorAdapter notes = new SimpleCursorAdapter(this,
					R.layout.feed_row, c, from, to);
			setListAdapter(notes);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ACast.KEY, mRowId);
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
		switch (item.getItemId()) {
		case PLAY_ID:
			playItem(getListView().getSelectedItemId());
			return true;
		case DOWNLOAD_ID:
			downloadItem(getListView().getSelectedItemId());
			return true;
		case DELETE_ID:
			deleteItem(getListView().getSelectedItemId());
			return true;
		case REFRESH_ID:
			refreshFeed();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void playItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mRowId, id);
		if(item.getMp3file() == null){
			MediaPlayer mp = MediaPlayer.create(this, Uri.parse(item.getMp3uri()));
			mp.start();
		}else{
			MediaPlayer mp = new MediaPlayer();
			try {
				mp.setDataSource(item.getMp3file());
				mp.prepare();
				mp.start();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void downloadItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mRowId, id);
		if(item.getMp3file() != null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Already downloaded: "+item.getMp3file());
			builder.show();
			return;
		}
		String file = item.getTitle();
		InputStream input = null;
		FileOutputStream output = null;
		
		ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("Downloading: "+file);
		progress.setMax(100);
		progress.show();
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpGet(item.getMp3uri().replaceAll(" ", "+")));
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
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
		progress.dismiss();
	}

	private void deleteItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mRowId, id);
		if(item != null && item.getMp3file() != null){
			deleteFile(item.getMp3file());
			item.setMp3file(null);
			mDbHelper.updateFeedItem(item);
		}
	}

	private void refreshFeed(){
		Cursor c = mDbHelper.fetchFeed(mRowId);
		String uri = c.getString(c.getColumnIndex(ACastDbAdapter.FEED_URI));
		Feed feed = new RssUtil().parse(uri);
		mDbHelper.updateFeed(mRowId, feed);
		populateFields();
	}

}
