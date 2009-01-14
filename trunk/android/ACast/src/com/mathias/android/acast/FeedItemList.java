package com.mathias.android.acast;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.common.services.update.IUpdateService;
import com.mathias.android.acast.common.services.update.IUpdateServiceCallback;
import com.mathias.android.acast.common.services.update.UpdateService;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;

public class FeedItemList extends ListActivity {

	private static final String TAG = FeedItemList.class.getSimpleName();

	private static final int REFRESH_ID = Menu.FIRST + 0;
	private static final int DOWNLOADALL_ID = Menu.FIRST + 1;

	private static final int QUEUE_ID = Menu.FIRST + 2;
	private static final int DOWNLOAD_ID = Menu.FIRST + 3;
	private static final int INFO_ID = Menu.FIRST + 4;
	private static final int DELETE_ID = Menu.FIRST + 5;
	private static final int CANCEL_ID = Menu.FIRST + 6;

	private Long mFeedId;

	private ACastDbAdapter mDbHelper;
	
	private FeedItemAdapter adapter;

	private IDownloadService downloadBinder;

	private ServiceConnection downloadServiceConn;

	private IMediaService mediaBinder;
	
	private ServiceConnection mediaServiceConn;

	private IUpdateService updateBinder;
	
	private ServiceConnection updateServiceConn;

	private Bitmap defaultIcon;

	private Feed feed;

