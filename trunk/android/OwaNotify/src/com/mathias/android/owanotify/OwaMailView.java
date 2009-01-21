package com.mathias.android.owanotify;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.owanotify.OwaParser.OwaInboxItem;
import com.mathias.android.owanotify.common.MSharedPreferences;
import com.mathias.android.owanotify.common.Util;

public class OwaMailView extends ListActivity {
	
	private static final String TAG = OwaMailView.class.getSimpleName();
	
	private OwaAdapter adapter;
	
	private MSharedPreferences prefs;

	private List<OwaInboxItem> inboxitems = new ArrayList<OwaInboxItem>();
	
	private WorkerThread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AlarmManager mAM = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, OwaMailView.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        mAM.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), (long)1800000, pendingIntent);

        setContentView(R.layout.main);
        
        prefs = new MSharedPreferences(this);

        adapter = new OwaAdapter(this);
    	setListAdapter(adapter);

    	thread = new WorkerThread();
        thread.start();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        thread.getNewEmails();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		boolean internalviewer = prefs.getBool(R.string.internalviewer_key);
    	if(!internalviewer){
    		String fullurl = OwaUtil.getFullInboxUrl(prefs);
    		startActivity(new Intent("android.intent.action.VIEW", Uri.parse(fullurl)));			
    	}else{
    		OwaInboxItem item = adapter.getItem(position);
    		thread.displayEmail(item);
    	}
    }

	private void populateView(){
		adapter.notifyDataSetChanged();
	}

    private class WorkerThread extends Thread {
    	
    	private boolean ready = false;

    	public void displayEmail(final OwaInboxItem item){
    		while(true){
        		if(ready){
            		handler.post(new Runnable(){
        				@Override
        				public void run() {
        					item.text = OwaUtil.fetchContent(prefs, item.url);
    		        		Intent i = new Intent(OwaMailView.this, OwaReadMail.class);
    		        		i.putExtra(OwaReadMail.EMAIL, item);
    		        		startActivity(i);
        				}
            		});
            		break;
        		}
        		try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
    		}
    	}

    	public void getNewEmails(){
    		while(true){
        		if(ready){
            		handler.post(new Runnable(){
        				@Override
        				public void run() {
        		    		try {
        		    			String inboxurl = OwaUtil.getFullInboxUrl(prefs);
        		    			String username = prefs.getString(R.string.username_key);
        		    			String password = prefs.getString(R.string.password_key);
        		    			if(inboxurl != null && username != null && password != null){
            						String str = Util.downloadFile(0, inboxurl, null, username, password);
            						inboxitems = OwaParser.parseInbox(str, false);
        		    			}else{
        		    				Intent i = new Intent(OwaMailView.this, SettingEdit.class);
        		    				startActivity(i);
        		    			}
        					} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
        					}
        					runOnUiThread(new Runnable(){
								@Override
								public void run() {
									populateView();
								}
        					});
        				}
            		});
            		break;
        		}
        		try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
    		}
    	}
    	
    	private Handler handler;
    	@Override
    	public void run() {
    		Looper.prepare();
    		handler = new Handler();
    		ready = true;
    		Looper.loop();
    	}
    }

    private class OwaAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

        private ViewHolder holder;
        
        public OwaAdapter(Context cxt){
    		mInflater = LayoutInflater.from(cxt);
        }

		@Override
		public int getCount() {
			return inboxitems.size();
		}

		@Override
		public OwaInboxItem getItem(int position) {
			return inboxitems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.main_row, null);
	            holder = new ViewHolder();
	            holder.from = (TextView) convertView.findViewById(R.id.from);
	            holder.subject = (TextView) convertView.findViewById(R.id.subject);
	            holder.date = (TextView) convertView.findViewById(R.id.date);
	            convertView.setTag(holder);
	        } else {
	            holder = (ViewHolder) convertView.getTag();
	        }

	        OwaInboxItem item = inboxitems.get(position);
	        if(item == null) {
	        	return null;
	        }
	        holder.from.setText(item.from);
	        holder.subject.setText(item.subject);
	        holder.date.setText(item.date);
			if(!item.read){
				holder.from.setTextColor(Color.WHITE);
				holder.subject.setTextColor(Color.WHITE);
				holder.date.setTextColor(Color.WHITE);
			}
	        return convertView;
		}
    	
    }
    
    private static class ViewHolder {
    	TextView from;
    	TextView subject;
    	TextView date;
    }

}
