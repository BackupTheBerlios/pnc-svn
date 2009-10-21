package com.mathias.android.searchwidget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {

	// Engine
	public static final String ENGINE_ID = "_id";
	public static final String ENGINE_TITLE = "title";
	public static final String ENGINE_URI = "uri";
	public static final String ENGINE_ICON = "icon";
	public static final String ENGINE_BUTTON = "button";
	public static final String ENGINE_DESCRIPTION = "description";

	public static final String SETTING_KEY = "key";
	public static final String SETTING_VALUE = "value";

	private static final String DATABASE_NAME = "searchwidget";
	public static final String DATABASE_TABLE_ENGINE = "engine";
	public static final String DATABASE_TABLE_SETTING = "setting";

	private static final String DATABASE_CREATE_ENGINE = "create table "+DATABASE_TABLE_ENGINE+" ("
			+ ENGINE_ID+" integer primary key autoincrement, "
			+ ENGINE_TITLE+" text not null unique, " 
			+ ENGINE_URI+" text not null, " 
			+ ENGINE_ICON+" text, "
			+ ENGINE_BUTTON+" integer, "
			+ ENGINE_DESCRIPTION+" text);";

	private static final String DATABASE_CREATE_SETTING = "create table "+DATABASE_TABLE_SETTING+" ("
			+ SETTING_KEY+" text primary key, "
			+ SETTING_VALUE+" text);";

	private static final int DATABASE_VERSION = 1;

	private static final String TAG = DbAdapter.class.getSimpleName();

	private DatabaseHelper mDbHelper;
	
	protected SQLiteDatabase mDb;
	
	public final String name;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			upgrade(db, 0, DATABASE_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			upgrade(db, oldVersion, newVersion);
		}

		private void upgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			if(oldVersion < 1){
				Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ENGINE);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SETTING);
				db.execSQL(DATABASE_CREATE_ENGINE);
				db.execSQL(DATABASE_CREATE_SETTING);

				createEngine(new Engine("I'm Feeling Lucky", "http://www.google.com/search?btnI&q=", "googlelucky", 0, "I'm Feeling Lucky"), db);
				createEngine(new Engine("Google", "http://www.google.com/search?q=", "google", 1, "Google"), db);
				createEngine(new Engine("Wikipedia", "http://en.wikipedia.org/wiki/Special:Search?go=Go&search=", "wikipedia", 2, "Wikipedia"), db);
				createEngine(new Engine("IMDB", "http://imdb.com/find?s=all&q=", "imdb", 3, "Internet Movie Database"), db);
				createEngine(new Engine("Market", "http://market.android.com/search?q=", "market", 4, "Android Market"), db);
				createEngine(new Engine("YouTube", "http://www.youtube.com/results?search_type=&aq=f&search_query=", "youtube", 5, "YouTube"), db);
				createEngine(new Engine("Bing", "http://www.bing.com/search?go=&form=QBLH&q=", "bing", 6, "Bing"), db);
				createEngine(new Engine("Maps", "http://maps.google.com/?q=", "googlemaps", 7, "Google Maps"), db);
			}
