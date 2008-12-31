package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadItem;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;
import com.mathias.android.acast.podcast.FeedItem;

public class DownloadedList extends ListActivity implements ServiceConnection {

	private static final String TAG = DownloadedList.class.getSimpleName();

	private static final int REFRESH_ID = Menu.FIRST + 0;
	private static final int INFO_ID = Menu.FIRST + 1;

	private ACastDbAdapter mDbHelper;

	private DownloadAdapter adapter;

	private Integer currPos;

	private IDownloadService binder;

	private List<DownloadItem> downloads = new ArrayList<DownloadItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloaded_list);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();

		Intent i = new Intent(this, DownloadService.class);
		startService(i);
		if(!bindService(i, this, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start download!");
		}

		// start progress handler loop
		populateList();
	}

	private void populateList() {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				adapter = new DownloadAdapter(DownloadedList.this, downloads);
				setListAdapter(adapter);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateList();
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
		if(REFRESH_ID == menuitem.getItemId()){
			populateList();
		}else if(pos >= 0 && INFO_ID == menuitem.getItemId()){
			DownloadItem item = adapter.getItem(pos);
			FeedItem feedItem = mDbHelper.fetchFeedItem(item.getExternalId());
			infoItem(feedItem);
		}
		//return super.onMenuItemSelected(featureId, menuitem);
		return true;
	}
	
	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(ACast.FEEDITEM, item);
		startActivityForResult(i, 0);
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
                convertView = mInflater.inflate(R.layout.downloaded_row, null);

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
			FeedItem item = mDbHelper.fetchFeedItem(downloadItem.getExternalId());
            if(item == null) {
            	return null;
            }
            holder.title.setText(item.getTitle());
            holder.author.setText(item.getAuthor());
			holder.icon.setImageResource(R.drawable.downloaded);

            return convertView;
		}

	}

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView author;
    }

}
