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

	public static final String FEED_ID = "_id";
	public static final String FEED_TITLE = "title";
	public static final String FEED_URI = "uri";
	public static final String FEED_ICON = "icon";
	public static final String FEED_LINK = "link";
	public static final String FEED_PUBDATE = "pubdate";
	public static final String FEED_CATEGORY = "category";
	public static final String FEED_AUTHOR = "author";
	public static final String FEED_DESCRIPTION = "description";

	public static final String FEEDITEM_ID = "_id";
	public static final String FEEDITEM_FEEDID = "feed_id";
	public static final String FEEDITEM_TITLE = "title";
	public static final String FEEDITEM_MP3URI = "mp3uri";
	public static final String FEEDITEM_MP3FILE = "mp3file";
	public static final String FEEDITEM_SIZE = "size";
	public static final String FEEDITEM_BOOKMARK = "bookmark";
	public static final String FEEDITEM_COMPLETED = "completed";
	public static final String FEEDITEM_DOWNLOADED = "downloaded";
	public static final String FEEDITEM_LINK = "link";
	public static final String FEEDITEM_PUBDATE = "pubdate";
	public static final String FEEDITEM_CATEGORY = "category";
	public static final String FEEDITEM_AUTHOR = "author";
	public static final String FEEDITEM_COMMENTS = "comments";
	public static final String FEEDITEM_DESCRIPTION = "description";

	public static final String SETTING_ID = "_id";
	public static final String SETTING_VOLUME = "volume";
	public static final String SETTING_LASTFEEDITEMID = "lastfeeditemid";

	private static final String DATABASE_NAME = "acast";
	private static final String DATABASE_TABLE_FEED = "feed";
	private static final String DATABASE_TABLE_FEEDITEM = "feeditem";
	private static final String DATABASE_TABLE_SETTING = "setting";

	private static final String DATABASE_CREATE_FEED = "create table feed ("
			+ FEED_ID+" integer primary key autoincrement, "
			+ FEED_TITLE+" text not null unique, " 
			+ FEED_URI+" text not null unique, " 
			+ FEED_ICON+" text, "
			+ FEED_LINK+" text, "
			+ FEED_PUBDATE+" text, "
			+ FEED_CATEGORY+" text, "
			+ FEED_AUTHOR+" text, "
			+ FEED_DESCRIPTION+" text);";

	private static final String DATABASE_CREATE_FEEDITEM = "create table feeditem ("
			+ FEEDITEM_ID+" integer primary key autoincrement, "
			+ FEEDITEM_FEEDID+" integer not null, "
			+ FEEDITEM_TITLE+" text not null unique, "
			+ FEEDITEM_MP3URI+" text not null, "
			+ FEEDITEM_MP3FILE+" text not null unique, "
			+ FEEDITEM_SIZE+" long not null, "
			+ FEEDITEM_BOOKMARK+" integer, "
			+ FEEDITEM_COMPLETED+" boolean, " 
			+ FEEDITEM_DOWNLOADED+" boolean, " 
			+ FEEDITEM_LINK+" text, "
			+ FEEDITEM_PUBDATE+" text, "
			+ FEEDITEM_CATEGORY+" text, "
			+ FEEDITEM_AUTHOR+" text, "
			+ FEEDITEM_COMMENTS+" text, "
			+ FEEDITEM_DESCRIPTION+" text);";

	private static final String DATABASE_CREATE_SETTING = "create table setting ("
			+ SETTING_ID+" integer primary key autoincrement, "
			+ SETTING_VOLUME+" integer, "
			+ SETTING_LASTFEEDITEMID+" integer);";

	private static final int DATABASE_VERSION = 30;

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

	private long createFeed(String title, String uri, String icon, String link,
			String pubdate, String category, String author, String description) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(FEED_TITLE, title);
		initialValues.put(FEED_URI, uri);
		initialValues.put(FEED_ICON, icon);
		initialValues.put(FEED_LINK, link);
		initialValues.put(FEED_PUBDATE, pubdate);
		initialValues.put(FEED_CATEGORY, category);
		initialValues.put(FEED_AUTHOR, author);
		initialValues.put(FEED_DESCRIPTION, description);
		return mDb.insert(DATABASE_TABLE_FEED, null, initialValues);
	}

	public boolean createFeed(Feed feed) {
		if(feed == null){
			Log.w(TAG, "Feed is null!");
			return false;
		}
		long id = createFeed(feed.getTitle(), feed.getUri(), feed.getIcon(),
				feed.getLink(), feed.getPubdate(), feed.getCategory(), feed
						.getAuthor(), feed.getDescription());
		deleteFeedItems(id);
		for (FeedItem item : feed.getItems()) {
			addFeedItem(id, item);
		}
		return true;
	}

	public boolean deleteFeed(long id) {
		mDb.delete(DATABASE_TABLE_FEEDITEM, FEEDITEM_FEEDID + "=" + id, null);
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

	public List<Feed> fetchAllFeeds() {
		List<Feed> feeds = new ArrayList<Feed>();
		Cursor c = mDb
				.query(DATABASE_TABLE_FEED, new String[] { FEED_ID, FEED_TITLE,
						FEED_URI, FEED_ICON, FEED_LINK, FEED_PUBDATE,
						FEED_CATEGORY, FEED_AUTHOR, FEED_DESCRIPTION }, null, null, null,
						null, null);
		if(c == null || !c.moveToFirst()){
			Log.w(TAG, "No feeds!");
		}else{
			do{
				long id = c.getLong(c.getColumnIndexOrThrow(FEED_ID));
				String title = c.getString(c.getColumnIndexOrThrow(FEED_TITLE));
				String uri = c.getString(c.getColumnIndexOrThrow(FEED_URI));
				String icon = c.getString(c.getColumnIndexOrThrow(FEED_ICON));
				String link = c.getString(c.getColumnIndexOrThrow(FEED_LINK));
				String pubdate = c.getString(c.getColumnIndexOrThrow(FEED_PUBDATE));
				String category = c.getString(c.getColumnIndexOrThrow(FEED_CATEGORY));
				String author = c.getString(c.getColumnIndexOrThrow(FEED_AUTHOR));
				String description = c.getString(c.getColumnIndexOrThrow(FEED_DESCRIPTION));
				Feed feed = new Feed(id, title, uri, icon, link, pubdate, category, author, description);
				List<FeedItem> items = fetchAllFeedItems(id);
				for (FeedItem item : items) {
					feed.addItem(item);
				}
				feeds.add(feed);
			}while(c.moveToNext());
		}
		Util.closeCursor(c);
		return feeds;
	}

	public Feed fetchFeed(long id) throws SQLException {
		Feed feed = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_FEED, new String[] {
				FEED_TITLE, FEED_URI, FEED_ICON, FEED_LINK, FEED_PUBDATE,
				FEED_CATEGORY, FEED_AUTHOR, FEED_DESCRIPTION }, FEED_ID + "=" + id, null,
				null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No feed for: "+id);
		}else{
			String title = c.getString(c.getColumnIndex(FEED_TITLE));
			String uri = c.getString(c.getColumnIndex(FEED_URI));
			String icon = c.getString(c.getColumnIndex(FEED_ICON));
			String link = c.getString(c.getColumnIndex(FEED_LINK));
			String pubdate = c.getString(c.getColumnIndex(FEED_PUBDATE));
			String category = c.getString(c.getColumnIndex(FEED_CATEGORY));
			String author = c.getString(c.getColumnIndex(FEED_AUTHOR));
			String description = c.getString(c.getColumnIndex(FEED_DESCRIPTION));
			feed = new Feed(id, title, uri, icon, link, pubdate, category,
					author, description);
			List<FeedItem> items = fetchAllFeedItems(id);
			for (FeedItem item : items) {
				feed.addItem(item);
			}
		}
		Util.closeCursor(c);
		return feed;
	}

	public List<FeedItem> fetchAllFeedItems(long feedId) throws SQLException {
		List<FeedItem> items = new ArrayList<FeedItem>();
		Cursor c = mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
				FEEDITEM_ID, FEEDITEM_TITLE, FEEDITEM_MP3URI, FEEDITEM_MP3FILE,
				FEEDITEM_SIZE, FEEDITEM_BOOKMARK, FEEDITEM_COMPLETED,
				FEEDITEM_DOWNLOADED, FEEDITEM_LINK, FEEDITEM_PUBDATE,
				FEEDITEM_CATEGORY, FEEDITEM_AUTHOR, FEEDITEM_COMMENTS,
				FEEDITEM_DESCRIPTION }, FEEDITEM_FEEDID + "=" + feedId, null,
				null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No feed items for: "+feedId);
		}else{
			do{
				long id = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_ID));
				String title = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TITLE));
				String mp3uri = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3URI));
				String mp3file = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3FILE));
				long size = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_SIZE));
				int bookmark = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_BOOKMARK));
				short completed = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_COMPLETED));
				short downloaded = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_DOWNLOADED));
				String link = c.getString(c.getColumnIndexOrThrow(FEEDITEM_LINK));
				String pubdate = c.getString(c.getColumnIndexOrThrow(FEEDITEM_PUBDATE));
				String category = c.getString(c.getColumnIndexOrThrow(FEEDITEM_CATEGORY));
				String author = c.getString(c.getColumnIndexOrThrow(FEEDITEM_AUTHOR));
				String comments = c.getString(c.getColumnIndexOrThrow(FEEDITEM_COMMENTS));
				String description = c.getString(c.getColumnIndexOrThrow(FEEDITEM_DESCRIPTION));
				FeedItem item = new FeedItem(id, feedId, title, mp3uri, mp3file,
						size, bookmark, completed != 0, downloaded != 0, link,
						pubdate, category, author, comments, description);
				items.add(item);
			}while(c.moveToNext());
		}
		Util.closeCursor(c);
		return items;
	}

	public FeedItem fetchFeedItem(long feedId, long feedItemId) throws SQLException {
		FeedItem item = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
				FEEDITEM_TITLE, FEEDITEM_MP3URI, FEEDITEM_MP3FILE,
				FEEDITEM_SIZE, FEEDITEM_BOOKMARK, FEEDITEM_COMPLETED,
				FEEDITEM_DOWNLOADED, FEEDITEM_LINK, FEEDITEM_PUBDATE,
				FEEDITEM_CATEGORY, FEEDITEM_AUTHOR, FEEDITEM_COMMENTS,
				FEEDITEM_DESCRIPTION }, FEEDITEM_FEEDID + "=" + feedId
				+ " and " + FEEDITEM_ID + "=" + feedItemId, null, null, null,
				null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No feed item for: "+feedId+" "+feedItemId);
		}else{
			String title = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TITLE));
			String mp3uri = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3URI));
			String mp3file = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3FILE));
			long size = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_SIZE));
			int bookmark = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_BOOKMARK));
			short completed = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_COMPLETED));
			short downloaded = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_DOWNLOADED));
			String link = c.getString(c.getColumnIndexOrThrow(FEEDITEM_LINK));
			String pubdate = c.getString(c.getColumnIndexOrThrow(FEEDITEM_PUBDATE));
			String category = c.getString(c.getColumnIndexOrThrow(FEEDITEM_CATEGORY));
			String author = c.getString(c.getColumnIndexOrThrow(FEEDITEM_AUTHOR));
			String comments = c.getString(c.getColumnIndexOrThrow(FEEDITEM_COMMENTS));
			String description = c.getString(c.getColumnIndexOrThrow(FEEDITEM_DESCRIPTION));
			item = new FeedItem(feedItemId, feedId, title, mp3uri, mp3file,
					size, bookmark, completed != 0, downloaded != 0, link,
					pubdate, category, author, comments, description);
		}
		Util.closeCursor(c);
		return item;
	}

	public FeedItem fetchFeedItem(long feedItemId) throws SQLException {
		FeedItem item = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
				FEEDITEM_ID, FEEDITEM_FEEDID, FEEDITEM_TITLE, FEEDITEM_MP3URI,
				FEEDITEM_MP3FILE, FEEDITEM_SIZE, FEEDITEM_BOOKMARK,
				FEEDITEM_COMPLETED, FEEDITEM_DOWNLOADED, FEEDITEM_LINK,
				FEEDITEM_PUBDATE, FEEDITEM_CATEGORY, FEEDITEM_AUTHOR,
				FEEDITEM_COMMENTS, FEEDITEM_DESCRIPTION }, FEEDITEM_ID + "="
				+ feedItemId, null, null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No feed item for: "+feedItemId);
		}else{
			long feedId = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_FEEDID));
			String title = c.getString(c.getColumnIndexOrThrow(FEEDITEM_TITLE));
			String mp3uri = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3URI));
			String mp3file = c.getString(c.getColumnIndexOrThrow(FEEDITEM_MP3FILE));
			long size = c.getLong(c.getColumnIndexOrThrow(FEEDITEM_SIZE));
			int bookmark = c.getInt(c.getColumnIndexOrThrow(FEEDITEM_BOOKMARK));
			short completed = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_COMPLETED));
			short downloaded = c.getShort(c.getColumnIndexOrThrow(FEEDITEM_DOWNLOADED));
			String link = c.getString(c.getColumnIndexOrThrow(FEEDITEM_LINK));
			String pubdate = c.getString(c.getColumnIndexOrThrow(FEEDITEM_PUBDATE));
			String category = c.getString(c.getColumnIndexOrThrow(FEEDITEM_CATEGORY));
			String author = c.getString(c.getColumnIndexOrThrow(FEEDITEM_AUTHOR));
			String comments = c.getString(c.getColumnIndexOrThrow(FEEDITEM_COMMENTS));
			String description = c.getString(c.getColumnIndexOrThrow(FEEDITEM_DESCRIPTION));
			item = new FeedItem(feedItemId, feedId, title, mp3uri, mp3file,
					size, bookmark, completed != 0, downloaded != 0, link,
					pubdate, category, author, comments, description);
		}
		Util.closeCursor(c);
		return item;
	}

	public boolean updateFeed(long id, Feed feed) {
		if(feed == null){
			Log.w(TAG, "Feed is null!");
			return false;
		}
		updateFeed(id, feed.getTitle(), feed.getUri(), feed.getIcon(), feed
				.getLink(), feed.getPubdate(), feed.getCategory(), feed
				.getAuthor(), feed.getDescription());
		deleteFeedItems(id);
		for (FeedItem item : feed.getItems()) {
			addFeedItem(id, item);
		}
		return true;
	}

	public boolean updateFeed(long id, String title, String uri, String icon,
			String link, String pubdate, String category, String author, String description) {
		ContentValues args = new ContentValues();
		args.put(FEED_TITLE, title);
		args.put(FEED_URI, uri);
		args.put(FEED_ICON, icon);
		args.put(FEED_LINK, link);
		args.put(FEED_PUBDATE, pubdate);
		args.put(FEED_CATEGORY, category);
		args.put(FEED_AUTHOR, author);
		args.put(FEED_DESCRIPTION, description);
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
		args.put(FEEDITEM_BOOKMARK, item.getBookmark());
		args.put(FEEDITEM_COMPLETED, (item.isCompleted() ? 1 : 0));
		args.put(FEEDITEM_DOWNLOADED, (item.isDownloaded() ? 1 : 0));
		args.put(FEEDITEM_LINK, item.getLink());
		args.put(FEEDITEM_PUBDATE, item.getPubdate());
		args.put(FEEDITEM_CATEGORY, item.getCategory());
		args.put(FEEDITEM_AUTHOR, item.getAuthor());
		args.put(FEEDITEM_COMMENTS, item.getComments());
		args.put(FEEDITEM_DESCRIPTION, item.getDescription());
		return mDb.update(DATABASE_TABLE_FEEDITEM, args, FEEDITEM_ID + "="
				+ item.getId(), null) > 0;
	}

