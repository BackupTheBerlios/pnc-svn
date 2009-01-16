package com.mathias.android.acast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.acast.common.DigitalPodcastUtil;
import com.mathias.android.acast.common.OpmlUtil;
import com.mathias.android.acast.common.PodGroveUtil;
import com.mathias.android.acast.common.PodcastAlleyUtil;
import com.mathias.android.acast.common.SearchItem;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.services.update.IUpdateService;
import com.mathias.android.acast.common.services.update.IUpdateServiceCallback;
import com.mathias.android.acast.common.services.update.UpdateService;
import com.mathias.android.acast.podcast.Feed;

public class FeedAdd extends ListActivity {

	private static final String TAG = FeedAdd.class.getSimpleName();
	
	private static final String OPMLLOCALFILE = File.separator + "sdcard"
			+ File.separator + "acast" + File.separator + "acast.opml";

	private static final int MENU_IMPORTLOCALOPML = 0;

	private static final int MENU_EXPORTLOCALOPML = 1;

    private ACastDbAdapter mDbHelper;
	
	private SearchItemAdapter adapter;

	private List<SearchItem> items = new ArrayList<SearchItem>();

    private WorkerThread thread;
    
	private IUpdateService updateBinder;

	private ServiceConnection updateServiceConn;
	
    private int selected = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.feedadd);
		
		setTitle("Add feed");

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		adapter = new SearchItemAdapter(this);
		setListAdapter(adapter);

		thread = new WorkerThread();
		thread.start();

		final EditText text = (EditText) findViewById(R.id.text);

		ImageButton add = (ImageButton) findViewById(R.id.add);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = text.getText().toString();
				if(!uri.startsWith("http://")) {
			        Util.showToastShort(FeedAdd.this, "You might want to add \'http://\' to RSS URL");
				}
				addFeed(uri);
			}
		});

		ImageButton searchpodgrove = (ImageButton) findViewById(R.id.searchpodgrove);
		searchpodgrove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchstr = text.getText().toString();
				thread.searchPodgrove(searchstr);
			}
		});

		ImageButton searchdigitalpodcast = (ImageButton) findViewById(R.id.searchdigitalpodcast);
		searchdigitalpodcast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchstr = text.getText().toString();
				thread.searchDigitalPodcast(searchstr);
			}
		});

		ImageButton top50podcastalley = (ImageButton) findViewById(R.id.top50podcastalley);
		top50podcastalley.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				thread.top50PodcastAlley();
			}
		});

		ImageButton importopml = (ImageButton) findViewById(R.id.importopml);
		importopml.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uristr = text.getText().toString();
				thread.importOpml(uristr);
			}
		});

		ImageButton search = (ImageButton) findViewById(R.id.findinresults);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String s = text.getText().toString().toLowerCase();
				if(selected >= items.size()){
					selected = 0;
				}
				for (int i = selected; i < items.size(); i++) {
					SearchItem item = items.get(i);
					if (item.getTitle().toLowerCase().contains(s)
							|| item.getUri().toLowerCase().contains(s)
							|| item.getDescription().toLowerCase().contains(s)) {
						setSelection(i);
						selected = i + 1;
						return;
					}
				}
				selected = 0;
				Util.showToastShort(FeedAdd.this, "Can't find the text: "+s);
			}
		});

		Intent i = new Intent(this, UpdateService.class);
		startService(i);
		updateServiceConn = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: "+name);
				updateBinder = IUpdateService.Stub.asInterface(service);
				try {
					updateBinder.registerCallback(updateCallback);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: "+name);
				updateBinder = null;
			}
		};
		if(!bindService(i, updateServiceConn, BIND_AUTO_CREATE)) {
			Util.showDialog(this, "Could not start update service!");
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final SearchItem item = (SearchItem) adapter.getItem(position);
		String msg = "Do you want to add "
			+ Util.fromHtmlNoImages(item.getTitle());
		Util.showConfirmationDialog(this, msg, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addFeed(item.getUri());
			}
		});
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		if(updateBinder != null){
			unbindService(updateServiceConn);
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item = menu.add(Menu.NONE, MENU_IMPORTLOCALOPML, Menu.NONE, R.string.importlocalopml);
		item.setIcon(android.R.drawable.stat_sys_download);
		item = menu.add(Menu.NONE, MENU_EXPORTLOCALOPML, Menu.NONE, R.string.exportlocalopml);
		item.setIcon(android.R.drawable.stat_sys_download);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(MENU_IMPORTLOCALOPML == item.getItemId()){
			thread.importLocalOpml();
		}else if(MENU_EXPORTLOCALOPML == item.getItemId()){
			thread.exportLocalOpml();
		}
		//return super.onOptionsItemSelected(item);
		return true;
	}
	
	private void addFeed(String url){
		try {
			updateBinder.addFeed(url);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void importAll(){
		for (SearchItem item : items) {
			addFeed(item.getUri());
		}
	}

	private class WorkerThread extends Thread {

		private Handler parsehandler;

		@Override
		public void run() {
			Looper.prepare();
			parsehandler = new Handler();
			Looper.loop();
		}

		public void searchPodgrove(final String searchstr){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.post(new Runnable(){
				@Override
				public void run() {
					items = PodGroveUtil.parse(searchstr);
					String resultstr = "PodGrove found "+items.size()+" results";
					Log.d(TAG, resultstr);
					hideProgessAndUpdateResultList(resultstr);
				}
			});
		}

		public void top50PodcastAlley(){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.post(new Runnable(){
				@Override
				public void run() {
					String resultstr = null;
					try {
						items = new PodcastAlleyUtil().parseTop50();
						Log.d(TAG, "PodcastAlley Top 50; "+items.size()+" results");
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						resultstr = e.getMessage();
					}
					hideProgessAndUpdateResultList(resultstr);
				}
			});
		}

		public void searchDigitalPodcast(final String searchstr){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.post(new Runnable(){
				@Override
				public void run() {
					items = DigitalPodcastUtil
							.parse(searchstr);
					String resultstr = "DigitalPodcast found "+items.size()+" results";
					Log.d(TAG, resultstr);
					hideProgessAndUpdateResultList(resultstr);
				}
			});
		}

		public void importOpml(final String uristr){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.post(new Runnable(){
				@Override
				public void run() {
					String resultstr = null;
					try {
						items = new OpmlUtil().parse(uristr);
						final String res = Util.buildString(
								"Import OPML found ", items.size(),
								" results. Import all items directly?");
						Log.d(TAG, res);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Util.showConfirmationDialog(FeedAdd.this, res, new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which) {
										importAll();
									}
								});
							}
						});
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						resultstr = e.getMessage();
					}
					hideProgessAndUpdateResultList(resultstr);
				}
			});
		}
		
		public void importLocalOpml(){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.post(new Runnable(){
				@Override
				public void run() {
					String resultstr = null;
					try {
						items = new OpmlUtil().parse(new File(OPMLLOCALFILE));
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String res = Util.buildString(
										"Import local OPML found ", items.size(),
										" results. Import all items directly?");
								Log.d(TAG, res);
								Util.showConfirmationDialog(FeedAdd.this, res, new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which) {
										importAll();
									}
								});
							}
						});
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						resultstr = e.getMessage();
					}
					hideProgessAndUpdateResultList(resultstr);
				}
			});
		}

		public void exportLocalOpml(){
			parsehandler.post(new Runnable(){
				@Override
				public void run() {
					Util.showConfirmationDialog(FeedAdd.this, "Are you sure?", new OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								OpmlUtil.Opml opml = new OpmlUtil.Opml(getString(R.string.app_name));
								List<Feed> feeds = mDbHelper.fetchAllFeeds();
								for (Feed feed : feeds) {
									opml.add(new OpmlUtil.OpmlItem(feed.title, feed.uri));
								}
								String export = OpmlUtil.exportOpml(opml);
								new FileOutputStream(OPMLLOCALFILE).write(export.getBytes());
								Util.showToastShort(FeedAdd.this, "Export done");
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
								Util.showToastShort(FeedAdd.this, e.getMessage());
							}
						}
					});
				}
			});
		}

		public void hideProgessAndUpdateResultList(final String resultstr) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "adapter.notifyDataSetChanged()");
					selected = 0;
					adapter.notifyDataSetChanged();
					setProgressBarIndeterminateVisibility(false);
					if (resultstr != null) {
						Util.showToastLong(FeedAdd.this, resultstr);
					}
				}
			});
		}
		
	}
	
	private class SearchItemAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public SearchItemAdapter(Context cxt){
			mInflater = LayoutInflater.from(cxt);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
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
                convertView = mInflater.inflate(R.layout.feedadd_row, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.uri = (TextView) convertView.findViewById(R.id.uri);
                holder.description = (TextView) convertView.findViewById(R.id.description);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.

            SearchItem item = items.get(position);
            if(item == null){
            	Log.w(TAG, "getView: search item=null for position="+position+" size="+items.size());
            }else{
                holder.title.setText(Util.fromHtmlNoImages(item.getTitle()));
                holder.uri.setText(Util.fromHtmlNoImages(item.getUri()));
                holder.description.setText(Util.fromHtmlNoImages(item.getDescription()));
            }

            return convertView;
		}
		

	}

	private static class ViewHolder {
        TextView title;
        TextView uri;
        TextView description;
    }

	private final IUpdateServiceCallback updateCallback = new IUpdateServiceCallback.Stub() {
		@Override
		public void onUpdateAllCompleted() throws RemoteException {
			Log.d(TAG, "onUpdateAllCompleted()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Log.d(TAG, "adapter.notifyDataSetChanged()");
					selected = 0;
					adapter.notifyDataSetChanged();
					setProgressBarIndeterminateVisibility(false);
				}
			});
		}
		@Override
		public void onUpdateItemCompleted(final String title) throws RemoteException {
			Log.d(TAG, "onUpdateItemCompleted()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Log.d(TAG, "adapter.notifyDataSetChanged()");
					selected = 0;
					adapter.notifyDataSetChanged();
					setProgressBarIndeterminateVisibility(false);
				}
			});
		}
		@Override
		public void onUpdateItemException(final String title, final String error) throws RemoteException {
			Log.d(TAG, "onUpdateItemException()");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Log.w(TAG, title+": "+ error);
					selected = 0;
					adapter.notifyDataSetChanged();
			        setProgressBarIndeterminateVisibility(false);
				}
			});
		}
	};

}
