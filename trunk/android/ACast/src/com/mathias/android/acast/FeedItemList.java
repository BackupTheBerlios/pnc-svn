package com.mathias.android.acast;

import java.io.File;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.acast.common.RssUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class FeedItemList extends ListActivity implements ServiceConnection {

	private static final String TAG = FeedItemList.class.getSimpleName();

	private static final int PLAY_ID = Menu.FIRST;
	private static final int DOWNLOAD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;
	private static final int INFO_ID = Menu.FIRST + 4;

	private Long mFeedId;

	private ACastDbAdapter mDbHelper;
	
	private FeedItemAdapter adapter;
	
	private ProgressDialog pd;
	
//	private Integer currPos;

	private IDownloadService binder;
	
	private Settings settings;
	
	private Bitmap defaultIcon;

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
		settings = mDbHelper.fetchSettings();
		if(settings == null){
			settings = new Settings(0, mFeedId, (long)0);
		}

		defaultIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.question);

		populateFields();
	}

	private void populateFields() {
		if (mFeedId != null && mDbHelper != null) {
			Feed feed = mDbHelper.fetchFeed(mFeedId);

	        String iconStr = feed.getIcon();
	        ImageView icon = (ImageView) findViewById(R.id.feedrowicon);
	        icon.setImageBitmap(iconStr != null ? BitmapFactory
					.decodeFile(iconStr) : defaultIcon);
	        TextView text = (TextView) findViewById(R.id.feedrowtext);
	        text.setText(feed.getTitle());
	        String author = feed.getAuthor();
	        TextView text2 = (TextView) findViewById(R.id.feedrowtext2);
	        text2.setText((author != null ? author : ""));

	        adapter = new FeedItemAdapter(this, feed.getItems());
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
		MenuItem item = menu.add(0, PLAY_ID, 0, R.string.playitem);
		item.setIcon(android.R.drawable.ic_media_play);
		item = menu.add(0, DOWNLOAD_ID, 0, R.string.downloaditem);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(0, DELETE_ID, 0, R.string.deleteitem);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item = menu.add(0, REFRESH_ID, 0, R.string.refreshfeed);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(0, INFO_ID, 0, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
//		currPos = position;
//		openOptionsMenu();
		playItem(adapter.getItem(position));
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuitem) {
		int pos = getSelectedItemPosition();
//		if(currPos != null){
//			pos = currPos;
//			currPos = null;
//		}
		if(pos >= 0 && PLAY_ID == menuitem.getItemId()){
			FeedItem item = adapter.getItem(pos);
			playItem(item);
			return true;
		}else if(pos >= 0 && DOWNLOAD_ID == menuitem.getItemId()){
			FeedItem item = adapter.getItem(pos);
			downloadItem(item);
			return true;
		}else if(pos >= 0 && DELETE_ID == menuitem.getItemId()){
			FeedItem item = adapter.getItem(pos);
			deleteItem(item);
			return true;
		}else if(REFRESH_ID == menuitem.getItemId()){
			refreshFeed();
			return true;
		}else if(INFO_ID == menuitem.getItemId()){
			FeedItem item = adapter.getItem(pos);
			infoItem(item);
			return true;
		}
		return super.onMenuItemSelected(featureId, menuitem);
	}

	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(ACast.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private void playItem(FeedItem item){
		settings.setLastFeedItemId(item.getId());
		mDbHelper.updateSettings(settings);
		Intent i = new Intent(this, Player.class);
		i.putExtra(ACast.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private void downloadItem(FeedItem item){
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
						binder.cancelAndRemoveCurrent();
					} catch (RemoteException e) {
					}
				}
			}
		});
		pd.setMax((int)item.getSize());
		pd.show();
	}

	private void deleteItem(final FeedItem item){
		if(item != null && item.getMp3file() != null){
			Util.showConfirmationDialog(this, "Are you sure?", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new File(item.getMp3file()).delete();
					item.setDownloaded(false);
					mDbHelper.updateFeedItem(item);
					populateFields();
				}
			});
		}
	}

	private void refreshFeed(){
		Feed feed = mDbHelper.fetchFeed(mFeedId);
		try {
			feed = new RssUtil().parse(feed.getUri());
			mDbHelper.updateFeed(mFeedId, feed);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showDialog(this, e.getMessage());
		}
		populateFields();
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		if(binder != null){
			unbindService(this);
		}
		super.onDestroy();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+name);
		binder = IDownloadService.Stub.asInterface(service);
		try {
			binder.registerCallback(downloadCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected: "+name);
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
			if(mDbHelper != null){
				mDbHelper.updateFeedItem(externalid, ACastDbAdapter.FEEDITEM_DOWNLOADED, true);
			}
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

	private static class FeedItemAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private List<FeedItem> items;

		public FeedItemAdapter(Context cxt, List<FeedItem> items){
			this.items = items;
			mInflater = LayoutInflater.from(cxt);
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
			return getItem(position).getId();
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

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.

            FeedItem item = items.get(position);
			if(item.isDownloaded()){
				if(item.isCompleted()){
					if(item.getBookmark() > 0){
			            holder.icon.setImageResource(R.drawable.downloaded_done_bm);
					}else{
						holder.icon.setImageResource(R.drawable.downloaded_done);
					}
				}else{
					if(item.getBookmark() > 0){
						holder.icon.setImageResource(R.drawable.downloaded_bm);
					}else{
						holder.icon.setImageResource(R.drawable.downloaded);
					}
				}
			}else{
				if(item.isCompleted()){
					if(item.getBookmark() > 0){
						holder.icon.setImageResource(R.drawable.notdownloaded_done_bm);
					}else{
						holder.icon.setImageResource(R.drawable.notdownloaded_done);
					}
				}else{
					if(item.getBookmark() > 0){
						holder.icon.setImageResource(R.drawable.notdownloaded_bm);
					}else{
						holder.icon.setImageResource(R.drawable.notdownloaded);
					}
				}
			}

            holder.text.setText(item.getTitle());
            String author = item.getAuthor();
            holder.text2.setText((author != null ? author : ""));

            return convertView;
		}

		private static class ViewHolder {
	        ImageView icon;
	        TextView text;
	        TextView text2;
	    }

	}

}
