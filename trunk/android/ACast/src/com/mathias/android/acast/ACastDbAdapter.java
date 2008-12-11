package com.mathias.android.acast;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;

public class ACastDbAdapter {

	public static final String FEED_ID = "_id";
	public static final String FEED_TITLE = "title";
	public static final String FEED_URI = "uri";

	private static final String FEEDITEM_ID = "_id";
	private static final String FEEDITEM_FEEDID = "feed_id";
	public static final String FEEDITEM_TITLE = "title";
	public static final String FEEDITEM_MP3URI = "mp3uri";
	public static final String FEEDITEM_MP3FILE = "mp3file";

	private static final String DATABASE_NAME = "acast";
	private static final String DATABASE_TABLE_FEED = "feed";
	private static final String DATABASE_TABLE_FEEDITEM = "feeditem";
	private static final int DATABASE_VERSION = 7;

	private static final String DATABASE_CREATE_FEED = "create table feed (_id integer primary key autoincrement, "
			+ "title text not null, uri text not null);";
	private static final String DATABASE_CREATE_FEEDITEM = "create table feeditem (_id integer primary key autoincrement, "
			+ "feed_id integer, title text, mp3uri text, mp3file text);";

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
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FEED);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FEEDITEM);
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

	public long createFeed(String title, String uri) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(FEED_TITLE, title);
		initialValues.put(FEED_URI, uri);
		return mDb.insert(DATABASE_TABLE_FEED, null, initialValues);
	}

	public boolean deleteFeed(long rowId) {
		return mDb.delete(DATABASE_TABLE_FEED, FEED_ID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllFeeds() {
		return mDb.query(DATABASE_TABLE_FEED, new String[] { FEED_ID,
				FEED_TITLE, FEED_URI }, null, null, null, null, null);
	}

	public Cursor fetchFeed(long rowId) throws SQLException {
		Cursor c = mDb.query(true, DATABASE_TABLE_FEED, new String[] {
				FEED_ID, FEED_TITLE, FEED_URI }, FEED_ID + "=" + rowId, null,
				null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public Feed fetchFeed2(long rowId) throws SQLException {
		Cursor c = fetchFeed(rowId);
		String title = c.getString(c.getColumnIndex(ACastDbAdapter.FEED_TITLE));
		String uri = c.getString(c.getColumnIndex(ACastDbAdapter.FEED_URI));
		Feed feed = new Feed(rowId, title, uri);
		Cursor items = fetchFeedItems(rowId);
		if(!items.moveToFirst()){
			Log.w(TAG, "No feed items!");
			return feed;
		}
		do{
			long itemId = c.getLong(c.getColumnIndexOrThrow(ACastDbAdapter.FEEDITEM_ID));
			String itemTitle = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEEDITEM_TITLE));
			String mp3uri = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEEDITEM_MP3URI));
			String mp3file = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEEDITEM_MP3FILE));
			feed.addItem(new FeedItem(itemId, rowId, itemTitle, mp3uri, mp3file));
		}while(items.moveToNext());
		return feed;
	}

	public Cursor fetchFeedItems(long rowId) throws SQLException {
		return mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
				FEEDITEM_ID, FEEDITEM_TITLE, FEEDITEM_MP3URI, FEEDITEM_MP3FILE }, FEEDITEM_FEEDID + "=" + rowId, null,
				null, null, null, null);
	}

	public FeedItem fetchFeedItem(long feedId, long feedItemId) throws SQLException {
		Cursor c = mDb.query(true, DATABASE_TABLE_FEEDITEM, new String[] {
				FEEDITEM_ID, FEEDITEM_TITLE, FEEDITEM_MP3URI, FEEDITEM_MP3FILE }, FEEDITEM_FEEDID + "=" + feedId+" and "+FEEDITEM_ID +"="+feedItemId, null,
				null, null, null, null);
		if (c != null) {
			c.moveToFirst();
			String title = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEEDITEM_TITLE));
			String mp3uri = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEEDITEM_MP3URI));
			String mp3file = c.getString(c.getColumnIndexOrThrow(ACastDbAdapter.FEEDITEM_MP3FILE));
			return new FeedItem(feedItemId, feedId, title, mp3uri, mp3file);
		}
		return null;
	}

	public boolean updateFeed(long rowId, Feed feed) {
		if(feed == null){
			return false;
		}
		updateFeed(rowId, feed.getTitle(), feed.getUri());
		deleteFeedItems(rowId);
		for (FeedItem item : feed.getItems()) {
			addFeedItem(rowId, item);
		}
		return true;
	}

	public boolean updateFeed(long rowId, String title, String uri) {
		ContentValues args = new ContentValues();
		args.put(FEED_TITLE, title);
		args.put(FEED_URI, uri);
		return mDb.update(DATABASE_TABLE_FEED, args, FEED_ID + "=" + rowId,
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
		return mDb.update(DATABASE_TABLE_FEEDITEM, args, FEEDITEM_ID + "=" + item.getId(),
				null) > 0;
	}

	public boolean deleteFeedItems(long rowId) {
		return mDb.delete(DATABASE_TABLE_FEEDITEM, FEEDITEM_FEEDID + "=" + rowId, null) > 0;
	}

	public long addFeedItem(long feedId, FeedItem item) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(FEEDITEM_FEEDID, feedId);
		initialValues.put(FEEDITEM_TITLE, item.getTitle());
		initialValues.put(FEEDITEM_MP3URI, item.getMp3uri());
		initialValues.put(FEEDITEM_MP3FILE, item.getMp3file());
		return mDb.insert(DATABASE_TABLE_FEEDITEM, null, initialValues);
	}

}