//			if(oldVersion < 2){
//			}
		}
	}

	public DbAdapter(String name) {
		this.name = name;
	}

	public DbAdapter open(Context ctx, boolean readonly) {
		Log.d(TAG, "open: "+name);
		mDbHelper = new DatabaseHelper(ctx);
		if(readonly){
			mDb = mDbHelper.getReadableDatabase();
		}else{
			mDb = mDbHelper.getWritableDatabase();
		}
		return this;
	}

	public void close() {
		Log.d(TAG, "close: "+name);
		mDbHelper.close();
		mDbHelper = null;
	}

	private static long createEngine(Engine feed, SQLiteDatabase db) {
		Log.d(TAG, "create engine: "+feed.title+" "+feed.uri);
		ContentValues initialValues = new ContentValues();
		initialValues.put(ENGINE_TITLE, feed.title);
		initialValues.put(ENGINE_URI, feed.uri);
		initialValues.put(ENGINE_ICON, feed.icon);
		initialValues.put(ENGINE_BUTTON, feed.button);
		initialValues.put(ENGINE_DESCRIPTION, feed.description);
		long id = Constants.INVALID_ID;
		synchronized (DbAdapter.class){
			id = db.insert(DATABASE_TABLE_ENGINE, null, initialValues);
		}
		return id;
	}

	public void deleteEngine(long id) throws DatabaseException {
		synchronized (DbAdapter.class){
			if(mDb.delete(DATABASE_TABLE_ENGINE, ENGINE_ID + "=" + id, null) == 0){
				throw new DatabaseException("Could not remove engine");
			}
		}
	}


	public List<Engine> fetchAllEngines() {
		List<Engine> feeds = new ArrayList<Engine>();
		synchronized (DbAdapter.class){
			Cursor c = mDb.query(DATABASE_TABLE_ENGINE, new String[] {
					ENGINE_ID, ENGINE_TITLE, ENGINE_URI, ENGINE_ICON, ENGINE_BUTTON,
					ENGINE_DESCRIPTION }, null, null, null, null, null);
			if(c == null || !c.moveToFirst()){
				Log.w(TAG, "No engines!");
			}else{
				do{
					feeds.add(fetchEngine(c));
				}while(c.moveToNext());
			}
			Util.closeCursor(c);
		}
		return feeds;
	}

	public Engine fetchEngine(long id) {
		Engine feed = null;
		synchronized (DbAdapter.class){
			Cursor c = mDb.query(true, DATABASE_TABLE_ENGINE, new String[] { ENGINE_ID,
					ENGINE_TITLE, ENGINE_URI, ENGINE_ICON, ENGINE_BUTTON, ENGINE_DESCRIPTION }, ENGINE_ID + "=" + id, null,
					null, null, null, null);
			if (c == null || !c.moveToFirst()) {
				Log.w(TAG, "No engine for: "+id);
			}else{
				feed = fetchEngine(c);
			}
			Util.closeCursor(c);
		}
		return feed;
	}

	public Engine fetchEngineByButton(int button){
		Engine engine = null;
		synchronized (DbAdapter.class){
			Cursor c = mDb.query(DATABASE_TABLE_ENGINE, new String[] {
					ENGINE_ID, ENGINE_TITLE, ENGINE_URI, ENGINE_ICON, ENGINE_BUTTON,
					ENGINE_DESCRIPTION }, ENGINE_BUTTON+"="+button, null, null, null, null);
			if(c != null && c.moveToFirst()){
				engine = fetchEngine(c);
			}
			Util.closeCursor(c);
		}
		return engine;
	}

	private static Engine fetchEngine(Cursor c){
		long id = c.getLong(c.getColumnIndexOrThrow(ENGINE_ID));
		String title = c.getString(c.getColumnIndexOrThrow(ENGINE_TITLE));
		String uri = c.getString(c.getColumnIndexOrThrow(ENGINE_URI));
		String icon = c.getString(c.getColumnIndexOrThrow(ENGINE_ICON));
		Integer button = c.getInt(c.getColumnIndexOrThrow(ENGINE_BUTTON));
		String description = c.getString(c.getColumnIndexOrThrow(ENGINE_DESCRIPTION));
		Engine feed = new Engine(id, title, uri, icon, button, description);
		return feed;
	}

	public static class ValueHolder {
		long id;
		int newitems;
		int sum;

		public ValueHolder(long id, int newitems, int sum) {
			this.id = id;
			this.newitems = newitems;
			this.sum = sum;
		}
	}

	/**
	 * @param id
	 * @param column
	 * @param value
	 * @return updated rows
	 */
	public int updateEngine(long id, ContentValues args) {
		int ret = 0;
		try{
			synchronized (DbAdapter.class){
				ret = mDb.update(DATABASE_TABLE_ENGINE, args,
						ENGINE_ID + "=" + id, null);
			}
			if(ret != 1) {
				Log.d(TAG, "No update done.");
			}
		}catch(Throwable t){
			Log.e(TAG, t.getMessage(), t);
		}
		return ret;
	}

	/**
	 * @param id
	 * @param column
	 * @param value
	 * @return updated rows
	 */
	public int updateEngine(long id, String column, Object value) {
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
			throw new RuntimeException("Could not store feed: " + value
					+ " in " + column);
		}
		int ret = 0;
		try{
			synchronized (DbAdapter.class){
				ret = mDb.update(DATABASE_TABLE_ENGINE, args,
						ENGINE_ID + "=" + id, null);
			}
			if(ret != 1) {
				Log.d(TAG, "No update done (column="+column+" value="+value+"). Value may already be set.");
			}
		}catch(Throwable t){
			Log.e(TAG, t.getMessage(), t);
		}
		return ret;
	}

	public int updateEngine(Engine engine) {
		return updateEngine(engine.id, engine);
	}

	public int updateEngine(long id, Engine engine) {
		Log.d(TAG, "updateEngine: "+id);
		ContentValues args = new ContentValues();
		args.put(ENGINE_TITLE, engine.title);
		args.put(ENGINE_URI, engine.uri);
		args.put(ENGINE_ICON, engine.icon);
		args.put(ENGINE_BUTTON, engine.button);
		args.put(ENGINE_DESCRIPTION, engine.description);
		int ret = 0;
		try{
			synchronized (DbAdapter.class){
				ret = mDb.update(DATABASE_TABLE_ENGINE, args, ENGINE_ID + "=" + id, null);
			}
			if(ret != 1){
				Log.w(TAG, "Could not update feed! "+id);
			}
		}catch(Throwable t){
			Log.e(TAG, t.getMessage(), t);
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
	public boolean update(String table, String idcolumn, long id, String column,
			Object value) {
		ContentValues args = new ContentValues();
		if (value instanceof String) {
			args.put(column, (String) value);
		}else if (value instanceof Integer) {
			args.put(column, (Integer) value);
		} else if (value instanceof Long) {
			args.put(column, (Long) value);
		} else if (value instanceof Float) {
			args.put(column, (Float) value);
		} else if (value instanceof Short) {
			args.put(column, (Short) value);
		} else if (value instanceof Boolean) {
			args.put(column, (short) (((Boolean) value) ? 1 : 0));
		} else {
			Log.e(TAG, Util.buildString(
					"Unknown type!!! Could not store value=", value,
					" in column=", column, ", table=" + table));
			return false;
		}
		int ret = 0;
		try{
			synchronized (DbAdapter.class){
				ret = mDb.update(table, args, idcolumn + "=" + id, null);
			}
			Log.d(TAG, "No update done (column=" + column + " value=" + value
					+ "). Value may already be set.");
		}catch(Throwable t){
			Log.e(TAG, t.getMessage(), t);
		}
		return ret == 1;
	}

	public boolean getSettingBool(String key) {
		String val = getSetting(key);
		return (val != null ? "true".equalsIgnoreCase(val) : false);
	}

	public int getSettingInt(String key) {
		String val = getSetting(key);
		return (val != null ? Integer.parseInt(val) : -1);
	}

	public long getSettingLong(String key, long def) {
		String val = getSetting(key);
		return (val != null ? Long.parseLong(val) : def);
	}

	public String getSetting(String key) {
		return getSetting(key, null);
	}

	public String getSetting(String key, String def) {
		synchronized (DbAdapter.class){
			Cursor c = mDb.query(true, DATABASE_TABLE_SETTING,
					new String[] { SETTING_VALUE }, SETTING_KEY + "='" + key+"'", null,
					null, null, null, null);
			if (c == null || !c.moveToFirst()) {
				Log.d(TAG, "No setting found for: "+key);
			}else{
				def = c.getString(c.getColumnIndexOrThrow(SETTING_VALUE));
			}
			Util.closeCursor(c);
		}
		return def;
	}

	public void setSetting(String key, String value) throws DatabaseException {
		ContentValues args = new ContentValues();
		args.put(SETTING_KEY, key);
		args.put(SETTING_VALUE, value);
		try {
			synchronized (DbAdapter.class){
				boolean update = mDb.update(DATABASE_TABLE_SETTING, args,
						SETTING_KEY + "='" + key + "'", null) > 0;
				if (!update) {
					if (-1 == mDb.insert(DATABASE_TABLE_SETTING, null, args)) {
						throw new DatabaseException("Could not set setting: " + key);
					}
				}
			}
		} catch (Throwable t) {
			if (t instanceof DatabaseException) {
				throw (DatabaseException) t;
			}
			Log.e(TAG, t.getMessage(), t);
		}
	}

	public void setSettings(final List<String> keyValues){
		for (Iterator<String> it = keyValues.iterator(); it.hasNext(); ) {
			try {
				String key = it.next();
				String value = it.next();
				setSetting(key, value);
			} catch (Throwable t) {
				Log.e(TAG, t.getMessage(), t);
			}
		}
	}

	public int getVersion(){
		return mDb.getVersion();
	}

	@SuppressWarnings("serial")
	public static class DatabaseException extends Exception {
		public DatabaseException(String msg){
			super(msg);
		}
	}

}
