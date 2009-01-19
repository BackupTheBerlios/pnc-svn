package com.mathias.android.acast;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.update.IUpdateService;
import com.mathias.android.acast.common.services.update.IUpdateServiceCallback;
import com.mathias.android.acast.common.services.update.UpdateService;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.FeedItemLight;
import com.mathias.android.acast.podcast.Settings;

public class FeedList extends ListActivity {

	private static final String TAG = FeedList.class.getSimpleName();

	private static final int REFRESHALL_ID = Menu.FIRST + 0;
	private static final int DOWNLOADALLLAST_ID = Menu.FIRST + 1;

	private static final int INFO_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;
	private static final int DELETE_ID = Menu.FIRST + 4;
	private static final int DOWNLOADALL_ID = Menu.FIRST + 5;
	private static final int CANCEL_ID = Menu.FIRST + 6;

    private NotificationManager mNM;
    
	private ACastDbAdapter mDbHelper;

	private FeedAdapter adapter;
	
	private WorkerThread thread;

	private IDownloadService downloadBinder;

	private ServiceConnection downloadServiceConn;
	
	private IUpdateService updateBinder;

	private ServiceConnection updateServiceConn;
	
	private Map<Long, ValueHolder> metaDataMap= new HashMap<Long, ValueHolder>();
	
	private boolean visible = false;
	
