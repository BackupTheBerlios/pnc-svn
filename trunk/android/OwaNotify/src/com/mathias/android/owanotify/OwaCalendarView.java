package com.mathias.android.owanotify;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import com.mathias.android.owanotify.OwaParser.OwaCalendarItem;
import com.mathias.android.owanotify.OwaParser.OwaInboxItem;

public class OwaCalendarView extends ListActivity {

	private static final String TAG = OwaMailView.class.getSimpleName();
	
	private OwaAdapter adapter;
	
	private DbAdapter dbHelper;

	private List<OwaCalendarItem> calendaritems = new ArrayList<OwaCalendarItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AlarmManager mAM = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, OwaMailView.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        mAM.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), (long)1800000, pendingIntent);

        setContentView(R.layout.main);
        
        dbHelper = new DbAdapter(this);
        dbHelper.open();

        adapter = new OwaAdapter(this);
    	setListAdapter(adapter);

    	WorkerThread thread = new WorkerThread();
        thread.start();
        thread.getEmail();
    }
    
    @Override
    protected void onDestroy() {
    	dbHelper.close();
    	dbHelper = null;
    	super.onDestroy();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		String fullurl = dbHelper.getSetting(Setting.FULLINBOXURL);
		startActivity(new Intent("android.intent.action.VIEW", Uri.parse(fullurl)));			
    }

    private void populateView(){
		adapter.notifyDataSetChanged();
    }
    
    private class WorkerThread extends Thread {
    	
    	private boolean ready = false;
    	
//    	private String url = "https://owa.smarttrust.com/exchange/";
//    	private String name = "Mathias.Axelsson";
//    	private String inbox = "/Inbox/?Cmd=contents";
//		private String calendar = "/Calendar/?Cmd=contents&View=Daily";
//    	private String username = "mataxe1";
//    	private String password = "*********";

    	public void getEmail(){
    		while(true){
        		if(ready){
            		handler.post(new Runnable(){
        				@Override
        				public void run() {
        		    		try {
        		    			String calendarurl = dbHelper.getSetting(Setting.FULLCALENDARURL);
        		    			String username = dbHelper.getSetting(Setting.USERNAME);
        		    			String password = dbHelper.getSetting(Setting.PASSWORD);
        						String str = Util.downloadFile(0, calendarurl, null, username, password);
        						calendaritems = OwaParser.parseCalendar(str);
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
			return calendaritems.size();
		}

		@Override
		public Object getItem(int position) {
			return calendaritems.get(position);
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

	        OwaCalendarItem item = calendaritems.get(position);
	        if(item == null) {
	        	return null;
	        }
	        holder.from.setText(item.title);
	        holder.subject.setText(item.titleLocation);
	        holder.date.setText(item.time);
	        return convertView;
		}
    	
    }
    
    private static class ViewHolder {
    	TextView from;
    	TextView subject;
    	TextView date;
    }

}