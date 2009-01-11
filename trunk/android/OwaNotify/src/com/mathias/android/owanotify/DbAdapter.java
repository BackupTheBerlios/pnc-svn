package com.mathias.android.owanotify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {

	public static final String SETTING_KEY = "key";
	public static final String SETTING_VALUE = "value";

	private static final String DATABASE_NAME = "owanotify";
	public static final String DATABASE_TABLE_SETTING = "setting";

	private static final String DATABASE_CREATE_SETTING = "create table setting ("
			+ SETTING_KEY+" text primary key, "
			+ SETTING_VALUE+" text);";

	private static final int DATABASE_VERSION = 1;

	private static final String TAG = DbAdapter.class.getSimpleName();
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_SETTING);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SETTING);
			onCreate(db);
		}
	}

	public DbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public boolean update(String table, String idcolumn, long id, String column, Object value) {
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
		return mDb.update(table, args,
				idcolumn + "=" + id, null) > 0;
	}

	public boolean getSettingBool(Enum<?> e) {
		String val = getSetting(e.name());
		return (val != null ? "true".equalsIgnoreCase(val) : false);
	}

	public String getSetting(Enum<?> e, String def) {
		String ret = getSetting(e.name());
		return (ret != null ? ret : def);
	}

	public String getSetting(Enum<?> e) {
		return getSetting(e.name());
	}

	public String getSetting(String key) {
		String value = null;
		Cursor c = mDb.query(true, DATABASE_TABLE_SETTING,
				new String[] { SETTING_VALUE }, SETTING_KEY + "='" + key+"'", null,
				null, null, null, null);
		if (c == null || !c.moveToFirst()) {
			Log.w(TAG, "No settings found!");
		}else{
			value = c.getString(c.getColumnIndexOrThrow(SETTING_VALUE));
		}
		closeCursor(c);
		return value;
	}

	public boolean setSettingBool(Enum<?> e, boolean value) {
		return setSetting(e.name(), ""+value);
	}

	public boolean setSetting(Enum<?> e, Object value) {
		return setSetting(e.name(), value);
	}

	public boolean setSetting(String key, Object value) {
		ContentValues args = new ContentValues();
		args.put(SETTING_KEY, key);
		args.put(SETTING_VALUE, value.toString());
		boolean update = mDb.update(DATABASE_TABLE_SETTING, args, SETTING_KEY
				+ "='"+key+"'", null) > 0;
		if (!update) {
			long id = mDb.insert(DATABASE_TABLE_SETTING, null, args);
			if(id != 0){
				return false;
			}
		}
		return true;
	}

	public static void closeCursor(Cursor c){
		if(c != null){
			c.close();
			c = null;
		}
	}

}