	private long lastId = Constants.INVALID_ID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.feed_list);

		setTitle("Feeds");
		
    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		thread = new WorkerThread();
		thread.start();

		// start progress handler loop
		lastId = (savedInstanceState != null ? savedInstanceState.getLong(
				Constants.FEEDID, Constants.INVALID_ID) : Constants.INVALID_ID);

		populateView();

		Intent i = new Intent(this, DownloadService.class);
		startService(i);
		downloadServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				downloadBinder = IDownloadService.Stub.asInterface(service);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: "+name);
				downloadBinder = null;
			}
		};
		if(!bindService(i, downloadServiceConn, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start download service!");
		}

		i = new Intent(this, UpdateService.class);
		startService(i);
		updateServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				updateBinder = IUpdateService.Stub.asInterface(service);
				try {
					updateBinder.registerCallback(updateCallback);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: "+name);
				updateBinder = null;
			}
		};
		if(!bindService(i, updateServiceConn, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start update service!");
		}

		getListView().setOnScrollListener(scrollListener);
		
		getListView().setOnCreateContextMenuListener(this);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuItem item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refresh);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.removefeed);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item = menu.add(Menu.NONE, DOWNLOADALL_ID, Menu.NONE, R.string.downloadall);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, R.string.cancel);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.NONE, REFRESHALL_ID, Menu.NONE, R.string.refreshall);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, DOWNLOADALLLAST_ID, Menu.NONE, R.string.downloadlast);
		item.setIcon(android.R.drawable.stat_sys_download);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int id = item.getItemId();
		int pos = menuInfo.position;
		lastId = adapter.getItemId(pos);
		if(pos != ListView.INVALID_POSITION){
			if(INFO_ID == id){
				infoFeed(adapter.getItem(pos));
				return true;
			}else if(REFRESH_ID == id){
				refreshFeed(adapter.getItem(pos));
				return true;
			}else if(DELETE_ID == id){
				deleteFeed(adapter.getItem(pos));
				return true;
			}else if(DOWNLOADALL_ID == id){
				downloadAllFeedItems(adapter.getItem(pos));
				return true;
			}else if(CANCEL_ID == id){
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		//Options menu
		int id = item.getItemId();
		if(REFRESHALL_ID == id){
			refreshFeeds();
			return true;
		}else if(DOWNLOADALLLAST_ID == id){
			downloadLatest();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		lastId = id;

		Intent i = new Intent(this, FeedItemList.class);
		i.putExtra(Constants.FEEDID, adapter.getItemId(position));
		startActivity(i);
	}

	private Handler uiHandler = new Handler();
	
	@Override
	protected void onResume() {
		super.onResume();
		metaDataMap.clear();
		visible = true;
		populateView();

		uiHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
		    	updateViewWithMetaData(getListView());
			}
		}, 1000);

		mNM.cancel(Constants.NOTIFICATION_UPDATESERVICE_ID);
	}
	
	@Override
	protected void onPause() {
		visible = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		mDbHelper.close();
		mDbHelper = null;
		if(downloadBinder != null){
			unbindService(downloadServiceConn);
		}
		if(updateBinder != null){
			unbindService(updateServiceConn);
		}
		super.onDestroy();
	}

	private void populateView() {
		if(mDbHelper == null){
			Log.w(TAG, "mDbHelper is null");
		}else{
			String lfuStr = mDbHelper.getSetting(Settings.LASTFULLUPDATE);
			TextView updatedate = (TextView) findViewById(R.id.updatedate);
			updatedate.setText((lfuStr != null ? lfuStr : "Unknown"));

			List<Feed> feeds = mDbHelper.fetchAllFeeds();
			adapter = new FeedAdapter(this, feeds);
			setListAdapter(adapter);

			if(lastId != Constants.INVALID_ID){
				int pos = 0;
				for(Iterator<Feed> it = feeds.iterator(); it.hasNext(); ){
					long feedId = it.next().id;
					if(lastId == feedId){
						getListView().setSelection(pos);
						break;
					}
					pos++;
				}
			}
		}
	}

	private void refreshFeeds(){
		try {
			if(updateBinder != null){
				updateBinder.updateAll();
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showToastShort(FeedList.this, e.getMessage());
		}
	}
	
	private void refreshFeed(Feed feed){
        setProgressBarIndeterminateVisibility(true);
		try {
			if(updateBinder != null){
				updateBinder.updateFeed(feed.id);
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showToastShort(FeedList.this, e.getMessage());
		}
	}
	
	private class WorkerThread extends Thread {

		private Handler handler;

		private void updateFeedMetaData(int position, final long feedId, final ViewHolder holder){
			handler.post(new Runnable(){
				@Override
				public void run() {
					if(visible && mDbHelper != null){
						List<FeedItemLight> items = mDbHelper.fetchAllFeedItemLights(feedId);
						Collections.sort(items, ACastUtil.FEEDITEMLIGHT_BYDATE);
						final ValueHolder values = new ValueHolder();
						values.latesttitle = (items.size() > 0 ? items.get(0).title : null);
						values.sum = items.size();
						boolean touched = false;
						for (FeedItemLight item : items) {
							if(item.completed){
								values.completed++;
								touched = true;
							}
							if(item.downloaded){
								values.downloaded++;
								touched = true;
							}
							if(item.bookmark > 0){
								values.bookmarked++;
								touched = true;
							}
							if(!touched){
								values.newitems++;
							}
						}
						metaDataMap.put(feedId, values);
						runOnUiThread(new Runnable(){
							@Override
							public void run() {
	                			updateViewWithValues(holder, values);
							}
						});
					}
				}
			});
		}

		@Override
		public void run() {
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
		}
	}
	
	private void downloadLatest(){
		int count = 0;
		for (Feed feed : adapter.feeds) {
			List<FeedItem> items = mDbHelper.fetchAllFeedItems(feed.id);
			for (FeedItem feeditem : items) {
				if (feeditem.mp3uri != null
						&& feeditem.mp3file != null) {
					try {
						if (!feeditem.downloaded) {
							downloadBinder.download(feeditem.id,
									feeditem.mp3uri, feeditem.mp3file,
									feeditem.size);
							count++;
						}
					} catch (RemoteException e) {
						Log.e(TAG, e.getMessage(), e);
					}
					break;
				}
			}
		}
		Util.showToastShort(this, "Downloading "+count+" items");
	}

	private void downloadAllFeedItems(Feed feed){
		List<FeedItem> items = mDbHelper.fetchAllFeedItems(feed.id);
		for (FeedItem feeditem : items) {
			if (feeditem.mp3uri != null
					&& feeditem.mp3file != null) {
				try {
					if (!feeditem.downloaded) {
						downloadBinder.download(feeditem.id, feeditem.mp3uri,
								feeditem.mp3file, feeditem.size);
					}
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		Util.showToastShort(this, "Downloading all items for "+feed.title);
	}

	private void deleteFeed(final Feed feed) {
		Util.showConfirmationDialog(this, "Are you sure you want to delete "
				+ feed.title + "?", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<FeedItem> items = mDbHelper.fetchAllFeedItems(feed.id);
				for(FeedItem item : items){
					if(item.mp3file != null){
						boolean deleted = new File(item.mp3file).delete();
						Log.d(TAG, "Deleted="+deleted+" file="+item.mp3file);
					}
				}
				try {
					mDbHelper.deleteFeed(feed.id);
				} catch (DatabaseException e) {
					Log.e(TAG, e.getMessage(), e);
					Util.showToastShort(FeedList.this, "Could not delete feed!");
				}
				populateView();
			}
		});
	}

	private void infoFeed(Feed feed) {
		Intent i = new Intent(this, FeedInfo.class);
		i.putExtra(Constants.FEED, feed);
		startActivity(i);
	}

	private class FeedAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;

		private List<Feed> feeds;
		
		public FeedAdapter(Context cxt, List<Feed> feeds){
			Collections.sort(feeds, ACastUtil.FEED_BYDATE);
			this.feeds = feeds;
			mInflater = LayoutInflater.from(cxt);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			Feed feed = feeds.get(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.feed_row, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.feedrowicon);
				holder.text = (TextView) convertView.findViewById(R.id.feedrowtext);
				holder.text2 = (TextView) convertView.findViewById(R.id.feedrowtext2);
				holder.text3 = (TextView) convertView.findViewById(R.id.feedrowtext3);
				holder.latesttitle = (TextView) convertView.findViewById(R.id.latesttitle);
				holder.newitems = (TextView) convertView.findViewById(R.id.newitems);
				holder.bookmarked = (TextView) convertView.findViewById(R.id.bookmarked);
				holder.completed = (TextView) convertView.findViewById(R.id.completed);
				holder.downloaded = (TextView) convertView.findViewById(R.id.downloaded);
				holder.sum = (TextView) convertView.findViewById(R.id.total);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String iconPath = feed.icon;
			Bitmap icon = BitmapCache.instance().get(iconPath);
			if(icon != null){
				holder.icon.setImageBitmap(icon);
			}else{
				holder.icon.setImageResource(R.drawable.question);
			}
			holder.text.setText(feed.title);
			String author = feed.author;
			holder.text2.setText((author != null ? author : ""));
			Date pubDate = new Date(feed.pubdate);
			holder.text3.setText((pubDate != null ? pubDate.toString() : ""));
			return convertView;
		}
		
		@Override
		public int getCount() {
			return feeds.size();
		}

		@Override
		public Feed getItem(int position) {
			return feeds.get(position);
		}

		@Override
		public long getItemId(int position) {
			return feeds.get(position).id;
		}

	}

    private static class ValueHolder {
    	String latesttitle;
    	int sum;
    	int downloaded;
    	int completed;
    	int newitems;
    	int bookmarked;
    }
    
    private static class ViewHolder {
        ImageView icon;
        TextView text;
        TextView text2;
        TextView text3;
        TextView latesttitle;
        TextView newitems;
        TextView downloaded;
        TextView completed;
        TextView bookmarked;
        TextView sum;
    }
    
    private ListView.OnScrollListener scrollListener = new ListView.OnScrollListener() {
    	@Override
    	public void onScroll(AbsListView view, int firstVisibleItem,
    			int visibleItemCount, int totalItemCount) {
    	}
    	@Override
    	public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
            	updateViewWithMetaData(view);
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                break;
            }
    	}
    };
    
    private void updateViewWithMetaData(AbsListView view){
        int first = view.getFirstVisiblePosition();
        int count = view.getChildCount();
        for (int i = 0; i < count; i++) {
        	View convertView = view.getChildAt(i);
        	ViewHolder holder = (ViewHolder) convertView.getTag();
        	if(holder != null){
        		long itemId = adapter.getItemId(first+i);
        		ValueHolder values = metaDataMap.get(itemId);
        		if(values == null){
        			thread.updateFeedMetaData(first+i, itemId, holder);
        		}else{
        			updateViewWithValues(holder, values);
        		}
        	}
        }
    }

    private static void updateViewWithValues(ViewHolder holder, ValueHolder values){
		if(holder != null){
			holder.latesttitle.setText(values.latesttitle);
			holder.newitems.setText(""+values.newitems);
			holder.bookmarked.setText(""+values.bookmarked);
			holder.completed.setText(""+values.completed);
			holder.downloaded.setText(""+values.downloaded);
			holder.sum.setText(""+values.sum);
		}
    }

	private final IUpdateServiceCallback updateCallback = new IUpdateServiceCallback.Stub() {
		@Override
		public void onUpdateAllCompleted() throws RemoteException {
			Log.d(TAG, "onUpdateAllCompleted()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					populateView();
				}
			});
		}
		@Override
		public void onUpdateItemCompleted(final String title) throws RemoteException {
			Log.d(TAG, "onUpdateItemCompleted()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					populateView();
			        setProgressBarIndeterminateVisibility(false);
				}
			});
		}
		@Override
		public void onUpdateItemException(final String title, final String error) throws RemoteException {
			Log.d(TAG, "onUpdateItemException()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Log.w(TAG, title+": "+ error);
					populateView();
			        setProgressBarIndeterminateVisibility(false);
				}
			});
		}
	};

}
