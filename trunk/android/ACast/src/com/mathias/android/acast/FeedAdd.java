package com.mathias.android.acast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.mathias.android.acast.common.RssUtil;
import com.mathias.android.acast.common.SearchItem;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;

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
				if(!uri.startsWith("http://")){
			        Util.showToastShort(FeedAdd.this, "You might want to add \'http://\' to RSS URL");
				}
				thread.parseRss(uri);
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

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final SearchItem item = (SearchItem) adapter.getItem(position);
		String msg = "Do you want to add "
			+ Util.fromHtmlNoImages(item.getTitle());
		Util.showConfirmationDialog(this, msg, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				thread.parseRss(item.getUri());
			}
		});
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
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

	private class WorkerThread extends Thread {

		private static final int PARSERSS = 0;
		private static final int SEARCHPODGROVE = 1;
		private static final int SEARCHDIGITALPODCAST = 2;
		private static final int TOP50PODCASTALLEY = 3;
		private static final int IMPORTITEMS = 4;
		private static final int IMPORTOPML = 5;
		private static final int IMPORTLOCALOPML = 6;
		private static final int EXPORTLOCALOPML = 7;

		private Handler parsehandler;
		
		public void parseRss(String uri){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.sendMessage(parsehandler.obtainMessage(PARSERSS, uri));
		}
		public void searchPodgrove(String searchstr){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.sendMessage(parsehandler.obtainMessage(SEARCHPODGROVE, searchstr));
		}

		public void top50PodcastAlley(){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.sendMessage(parsehandler.obtainMessage(TOP50PODCASTALLEY));
		}

		public void searchDigitalPodcast(String searchstr){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.sendMessage(parsehandler.obtainMessage(SEARCHDIGITALPODCAST, searchstr));
		}

		public void importOpml(String uristr){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.sendMessage(parsehandler.obtainMessage(IMPORTOPML, uristr));
		}
		
		public void importLocalOpml(){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.sendMessage(parsehandler.obtainMessage(IMPORTLOCALOPML));
		}

		public void exportLocalOpml(){
			parsehandler.sendMessage(parsehandler.obtainMessage(EXPORTLOCALOPML));
		}

		public void importItems(){
			setProgressBarIndeterminateVisibility(true);
			parsehandler.sendMessage(parsehandler.obtainMessage(IMPORTITEMS));
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
		
		@Override
		public void run() {
			Looper.prepare();
			parsehandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					String resultstr = null;
					if(PARSERSS == msg.what){
						// obj is String uri
						try {
							if(msg.obj != null){
								Map<Feed, List<FeedItem>> result = new RssUtil().parse(msg.obj.toString());
    							Feed resfeed = result.keySet().toArray(new Feed[0])[0];
    							mDbHelper.createFeed(resfeed, result.get(resfeed));
    							resultstr = "Added "+resfeed.title;
							}else{
    							resultstr = "Could not add feed";
							}
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							resultstr = e.getMessage();
						}
						hideProgessAndUpdateResultList(resultstr);
					}else if(SEARCHPODGROVE == msg.what){
						// obj is String search
						items = PodGroveUtil.parse(msg.obj.toString());
						resultstr = "PodGrove found "+items.size()+" results";
						Log.d(TAG, resultstr);
						hideProgessAndUpdateResultList(resultstr);
					}else if(SEARCHDIGITALPODCAST == msg.what){
						// obj is String search
						items = DigitalPodcastUtil
								.parse(msg.obj.toString());
						resultstr = "DigitalPodcast found "+items.size()+" results";
						Log.d(TAG, resultstr);
						hideProgessAndUpdateResultList(resultstr);
					}else if(TOP50PODCASTALLEY == msg.what){
						try {
							items = new PodcastAlleyUtil().parseTop50();
							Log.d(TAG, "PodcastAlley Top 50; "+items.size()+" results");
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							resultstr = e.getMessage();
						}
						hideProgessAndUpdateResultList(resultstr);
					}else if(IMPORTOPML == msg.what){
						// obj is URI
						try {
							items = new OpmlUtil().parse(msg.obj.toString());
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
											importItems();
										}
									});
								}
							});
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							resultstr = e.getMessage();
						}
						hideProgessAndUpdateResultList(resultstr);
					}else if(IMPORTLOCALOPML == msg.what){
						try {
							items = new OpmlUtil().parse(new File(OPMLLOCALFILE));
							final String res = Util.buildString(
									"Import local OPML found ", items.size(),
									" results. Import all items directly?");
							Log.d(TAG, res);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Util.showConfirmationDialog(FeedAdd.this, res, new OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which) {
											importItems();
										}
									});
								}
							});
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							resultstr = e.getMessage();
						}
						hideProgessAndUpdateResultList(resultstr);
					}else if(EXPORTLOCALOPML == msg.what){
						Util.showConfirmationDialog(FeedAdd.this, "Are you sure?", new OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									setProgressBarIndeterminateVisibility(true);
									OpmlUtil.Opml opml = new OpmlUtil.Opml(getString(R.string.app_name));
									List<Feed> feeds = mDbHelper.fetchAllFeeds();
									for (Feed feed : feeds) {
										opml.add(new OpmlUtil.OpmlItem(feed.title, feed.uri));
									}
									String export = OpmlUtil.exportOpml(opml);
									new FileOutputStream(OPMLLOCALFILE).write(export.getBytes());
									setProgressBarIndeterminateVisibility(false);
									Util.showToastShort(FeedAdd.this, "Export done");
								} catch (Exception e) {
									Log.e(TAG, e.getMessage(), e);
								}
							}
						});
						hideProgessAndUpdateResultList(resultstr);
					} else if(IMPORTITEMS == msg.what) {
						for (SearchItem item : items) {
							try {
								Map<Feed, List<FeedItem>> result = new RssUtil().parse(item.getUri());
    							Feed resfeed = result.keySet().toArray(new Feed[0])[0];
    							mDbHelper.createFeed(resfeed, result.get(resfeed));
    							Util.showToastShort(FeedAdd.this, "Added "+resfeed.title);
							} catch (Exception e) {
								Log.e(TAG, resultstr, e);
							}
						}
						hideProgessAndUpdateResultList(resultstr);
					}
				}
			};
			Looper.loop();
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

}
