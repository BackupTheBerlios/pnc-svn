package com.mathias.android.owanotify;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mathias.android.owanotify.OwaParser.OwaCalendarItem;
import com.mathias.android.owanotify.common.MSharedPreferences;
import com.mathias.android.owanotify.common.Util;

public class OwaCalendarView extends ListActivity {

	private static final String TAG = OwaCalendarView.class.getSimpleName();
	
	private static final int EMAIL_ID = Menu.FIRST+0;
	private static final int REFRESH_ID = Menu.FIRST+1;
	private static final int SETTINGS_ID = Menu.FIRST+2;

	private OwaAdapter adapter;

	private List<OwaCalendarItem> calendaritems = new ArrayList<OwaCalendarItem>();

	private MSharedPreferences prefs;
	
	private WorkerThread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);
        
        AlarmManager mAM = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, OwaCalendarView.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        mAM.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), (long)1800000, pendingIntent);

        prefs = new MSharedPreferences(this);

        adapter = new OwaAdapter(this);
    	setListAdapter(adapter);

    	thread = new WorkerThread();
        thread.start();
        thread.getCalendar();
        
        TextView empty = (TextView) findViewById(android.R.id.empty);
        empty.setText("No calendar items found...");
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		String fullurl = OwaUtil.getFullInboxUrl(prefs);
		startActivity(new Intent("android.intent.action.VIEW", Uri.parse(fullurl)));			
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.NONE, EMAIL_ID, Menu.NONE, "E-Mails");
		item.setIcon(android.R.drawable.ic_menu_send);
		item = menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, "Refresh");
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item = menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, "Settings");
		item.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(EMAIL_ID == item.getItemId()){
			Intent i = new Intent(this, OwaMailView.class);
			startActivity(i);
			return true;
		}else if(REFRESH_ID == item.getItemId()){
	        thread.getCalendar();
			return true;
		}else if(SETTINGS_ID == item.getItemId()){
			Intent i = new Intent(this, SettingEdit.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private void populateView(){
		adapter.notifyDataSetChanged();
    }
    
    private class WorkerThread extends Thread {
    	
    	private boolean ready = false;

    	public void getCalendar(){
			final ProgressDialog pd = ProgressDialog.show(OwaCalendarView.this, null, "Fetching calendar items");
    		while(true){
        		if(ready){
            		handler.post(new Runnable(){
        				@Override
        				public void run() {
        		    		try {
        		    			String calendarurl = OwaUtil.getFullCalendarUrl(prefs);
        		    			String username = prefs.getString(R.string.username_key);
        		    			String password = prefs.getString(R.string.password_key);
        		    			if(calendarurl != null && username != null && password != null){
            						String str = Util.downloadFile(0, calendarurl, null, username, password);
            				        String sadd = prefs.getString(R.string.timezoneadj_key, "0");
            				        int timezoneadj = Integer.parseInt(sadd);
            						calendaritems = OwaParser.parseCalendar(str, timezoneadj);
        		    			}else{
        		    				Intent i = new Intent(OwaCalendarView.this, SettingEdit.class);
        		    				startActivity(i);
        		    			}
        					} catch (Exception e) {
        						Log.e(TAG, e.getMessage(), e);
        					}
        					runOnUiThread(new Runnable(){
								@Override
								public void run() {
	        						populateView();
	        		    	        pd.dismiss();
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
//	        if(item.date != null){
//		        holder.date.setText(item.date.toString());
//	        }
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
