package com.mathias.android.acast;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
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
	public static final String FEEDITEM = "feeditem";

	private static final int INSERT_ID = Menu.FIRST;
	private static final int UPDATE_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int REFRESH_ID = Menu.FIRST + 3;

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

		Button resume = (Button) findViewById(R.id.resume);
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
		List<Feed> feeds = mDbHelper.fetchAllFeedsLight();
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
		menu.add(0, INSERT_ID, 0, R.string.addfeed);
		menu.add(0, UPDATE_ID, 0, R.string.editfeed);
		menu.add(0, DELETE_ID, 0, R.string.removefeed);
		menu.add(0, REFRESH_ID, 0, R.string.refreshfeeds);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, FeedItemList.class);
		long feedId = adapter.getItemId(position);
		i.putExtra(KEY, feedId);
		startActivityForResult(i, 0);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(INSERT_ID == item.getItemId()){
			createFeed();
			return true;
		}else if(UPDATE_ID == item.getItemId()){
			int pos = getSelectedItemPosition();
			if(pos >= 0){
				long feedId = adapter.getItemId(pos);
				editFeed(feedId);
			}
			return true;
		}else if(DELETE_ID == item.getItemId()){
			int pos = getSelectedItemPosition();
			if(pos >= 0){
				long feedId = adapter.getItemId(pos);
				deleteFeed(feedId);
			}
			return true;
		}else if(REFRESH_ID == item.getItemId()){
			refreshFeeds();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
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

	private void deleteFeed(long id) {
		mDbHelper.deleteFeed(id);
		fillData();
	}

	private void refreshFeeds(){
		List<Feed> feeds = mDbHelper.fetchAllFeedsLight();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillData();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		mDbHelper.close();
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
                convertView = mInflater.inflate(/* R.layout.list_item_icon_text */ android.R.layout.activity_list_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(/*R.id.text*/ android.R.id.text1);
                holder.icon = (ImageView) convertView.findViewById(/*R.id.icon*/ android.R.id.icon);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            
            holder.text.setText(feeds.get(position).getTitle());
            String icon = feeds.get(position).getIcon();
            holder.icon.setImageBitmap(icon != null ? BitmapFactory
					.decodeFile(icon) : defaultIcon);

            return convertView;
		}
		
		@Override
		public int getCount() {
			return feeds.size();
		}

		@Override
		public Object getItem(int position) {
			return feeds.get(position);
		}

		@Override
		public long getItemId(int position) {
			return feeds.get(position).getId();
		}

	}

    private static class ViewHolder {
        TextView text;
        ImageView icon;
    }

}
