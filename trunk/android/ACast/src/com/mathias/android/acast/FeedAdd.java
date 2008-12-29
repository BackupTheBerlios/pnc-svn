package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mathias.android.acast.common.DigitalPodcastUtil;
import com.mathias.android.acast.common.OpmlUtil;
import com.mathias.android.acast.common.PodGroveUtil;
import com.mathias.android.acast.common.PodcastAlleyUtil;
import com.mathias.android.acast.common.RssUtil;
import com.mathias.android.acast.common.SearchItem;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;

public class FeedAdd extends ListActivity {

	private static final String TAG = FeedAdd.class.getSimpleName();

    private ACastDbAdapter mDbHelper;
	
	private SearchItemAdapter adapter;

	private List<SearchItem> items = new ArrayList<SearchItem>();

    private ProgressDialog progressDialog;

    private ParseThread parsethread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.feedadd);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
		adapter = new SearchItemAdapter(this);
		setListAdapter(adapter);

		parsethread = new ParseThread();
		parsethread.start();

		final EditText text = (EditText) findViewById(R.id.text);

		ImageButton add = (ImageButton) findViewById(R.id.add);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = text.getText().toString();
				if(!uri.startsWith("http://")){
					Toast.makeText(FeedAdd.this,
							"You might want to add \'http://\' to RSS URL",
							Toast.LENGTH_SHORT).show();
				}
				parsethread.parseRss(uri);
                showDialog(0);
			}
		});

		ImageButton searchpodgrove = (ImageButton) findViewById(R.id.searchpodgrove);
		searchpodgrove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchstr = text.getText().toString();
				parsethread.searchPodgrove(searchstr);
                showDialog(0);
			}
		});

		ImageButton searchdigitalpodcast = (ImageButton) findViewById(R.id.searchdigitalpodcast);
		searchdigitalpodcast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchstr = text.getText().toString();
				parsethread.searchDigitalPodcast(searchstr);
                showDialog(0);
			}
		});

		ImageButton top50podcastalley = (ImageButton) findViewById(R.id.top50podcastalley);
		top50podcastalley.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				parsethread.top50PodcastAlley();
                showDialog(0);
			}
		});

		ImageButton importopml = (ImageButton) findViewById(R.id.importopml);
		importopml.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uristr = text.getText().toString();
				parsethread.importOpml(uristr);
                showDialog(0);
			}
		});

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Please wait while loading...");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		return progressDialog;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final SearchItem item = (SearchItem) adapter.getItem(position);
		String msg = "Do you want to add "
			+ Html.fromHtml(item.getTitle(), Util.NULLIMAGEGETTER, null);
		Util.showConfirmationDialog(this, msg, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				parsethread.parseRss(item.getUri());
		        showDialog(0);
			}
		});
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}
	
	private MyHandler handler = new MyHandler();

	private class MyHandler {

		public void hideProgessAndUpdateResultList(String msg) {
			handler.sendMessage(handler.obtainMessage(0, msg));
		}

		private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				progressDialog.hide();
				Log.d(TAG, "adapter.notifyDataSetChanged()");
				adapter.notifyDataSetChanged();
				if (msg.obj != null) {
					Toast.makeText(FeedAdd.this, msg.obj.toString(),
							Toast.LENGTH_LONG).show();
				}
			}
		};
	}

	private class ParseThread extends Thread {

		private static final int PARSERSS = 0;
		private static final int SEARCHPODGROVE = 1;
		private static final int SEARCHDIGITALPODCAST = 2;
		private static final int TOP50PODCASTALLEY = 3;
		private static final int IMPORTOPML = 4;

		private Handler parsehandler;
		
		public void parseRss(String uri){
			parsehandler.sendMessage(parsehandler.obtainMessage(PARSERSS, uri));
		}
		public void searchPodgrove(String searchstr){
			parsehandler.sendMessage(parsehandler.obtainMessage(SEARCHPODGROVE, searchstr));
		}

		public void top50PodcastAlley(){
			parsehandler.sendMessage(parsehandler.obtainMessage(TOP50PODCASTALLEY));
		}

		public void searchDigitalPodcast(String searchstr){
			parsehandler.sendMessage(parsehandler.obtainMessage(SEARCHDIGITALPODCAST, searchstr));
		}

		public void importOpml(String uristr){
			parsehandler.sendMessage(parsehandler.obtainMessage(IMPORTOPML, uristr));
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
    							Feed feed = new RssUtil().parse(msg.obj.toString());
    							mDbHelper.createFeed(feed);
    							resultstr = "Added "+feed.getTitle();
							}else{
    							resultstr = "Could not add feed";
							}
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							resultstr = e.getMessage();
						}
						handler.hideProgessAndUpdateResultList(resultstr);
					}else if(SEARCHPODGROVE == msg.what){
						// obj is String search
						items = PodGroveUtil.parse(msg.obj.toString());
						resultstr = "PodGrove found "+items.size()+" results";
						Log.d(TAG, resultstr);
						handler.hideProgessAndUpdateResultList(resultstr);
					}else if(SEARCHDIGITALPODCAST == msg.what){
						// obj is String search
						items = DigitalPodcastUtil
								.parse(msg.obj.toString());
						resultstr = "DigitalPodcast found "+items.size()+" results";
						Log.d(TAG, resultstr);
						handler.hideProgessAndUpdateResultList(resultstr);
					}else if(TOP50PODCASTALLEY == msg.what){
						try {
							items = new PodcastAlleyUtil().parseTop50();
							Log.d(TAG, "PodcastAlley Top 50; "+items.size()+" results");
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							resultstr = e.getMessage();
						}
						handler.hideProgessAndUpdateResultList(resultstr);
					}else if(IMPORTOPML == msg.what){
						// obj is URI
						try {
							items = new OpmlUtil().parse(msg.obj.toString());
							resultstr = "Import OPML found "+items.size()+" results";
							Log.d(TAG, resultstr);
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							resultstr = e.getMessage();
						}
						handler.hideProgessAndUpdateResultList(resultstr);
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
                holder.title.setText(Html.fromHtml(item.getTitle(), Util.NULLIMAGEGETTER, null));
                holder.uri.setText(Html.fromHtml(item.getUri(), Util.NULLIMAGEGETTER, null));
                holder.description.setText(Html.fromHtml(item.getDescription(), Util.NULLIMAGEGETTER, null));
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
