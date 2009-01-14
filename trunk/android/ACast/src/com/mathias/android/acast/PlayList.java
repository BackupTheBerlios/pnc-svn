package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.mathias.android.acast.adapter.DetailFeedItemAdapter;
import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.media.IMediaService;
import com.mathias.android.acast.common.services.media.IMediaServiceCallback;
import com.mathias.android.acast.common.services.media.MediaService;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class PlayList extends ListActivity implements ServiceConnection {

	private static final long UPDATE_DELAY = 1000;

	private static final String TAG = PlayList.class.getSimpleName();

	private static final int CANCELALL_ID = Menu.FIRST + 0;
	private static final int REFRESH_ID = Menu.FIRST + 1;

	private static final int PLAY_ID = Menu.FIRST + 2;
	private static final int INFO_ID = Menu.FIRST + 3;
	private static final int REMOVE_ID = Menu.FIRST + 4;
	private static final int CANCEL_ID = Menu.FIRST + 5;

	private ACastDbAdapter mDbHelper;

	private DetailFeedItemAdapter adapter;

	private IMediaService mediaBinder;

	private WorkerThread thread;

	private boolean tracking = false;
	
	private ViewHolder holder = new ViewHolder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "PlayList", R.layout.play_list);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		thread = new WorkerThread();
		thread.start();

		Intent i = new Intent(this, MediaService.class);
		startService(i);
		if(!bindService(i, this, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start media!");
		}

		holder.feedicon = (ImageView) findViewById(R.id.icon);
		holder.title = (TextView) findViewById(R.id.title);
		holder.author = (TextView) findViewById(R.id.author);
		holder.seekbar = (SeekBar) findViewById(R.id.seekbar);
		holder.seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				tracking = true;
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				tracking = false;
				try{
					if(mediaBinder != null){
						mediaBinder.setCurrentPosition(seekBar.getProgress());
					}
				}catch(Exception e){
					Log.e(TAG, e.getMessage(), e);
					Util.showToastShort(PlayList.this, e.getMessage());
				}
			}
		});
		holder.playpause = (ImageButton)findViewById(R.id.playpause);
		holder.playpause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					if(mediaBinder != null){
						if(mediaBinder.isPlaying()){
							mediaBinder.pause();
							holder.playpause
									.setImageResource(android.R.drawable.ic_media_play);
						}else{
							mediaBinder.play();
							holder.playpause
									.setImageResource(android.R.drawable.ic_media_pause);
						}
					}
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		});
		holder.next = (ImageButton)findViewById(R.id.next);
		holder.next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					if(mediaBinder != null){
						mediaBinder.next();
					}
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		});

		getListView().setOnCreateContextMenuListener(this);

		// start progress update loop
		thread.startProgressUpdate();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		try {
			mediaBinder.initItem(adapter.getItemId(position));
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
			Util.showToastShort(this, e.getMessage());
		}
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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuItem item = menu.add(Menu.NONE, PLAY_ID, Menu.NONE, R.string.play);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		item = menu.add(Menu.NONE, REMOVE_ID, Menu.NONE, R.string.remove);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		item = menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, R.string.cancel);
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuid = item.getItemId();
		int listpos = menuInfo.position;
		if(listpos != ListView.INVALID_POSITION){
			if(PLAY_ID == menuid){
				infoItem(adapter.getItem(listpos));
				return true;
			}else if(INFO_ID == menuid){
				infoItem(adapter.getItem(listpos));
				return true;
			}else if(REMOVE_ID == menuid){
				removeFromQueue(adapter.getItemId(listpos));
				return true;
			}else if(CANCEL_ID == menuid){
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuitem) {
		//Options menu
		int menuid = menuitem.getItemId();
		if(CANCELALL_ID == menuid){
			cancelAndRemoveAll();
			return true;
		}else if(REFRESH_ID == menuid){
			thread.populateView();
			return true;
		}
		return super.onMenuItemSelected(featureId, menuitem);
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
	protected void onResume() {
		super.onResume();
		thread.populateView();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+name);
		mediaBinder = IMediaService.Stub.asInterface(service);
		try {
			mediaBinder.registerCallback(mediaCallback);
			thread.populateView();
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
		public void onPlaylistCompleted() throws RemoteException {
		}
		@Override
		public void onTrackCompleted() throws RemoteException {
			Log.d(TAG, "onTrackCompleted()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					thread.populateView();
				}
			});
		}
	};
	
	private class WorkerThread extends Thread {

		private Handler handler;
		
		public void startProgressUpdate(){
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					try {
						int pos;
						if(!tracking && mediaBinder != null){
							pos = mediaBinder.getCurrentPosition();
							holder.seekbar.setProgress(pos);
						}else{
							pos = holder.seekbar.getProgress();
						}
//						String dur = Util.convertDuration(pos);
//						if(mediaBinder != null){
//							dur += "/"+Util.convertDuration(itemDuration);
//							duration.setText(dur);
//						}
					}catch(Exception e){
						Log.e(TAG, e.getMessage(), e);
						Util.showToastShort(PlayList.this, "Error: "+e.getMessage());
					}
					handler.postDelayed(this, UPDATE_DELAY);
				}
			}, UPDATE_DELAY);
		}

		public void populateView(){
			handler.post(new Runnable(){
				@Override
				public void run() {
					Log.d(TAG, "populateView()");
					try {
						if(mediaBinder != null){
							FeedItem item = null;
							if(mDbHelper != null){
								long id = mediaBinder.getId();
								if(id < 0){
									String lastid = mDbHelper
											.getSetting(Settings.LASTFEEDITEMID);
									if(lastid != null){
										id = Long.parseLong(lastid);
									}
								}
								item = mDbHelper.fetchFeedItem(id);
							}
							if(item != null){
								int dur = mediaBinder.getDuration();
								if(dur > 0){
									holder.seekbar.setMax(dur);
								}
								final boolean playing = mediaBinder.isPlaying();
								final FeedItem currItem = item;
								List<Long> queue = mediaBinder.getQueue();
								final List<FeedItem> items = new ArrayList<FeedItem>(queue.size());
								for (Long itemid : queue) {
									items.add(mDbHelper.fetchFeedItem(itemid));
								}
								Log.d(TAG, "items.length="+items.size());
								runOnUiThread(new Runnable(){
									@Override
									public void run() {
										holder.feedicon.setImageBitmap(BitmapCache
												.instance().get(currItem.id, mDbHelper));
										holder.title.setText(currItem.title);
										holder.author.setText(currItem.author);
										if (playing) {
											holder.playpause
													.setImageResource(android.R.drawable.ic_media_pause);
										} else {
											holder.playpause
													.setImageResource(android.R.drawable.ic_media_play);
										}
										adapter = new DetailFeedItemAdapter(PlayList.this, mDbHelper, items);
										setListAdapter(adapter);
									}
								});
							}
						}else{
							Log.w(TAG, "binder is null");
						}
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						Util.showToastShort(PlayList.this, e.getMessage());
					}
				}
			});
		}
		@Override
		public void run() {
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
		}
	}
	
	private void removeFromQueue(long id){
		try {
			mediaBinder.clearQueueItem(id);
			thread.populateView();
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void infoItem(FeedItem item){
		Intent i = new Intent(this, FeedItemInfo.class);
		i.putExtra(Constants.FEEDITEM, item);
		startActivityForResult(i, 0);
	}

	private void cancelAndRemoveAll(){
		try {
			mediaBinder.clearQueue();
			thread.populateView();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public static class ViewHolder {
        ImageView feedicon;
        TextView title;
        TextView author;
    	SeekBar seekbar;
    	ImageButton playpause;
    	ImageButton next;
    }

}
