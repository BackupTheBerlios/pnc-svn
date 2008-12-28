package com.mathias.android.acast;

import java.io.File;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;
import com.mathias.android.acast.rss.RssUtil;

public class ACast extends ListActivity {

	private static final String TAG = ACast.class.getSimpleName();

	public static final String KEY = "key";
	public static final String FEED = "feed";
	public static final String FEEDITEM = "feeditem";

	private static final int INSERT_ID = Menu.FIRST + 0;
	private static final int UPDATE_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;
	private static final int INFO_ID = Menu.FIRST + 4;
	private static final int DOWNLOADALL_ID = Menu.FIRST + 5;
	private static final int DOWNLOADMANAGER_ID = Menu.FIRST + 6;
	private static final int SETTINGS_ID = Menu.FIRST + 7;

	private ACastDbAdapter mDbHelper;

	private FeedAdapter adapter;
	
	private Settings settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_list);
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		fillData();

		ImageButton resume = (ImageButton) findViewById(R.id.resume);
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
		List<Feed> feeds = mDbHelper.fetchAllFeeds();
		adapter = new FeedAdapter(this, feeds);
		setListAdapter(adapter);

		settings = mDbHelper.fetchSettings();
		if(settings != null && settings.getLastFeedItemId() != null){
			TextView resumetitle = (TextView) findViewById(R.id.resumetitle);
			FeedItem item = mDbHelper.fetchFeedItem(settings.getLastFeedItemId());
			if(item != null){
				resumetitle.setText(item.getTitle());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.addfeed);
		item.setIcon(android.R.drawable.ic_menu_add);
		//item = menu.add(Menu.NONE, UPDATE_ID, Menu.NONE, R.string.editfeed);
		//item.setIcon(android.R.drawable.ic_input_edit);
		item = menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.removefeed);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refreshfeeds);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		item = menu.add(Menu.NONE, DOWNLOADALL_ID, Menu.NONE, R.string.downloadall);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, DOWNLOADMANAGER_ID, Menu.NONE, R.string.downloadmanager);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, R.string.settings);
		item.setIcon(android.R.drawable.stat_sys_download);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, FeedItemList.class);
		i.putExtra(KEY, adapter.getItemId(position));
		startActivityForResult(i, 0);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int pos = getSelectedItemPosition();
		if(INSERT_ID == item.getItemId()){
			createFeed();
			return true;
		}else if(UPDATE_ID == item.getItemId()){
			if(pos >= 0){
				editFeed(adapter.getItemId(pos));
			}
			return true;
		}else if(DELETE_ID == item.getItemId()){
			if(pos >= 0){
				deleteFeed(adapter.getItem(pos));
			}
			return true;
		}else if(REFRESH_ID == item.getItemId()){
			new Thread(){
				@Override
				public void run() {
					refreshFeeds();
					handler.sendEmptyMessage(0);
				}
			}.start();
			return true;
		}else if(INFO_ID == item.getItemId()){
			if(pos >= 0){
				infoFeed(adapter.getItem(pos));
			}
			return true;
		}else if(DOWNLOADALL_ID == item.getItemId()){
			if(pos >= 0){
				downloadAll(adapter.getItem(pos));
			}
			return true;
		}else if(DOWNLOADMANAGER_ID == item.getItemId()){
			showDownloadManager();
			return true;
		}else if(SETTINGS_ID == item.getItemId()){
			showSettings();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			fillData();
		}
	};

	private void showSettings() {
		Intent i = new Intent(this, SettingsEdit.class);
		startActivityForResult(i, 0);
	}

	private void showDownloadManager() {
		Intent i = new Intent(this, DownloadList.class);
		startActivityForResult(i, 0);
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

	private void deleteFeed(Feed feed) {
		for(FeedItem item : feed.getItems()){
			boolean deleted = new File(item.getMp3file()).delete();
			Log.d(TAG, "File deleted="+deleted+" file="+item.getMp3file());
		}
		mDbHelper.deleteFeed(feed.getId());
		fillData();
	}

	private void refreshFeeds(){
		List<Feed> feeds = mDbHelper.fetchAllFeeds();
		for (Feed feed : feeds) {
			try {
				long rowId = feed.getId();
				String uri = feed.getUri();
				feed = new RssUtil().parse(uri);
				mDbHelper.updateFeed(rowId, feed);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				Util.showDialog(this, e.getMessage());
			}
		}
	}

	private void infoFeed(Feed feed) {
		Intent i = new Intent(this, FeedInfo.class);
		i.putExtra(ACast.FEED, feed);
		startActivityForResult(i, 0);
	}

	private void downloadAll(Feed feed) {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		boolean connected = info != null && info.getSSID() != null;
		if(!settings.isOnlyWifiDownload() || connected){
			Intent i = new Intent(this, DownloadList.class);
			i.putExtra(ACast.FEED, feed);
			startActivityForResult(i, 0);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillData();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}

	private class FeedAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private List<Feed> feeds;
		private Bitmap defaultIcon;
		
		public FeedAdapter(Context cxt, List<Feed> feeds){
			this.feeds = feeds;
			mInflater = LayoutInflater.from(cxt);
			defaultIcon = BitmapFactory.decodeResource(cxt.getResources(),
					R.drawable.question);
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
                convertView = mInflater.inflate(R.layout.feed_row, null);

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
            
            String icon = feeds.get(position).getIcon();
            holder.icon.setImageBitmap(icon != null ? BitmapFactory
					.decodeFile(icon) : defaultIcon);
            holder.text.setText(feeds.get(position).getTitle());
            String author = feeds.get(position).getAuthor();
            holder.text2.setText((author != null ? author : ""));

//    		OnLongClickListener listener = new OnLongClickListener(){
//    			@Override
//    			public boolean onLongClick(View v) {
//    				ACast.this.openOptionsMenu();
//    				return true;
//    			}
//    		};
//    		parent.setOnLongClickListener(listener);

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
			return feeds.get(position).getId();
		}

	}

    private static class ViewHolder {
        ImageView icon;
        TextView text;
        TextView text2;
    }

}
