package com.mathias.android.acast;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadItem;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;

public class DownloadList extends ListActivity implements ServiceConnection {

	private static final String TAG = DownloadList.class.getSimpleName();

	private static final long UPDATE_DELAY = 3000;

	private static final int CANCEL_ID = Menu.FIRST;
	private static final int CANCELALL_ID = Menu.FIRST + 1;
	private static final int REFRESH_ID = Menu.FIRST + 2;
	private static final int INFO_ID = Menu.FIRST + 3;

	private ACastDbAdapter mDbHelper;

	private DownloadAdapter adapter;

	private Integer currPos;

	private IDownloadService binder;
	
	private Feed feed;
	
	private Map<Long, FeedItem> feeditems = new HashMap<Long, FeedItem>();
	
	private boolean visible = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_list);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		feed = (Feed) (savedInstanceState != null ? savedInstanceState
				.getSerializable(ACast.FEED) : null);
		if (feed == null) {
			Bundle extras = getIntent().getExtras();
			feed = (Feed) (extras != null ? extras.getSerializable(ACast.FEED)
					: null);
		}

		Intent i = new Intent(this, DownloadService.class);
		bindService(i, this, BIND_AUTO_CREATE);

		// start progress handler loop
		progressHandler.sendEmptyMessageDelayed(0, UPDATE_DELAY);
	}

	private Handler progressHandler = new Handler(){
		@Override
		public void handleMessage(Message not_used) {
			Log.d(TAG, "handleMessage! not_used="+not_used);
			if(binder != null){
				try {
					List downloads = binder.getDownloads();
					Log.d(TAG, "downloads.length="+downloads.size());
					for (Object object : downloads) {
						DownloadItem item = (DownloadItem) object;
						Log.d(TAG, item.toString());
					}
					adapter = new DownloadAdapter(DownloadList.this, downloads);
					setListAdapter(adapter);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
					Util.showDialog(DownloadList.this, e.getClass().getSimpleName(), e.getMessage());
				}
			}
			if(visible){
				sendEmptyMessageDelayed(0, UPDATE_DELAY);
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		visible = true;
		progressHandler.sendEmptyMessage(0);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		visible = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, R.string.cancel);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		item = menu.add(Menu.NONE, CANCELALL_ID, Menu.NONE, R.string.cancelall);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refresh);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		currPos = position;
		openOptionsMenu();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuitem) {
		int pos = getSelectedItemPosition();
		if(currPos != null){
			pos = currPos;
			currPos = null;
		}
		if(pos >= 0 && CANCEL_ID == menuitem.getItemId()){
			cancelAndRemove(adapter.getItemId(pos));
			return true;
		}else if(CANCELALL_ID == menuitem.getItemId()){
			cancelAndRemoveAll();
			return true;
		}else if(REFRESH_ID == menuitem.getItemId()){
			progressHandler.sendEmptyMessage(0);
			return true;
		}else if(pos >= 0 && INFO_ID == menuitem.getItemId()){
			DownloadItem item = adapter.getItem(pos);
			infoItem(feeditems.get(item.getExternalId()));
			return true;
		}
		return super.onMenuItemSelected(featureId, menuitem);
	}
	
	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(ACast.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private void cancelAndRemove(long externalId){
		try {
			binder.cancelAndRemove(externalId);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	private void cancelAndRemoveAll(){
		try {
			binder.cancelAndRemoveAll();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
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

			if(feed != null){
				Feed feed2 = mDbHelper.fetchFeed(feed.getId());
				for (FeedItem item : feed2.getItems()) {
					if(!new File(item.getMp3file()).exists()){
						feeditems.put(item.getId(), item);
						binder.download(item.getId(), item.getMp3uri(), item.getMp3file());
					}
				}
				feed = null;
			}
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
			Log.d(TAG, "onCompleted: externalid="+externalid);
			if(mDbHelper != null){
				mDbHelper.updateFeedItem(externalid, ACastDbAdapter.FEEDITEM_DOWNLOADED, true);
			}
			progressHandler.sendEmptyMessage(0);
		}
		@Override
		public void onException(long externalid, String exception) throws RemoteException {
			Log.d(TAG, "onException: externalid="+externalid+" exception="+exception);
			Util.showDialog(DownloadList.this, "Download failed: "+exception);
		}
		@Override
		public void onProgress(long externalid, long diff) throws RemoteException {
//			Log.d(TAG, "onProgress: externalid="+externalid+" diff="+diff);
		}
	};

	private class DownloadAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private List<DownloadItem> items;

		public DownloadAdapter(Context cxt, List<DownloadItem> items){
			this.items = items;
			mInflater = LayoutInflater.from(cxt);
		}
		public DownloadItem getByExternalId(long externalId){
			for (DownloadItem item : items) {
				if(externalId == item.getExternalId()){
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
		public DownloadItem getItem(int position) {
			return items.get(position);
		}
		@Override
		public long getItemId(int position) {
			return getItem(position).getExternalId();
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
                convertView = mInflater.inflate(R.layout.download_row, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.text2 = (TextView) convertView.findViewById(R.id.text2);
                holder.progressbar = (ProgressBar) convertView.findViewById(R.id.progressbar);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            Log.d(TAG, "getView; position="+position+" items="+items.size());
            DownloadItem downloadItem = items.get(position);
            FeedItem item = feeditems.get(downloadItem.getExternalId());
            if(item == null) {
            	return null;
            }
            holder.text.setText(item.getTitle());
            holder.text2.setText(item.getAuthor());
            holder.progressbar.setMax((int)item.getSize());
            holder.progressbar.setProgress((int)downloadItem.getProgress());

            boolean downloaded = downloadItem.getProgress() >= item.getSize();
			if(downloaded){
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

            return convertView;
		}

	}

    private static class ViewHolder {
        ImageView icon;
        TextView text;
        TextView text2;
        ProgressBar progressbar;
    }

}
