package com.mathias.android.owanotify;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mathias.android.owanotify.beans.CalendarItem;
import com.mathias.android.owanotify.beans.MailItem;
import com.mathias.android.owanotify.common.Util;

public class OwaNotifyDbAdapter {

	public static final String MAIL_ID = "_id";
	public static final String MAIL_SUBJECT = "subject";
	public static final String MAIL_TO = "to";
	public static final String MAIL_CC = "cc";
	public static final String MAIL_SENDER = "sender";
	public static final String MAIL_DATE = "date";
	public static final String MAIL_READ = "read";
	public static final String MAIL_CONTENT = "content";
	public static final String MAIL_URL = "url";

	public static final String CALENDAR_ID = "_id";
	public static final String CALENDAR_SUBJECT = "subject";
	public static final String CALENDAR_TO = "to";
	public static final String CALENDAR_CC = "cc";
	public static final String CALENDAR_SENDER = "sender";
	public static final String CALENDAR_DATE = "date";
	public static final String CALENDAR_READ = "read";
	public static final String CALENDAR_CONTENT = "content";
	public static final String CALENDAR_URL = "url";
	public static final String CALENDAR_START = "start";
	public static final String CALENDAR_STOP = "stop";
	public static final String CALENDAR_LOCATION = "location";

	public static final String SETTING_KEY = "key";
	public static final String SETTING_VALUE = "value";

	private static final String DATABASE_NAME = "owanotify";
	public static final String DATABASE_TABLE_MAIL = "mail";
	public static final String DATABASE_TABLE_CALENDAR = "calendar";
	public static final String DATABASE_TABLE_SETTING = "setting";

	private static final String DATABASE_CREATE_MAIL = "create table "+DATABASE_TABLE_MAIL+" ("
			+ MAIL_ID+" integer primary key autoincrement, "
			+ MAIL_URL+" text not null unique, " 
			+ MAIL_CC+" text, " 
			+ MAIL_CONTENT+" text, "
			+ MAIL_DATE+" integer, "
			+ MAIL_SENDER+" integer, "
			+ MAIL_READ+" boolean not null, "
			+ MAIL_SUBJECT+" text, "
			+ MAIL_TO+" text);";

	private static final String DATABASE_CREATE_CALENDAR = "create table "+DATABASE_TABLE_CALENDAR+" ("
			+ CALENDAR_ID+" integer primary key autoincrement, "
			+ CALENDAR_URL+" text not null unique, " 
			+ CALENDAR_CC+" text, " 
			+ CALENDAR_CONTENT+" text, "
			+ CALENDAR_DATE+" integer, "
			+ CALENDAR_SENDER+" integer, "
			+ CALENDAR_READ+" boolean not null, "
			+ CALENDAR_SUBJECT+" text, "
			+ CALENDAR_TO+" text, "
			+ CALENDAR_START+" integer, "
			+ CALENDAR_STOP+" integer, "
			+ CALENDAR_LOCATION+" string);";

	private static final String DATABASE_CREATE_SETTING = "create table "+DATABASE_TABLE_SETTING+" ("
			+ SETTING_KEY+" text primary key, "
			+ SETTING_VALUE+" text);";

	private static final int DATABASE_VERSION = 1;

