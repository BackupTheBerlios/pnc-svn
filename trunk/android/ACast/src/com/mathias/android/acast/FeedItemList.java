package com.mathias.android.acast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.mathias.android.acast.common.ChoiceSimpleAdapter;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.rss.RssUtil;

public class FeedItemList extends ListActivity implements ServiceConnection {

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

	private IDownloadService binder;

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
			if(item.isDownloaded()){
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

	private void downloadItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		String file = item.getMp3file();
		if(item.isDownloaded()){
			Util.showDialog(this, "Already downloaded: "+file);
			return;
		}

		String srcuri = item.getMp3uri().replace(' ', '+');
		Intent i = new Intent(this, DownloadService.class);
		i.putExtra(DownloadService.EXTERNALID, item.getId());
		i.putExtra(DownloadService.SRCURI, srcuri);
		i.putExtra(DownloadService.DESTFILE, item.getMp3file());
		startService(i);
		if(!bindService(i, this, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start download!");
			return;
		}

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
				if(binder != null){
					try {
						binder.cancelAndRemove();
					} catch (RemoteException e) {
					}
				}
			}
		});
		pd.setMax((int)item.getSize());
		pd.show();
	}

	private void deleteItem(long id){
		FeedItem item = mDbHelper.fetchFeedItem(mFeedId, id);
		if(item != null && item.getMp3file() != null){
			new File(item.getMp3file()).delete();
			item.setDownloaded(false);
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

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.e(TAG, "onServiceConnected: "+name);
		binder = IDownloadService.Stub.asInterface(service);
		try {
			binder.registerCallback(downloadCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.e(TAG, "onServiceDisconnected: "+name);
		try {
			binder.unregisterCallback(downloadCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		binder = null;
	}

	private final IDownloadServiceCallback downloadCallback = new IDownloadServiceCallback.Stub() {
		@Override
		public void onCompleted(long externalid) throws RemoteException {
			pd.dismiss();
			mDbHelper.updateFeedItem(externalid, ACastDbAdapter.FEEDITEM_DOWNLOADED, true);
			populateFields();
		}
		@Override
		public void onException(long externalid, String exception) throws RemoteException {
			pd.dismiss();
			Util.showDialog(FeedItemList.this, "Download failed: "+exception);
			populateFields();
		}
		@Override
		public void onProgress(long externalid, long diff) throws RemoteException {
			pd.incrementProgressBy((int)diff);
		}
	};

}
