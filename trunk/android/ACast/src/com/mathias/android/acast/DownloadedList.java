package com.mathias.android.acast;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mathias.android.acast.adapter.DetailFeedItemAdapter;
import com.mathias.android.acast.common.ACastUtil;
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

	private static final int QUEUE_ID = Menu.FIRST + 1;
	private static final int INFO_ID = Menu.FIRST + 2;
	private static final int DELETE_ID = Menu.FIRST + 3;
	private static final int CANCEL_ID = Menu.FIRST + 4;

	private ACastDbAdapter mDbHelper;

	private DetailFeedItemAdapter adapter;

	private IMediaService mediaBinder;
	
	private IDownloadService downloadBinder;

	private ServiceConnection mediaServiceConn;

	private ServiceConnection downloadServiceConn;

	private List<FeedItem> downloads = new ArrayList<FeedItem>();

    private NotificationManager mNM;
    
    private long lastId = Constants.INVALID_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");

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
		lastId = (savedInstanceState != null ? savedInstanceState.getLong(
				Constants.FEEDITEMID, Constants.INVALID_ID)
				: Constants.INVALID_ID);

		populateList();
		
		getListView().setOnCreateContextMenuListener(this);

	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume: "+lastId);
		super.onResume();
		populateList();
		mNM.cancel(Constants.NOTIFICATION_DOWNLOADSERVICE_ID);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		lastId = id;
		FeedItem item = adapter.getItem(position);
		if(item.mp3uri == null){
			infoItem(item);
		}else{
			ACastUtil.playQueueItem(this, mediaBinder, item, downloads);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refresh);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuItem item = menu.add(Menu.NONE, QUEUE_ID, Menu.NONE, R.string.queueitem);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		item = menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.deleteitem);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item = menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, R.string.cancel);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int id = item.getItemId();
		int pos = menuInfo.position;
		lastId = adapter.getItemId(pos);
		if(pos != ListView.INVALID_POSITION){
			if(QUEUE_ID == id){
				ACastUtil.queueItem(mediaBinder, adapter.getItem(pos));
				return true;
			}else if(INFO_ID == id){
				infoItem(adapter.getItem(pos));
				return true;
			}else if(DELETE_ID == id){
				deleteItem(adapter.getItem(pos));
				return true;
			}else if(CANCEL_ID == id){
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuitem) {
		int id = menuitem.getItemId();
		if(REFRESH_ID == id){
			populateList();
			return true;
		}
		return super.onMenuItemSelected(featureId, menuitem);
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

	private void populateList() {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(mDbHelper != null) {
					downloads = mDbHelper.fetchDownloadedFeedItems();
					adapter = new DetailFeedItemAdapter(DownloadedList.this, mDbHelper, downloads);
					setListAdapter(adapter);
					
					if(lastId != Constants.INVALID_ID){
						int pos = 0;
						for(Iterator<FeedItem> it = downloads.iterator(); it.hasNext(); ){
							long itemId = it.next().id;
							if(lastId == itemId){
								getListView().setSelection(pos);
								break;
							}
							pos++;
						}
					}
				}
			}
		});
	}

	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(Constants.FEEDITEM, item);
		startActivity(i);
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
						Util.showToastShort(DownloadedList.this,
										"Could not update feed item " + item.title);
					}
					populateList();
				}
			});
		}
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

}