//	public boolean updateFeedItemBookmark(long id, int bookmark) {
//		ContentValues args = new ContentValues();
//		args.put(FEEDITEM_BOOKMARK, bookmark);
//		return mDb.update(DATABASE_TABLE_FEEDITEM, args,
//				FEEDITEM_ID + "=" + id, null) > 0;
//	}
//
//	public boolean updateFeedItemCompleted(long id, boolean completed) {
//		ContentValues args = new ContentValues();
//		args.put(FEEDITEM_COMPLETED, (completed ? 1 : 0));
//		return mDb.update(DATABASE_TABLE_FEEDITEM, args,
//				FEEDITEM_ID + "=" + id, null) > 0;
//	}

	public boolean updateFeedItem(long id, String column, Object value) {
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
			throw new RuntimeException("Could not store feeditem: " + value
					+ " in " + column);
		}
		return mDb.update(DATABASE_TABLE_FEEDITEM, args,
				FEEDITEM_ID + "=" + id, null) > 0;
	}

	public boolean deleteFeedItems(long id) {
		return mDb.delete(DATABASE_TABLE_FEEDITEM, FEEDITEM_FEEDID + "=" + id, null) > 0;
	}

	private long addFeedItem(long feedId, FeedItem item) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(FEEDITEM_FEEDID, feedId);
		initialValues.put(FEEDITEM_TITLE, item.getTitle());
		initialValues.put(FEEDITEM_MP3URI, item.getMp3uri());
		initialValues.put(FEEDITEM_MP3FILE, item.getMp3file());
		initialValues.put(FEEDITEM_SIZE, item.getSize());
		initialValues.put(FEEDITEM_BOOKMARK, item.getBookmark());
		initialValues.put(FEEDITEM_COMPLETED, (item.isCompleted() ? 1 : 0));
		initialValues.put(FEEDITEM_DOWNLOADED, (item.isDownloaded() ? 1 : 0));
		initialValues.put(FEEDITEM_LINK, item.getLink());
		initialValues.put(FEEDITEM_PUBDATE, item.getPubdate());
		initialValues.put(FEEDITEM_CATEGORY, item.getCategory());
		initialValues.put(FEEDITEM_AUTHOR, item.getAuthor());
		initialValues.put(FEEDITEM_COMMENTS, item.getComments());
		initialValues.put(FEEDITEM_DESCRIPTION, item.getDescription());
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
