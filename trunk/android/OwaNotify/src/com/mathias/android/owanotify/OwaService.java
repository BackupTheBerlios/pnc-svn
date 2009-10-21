package com.mathias.android.owanotify;

import java.util.Date;
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

import com.mathias.android.owanotify.beans.CalendarItem;
import com.mathias.android.owanotify.beans.MailItem;
import com.mathias.android.owanotify.common.MSharedPreferences;

public class OwaService extends Service {
	
	private static final String TAG = OwaService.class.getSimpleName();

	private enum Last {
		INBOX,
		CALENDAR
	}

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
	
	public static final int NOTIFICATION_INBOX_ID = R.id.date;

	public static final int NOTIFICATION_CALENDAR_ID = R.id.from;

	private NotificationManager mNM;

	private MSharedPreferences prefs;
	
	private OwaNotifyDbAdapter dbHelper;

	@Override
	public void onCreate() {
    	Log.d(TAG, "onCreate");
		super.onCreate();

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    	dbHelper = new OwaNotifyDbAdapter(this);
    	dbHelper.open();

		prefs = new MSharedPreferences(this);

		new WorkingThread().start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
    	Log.d(TAG, "onBind");
		return mBinder;
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
	    	
			boolean dontcheckmail = prefs.getBool(R.string.dontcheckemail_key);
			boolean dontcheckcalendar = prefs.getBool(R.string.dontcheckcalendar_key);
			boolean alwaysshowcount = prefs.getBool(R.string.alwaysshowcount_key);
			try {
				if(!dontcheckmail) {
					List<MailItem> items = OwaUtil.fetchInboxNew(prefs, dbHelper);
					if(items != null && items.size() > 0){
				    	Log.d(TAG, "showInboxNotification, items="+items.size());
						showInboxNotification(items.size(), items.get(0));
					}else if(alwaysshowcount){
						showInboxNotification(0, null);
					}else{
						cancelInboxNotification();
					}
				}
				String sreminder = prefs.getString(R.string.reminder_key, "60");
				int reminder = Integer.parseInt(sreminder);
				if(!dontcheckcalendar && reminder != 0) {
					List<CalendarItem> items = OwaUtil.fetchCalendar(prefs);
					if(items != null && items.size() > 0){
						Date curr = new Date();
						int currmin = curr.getHours()*60+curr.getMinutes();
						for (CalendarItem item : items) {
							if(currmin >= item.startmin - reminder && currmin <= item.stopmin){
						    	Log.d(TAG, "showCalendarNotification, items="+items.size());
								showCalendarNotification(items.size(), item);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
			stopSelf();
		}
	}

	private void showInboxNotification(int num, MailItem item) {
		int lastInboxNum = Integer.parseInt(System.getProperty(Last.INBOX
				.name(), "0"));
		if(lastInboxNum >= num && item != null){
			return;
		}

		Integer res = mail_res.get(num);
		if(num > 9){
			res = R.drawable.mail9plus;
		}
		Notification notification = new Notification((res != null ? res : R.drawable.mail),
				"New mail ("+num+")", System.currentTimeMillis());

		Intent i;
		boolean internalviewer = prefs.getBool(R.string.internalviewer_key);
		if(internalviewer) {
			i = new Intent(this, OwaMailView.class);
		}else{
			String fullurl = OwaUtil.getFullInboxUrl(prefs);
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(fullurl));			
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		if(item == null){
			item = new MailItem(null, "No mail", "No new mail", 0, null, false);
		}
		notification.setLatestEventInfo(this, item.sender, item.subject,
				contentIntent);

		if(num > lastInboxNum){
			notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
		}

		mNM.notify(NOTIFICATION_INBOX_ID, notification);
		System.setProperty(Last.INBOX.name(), ""+num);
	}

	private void showCalendarNotification(int num, CalendarItem item) {
		int lastCalendarNum = Integer.parseInt(System.getProperty(Last.CALENDAR
				.name(), "0"));
		if(lastCalendarNum >= num){
			return;
		}

		Notification notification = new Notification(R.drawable.calendar,
				"Reminder", System.currentTimeMillis());

		Intent i;
		boolean internalviewer = prefs.getBool(R.string.internalviewer_key);
		if(internalviewer){
			i = new Intent(this, OwaCalendarView.class);
		}else{
			String fullurl = OwaUtil.getFullUrl(prefs, item.url);
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(fullurl));			
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		notification.setLatestEventInfo(this, item.subject, OwaUtil.buildTime(
				item.startmin, item.stopmin)
				+ " " + item.subject, contentIntent);

		notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;

		mNM.notify(NOTIFICATION_CALENDAR_ID, notification);
		System.setProperty(Last.CALENDAR.name(), ""+num);
	}

	private void cancelInboxNotification(){
		Log.d(TAG, "cancelInboxNotification");
		mNM.cancel(NOTIFICATION_INBOX_ID);
		System.setProperty(Last.INBOX.name(), ""+0);
	}

	private void cancelCalendarNotification(){
		Log.d(TAG, "cancelCalendarNotification");
		mNM.cancel(NOTIFICATION_CALENDAR_ID);
		System.setProperty(Last.CALENDAR.name(), ""+0);
	}

}