	private static final String TAG = OwaNotifyDbAdapter.class.getSimpleName();
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_MAIL);
			db.execSQL(DATABASE_CREATE_CALENDAR);
			db.execSQL(DATABASE_CREATE_SETTING);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MAIL);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CALENDAR);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SETTING);
			onCreate(db);
		}
	}

	public OwaNotifyDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public OwaNotifyDbAdapter open() {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createMail(MailItem item) {
		Log.d(TAG, "create mail: "+item.subject);
		ContentValues initialValues = new ContentValues();
		initialValues.put(MAIL_CC, item.cc);
		initialValues.put(MAIL_CONTENT, item.text);
		initialValues.put(MAIL_DATE, item.date);
		initialValues.put(MAIL_SENDER, item.sender);
		initialValues.put(MAIL_READ, item.read);
		initialValues.put(MAIL_SUBJECT, item.subject);
		initialValues.put(MAIL_TO, item.to);
		initialValues.put(MAIL_URL, item.url);
		long id = mDb.insert(DATABASE_TABLE_MAIL, null, initialValues);
		return id;
	}

	public void createMails(List<MailItem> items) {
		Log.d(TAG, "create mails: "+items.size());
		ContentValues initialValues = new ContentValues();
		for (MailItem item : items) {
			initialValues.put(MAIL_CC, item.cc);
			initialValues.put(MAIL_CONTENT, item.text);
			initialValues.put(MAIL_DATE, item.date);
			initialValues.put(MAIL_SENDER, item.sender);
			initialValues.put(MAIL_READ, item.read);
			initialValues.put(MAIL_SUBJECT, item.subject);
			initialValues.put(MAIL_TO, item.to);
			initialValues.put(MAIL_URL, item.url);
			long id = mDb.insert(DATABASE_TABLE_MAIL, null, initialValues);
			if(id == -1){
				Log.w(TAG, "Could not add: "+item.subject);
			}
		}
	}

	public long createCalendar(CalendarItem item){
		Log.d(TAG, "create calendar: "+item.subject);
		ContentValues initialValues = new ContentValues();
		initialValues.put(CALENDAR_CC, item.cc);
		initialValues.put(CALENDAR_CONTENT, item.text);
		initialValues.put(CALENDAR_DATE, item.date);
		initialValues.put(CALENDAR_SENDER, item.sender);
		initialValues.put(CALENDAR_LOCATION, item.location);
		initialValues.put(CALENDAR_READ, item.read);
		initialValues.put(CALENDAR_START, item.startmin);
		initialValues.put(CALENDAR_STOP, item.stopmin);
		initialValues.put(CALENDAR_SUBJECT, item.subject);
		initialValues.put(CALENDAR_TO, item.to);
		initialValues.put(CALENDAR_URL, item.url);
		long id = mDb.insert(DATABASE_TABLE_CALENDAR, null, initialValues);
		return id;
	}

	public void createCalendars(List<CalendarItem> items) {
		Log.d(TAG, "create mails: "+items.size());
		ContentValues initialValues = new ContentValues();
		for (CalendarItem item : items) {
			initialValues.put(CALENDAR_CC, item.cc);
			initialValues.put(CALENDAR_CONTENT, item.text);
			initialValues.put(CALENDAR_DATE, item.date);
			initialValues.put(CALENDAR_SENDER, item.sender);
			initialValues.put(CALENDAR_LOCATION, item.location);
			initialValues.put(CALENDAR_READ, item.read);
			initialValues.put(CALENDAR_START, item.startmin);
			initialValues.put(CALENDAR_STOP, item.stopmin);
			initialValues.put(CALENDAR_SUBJECT, item.subject);
			initialValues.put(CALENDAR_TO, item.to);
			initialValues.put(CALENDAR_URL, item.url);
			long id = mDb.insert(DATABASE_TABLE_CALENDAR, null, initialValues);
			if(id == -1){
				Log.w(TAG, "Could not add: "+item.subject);
			}
		}
	}

	public boolean deleteMail(long id) {
		boolean ret = mDb.delete(DATABASE_TABLE_MAIL, MAIL_ID + "=" + id, null) == 0;
		Log.w(TAG, "Could not remove mail");
		return ret;
	}

	public boolean deleteCalendar(long id) {
		boolean ret = mDb.delete(DATABASE_TABLE_CALENDAR, CALENDAR_ID + "=" + id, null) == 0;
		Log.w(TAG, "Could not remove calendar");
		return ret;
	}

	public List<MailItem> fetchAllMail() {
		List<MailItem> feeds = new ArrayList<MailItem>();
		Cursor c = mDb.query(DATABASE_TABLE_MAIL, new String[] { MAIL_ID,
				MAIL_URL, MAIL_CC, MAIL_CONTENT, MAIL_DATE, MAIL_SENDER,
				MAIL_READ, MAIL_SUBJECT, MAIL_TO }, null, null, null, null,
				null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
		}else{
			do{
				MailItem item = new MailItem();
				item.id = c.getLong(c.getColumnIndexOrThrow(MAIL_ID));
				item.cc = c.getString(c.getColumnIndexOrThrow(MAIL_CC));
				item.date = c.getLong(c.getColumnIndexOrThrow(MAIL_DATE));
				item.sender = c.getString(c.getColumnIndexOrThrow(MAIL_SENDER));
				item.read = 0 != c.getShort(c.getColumnIndexOrThrow(MAIL_READ));
				item.subject = c.getString(c.getColumnIndexOrThrow(MAIL_SUBJECT));
				item.text = c.getString(c.getColumnIndexOrThrow(MAIL_CONTENT));
				item.to = c.getString(c.getColumnIndexOrThrow(MAIL_TO));
				item.url = c.getString(c.getColumnIndexOrThrow(MAIL_URL));
				feeds.add(item);
			}while(c.moveToNext());
		}
		Util.closeCursor(c);
		return feeds;
	}

	public List<CalendarItem> fetchAllCalendar() {
		List<CalendarItem> feeds = new ArrayList<CalendarItem>();
		Cursor c = mDb.query(DATABASE_TABLE_CALENDAR, new String[] { CALENDAR_ID,
				CALENDAR_URL, CALENDAR_CC, CALENDAR_CONTENT, CALENDAR_DATE, CALENDAR_SENDER,
				CALENDAR_READ, CALENDAR_SUBJECT, CALENDAR_TO }, null, null, null, null,
				null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
		}else{
			do{
				CalendarItem item = new CalendarItem();
				item.id = c.getLong(c.getColumnIndexOrThrow(CALENDAR_ID));
				item.cc = c.getString(c.getColumnIndexOrThrow(CALENDAR_CC));
				item.date = c.getLong(c.getColumnIndexOrThrow(CALENDAR_DATE));
				item.sender = c.getString(c.getColumnIndexOrThrow(CALENDAR_SENDER));
				item.read = 0 != c.getShort(c.getColumnIndexOrThrow(CALENDAR_READ));
				item.subject = c.getString(c.getColumnIndexOrThrow(CALENDAR_SUBJECT));
				item.text = c.getString(c.getColumnIndexOrThrow(CALENDAR_CONTENT));
				item.to = c.getString(c.getColumnIndexOrThrow(CALENDAR_TO));
				item.url = c.getString(c.getColumnIndexOrThrow(CALENDAR_URL));
				item.startmin = c.getInt(c.getColumnIndexOrThrow(CALENDAR_START));
				item.stopmin = c.getInt(c.getColumnIndexOrThrow(CALENDAR_STOP));
				item.location = c.getString(c.getColumnIndexOrThrow(CALENDAR_LOCATION));
				feeds.add(item);
			}while(c.moveToNext());
		}
		Util.closeCursor(c);
		return feeds;
	}

	public MailItem fetchMail(long id) {
		MailItem item = null;
		Cursor c = mDb.query(DATABASE_TABLE_MAIL, new String[] {
				MAIL_URL, MAIL_CC, MAIL_CONTENT, MAIL_DATE, MAIL_SENDER,
				MAIL_READ, MAIL_SUBJECT, MAIL_TO }, MAIL_ID + "=" + id, null, null, null,
				null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
		}else{
			item = new MailItem();
			item.id = id;
			item.cc = c.getString(c.getColumnIndexOrThrow(MAIL_CC));
			item.date = c.getLong(c.getColumnIndexOrThrow(MAIL_DATE));
			item.sender = c.getString(c.getColumnIndexOrThrow(MAIL_SENDER));
			item.read = 0 != c.getShort(c.getColumnIndexOrThrow(MAIL_READ));
			item.subject = c.getString(c.getColumnIndexOrThrow(MAIL_SUBJECT));
			item.text = c.getString(c.getColumnIndexOrThrow(MAIL_CONTENT));
			item.to = c.getString(c.getColumnIndexOrThrow(MAIL_TO));
			item.url = c.getString(c.getColumnIndexOrThrow(MAIL_URL));
		}
		Util.closeCursor(c);
		return item;
	}

	public MailItem fetchMail(String url) {
		MailItem item = null;
		Cursor c = mDb.query(DATABASE_TABLE_MAIL, new String[] {
				MAIL_ID, MAIL_URL, MAIL_CC, MAIL_CONTENT, MAIL_DATE, MAIL_SENDER,
				MAIL_READ, MAIL_SUBJECT, MAIL_TO }, MAIL_URL + "=" + url, null, null, null,
				null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
		}else{
			item = new MailItem();
			item.id = c.getLong(c.getColumnIndexOrThrow(MAIL_ID));
			item.cc = c.getString(c.getColumnIndexOrThrow(MAIL_CC));
			item.date = c.getLong(c.getColumnIndexOrThrow(MAIL_DATE));
			item.sender = c.getString(c.getColumnIndexOrThrow(MAIL_SENDER));
			item.read = 0 != c.getShort(c.getColumnIndexOrThrow(MAIL_READ));
			item.subject = c.getString(c.getColumnIndexOrThrow(MAIL_SUBJECT));
			item.text = c.getString(c.getColumnIndexOrThrow(MAIL_CONTENT));
			item.to = c.getString(c.getColumnIndexOrThrow(MAIL_TO));
			item.url = c.getString(c.getColumnIndexOrThrow(MAIL_URL));
		}
		Util.closeCursor(c);
		return item;
	}

	public CalendarItem fetchCalendar(long id) {
		CalendarItem item = null;
		Cursor c = mDb.query(DATABASE_TABLE_CALENDAR, new String[] {
				CALENDAR_URL, CALENDAR_CC, CALENDAR_CONTENT, CALENDAR_DATE, CALENDAR_SENDER,
				CALENDAR_READ, CALENDAR_SUBJECT, CALENDAR_TO }, CALENDAR_ID + "=" + id, null, null, null,
				null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
		}else{
			item = new CalendarItem();
			item.id = id;
			item.cc = c.getString(c.getColumnIndexOrThrow(CALENDAR_CC));
			item.date = c.getLong(c.getColumnIndexOrThrow(CALENDAR_DATE));
			item.sender = c.getString(c.getColumnIndexOrThrow(CALENDAR_SENDER));
			item.read = 0 != c.getShort(c.getColumnIndexOrThrow(CALENDAR_READ));
			item.subject = c.getString(c.getColumnIndexOrThrow(CALENDAR_SUBJECT));
			item.text = c.getString(c.getColumnIndexOrThrow(CALENDAR_CONTENT));
			item.to = c.getString(c.getColumnIndexOrThrow(CALENDAR_TO));
			item.url = c.getString(c.getColumnIndexOrThrow(CALENDAR_URL));
			item.startmin = c.getInt(c.getColumnIndexOrThrow(CALENDAR_START));
			item.stopmin = c.getInt(c.getColumnIndexOrThrow(CALENDAR_STOP));
			item.location = c.getString(c.getColumnIndexOrThrow(CALENDAR_LOCATION));
		}
		Util.closeCursor(c);
		return item;
	}

	public boolean updateMail(MailItem item) {
		Log.d(TAG, "update mail: "+item.subject);
		ContentValues initialValues = new ContentValues();
		initialValues.put(MAIL_CC, item.cc);
		initialValues.put(MAIL_CONTENT, item.text);
		initialValues.put(MAIL_DATE, item.date);
		initialValues.put(MAIL_SENDER, item.sender);
		initialValues.put(MAIL_READ, item.read);
		initialValues.put(MAIL_SUBJECT, item.subject);
		initialValues.put(MAIL_TO, item.to);
		initialValues.put(MAIL_URL, item.url);
		boolean ret = mDb.update(DATABASE_TABLE_MAIL, initialValues, MAIL_ID + "=" + item.id, null) != 1;
		if(!ret){
			Log.w(TAG, "Could not update mail: "+item.id);
		}
		return ret;
	}

	public boolean updateCalendar(CalendarItem item) {
		Log.d(TAG, "update calendar: "+item.subject);
		ContentValues initialValues = new ContentValues();
		initialValues.put(CALENDAR_CC, item.cc);
		initialValues.put(CALENDAR_CONTENT, item.text);
		initialValues.put(CALENDAR_DATE, item.date);
		initialValues.put(CALENDAR_SENDER, item.sender);
		initialValues.put(CALENDAR_LOCATION, item.location);
		initialValues.put(CALENDAR_READ, item.read);
		initialValues.put(CALENDAR_START, item.startmin);
		initialValues.put(CALENDAR_STOP, item.stopmin);
		initialValues.put(CALENDAR_SUBJECT, item.subject);
		initialValues.put(CALENDAR_TO, item.to);
		initialValues.put(CALENDAR_URL, item.url);
		boolean ret = mDb.update(DATABASE_TABLE_CALENDAR, initialValues, CALENDAR_ID + "=" + item.id, null) != 1;
		if(!ret){
			Log.w(TAG, "Could not update calendar: "+item.id);
		}
		return ret;
	}

	/**
	 * @param table
	 * @param idcolumn
	 * @param id
	 * @param column
	 * @param value
	 * @return updated rows
	 */
	public int update(String table, String idcolumn, long id, String column, Object value) {
		ContentValues args = new ContentValues();
		if (value instanceof Integer) {
			args.put(column, (Integer) value);
		} else if (value instanceof Long) {
			args.put(column, (Long) value);
		} else if (value instanceof Short) {
			args.put(column, (Short) value);
		} else if (value instanceof Boolean) {
			args.put(column, (short) (((Boolean) value) ? 1 : 0));
		} else {
			throw new RuntimeException(Util.buildString(
					"Could not store value=", value, " in column=", column,
					", table=" + table));
		}
		int ret = mDb.update(table, args, idcolumn + "=" + id, null);
		Log.d(TAG, "No update done (column=" + column + " value=" + value
				+ "). Value may already be set.");
		return ret;
	}

	public boolean getSettingBool(Enum<?> e) {
		String val = getSetting(e);
		return (val != null ? "true".equalsIgnoreCase(val) : false);
	}

	public int getSettingInt(Enum<?> e) {
		String val = getSetting(e);
		return (val != null ? Integer.parseInt(val) : -1);
	}

	public long getSettingLong(Enum<?> e, long def) {
		String val = getSetting(e);
		return (val != null ? Long.parseLong(val) : def);
	}

	public String getSetting(Enum<?> setting) {
		return getSetting(setting, null);
	}

	public String getSetting(Enum<?> setting, String def) {
		String key = setting.name();

		Cursor c = mDb.query(true, DATABASE_TABLE_SETTING,
				new String[] { SETTING_VALUE }, SETTING_KEY + "='" + key+"'", null,
				null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No settings found!");
		}else{
			def = c.getString(c.getColumnIndexOrThrow(SETTING_VALUE));
		}
		Util.closeCursor(c);
		return def;
	}

	public boolean setSetting(Enum<?> setting, Object value) {
		String key = setting.name();
		ContentValues args = new ContentValues();
		args.put(SETTING_KEY, key);
		args.put(SETTING_VALUE, value.toString());
		boolean update = mDb.update(DATABASE_TABLE_SETTING, args, SETTING_KEY
				+ "='"+key+"'", null) != 0;
		if (!update) {
			if(-1 == mDb.insert(DATABASE_TABLE_SETTING, null, args)){
				Log.d(TAG, "Could not set setting: "+key);
				return false;
			}
		}
		return true;
	}

}
