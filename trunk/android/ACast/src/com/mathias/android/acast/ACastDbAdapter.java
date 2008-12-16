package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mathias.android.acast.common.Util;
import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;
import com.mathias.android.acast.podcast.Settings;

public class ACastDbAdapter {

	private static final String FEED_ID = "_id";
	private static final String FEED_TITLE = "title";
	private static final String FEED_URI = "uri";

	private static final String FEEDITEM_ID = "_id";
	private static final String FEEDITEM_FEEDID = "feed_id";
	private static final String FEEDITEM_TITLE = "title";
	private static final String FEEDITEM_MP3URI = "mp3uri";
	private static final String FEEDITEM_MP3FILE = "mp3file";
	private static final String FEEDITEM_SIZE = "size";
	private static final String FEEDITEM_TYPE = "type";
	private static final String FEEDITEM_BOOKMARK = "bookmark";
	private static final String FEEDITEM_COMPLETED = "completed";

	private static final String SETTING_ID = "_id";
	private static final String SETTING_VOLUME = "volume";
	private static final String SETTING_LASTFEEDITEMID = "lastfeeditemid";

	private static final String DATABASE_NAME = "acast";
	private static final String DATABASE_TABLE_FEED = "feed";
	private static final String DATABASE_TABLE_FEEDITEM = "feeditem";
	private static final String DATABASE_TABLE_SETTING = "setting";

	private static final String DATABASE_CREATE_FEED = "create table feed (_id integer primary key autoincrement, "
			+ "title text not null, uri text not null);";
	private static final String DATABASE_CREATE_FEEDITEM = "create table feeditem (_id integer primary key autoincrement, "
			+ "feed_id integer, title text, mp3uri text, mp3file text, size long, type text, bookmark integer, completed boolean);";
	private static final String DATABASE_CREATE_SETTING = "create table setting (_id integer primary key autoincrement, "
		+ "volume integer, lastfeeditemid integer);";

	private static final int DATABASE_VERSION = 18;

