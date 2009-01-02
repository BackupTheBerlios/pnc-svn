//TODO 5: sort on pubDate, title, ... (settings)
//TODO 6: Option menu should be for actions which affect all feeds/items. LongPress (ContectMenu?) should affect single items
//TODO 6: Increase size of feed/feeditem list rows
//TODO 5: dont show/send all download progress msgs
//TODO 5: Download estimated time
//TODO 6: Progress dialog should show title instead of url
//TODO 5: Add indeterminate status progress bar during play init
//TODO 6: Bug: audio notification triggered player does not stop audio
//TODO 6: Bug: info for feed item is not shown when opening player with notifiction
//TODO 6: Download queue not visible after close. How to go to file download page? Main page menu item.
//TODO 6: Remove from queue
//TODO 6: Download complete notification, whole queue or each download?
//TODO 6: Queue system
//TODO 6: list all downloaded together, let user play from list
//TODO 5: Longpress to open menu for feed/feeditem
//TODO 5: Bug: show error when feed can not be added
//TODO 5: Bug: Downloads can not handle screen timeout, when screen locks audio stops
//TODO 5: ContentProvider, MIME (text/xml) association, should trigger intent for acast. Update android manifest.
//      <intent-filter>
//      	<action android:name="android.intent.action.VIEW" />
//      	<action android:name="android.intent.action.BROWSABLE" />
//      	<category android:name="android.intent.category.DEFAULT" />
//      	<data android:mimeType="audio/x-scpls" />
//      	<data android:mimeType="audio/x-mpegurl" />
//      </intent-filter>
//TODO 5: What happens if feed already exist? Show dialog...
//TODO 5: Show activity name in title
//TODO 5: Settings dialog: 
//       auto delete, 
//       auto refresh all feeds at spec time (hourly, daily, week, month), 
//       auto download. 
//       Only download through wifi. 
//       Resume partly downloaded. 
//       Auto delete after playing
//       Auto-play next unplayed Episode when an Episode has finished playing.
//TODO 5: Other podcatchers: Dogg Catcher, NPE, Podcaster?, PodWeasel
//TODO 5: On multidownload, press (drag?) to activate/setcurrent download of podcast.
//TODO 5: Pause when incoming call, resume afterwards. Pause playback when a call is initiated. 
//TODO 5: Support video
//TODO 5: Pause/play with button on headset
//TODO 5: Show partial downloads
//TODO 5: System tool to prevent phone from sleeping?
//TODO 5: Show if streaming when playing
//TODO 5: Use toast more. Toast.makeText().show()!!
//TODO 5: Show if new podcasts are available in feed list.
//TODO 5: Show if any podcasts is ongoing in feed list.
//TODO 5: Multiple Player activites, singleinstance? (launchMode="singleInstance" in the manifest file)
//TODO 5: Show new items that are newer then old update time.
//TODO 5: Ability to exit the application
//TODO 5: Error by notification from services, download/media
//TODO 5: Text size for feed/feeditem lists
//TODO 5: Low: Bug: mediaplayer prepare can not handle streaming mp3 url with redirect, http://podtrac.com/pts/redirect.mp3?http://www.48days.bullakaclients.com/12-17-08.mp3  
//TODO 5: Low: Show info on right side of screen during landscape mode?
//TODO 5: Low: Bug: MediaPlayer thread is started and never really released?
//TODO 5: Low: rotate with gravity or keyboard?
//TODO 5: Low: All Download progress screen. Queue downloads, show download queue and current progress. notification on completion/queue empty
//TODO 5: Low: show kb/s for download progress window
//TODO 5: Low: Username/password protected RSS
//TODO 5: Low: Use default media player with broadcast sender.
// Loading on refresh all feeds, show status bar processing circle
// Remove information from player, can be shown with menu->information.
// remove feed/feeditem confirmation
// Import through OPML
// Select/Search podcasts from podgrove.com, podcastalley.com?, digitalpodcast.com
// Play/Pause button, Bigger buttons, black and white? Use styled ic_media_play/pause?
// Refresh feed should not clear -downloads-/bookmark/completed
// Show file size and actual file size
// Thread RSS parsing and html get to awoid wait/force kill dialog.
// When lock screen and enable screen, Player controls are not working. 
// LOW: Download all feed items in sequence (if not already downloaded)
// Change notification icon to main(RSS) icon (to same as main icon?)
// Add icon to menu items
// On remove feed, remove physical files
// Pause mp on start to avoid short sound before first setPosition
// Bigger letters in feed list.
// Bug: Fix threaded mediaplayer bug which does not stop playback sometimes. 
// Individual bookmark
// Illegal character in path at index 37: http://192.168.254.125/audio/music/14 - Nowhere.mp3
// fetch size from rss xml
// show duration, seekbar duration update
// use file size with download progress bar
// use duration with player progress bar
// volume slider? No, use hard volume controller
// Show in (not)downloaded icons if feed item has ever completed
// http://192.168.254.125/mp3.xml does not work. mount -o bind problem...
// show if downloaded with icon
// Show in (not)downloaded icons if feed item has bookmark or not
// Player, press home, start acast = problem. Prob because of continuing duration progress thread
// Error when selecting add feed textview and switching to landscape mode
// resume last item button, show title of feed item
// store mp3s on sdcard
// escape chars in title before creating file name
// play in background, show notification in tray. Service, NotificationManager
// Use NotificationManager to be able to go back to current playing feed item
// (not used) AlarmManager, Context.getSystemService(Context.ALARM_SERVICE)
// Remove notification on pause and stop
// Draw main icon
// End service and remove notification when song is complete
// Fetch icon from RSS and present in feed list 
// Bug: Player, change screen mode, media restarts from last save point
// create /sdcard/<feed> dirs for mp3s
// Feed and feeditem extended information Dialog (or show everything in listitem)
// Increase size of start icon...
// Edit feed does not work. Remove?
// detailMessage	"Only the original thread that created a view hierarchy can touch its views." (id=830057026112); use handler
// E/MediaPlayer(  277): Attempt to call getDuration without a valid mediaplayer

