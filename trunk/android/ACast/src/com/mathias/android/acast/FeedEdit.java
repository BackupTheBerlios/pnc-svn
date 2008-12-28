package com.mathias.android.acast;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.mathias.android.acast.common.PodGroveUtil;
import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.common.PodGroveUtil.RssItem;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.rss.RssUtil;

public class FeedEdit extends Activity {
	
	private static final String TAG = FeedEdit.class.getSimpleName();
	
	private static final int PARSERSS = 0;

	private static final int PARSEPODGROVE = 1;

	private ACastDbAdapter mDbHelper;
	
	private Handler parsehandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.requestProgressBar(this);
		
		setContentView(R.layout.feed_edit);

		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
		
    	new Thread(){
    		@Override
    		public void run() {
    			Looper.prepare();
    			
    			parsehandler = new Handler(){
    				@Override
    				public void handleMessage(Message msg) {
    					switch(msg.what){
    					case PARSERSS:
    						try {
    							if(msg.obj != null){
        							Feed feed = new RssUtil().parse(msg.obj.toString());
        							mDbHelper.createFeed(feed);
        							Toast.makeText(FeedEdit.this, "Added "+feed.getTitle(), Toast.LENGTH_LONG);
    							}else{
    								Toast.makeText(FeedEdit.this, "Could not add feed", Toast.LENGTH_LONG);
    							}
    						} catch (Exception e) {
    							Log.e(TAG, e.getMessage(), e);
    							Util.showDialog(FeedEdit.this, e.getMessage());
    						}
    						Util.hideProgressBar(FeedEdit.this);
//    						setResult(RESULT_OK);
//    						finish();
    						break;
    					case PARSEPODGROVE:
							List<RssItem> result = PodGroveUtil
									.parse(msg.obj.toString());
							handler.sendMessage(handler.obtainMessage(0, result));
    						break;
    					}
    				}
    			};
    			
    			Looper.loop();
    		}
    	}.start();
    	
		Spinner res = (Spinner) findViewById(R.id.result);
		res.setEmptyView(findViewById(android.R.id.empty));

		final EditText text = (EditText) findViewById(R.id.text);

		ImageButton add = (ImageButton) findViewById(R.id.add);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = text.getText().toString();
				if(!uri.startsWith("http://")){
					Toast.makeText(FeedEdit.this,
							"You might want to add \'http://\' to RSS URL",
							Toast.LENGTH_LONG);
					//Util.showDialog(FeedEdit.this, "You might want to add \'http://\' to URL");
				}
				parsehandler.sendMessage(parsehandler.obtainMessage(PARSERSS, uri));
				Util.showProgressBar(FeedEdit.this);
			}
		});
		
		ImageButton addresult = (ImageButton) findViewById(R.id.addresult);
		addresult.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Spinner res = (Spinner) findViewById(R.id.result);
				RssItem item = (RssItem) res.getSelectedItem();
				parsehandler.sendMessage(parsehandler.obtainMessage(PARSERSS, item.uri));
				Util.showProgressBar(FeedEdit.this);
			}
		});
		
		ImageButton search = (ImageButton) findViewById(R.id.search);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchstr = text.getText().toString();
				parsehandler.sendMessage(parsehandler.obtainMessage(PARSEPODGROVE, searchstr));
				Util.showProgressBar(FeedEdit.this);
			}
		});

	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Spinner res = (Spinner) findViewById(R.id.result);
			ArrayAdapter<RssItem> adapter = new ArrayAdapter<RssItem>(
					FeedEdit.this,
					android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			List<RssItem> result = (List<RssItem>) msg.obj;
			for (RssItem item : result) {
				adapter.add(item);
			}
			res.setAdapter(adapter);
			ImageButton addresult = (ImageButton) findViewById(R.id.addresult);
			addresult.setVisibility(View.VISIBLE);
			Util.hideProgressBar(FeedEdit.this);
		}
	};

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}

}
