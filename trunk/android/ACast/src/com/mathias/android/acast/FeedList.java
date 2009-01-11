package com.mathias.android.acast;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
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
import android.os.Message;
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
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.common.RssUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.FeedItemLight;
import com.mathias.android.acast.podcast.Settings.SettingEnum;

public class FeedList extends ListActivity {

	private static final String TAG = FeedList.class.getSimpleName();

	private static final int REFRESHALL_ID = Menu.FIRST + 0;
	private static final int DOWNLOADALLLAST_ID = Menu.FIRST + 1;

	private static final int INFO_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;
	private static final int DELETE_ID = Menu.FIRST + 4;
	private static final int DOWNLOADALL_ID = Menu.FIRST + 5;
	private static final int CANCEL_ID = Menu.FIRST + 6;

	private ACastDbAdapter mDbHelper;

	private FeedAdapter adapter;
	
	private WorkerThread thread;

	private IDownloadService downloadBinder;

	private ServiceConnection downloadServiceConn;
	
	private Map<Long, ValueHolder> metaDataMap= new HashMap<Long, ValueHolder>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.feed_list);

		setTitle("Feeds");

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		thread = new WorkerThread();
		thread.start();

		getListView().setLongClickable(true);
		getListView().setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				Log.d(TAG, "onLongClick pressed!");
				return false;
			}
		});

		fillData();

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
		if(pos != ListView.INVALID_POSITION){
			if(INFO_ID == id){
				infoFeed(adapter.getItem(pos));
				return true;
			}else if(REFRESH_ID == id){
				thread.refreshFeed(adapter.getItem(pos));
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
			thread.refreshFeeds();
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
		Intent i = new Intent(this, FeedItemList.class);
		i.putExtra(Constants.FEEDID, adapter.getItemId(position));
		startActivity(i);
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillData();

		uiHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
		    	updateViewWithMetaData(getListView());
			}
		}, 1000);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		mDbHelper.close();
		mDbHelper = null;
		if(downloadBinder != null){
			unbindService(downloadServiceConn);
		}
		super.onDestroy();
	}

	private void fillData() {
		String lfuStr = (mDbHelper != null ? mDbHelper.getSetting(SettingEnum.LASTFULLUPDATE) : null);
		TextView updatedate = (TextView) findViewById(R.id.updatedate);
		updatedate.setText((lfuStr != null ? lfuStr : "Unknown"));

		List<Feed> feeds = mDbHelper.fetchAllFeeds();
		adapter = new FeedAdapter(this, feeds);
		setListAdapter(adapter);
	}
	
	private Handler uiHandler = new Handler();

	private class WorkerThread extends Thread {
		
		private final static int REFRESHFEEDS = 0;

		private final static int REFRESHFEED = 1;

		private final static int UPDATEFEEDMETADATA = 2;

		private Handler handler;

		private void refreshFeeds(){
	        setProgressBarIndeterminateVisibility(true);
			handler.sendEmptyMessage(REFRESHFEEDS);
		}
		
		private void refreshFeed(Feed feed){
	        setProgressBarIndeterminateVisibility(true);
			handler.sendMessage(handler.obtainMessage(REFRESHFEED, feed));
		}
		
		private void updateFeedMetaData(int position, long feedId, ViewHolder textView){
			handler.sendMessage(handler.obtainMessage(UPDATEFEEDMETADATA,
					position, (int) feedId, textView));
		}

		@Override
		public void run() {
			Looper.prepare();
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					try {
						if(msg.what == REFRESHFEEDS){
							List<Feed> feeds = mDbHelper.fetchAllFeeds();
							for (Feed feed : feeds) {
								long rowId = feed.id;
								final String title = feed.title;
								Log.d(TAG, "Parsing "+title);
								Map<Feed, List<FeedItem>> result = new RssUtil().parse(feed.uri);
								mDbHelper.updateFeed(rowId, result);
						        Util.showToastShort(FeedList.this, "Updated "+title);
							}
							mDbHelper.setSetting(SettingEnum.LASTFULLUPDATE, new Date());
							Log.d(TAG, "Done");
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									fillData();
							        setProgressBarIndeterminateVisibility(false);
							        Util.showToastShort(FeedList.this, "Feeds updated");
								}
							});
						}else if(msg.what == REFRESHFEED){
							Feed feed = (Feed) msg.obj;
							final String title = feed.title;
							Log.d(TAG, "Parsing "+title);
							Map<Feed, List<FeedItem>> result = new RssUtil().parse(feed.uri);
							mDbHelper.updateFeed(feed.id, result);
							Log.d(TAG, "Done");
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									fillData();
							        setProgressBarIndeterminateVisibility(false);
							        Util.showToastShort(FeedList.this, "Updated "+title);
								}
							});
						}else if(msg.what == UPDATEFEEDMETADATA){
//							int position = msg.arg1;
							long feedId = msg.arg2;
							final ViewHolder holder = (ViewHolder) msg.obj;
							if(mDbHelper != null){
								List<FeedItemLight> items = mDbHelper.fetchAllFeedItemLights(feedId);
								Collections.sort(items, ACastUtil.FEEDITEMLIGHT_BYDATE);
								final ValueHolder values = new ValueHolder();
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
					} catch (final Exception e) {
						Log.e(TAG, e.getMessage(), e);
						runOnUiThread(new Runnable(){
							@Override
							public void run() {
								Util.showDialog(FeedList.this, e.getMessage());
						        setProgressBarIndeterminateVisibility(false);
							}
						});
					}
				}
			};
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
							downloadBinder.download(feeditem.id, feeditem
									.mp3uri, feeditem.mp3file);
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
						downloadBinder.download(feeditem.id, feeditem
								.mp3uri, feeditem.mp3file);
					}
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		Util.showToastShort(this, "Downloading all items for "+feed.title);
	}

	private void deleteFeed(final Feed feed) {
		Util.showConfirmationDialog(this, "Are you sure?", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<FeedItem> items = mDbHelper.fetchAllFeedItems(feed.id);
				for(FeedItem item : items){
					boolean deleted = new File(item.mp3file).delete();
					Log.d(TAG, "File deleted="+deleted+" file="+item.mp3file);
				}
				mDbHelper.deleteFeed(feed.id);
				fillData();
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
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			Feed feed = feeds.get(position);

			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.feed_row, null);

				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.feedrowicon);
				holder.text = (TextView) convertView.findViewById(R.id.feedrowtext);
				holder.text2 = (TextView) convertView.findViewById(R.id.feedrowtext2);
				holder.text3 = (TextView) convertView.findViewById(R.id.feedrowtext3);
				holder.newitems = (TextView) convertView.findViewById(R.id.newitems);
				holder.bookmarked = (TextView) convertView.findViewById(R.id.bookmarked);
				holder.completed = (TextView) convertView.findViewById(R.id.completed);
				holder.downloaded = (TextView) convertView.findViewById(R.id.downloaded);
				holder.sum = (TextView) convertView.findViewById(R.id.total);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
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
			holder.newitems.setText(""+values.newitems);
			holder.bookmarked.setText(""+values.bookmarked);
			holder.completed.setText(""+values.completed);
			holder.downloaded.setText(""+values.downloaded);
			holder.sum.setText(""+values.sum);
		}
    }

}
