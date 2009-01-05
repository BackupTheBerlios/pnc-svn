package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
import android.widget.TextView;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.common.services.media.IMediaServiceCallback;
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.podcast.FeedItem;

public class PlayList extends ListActivity implements ServiceConnection {

	private static final String TAG = PlayList.class.getSimpleName();

	private static final int CANCEL_ID = Menu.FIRST;
	private static final int CANCELALL_ID = Menu.FIRST + 1;
	private static final int REFRESH_ID = Menu.FIRST + 2;
	private static final int INFO_ID = Menu.FIRST + 3;

	private ACastDbAdapter mDbHelper;

	private MediaAdapter adapter;

	private Integer currPos;

	private IMediaService mediaBinder;
	
	private WorkerThread thread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "Play", R.layout.play_list);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		thread = new WorkerThread();
		thread.start();

		Intent i = new Intent(this, MediaService.class);
		startService(i);
		if(!bindService(i, this, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start media!");
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		currPos = position;
		openOptionsMenu();
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
			thread.populateAdapter();
			return true;
		}else if(pos >= 0 && INFO_ID == menuitem.getItemId()){
//			MediaItem item = adapter.getItem(pos);
//			FeedItem feedItem = mDbHelper.fetchFeedItem(item.getExternalId());
//			infoItem(feedItem);
			return true;
		}
		return super.onMenuItemSelected(featureId, menuitem);
	}
	
	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(Constants.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private void cancelAndRemove(long externalId){
//		try {
//			binder.cancelAndRemove(externalId);
//			populateList();
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage(), e);
//		}
	}
	
	private void cancelAndRemoveAll(){
//		try {
//			binder.cancelAndRemoveAll();
//			populateList();
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage(), e);
//		}
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		if(mediaBinder != null){
			unbindService(this);
		}
		super.onDestroy();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+name);
		mediaBinder = IMediaService.Stub.asInterface(service);
		try {
			thread.populateAdapter();
			mediaBinder.registerCallback(mediaCallback);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected: "+name);
		try {
			mediaBinder.unregisterCallback(mediaCallback);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		mediaBinder = null;
	}

	private final IMediaServiceCallback mediaCallback = new IMediaServiceCallback.Stub() {

		@Override
		public void onCompletion() throws RemoteException {
		}
	};
	
	private class WorkerThread extends Thread {
		private Handler handler;
		
		public void populateAdapter(){
			handler.post(new Runnable(){
				@Override
				public void run() {
					try {
						if(mediaBinder != null){
							List<Long> queue = mediaBinder.getQueue();
							final List<FeedItem> items = new ArrayList<FeedItem>(queue.size());
							for (Long itemid : queue) {
								FeedItem item = mDbHelper.fetchFeedItem(itemid);
								items.add(item);
							}
							Log.d(TAG, "items.length="+items.size());
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									adapter = new MediaAdapter(PlayList.this, items);
									setListAdapter(adapter);
								}
							});
						}
					} catch (RemoteException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			});
		}
		@Override
		public void run() {
			Looper.prepare();
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
				}
			};
			Looper.loop();
		}
	}

	private class MediaAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private List<FeedItem> items;

		public MediaAdapter(Context cxt, List<FeedItem> items){
			this.items = items;
			mInflater = LayoutInflater.from(cxt);
		}
		public FeedItem getByExternalId(long externalId){
			for (FeedItem item : items) {
				if(externalId == item.getId()){
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
                convertView = mInflater.inflate(R.layout.play_row, null);

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
            FeedItem item = items.get(position);
            if(item == null) {
            	return null;
            }
            holder.title.setText(item.getTitle());
            holder.author.setText(item.getAuthor());
			holder.icon.setImageResource(R.drawable.notdownloaded);

            return convertView;
		}

	}

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView author;
    }

}