	private static final String TAG = ACastDbAdapter.class.getSimpleName();
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_FEED);
			db.execSQL(DATABASE_CREATE_FEEDITEM);
			db.execSQL(DATABASE_CREATE_SETTING);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FEED);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FEEDITEM);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SETTING);
			onCreate(db);
		}
	}

	public ACastDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public ACastDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	private long createFeed(String title, String uri) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(FEED_TITLE, title);
		initialValues.put(FEED_URI, uri);
		return mDb.insert(DATABASE_TABLE_FEED, null, initialValues);
	}

	public boolean createFeed(Feed feed) {
		if(feed == null){
			Log.w(TAG, "Feed is null!");
			return false;
		}
		long id = createFeed(feed.getTitle(), feed.getUri());
		deleteFeedItems(id);
		for (FeedItem item : feed.getItems()) {
			addFeedItem(id, item);
		}
		return true;
	}

	public boolean deleteFeed(long id) {
		return mDb.delete(DATABASE_TABLE_FEED, FEED_ID + "=" + id, null) > 0;
	}

	public List<String> fetchAllFeedNames() {
		List<String> names = new ArrayList<String>();
		Cursor c = mDb.query(DATABASE_TABLE_FEED, new String[] { FEED_TITLE },
				null, null, null, null, null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
			return names;
		}else{
			do{
				names.add(c.getString(c.getColumnIndexOrThrow(FEED_TITLE)));
			}while(c.moveToNext());
		}
		Util.closeCursor(c);
		return names;
	}

	public List<Feed> fetchAllFeedsLight() {
		List<Feed> uris = new ArrayList<Feed>();
		Cursor c = mDb.query(DATABASE_TABLE_FEED, new String[] { FEED_ID,
				FEED_TITLE, FEED_URI }, null, null, null, null, null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
		}else{
			do{
				long id = c.getLong(c.getColumnIndexOrThrow(FEED_ID));
				String title = c.getString(c.getColumnIndexOrThrow(FEED_TITLE));
				String uri = c.getString(c.getColumnIndexOrThrow(FEED_URI));
				uris.add(new Feed(id, title, uri));
			}while(c.moveToNext());
		}
		Util.closeCursor(c);
		return uris;
	}

	public Feed fetchFeed(long id) throws SQLException {
		Feed feed = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_FEED, new String[] {
				FEED_TITLE, FEED_URI }, FEED_ID + "=" + id, null,
				null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No feed for: "+id);
		}else{
			String title = c.getString(c.getColumnIndex(FEED_TITLE));
			String uri = c.getString(c.getColumnIndex(FEED_URI));
			Util.closeCursor(c);
			feed = new Feed(id, title, uri);
			c = mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
					FEEDITEM_ID, FEEDITEM_TITLE, FEEDITEM_MP3URI,
					FEEDITEM_MP3FILE, FEEDITEM_SIZE, FEEDITEM_TYPE,
					FEEDITEM_BOOKMARK, FEEDITEM_COMPLETED }, FEEDITEM_FEEDID
					+ "=" + id, null, null, null, null, null);
			if(c == null || !c.moveToFirst()) {
				Log.w(TAG, "No feed items for: "+id);
			}else{
				do{
					long itemId = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_ID));
					String itemTitle = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TITLE));
					String mp3uri = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3URI));
					String mp3file = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3FILE));
					long size = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_SIZE));
					String type = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TYPE));
					int bookmark = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_BOOKMARK));
					short completed = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_COMPLETED));
					feed.addItem(new FeedItem(itemId, id, itemTitle, mp3uri, mp3file, size, type, bookmark, completed != 0));
				}while(c.moveToNext());
			}
		}
		Util.closeCursor(c);
		return feed;
	}

	public FeedItem fetchFeedItem(long feedId, long feedItemId) throws SQLException {
		FeedItem item = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
				FEEDITEM_ID, FEEDITEM_TITLE, FEEDITEM_MP3URI, FEEDITEM_MP3FILE,
				FEEDITEM_SIZE, FEEDITEM_TYPE, FEEDITEM_BOOKMARK,
				FEEDITEM_COMPLETED }, FEEDITEM_FEEDID + "=" + feedId + " and "
				+ FEEDITEM_ID + "=" + feedItemId, null, null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No feed item for: "+feedId+" "+feedItemId);
		}else{
			String title = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TITLE));
			String mp3uri = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3URI));
			String mp3file = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3FILE));
			long size = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_SIZE));
			String type = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TYPE));
			int bookmark = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_BOOKMARK));
			short completed = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_COMPLETED));
			item = new FeedItem(feedItemId, feedId, title, mp3uri, mp3file, size, type, bookmark, completed != 0);
		}
		Util.closeCursor(c);
		return item;
	}

	public FeedItem fetchFeedItem(long feedItemId) throws SQLException {
		FeedItem item = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
				FEEDITEM_ID, FEEDITEM_FEEDID, FEEDITEM_TITLE, FEEDITEM_MP3URI,
				FEEDITEM_MP3FILE, FEEDITEM_SIZE, FEEDITEM_TYPE,
				FEEDITEM_BOOKMARK, FEEDITEM_COMPLETED }, FEEDITEM_ID + "="
				+ feedItemId, null, null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No feed item for: "+feedItemId);
		}else{
			long feedId = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_FEEDID));
			String title = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TITLE));
			String mp3uri = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3URI));
			String mp3file = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3FILE));
			long size = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_SIZE));
			String type = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TYPE));
			int bookmark = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_BOOKMARK));
			short completed = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_COMPLETED));
			item = new FeedItem(feedItemId, feedId, title, mp3uri, mp3file, size, type, bookmark, completed != 0);
		}
		Util.closeCursor(c);
		return item;
	}

	public boolean updateFeed(long id, Feed feed) {
		if(feed == null){
			Log.w(TAG, "Feed is null!");
			return false;
		}
		updateFeed(id, feed.getTitle(), feed.getUri());
		deleteFeedItems(id);
		for (FeedItem item : feed.getItems()) {
			addFeedItem(id, item);
		}
		return true;
	}

	public boolean updateFeed(long id, String title, String uri) {
		ContentValues args = new ContentValues();
		args.put(FEED_TITLE, title);
		args.put(FEED_URI, uri);
		return mDb.update(DATABASE_TABLE_FEED, args, FEED_ID + "=" + id,
				null) > 0;
	}

	public boolean updateFeedItem(FeedItem item) {
		if(item == null){
			return false;
		}
		ContentValues args = new ContentValues();
		args.put(FEEDITEM_ID, item.getId());
		args.put(FEEDITEM_FEEDID, item.getFeedId());
		args.put(FEEDITEM_TITLE, item.getTitle());
		args.put(FEEDITEM_MP3URI, item.getMp3uri());
		args.put(FEEDITEM_MP3FILE, item.getMp3file());
		args.put(FEEDITEM_SIZE, item.getSize());
		args.put(FEEDITEM_TYPE, item.getType());
		args.put(FEEDITEM_BOOKMARK, item.getBookmark());
		args.put(FEEDITEM_BOOKMARK, (item.isCompleted() ? 1 : 0));
		return mDb.update(DATABASE_TABLE_FEEDITEM, args, FEEDITEM_ID + "="
				+ item.getId(), null) > 0;
	}

	public boolean updateFeedItemBookmark(long id, int bookmark) {
		ContentValues args = new ContentValues();
		args.put(FEEDITEM_BOOKMARK, bookmark);
		return mDb.update(DATABASE_TABLE_FEEDITEM, args,
				FEEDITEM_ID + "=" + id, null) > 0;
	}

	public boolean updateFeedItemCompleted(long id, boolean completed) {
		ContentValues args = new ContentValues();
		args.put(FEEDITEM_COMPLETED, (completed ? 1 : 0));
		return mDb.update(DATABASE_TABLE_FEEDITEM, args,
				FEEDITEM_ID + "=" + id, null) > 0;
	}

