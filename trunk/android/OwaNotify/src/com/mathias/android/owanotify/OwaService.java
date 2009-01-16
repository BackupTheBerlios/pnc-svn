package com.mathias.android.owanotify;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.mathias.android.owanotify.OwaParser.OwaCalendarItem;
import com.mathias.android.owanotify.OwaParser.OwaInboxItem;

public class OwaService extends Service {
	
	private static final String TAG = OwaService.class.getSimpleName();
	
	private static final Map<Integer, Integer> mail_res = new HashMap<Integer, Integer>();
	static {
		mail_res.put(0, R.drawable.mail0);
		mail_res.put(1, R.drawable.mail1);
		mail_res.put(2, R.drawable.mail2);
		mail_res.put(3, R.drawable.mail3);
		mail_res.put(4, R.drawable.mail4);
		mail_res.put(5, R.drawable.mail5);
		mail_res.put(6, R.drawable.mail6);
		mail_res.put(7, R.drawable.mail7);
		mail_res.put(8, R.drawable.mail8);
		mail_res.put(9, R.drawable.mail9);
	}
	
	private static final int NOTIFICATION_INBOX_ID = R.id.inbox;

	private static final int NOTIFICATION_CALENDAR_ID = R.id.calendar;

	private NotificationManager mNM;

	private DbAdapter dbHelper;

	@Override
	public void onCreate() {
    	Log.d(TAG, "onCreate");
		super.onCreate();

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        dbHelper = new DbAdapter(this);
        dbHelper.open();

		new WorkingThread().start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
    	Log.d(TAG, "onBind");
		return mBinder;
	}

	@Override
	public void onDestroy() {
    	Log.d(TAG, "onDestroy");
		dbHelper.close();
		dbHelper = null;
		super.onDestroy();
	}

    private final IBinder mBinder = new Binder() {
        @Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
		        int flags) throws RemoteException {
        	Log.d(TAG, "onTransact");
            return super.onTransact(code, data, reply, flags);
        }
    };

    private class WorkingThread extends Thread {
		@Override
		public void run() {
	    	Log.d(TAG, "WorkingThread.run()");
			String url = dbHelper.getSetting(Setting.URL);
			String name = dbHelper.getSetting(Setting.NAME);
			String inbox = dbHelper.getSetting(Setting.INBOX);
			String calendar = dbHelper.getSetting(Setting.CALENDAR);
			String username = dbHelper.getSetting(Setting.USERNAME);
			String password = dbHelper.getSetting(Setting.PASSWORD);
			if(url == null){
		    	Log.d(TAG, "url is null");
			}else{
				try {
					if(dbHelper.getSettingBool(Setting.CHECKMAIL)){
						String inb = Util.downloadFile(0, url+name+inbox, null, username, password);
						List<OwaInboxItem> items = OwaParser.parseInboxNew(inb);
						if(items != null && items.size() > 0){
					    	Log.d(TAG, "showInboxNotification, items="+items.size());
							showInboxNotification(items.size(), items.get(0));
						}else if(dbHelper.getSettingBool(Setting.ALWAYSSHOWMAILCOUNT)){
							showInboxNotification(0, null);
						}else{
							mNM.cancel(NOTIFICATION_INBOX_ID);
						}
					}
					if(dbHelper.getSettingBool(Setting.CHECKCALENDAR)){
						String cal = Util.downloadFile(0, url+name+calendar, null, username, password);
						List<OwaCalendarItem> items = OwaParser.parseCalendar(cal);
						if(items != null && items.size() > 0){
							OwaCalendarItem item = items.get(0);
							GregorianCalendar gc = new GregorianCalendar();
							dbHelper.getSetting(Setting.FREQUENCY);
							int currHour = gc.get(Calendar.HOUR);
							int currMinute = gc.get(Calendar.MINUTE);
							int calHour = Util.getHour(item.time);
							int calMinute = Util.getMinute(item.time);
							if(currHour-5 < calHour){
								if (currHour > calHour
										|| (currHour == calHour && currMinute >= calMinute)) {
							    	Log.d(TAG, "showCalendarNotification, items="+items.size());
									showCalendarNotification(items.size(), item);
								}
							}
						}else{
							mNM.cancel(NOTIFICATION_CALENDAR_ID);
						}
					}
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			stopSelf();
		}
	}

	private void showInboxNotification(int num, OwaInboxItem item) {

		Integer res = mail_res.get(num);
		if(num > 9){
			res = R.drawable.mail9plus;
		}
		Notification notification = new Notification((res != null ? res : R.drawable.mail),
				"New mail ("+num+")", System.currentTimeMillis());

		Intent i;
		if(dbHelper.getSettingBool(Setting.INTERNALVIEWER)){
			i = new Intent(this, OwaMailView.class);			
		}else{
			String fullurl = dbHelper.getSetting(Setting.FULLINBOXURL);
			i = new Intent("android.intent.action.VIEW", Uri.parse(fullurl));			
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		if(item == null){
			item = new OwaInboxItem("No mail", "No new mail", null);
		}
		notification.setLatestEventInfo(this, item.from, item.subject,
				contentIntent);

		notification.defaults = Notification.DEFAULT_ALL;

		mNM.notify(NOTIFICATION_INBOX_ID, notification);
	}

	private void showCalendarNotification(int num, OwaCalendarItem item) {

		Notification notification = new Notification(R.drawable.calendar,
				"New appointment", System.currentTimeMillis());

		Intent i;
		if(dbHelper.getSettingBool(Setting.INTERNALVIEWER)){
			i = new Intent(this, OwaCalendarView.class);			
		}else{
			String fullurl = dbHelper.getSetting(Setting.FULLCALENDARURL);
			i = new Intent("android.intent.action.VIEW", Uri.parse(fullurl));			
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this,
				item.title, item.time+" "+item.title, contentIntent);
		
		notification.defaults = Notification.DEFAULT_ALL;

		mNM.notify(NOTIFICATION_CALENDAR_ID, notification);
	}

}