	private List<FeedItem> items;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.feeditem_list);
		
		setTitle("Feed items");

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		mFeedId = savedInstanceState != null ? savedInstanceState
				.getLong(Constants.FEEDID) : null;
		if (mFeedId == null) {
			Bundle extras = getIntent().getExtras();
			mFeedId = extras != null ? extras.getLong(Constants.FEEDID) : null;
		}

		defaultIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.question);

		populateFields();
		
		getListView().setOnCreateContextMenuListener(this);

		Intent i = new Intent(this, DownloadService.class);
		startService(i);
		downloadServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				downloadBinder = IDownloadService.Stub.asInterface(service);
				try {
					downloadBinder.registerCallback(downloadCallback);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: "+name);
				try {
					downloadBinder.unregisterCallback(downloadCallback);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
				downloadBinder = null;
			}
		};
		if(!bindService(i, downloadServiceConn, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start download service!");
		}

		i = new Intent(this, MediaService.class);
		startService(i);
		mediaServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				mediaBinder = IMediaService.Stub.asInterface(service);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: "+name);
				mediaBinder = null;
			}

		};
		if(!bindService(i, mediaServiceConn, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start media service!");
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
				try {
					updateBinder.unregisterCallback(updateCallback);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
				updateBinder = null;
			}

		};
		if(!bindService(i, updateServiceConn, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start update service!");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Constants.FEEDID, mFeedId);
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		FeedItem item = adapter.getItem(position);
		if(item.mp3uri == null){
			infoItem(item);
		}else{
			ACastUtil.playQueueItem(this, mediaBinder, item, items);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuItem item = menu.add(Menu.NONE, QUEUE_ID, Menu.NONE, R.string.queueitem);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, DOWNLOAD_ID, Menu.NONE, R.string.downloaditem);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		item = menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.deleteitem);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item = menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, R.string.cancel);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refresh);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, DOWNLOADALL_ID, Menu.NONE, R.string.downloadall);
		item.setIcon(android.R.drawable.stat_sys_download);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int id = item.getItemId();
		int pos = menuInfo.position;
		if(pos != ListView.INVALID_POSITION){
			if(QUEUE_ID == id){
				queueItem(adapter.getItem(pos));
				return true;
			}else if(DOWNLOAD_ID == id){
				downloadItem(adapter.getItem(pos));
				return true;
			}else if(DELETE_ID == id){
				deleteItem(adapter.getItem(pos));
				return true;
			}else if(INFO_ID == id){
				infoItem(adapter.getItem(pos));
				return true;
			}else if(CANCEL_ID == id){
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuitem) {
		if(REFRESH_ID == menuitem.getItemId()){
			updateFeed();
			return true;
		}else if(DOWNLOADALL_ID == menuitem.getItemId()){
			downloadAll(adapter.items);
			return true;
		}
		return super.onMenuItemSelected(featureId, menuitem);
	}
	
	private void updateFeed(){
		try {
	        setProgressBarIndeterminateVisibility(true);
			updateBinder.updateFeed(feed.id);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void populateFields() {
		if (mDbHelper != null && mFeedId != null) {
			feed = mDbHelper.fetchFeed(mFeedId);
			items = mDbHelper.fetchAllFeedItems(mFeedId);
			Collections.sort(items, ACastUtil.FEEDITEM_BYDATE);

			String iconStr = feed.icon;
			ImageView icon = (ImageView) findViewById(R.id.feedrowicon);
			Bitmap iconBM = BitmapCache.instance().get(iconStr);
			icon.setImageBitmap(iconBM != null ? iconBM : defaultIcon);
			TextView text = (TextView) findViewById(R.id.feedrowtext);
			text.setText(feed.title);
			String author = feed.author;
			TextView text2 = (TextView) findViewById(R.id.feedrowtext2);
			text2.setText((author != null ? author : ""));
			String pubDate = new Date(feed.pubdate).toString();
			TextView text3 = (TextView) findViewById(R.id.feedrowtext3);
			text3.setText((feed.pubdate != 0 ? pubDate : ""));

			adapter = new FeedItemAdapter(this, items);
			setListAdapter(adapter);
		}
	}

	private void downloadAll(List<FeedItem> items) {
		try {
			for (FeedItem item : items) {
				if(!item.downloaded){
					downloadBinder.download(item.id, item.mp3uri,
							item.mp3file);
				}
			}
			Util.showToastShort(this, "Downloading all " + feed.title);
		} catch (RemoteException e) {
			Util.showToastShort(this, e.getMessage());
		}
	}

	private void infoItem(FeedItem item){
		if(item.mp3uri == null && !item.completed){
			item.completed = true;
			try {
				mDbHelper.updateFeedItem(item);
			} catch (DatabaseException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(Constants.FEEDITEM, item);
		startActivity(i);
	}

	private void queueItem(FeedItem item){
		boolean res = ACastUtil.queueItem(mediaBinder, item);
		if(res){
			Util.showToastShort(this, "Queued "+item.title);
		}else{
			Util.showToastShort(this, "Failed to queue "+item.title);
		}
	}

	private void downloadItem(FeedItem item){
		String file = item.mp3file;
		if(file == null || item.mp3uri == null){
			Util.showToastShort(this, "Feed item has no audio to download!");
			return;
		}else if(item.downloaded){
			Util.showToastShort(this, "Already downloaded: "+file);
			return;
		}else if(downloadBinder == null){
			Log.e(TAG, "binder is null. No connection to download service!");
			return;
		}
		
		String srcuri = item.mp3uri.replace(' ', '+');
		try {
			downloadBinder.download(item.id, srcuri, item.mp3file);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}

		Util.showToastShort(this, "Download added to download queue!");
	}

	private void deleteItem(final FeedItem item){
		if(item != null && item.mp3file != null){
			Util.showConfirmationDialog(this, "Are you sure?", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new File(item.mp3file).delete();
					item.downloaded = false;
					try {
						mDbHelper.updateFeedItem(item);
					} catch (DatabaseException e) {
						Log.e(TAG, e.getMessage(), e);
					}
					populateFields();
				}
			});
		}
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		if(downloadBinder != null){
			unbindService(downloadServiceConn);
		}
		if(mediaBinder != null){
			unbindService(mediaServiceConn);
		}
		if(updateBinder != null){
			unbindService(updateServiceConn);
		}
		super.onDestroy();
	}


	private final IDownloadServiceCallback downloadCallback = new IDownloadServiceCallback.Stub() {
		@Override
		public void onCompleted(final long externalid) throws RemoteException {
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					populateFields();
			        setProgressBarIndeterminateVisibility(false);
			        Util.showToastShort(FeedItemList.this, "Downloaded");
				}
			});
		}
		@Override
		public void onException(long externalid, final String exception) throws RemoteException {
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					populateFields();
			        setProgressBarIndeterminateVisibility(false);
			        Util.showToastShort(FeedItemList.this, "Download failed: "+exception);
				}
			});
		}
		@Override
		public void onProgress(long externalid, long diff) throws RemoteException {
		}
	};

	private final IUpdateServiceCallback updateCallback = new IUpdateServiceCallback.Stub() {
		@Override
		public void onUpdateAllCompleted() throws RemoteException {}
		@Override
		public void onUpdateItemCompleted(final String title) throws RemoteException {
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					populateFields();
			        setProgressBarIndeterminateVisibility(false);
				}
			});
		}
		@Override
		public void onUpdateItemException(String title, final String error)
				throws RemoteException {
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					populateFields();
			        setProgressBarIndeterminateVisibility(false);
				}
			});
		}
	};

	private static class FeedItemAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private List<FeedItem> items;

		public FeedItemAdapter(Context cxt, List<FeedItem> items){
			mInflater = LayoutInflater.from(cxt);
			this.items = items;
		}
		@Override
		public int getCount() {
			return items.size();
		}
		@Override
		public FeedItem getItem(int position) {
			if(position == -1){
				return null;
			}
			return items.get(position);
		}
		@Override
		public long getItemId(int position) {
			if(position == -1){
				return -1;
			}
			return getItem(position).id;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.feeditem_row, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.feedrowicon);
                holder.text = (TextView) convertView.findViewById(R.id.feedrowtext);
                holder.text2 = (TextView) convertView.findViewById(R.id.feedrowtext2);
                holder.text3 = (TextView) convertView.findViewById(R.id.feedrowtext3);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            FeedItem item = items.get(position);
            holder.icon.setImageResource(ACastUtil.getStatusIcon(item));
            holder.text.setText(item.title);
            String author = item.author;
            holder.text2.setText((author != null ? author : ""));
	        String pubDate = new Date(item.pubdate).toString();
	        holder.text3.setText((item.pubdate != 0 ? pubDate : ""));

            return convertView;
		}

		private static class ViewHolder {
	        ImageView icon;
	        TextView text;
	        TextView text2;
	        TextView text3;
	    }

	}

}