//	public boolean updateFeedItem(long id, String column, Object value) {
//		ContentValues args = new ContentValues();
//		if(value instanceof Integer){
//			args.put(column, (Integer)value);
//		}else if(value instanceof Long){
//			args.put(column, (Long)value);
//		}else if(value instanceof Short){
//			args.put(column, (Short)value);
//		}else if(value instanceof Boolean){
//			args.put(column, (short)(((Boolean)value) ? 1 : 0));
//		}else{
//			throw new RuntimeException("Could not store feeditem: "+value+" in "+column);
//		}
//		return mDb.update(DATABASE_TABLE_FEEDITEM, args,
//				FEEDITEM_ID + "=" + id, null) > 0;
//	}

	public boolean deleteFeedItems(long id) {
		return mDb.delete(DATABASE_TABLE_FEEDITEM, FEEDITEM_FEEDID + "=" + id, null) > 0;
	}

	public long addFeedItem(long feedId, FeedItem item) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(FEEDITEM_FEEDID, feedId);
		initialValues.put(FEEDITEM_TITLE, item.getTitle());
		initialValues.put(FEEDITEM_MP3URI, item.getMp3uri());
		initialValues.put(FEEDITEM_MP3FILE, item.getMp3file());
		initialValues.put(FEEDITEM_SIZE, item.getSize());
		initialValues.put(FEEDITEM_TYPE, item.getType());
		initialValues.put(FEEDITEM_BOOKMARK, item.getBookmark());
		initialValues.put(FEEDITEM_COMPLETED, (item.isCompleted() ? 1 : 0));
		return mDb.insert(DATABASE_TABLE_FEEDITEM, null, initialValues);
	}

	public Settings fetchSettings() {
		Settings settings = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_SETTING, new String[] {
				SETTING_VOLUME, SETTING_LASTFEEDITEMID }, SETTING_ID + "=0",
				null, null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No settings found!");
		}else{
			Integer volume = c.getInt(c.getColumnIndexOrThrow(SETTING_VOLUME));
			if(volume <= 0){
				volume = null;
			}
			Long lastfeeditemid = c.getLong(c.getColumnIndexOrThrow(SETTING_LASTFEEDITEMID));
			if(lastfeeditemid <= 0){
				lastfeeditemid = null;
			}
			settings = new Settings(volume, lastfeeditemid);
		}
		Util.closeCursor(c);
		return settings;
	}

	public boolean updateSettings(Settings settings) {
		if(settings == null){
			return false;
		}
		ContentValues args = new ContentValues();
		args.put(SETTING_ID, 0);
		args.put(SETTING_VOLUME, settings.getVolume());
		args.put(SETTING_LASTFEEDITEMID, settings.getLastFeedItemId());
		boolean update = mDb.update(DATABASE_TABLE_SETTING, args, SETTING_ID
				+ "= 0", null) > 0;
		if (!update) {
			long id = mDb.insert(DATABASE_TABLE_SETTING, null, args);
			if(id != 0){
				return false;
			}
		}
		return true;
	}

}
