package com.ragentek.ypush.service.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseUtil{

	private static final String TAG = "DatabaseUtil";

	/**
	 * Database Name
	 */
	private static final String DATABASE_NAME = "ypush_db";

	/**
	 * Database Version
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * Table Name
	 */
	public static final String TB_MSG = "tb_msg";
	
	/**
	 * Table Name
	 */
	public static final String TB_USER_INFO = "tb_user_info";

	/**
	 * Table columns
	 */
	public static final String KEY_ROWID = "_id";

	/**
	 * Database creation sql statement
	 */
	private static final String CREATE_MSG_TABLE =
		"create table " + TB_MSG + " (" + KEY_ROWID + " integer primary key autoincrement, "
		+ "msgType text not null, msgContext text not null,status text not null,createdOn text not null);";

	private static final String CREATE_APP_TABLE = 
			"create table " + TB_USER_INFO + " (" + KEY_ROWID + " integer primary key autoincrement, "
					 + "appName text not null,  appPackageName text not null,status text not null,createdOn text not null);";
	 
	/**
	 * Context
	 */
	private final Context mCtx;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Inner private class. Database Helper class for creating and updating database.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		/**
		 * onCreate method is called for the 1st time when database doesn't exists.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {			
			db.execSQL(CREATE_MSG_TABLE);
			db.execSQL(CREATE_APP_TABLE);
			Log.i(TAG, "Creating DataBase: " + CREATE_MSG_TABLE);
			Log.i(TAG, "Creating DataBase: " + CREATE_APP_TABLE);
		}
		/**
		 * onUpgrade method is called when database version changes.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 *
	 * @param ctx the Context within which to work
	 */
	public DatabaseUtil(Context ctx) {
		this.mCtx = ctx;
	}
	/**
	 * This method is used for creating/opening connection
	 * @return instance of DatabaseUtil
	 * @throws SQLException
	 */
	public DatabaseUtil open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	/**
	 * This method is used for closing the connection.
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * This method is used to create/insert new record  record.
	 * @param name
	 * @param grade
	 * @return long
	 */
	public long add(String tableName , ContentValues initialValues) {
		return mDb.insert(tableName, null, initialValues);
	}
	/**
	 * This method will delete record.
	 * @param rowId
	 * @return boolean
	 */
	public boolean delete(String tableName ,long rowId) {
		return mDb.delete(tableName, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * This method will return Cursor holding all the records.
	 * @return Cursor
	 */
	public Cursor fetchAll(String tableName,String[] queryCloumn) {
		return mDb.query(tableName, queryCloumn, null, null, null, null, null);
	}

	/**
	 * This method will return Cursor holding the specific record.
	 * @param id
	 * @return Cursor
	 * @throws SQLException
	 */
	public Cursor fetchOne(String tableName,long id,String[] queryCloumn) throws SQLException {
		Cursor mCursor =
			mDb.query(true, tableName, queryCloumn, KEY_ROWID + "=" + id, null,
					null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * This method will update table record.
	 * @param id
	 * @param name
	 * @param standard
	 * @return boolean
	 */
	public boolean update(String tableName,int id, ContentValues args) {		
		return mDb.update(tableName, args, KEY_ROWID + "=" + id, null) > 0;
	}
}
