package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
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

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.podcast.FeedItem;

public class DownloadedList extends ListActivity {

	private static final String TAG = DownloadedList.class.getSimpleName();

	private static final int REFRESH_ID = Menu.FIRST + 0;
	private static final int INFO_ID = Menu.FIRST + 1;

	private ACastDbAdapter mDbHelper;

	private DownloadAdapter adapter;

	private IMediaService mediaBinder;
	
	private IDownloadService downloadBinder;

	private ServiceConnection mediaServiceConn;

	private ServiceConnection downloadServiceConn;

	private List<FeedItem> downloads = new ArrayList<FeedItem>();
	
	private Map<Long, Bitmap> iconCache = new HashMap<Long, Bitmap>();

    private NotificationManager mNM;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "Downloaded", R.layout.downloaded_list);

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		Intent i = new Intent(this, DownloadService.class);
		startService(i);
		downloadServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				downloadBinder = IDownloadService.Stub.asInterface(service);
				try {
					downloadBinder.registerCallback(downloadCallback);
				} catch (Exception e) {
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
			Util.showDialog(this, "Could not start download!");
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

		// start progress handler loop
		populateList();
	}

	private void populateList() {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(mDbHelper != null) {
					downloads = mDbHelper.fetchDownloadedFeedItems();
					adapter = new DownloadAdapter(DownloadedList.this, downloads);
					setListAdapter(adapter);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateList();
		mNM.cancel(Constants.NOTIFICATION_DOWNLOADSERVICE_ID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refresh);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		FeedItem item = adapter.getItem(position);
		if(item.mp3uri == null){
			infoItem(item);
		}else{
			playItem(item);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuitem) {
		int pos = getSelectedItemPosition();
		if(REFRESH_ID == menuitem.getItemId()){
			populateList();
		}else if(pos >= 0 && INFO_ID == menuitem.getItemId()){
			FeedItem feedItem = adapter.getItem(pos);
			infoItem(feedItem);
		}
		//return super.onMenuItemSelected(featureId, menuitem);
		return true;
	}
	
	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(Constants.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private void playItem(FeedItem item) {
		try {
			if (item != null && mediaBinder != null
					&& (!mediaBinder.isPlaying() || mediaBinder.getId() != item
							.id)) {
				ACastUtil.playItem(mediaBinder, item);
				ACastUtil.queueItems(mediaBinder, downloads, item.id);
			}else{
				Log.d(TAG, "isPlaying or mediaBinder == null");
			}
		} catch (Exception e) {
			String msg = e.getMessage();
			Log.e(TAG, (msg != null ? msg : e.toString()), e);
		}
		Intent i = new Intent(this, Player.class);
		startActivity(i);
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
		super.onDestroy();
	}

	private final IDownloadServiceCallback downloadCallback = new IDownloadServiceCallback.Stub() {
		@Override
		public void onCompleted(long externalid) throws RemoteException {
			Log.d(TAG, "onCompleted: externalid="+externalid);
			populateList();
		}
		@Override
		public void onException(long externalid, String exception) throws RemoteException {
			Log.d(TAG, "onException: externalid="+externalid+" exception="+exception);
			populateList();
		}
		@Override
		public void onProgress(long externalid, long diff) throws RemoteException {
		}
	};

	private class DownloadAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private List<FeedItem> items;

		public DownloadAdapter(Context cxt, List<FeedItem> items){
			Collections.sort(items, ACastUtil.FEEDITEM_BYDATE);
			this.items = items;
			mInflater = LayoutInflater.from(cxt);
		}
		public FeedItem getByExternalId(long externalId){
			for (FeedItem item : items) {
				if(externalId == item.id){
					return item;
				}
			}
			return null;
		}
		@Override
		public int getCount() {
			return items.size();
		}
		@Override
		public FeedItem getItem(int position) {
			return items.get(position);
		}
		@Override
		public long getItemId(int position) {
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
                //Log.d(TAG, "getView; convertView=null position="+position+" items="+items.size());

                convertView = mInflater.inflate(R.layout.downloaded_row, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.author = (TextView) convertView.findViewById(R.id.author);
                holder.pubdate = (TextView) convertView.findViewById(R.id.pubdate);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            FeedItem item = items.get(position);
            if(item == null) {
            	return null;
            }
            Bitmap bm = getIcon(item.feedId);
			holder.icon.setImageBitmap(bm);
            holder.title.setText(item.title);
            holder.author.setText(item.author);
            holder.pubdate.setText(new Date(item.pubdate).toString());

            return convertView;
		}

	}

	private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView author;
        TextView pubdate;
    }

    private Bitmap getIcon(long feedid) {
		Bitmap icon = iconCache.get(feedid);
		if (icon == null) {
			icon = BitmapCache.instance().get(mDbHelper.fetchFeedIcon(feedid));
			iconCache.put(feedid, icon);
		}
		return icon;
	}

}