/** Description
 * It is also possible to use ACast as a RSS feed reader.
 */

package com.mathias.android.acast;

import java.io.File;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.acast.common.RssUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.download.DownloadService;
import com.mathias.android.acast.common.services.download.IDownloadService;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class ACast extends ListActivity implements ServiceConnection {

	private static final String TAG = ACast.class.getSimpleName();

	public static final String FEED = "feed";
	public static final String FEEDID = "feedid";
	public static final String FEEDITEM = "feeditem";
	public static final String FEEDITEMID = "feeditemid";

	public static final int NOTIFICATION_DOWNLOADSERVICE_ID = R.id.add;
	public static final int NOTIFICATION_MEDIASERVICE_ID = R.id.addresult;

	private static final int INSERT_ID = Menu.FIRST + 0;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int REFRESH_ID = Menu.FIRST + 2;
	private static final int INFO_ID = Menu.FIRST + 3;
	private static final int DOWNLOADALL_ID = Menu.FIRST + 4;
	private static final int DOWNLOADQUEUE_ID = Menu.FIRST + 5;
	private static final int DOWNLOADLIST_ID = Menu.FIRST + 6;
	private static final int SETTINGS_ID = Menu.FIRST + 7;

	private ACastDbAdapter mDbHelper;

	private FeedAdapter adapter;
	
	private WorkerThread thread;
	
	private Settings settings;

	private Bitmap defaultIcon;

	private IDownloadService binder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.feed_list);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		thread = new WorkerThread();
		thread.start();

		defaultIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.question);

		getListView().setLongClickable(true);
		getListView().setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				Log.d(TAG, "onLongClick pressed!");
				return false;
			}
		});

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

		Intent i = new Intent(this, DownloadService.class);
		startService(i);
		if(!bindService(i, this, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start download service!");
		}
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
		item = menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.removefeed);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refreshall);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, INFO_ID, Menu.NONE, R.string.info);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		item = menu.add(Menu.NONE, DOWNLOADALL_ID, Menu.NONE, R.string.downloadall);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, DOWNLOADQUEUE_ID, Menu.NONE, R.string.downloadqueue);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, DOWNLOADLIST_ID, Menu.NONE, R.string.downloadlist);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, R.string.settings);
		item.setIcon(android.R.drawable.stat_sys_download);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, FeedItemList.class);
		i.putExtra(FEEDID, adapter.getItemId(position));
		startActivityForResult(i, 0);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(INSERT_ID == item.getItemId()){
			createFeed();
		}else if(REFRESH_ID == item.getItemId()){
			thread.refreshFeeds();
		}else if(DOWNLOADQUEUE_ID == item.getItemId()){
			Intent i = new Intent(this, DownloadQueueList.class);
			startActivityForResult(i, 0);
		}else if(DOWNLOADLIST_ID == item.getItemId()){
			Intent i = new Intent(this, DownloadedList.class);
			startActivityForResult(i, 0);
		}else if(SETTINGS_ID == item.getItemId()){
			showSettings();
		}else{
			// items which needs position
			int pos = getSelectedItemPosition();
			if(pos < 0){
				Util.showToastShort(this, "No item selected!");
			}else if(DOWNLOADALL_ID == item.getItemId()){
				downloadAll(adapter.getItem(pos));
			}else if(INFO_ID == item.getItemId()){
				infoFeed(adapter.getItem(pos));
			}else if(DELETE_ID == item.getItemId()){
				deleteFeed(adapter.getItem(pos));
			}
		}
		//return super.onMenuItemSelected(featureId, item);
		return true;
	}

	private class WorkerThread extends Thread {

		private Handler handler;

		private void refreshFeeds(){
	        setProgressBarIndeterminateVisibility(true);
			handler.sendEmptyMessage(0);
		}

		@Override
		public void run() {
			Looper.prepare();
			
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					try {
						if(msg.what == 0){
							List<Feed> feeds = mDbHelper.fetchAllFeeds();
							for (Feed feed : feeds) {
								long rowId = feed.getId();
								final String title = feed.getTitle();
								Log.d(TAG, "Parsing "+title);
								feed = new RssUtil().parse(feed.getUri());
								mDbHelper.updateFeed(rowId, feed);
							}
							Log.d(TAG, "Done");
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									fillData();
							        setProgressBarIndeterminateVisibility(false);
							        Util.showToastShort(ACast.this, "Feeds updated");
								}
							});
						}
					} catch (final Exception e) {
						Log.e(TAG, e.getMessage(), e);
						runOnUiThread(new Runnable(){
							@Override
							public void run() {
								Util.showDialog(ACast.this, e.getMessage());
						        setProgressBarIndeterminateVisibility(false);
							}
						});
					}
				}
			};
				
			Looper.loop();
		}
	}

	private void showSettings() {
		Intent i = new Intent(this, SettingsEdit.class);
		startActivityForResult(i, 0);
	}

	private void createFeed() {
		Intent i = new Intent(this, FeedAdd.class);
		startActivityForResult(i, 0);
	}

	private void deleteFeed(final Feed feed) {
		Util.showConfirmationDialog(this, "Are you sure?", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for(FeedItem item : feed.getItems()){
					boolean deleted = new File(item.getMp3file()).delete();
					Log.d(TAG, "File deleted="+deleted+" file="+item.getMp3file());
				}
				mDbHelper.deleteFeed(feed.getId());
				fillData();
			}
		});
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
			try {
				for (FeedItem item : feed.getItems()) {
					binder.download(item.getId(), item.getMp3uri(), item
							.getMp3file());
				}
				Util.showToastShort(this, "Downloading all "+feed.getTitle());
			} catch (RemoteException e) {
				Util.showToastShort(this, e.getMessage());
			}
		}else{
			Util.showToastShort(this, "Only Wifi download is allowed");
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
		if(binder != null){
			unbindService(this);
		}
		super.onDestroy();
	}

	private class FeedAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private List<Feed> feeds;
		
		public FeedAdapter(Context cxt, List<Feed> feeds){
			this.feeds = feeds;
			mInflater = LayoutInflater.from(cxt);
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
                holder.text3 = (TextView) convertView.findViewById(R.id.feedrowtext3);

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
            String pubDate = feeds.get(position).getPubdate();
            holder.text3.setText((pubDate != null ? pubDate : ""));

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
        TextView text3;
    }

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected: "+name);
		binder = IDownloadService.Stub.asInterface(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected: "+name);
		binder = null;
	}

}
