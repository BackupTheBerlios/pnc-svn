package com.mathias.android.acast;

import java.util.List;

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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadItem;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;
import com.mathias.android.acast.podcast.FeedItem;

public class DownloadQueueList extends ListActivity implements ServiceConnection {

	private static final String TAG = DownloadQueueList.class.getSimpleName();

	private static final long UPDATE_DELAY = 3000;

	private static final int CANCELALL_ID = Menu.FIRST + 0;
	private static final int REFRESH_ID = Menu.FIRST + 1;

	private static final int CANCEL_ID = Menu.FIRST + 2;
	private static final int INFO_ID = Menu.FIRST + 3;

	private ACastDbAdapter mDbHelper;

	private DownloadAdapter adapter;

	private IDownloadService binder;

	private ViewHolder header = new ViewHolder();
	
	private boolean visible = false;
	
	private FeedItem currItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "Queue", R.layout.downloadqueue_list);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		Intent i = new Intent(this, DownloadService.class);
		startService(i);
		if(!bindService(i, this, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start download!");
		}
		
		header.icon = (ImageView) findViewById(R.id.icon);
		header.title = (TextView) findViewById(R.id.title);
		header.author = (TextView) findViewById(R.id.author);
		header.progress = (ProgressBar) findViewById(R.id.progressbar);

		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		visible = true;
		populateList();
		progressHandler.sendEmptyMessage(0);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		visible = false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuItem item = menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, R.string.cancel);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.NONE, CANCELALL_ID, Menu.NONE, R.string.cancelall);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refresh);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuid = item.getItemId();
		int listpos = menuInfo.position;
		if(listpos != ListView.INVALID_POSITION){
			if(INFO_ID == menuid){
				FeedItem fi = mDbHelper.fetchFeedItem(adapter
						.getItemId(listpos));
				infoItem(fi);
				return true;
			}else if(CANCEL_ID == menuid){
				cancelAndRemove(adapter.getItemId(listpos));
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuitem) {
		if(CANCELALL_ID == menuitem.getItemId()){
			cancelAndRemoveAll();
			return true;
		}else if(REFRESH_ID == menuitem.getItemId()){
			populateList();
			return true;
		}
		return super.onMenuItemSelected(featureId, menuitem);
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
	
	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(Constants.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private void cancelAndRemove(long externalId){
		try {
			binder.cancelAndRemove(externalId);
			populateList();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	private void cancelAndRemoveAll(){
		try {
			binder.cancelAndRemoveAll();
			populateList();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void populateList() {
		try{
			if(binder != null){
				final List<DownloadItem> downloads = binder.getDownloads();
				Log.d(TAG, "downloads.length="+downloads.size());
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						adapter = new DownloadAdapter(DownloadQueueList.this, downloads);
						setListAdapter(adapter);
					}
				});
			}else{
				Log.d(TAG, "populateList: binder is null");
			}
		}catch(RemoteException e){
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+name);
		binder = IDownloadService.Stub.asInterface(service);
		try {
			populateList();
			binder.registerCallback(downloadCallback);
		} catch (Exception e) {
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

//	private static class WorkerThread extends Thread {
//		private Handler handler;
//		@Override
//		public void run() {
//			Looper.prepare();
//			handler = new Handler(){
//				@Override
//				public void handleMessage(Message msg) {
//				}
//			};
//			Looper.loop();
//		}
//	}

	private Handler progressHandler = new Handler() {
		@Override
		public void handleMessage(Message not_used) {
			Log.d(TAG, "progressHandler...");
			if(binder != null && mDbHelper != null){
				try {
					long currId = binder.getCurrentDownload();
					if(currId == Constants.INVALID_ID) {
						currItem = null;
					}else if(currItem == null || currItem.id != currId){
						currItem = mDbHelper.fetchFeedItem(currId);
					}
					if(currItem != null){
						header.icon.setImageBitmap(BitmapCache.instance()
								.get(currItem.feedId, mDbHelper));
						header.title.setText(currItem.title);
						header.author.setText(currItem.author);
						header.progress.setMax((int)currItem.size);
						header.progress.setProgress((int)binder.getProgress());
					}else{
						header.icon.setImageResource(R.drawable.question);
						header.title.setText("No current download");
						header.author.setText("No current download");
						header.progress.setVisibility(View.GONE);
					}
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
					Util.showDialog(DownloadQueueList.this, e.getClass().getSimpleName(), e.getMessage());
				}
			}else{
				Log.d(TAG, "binder or dbhelper is null!!");
			}
			if(visible){
				sendEmptyMessageDelayed(0, UPDATE_DELAY);
			}
		}
	};

	private final IDownloadServiceCallback downloadCallback = new IDownloadServiceCallback.Stub() {
		@Override
		public void onCompleted(long externalid) throws RemoteException {
			Log.d(TAG, "onCompleted: externalid="+externalid);
			populateList();
		}
		@Override
		public void onException(long externalid, final String exception) throws RemoteException {
			Log.d(TAG, "onException: externalid="+externalid+" exception="+exception);
			populateList();
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
				if(externalId == item.externalId){
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
			return getItem(position).externalId;
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
                convertView = mInflater.inflate(R.layout.downloadqueue_row, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.author = (TextView) convertView.findViewById(R.id.author);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            Log.d(TAG, "getView; position="+position+" items="+items.size());
            DownloadItem downloadItem = items.get(position);
			FeedItem item = mDbHelper.fetchFeedItem(downloadItem.externalId);
            if(item == null) {
            	return null;
            }
            holder.title.setText(item.title);
            holder.author.setText(item.author);
			holder.icon.setImageResource(R.drawable.notdownloaded);

            return convertView;
		}

	}

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView author;
        ProgressBar progress;
    }

}
