package com.ragentek.ypush.service.db;

import com.ragentek.ypush.service.util.YPushConfig;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ragentek.ypush.service.db.DatabaseHelper;

public class MyDatabaseUtil {

	private static final String TAG = "YPushService.MyDatabaseUtil";

	/**
	 * Database Version
	 */
	private static final int DATABASE_VERSION = 2;

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


	private static MyDatabaseUtil mInstance;

	public static MyDatabaseUtil getInstance(Context context){
		if(mInstance == null){
			mInstance = new MyDatabaseUtil(context);
		}
		return mInstance;
	}
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 *
	 * @param ctx the Context within which to work
	 */
	public MyDatabaseUtil(Context ctx) {
		this.mCtx = ctx;
		
		mDbHelper = new DatabaseHelper(mCtx, YPushConfig.DATABASE_NAME);
		mDb = mDbHelper.getWritableDatabase();
		
	}
	
	//zhaoshh. for DB
	public synchronized String myDBQuery(String key) {
		String currentQueryStr = "";
		
		SQLiteDatabase tempSQLiteDatabase = mCtx.openOrCreateDatabase(YPushConfig.DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = tempSQLiteDatabase.rawQuery( "select * from YPushServiceDB" , null);
		
		try {
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						int columnIndex = cursor.getColumnIndex(key);
						currentQueryStr = cursor.getString(columnIndex);
					} while (cursor.moveToNext());
				}

				//cursor.close();
			}
			
		} finally {
			if (cursor != null) {
				Log.d(TAG, "close the cursor!");
				cursor.close();
			}

			tempSQLiteDatabase.close();
		}
		
		Log.d(TAG, "currentQueryStr=" + currentQueryStr);
		
		return currentQueryStr;
	}

	//sherry for DB
	public synchronized String myEventStatisticsDBQuery(String key) {
		String currentQueryStr = "";

		SQLiteDatabase tempSQLiteDatabase = mCtx.openOrCreateDatabase(YPushConfig.DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = tempSQLiteDatabase.rawQuery( "select * from EventStatisticsDB" , null);

		try {
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						int columnIndex = cursor.getColumnIndex(key);
						currentQueryStr = cursor.getString(columnIndex);
					} while (cursor.moveToNext());
				}

				//cursor.close();
			}

		} finally {
			if (cursor != null) {
				Log.d(TAG, "close the cursor!");
				cursor.close();
			}

			tempSQLiteDatabase.close();
		}

		Log.d(TAG, "currentQueryStr=" + currentQueryStr);

		return currentQueryStr;
	}

	public synchronized void myDBUpdate(String key, String value) {

		SQLiteDatabase tempSQLiteDatabase = mDbHelper.getWritableDatabase();
		
		ContentValues contentValus = new ContentValues();
		contentValus.put(key, value);
		
		tempSQLiteDatabase.update("YPushServiceDB", contentValus, null, null);
		tempSQLiteDatabase.close();
	}

	//sherry for db
	public synchronized void myEventStatisticsDBUpdate(String key, String value) {

		SQLiteDatabase tempSQLiteDatabase = mDbHelper.getWritableDatabase();

		ContentValues contentValus = new ContentValues();
		contentValus.put(key, value);

		tempSQLiteDatabase.update("EventStatisticsDB", contentValus, null, null);
		tempSQLiteDatabase.close();
	}

	public synchronized void initDb() {
		SQLiteDatabase tempSQLiteDatabase = mDbHelper.getWritableDatabase();
		ContentValues contentValus = new ContentValues();
		
		contentValus.put("haveInitDB", "1");//0:not init DB; 1:have init DB
		contentValus.put("haveLocation", "0");//0:not get location from gps; 1:have get location from gps
		contentValus.put("requestLocaton", "0");//0:not have service get location cmd; 1:have service get location cmd
		contentValus.put("haveResponse", "0");//0:not response service get location cmd; 1:have response service get location cmd
		contentValus.put("Longitude", "0");//0:default Longitude
		contentValus.put("Latitude", "0");//0:default Latitude
		
		contentValus.put("haveResolution", "0");//0:not get resolution; 1:have get resolution
		contentValus.put("resoWidth", "0");//0:default resoWidth
		contentValus.put("resoHeight", "0");//0:default resoHeight
		
		contentValus.put("imei", "0");//0:imei
		contentValus.put("ipAddress", "0");//0: default ip
		contentValus.put("haveIpAddress", "0");//0:not get ip from http; 1:have get ip from http
		contentValus.put("notificationId", "100");//100: first notificationId
		contentValus.put("curIntervalTime", "0");//3: current interval time. default: 0 minute
		contentValus.put("bootConnected", "0");//boot or restart will connect socket flag. 0:not connect,1:have connected. default:0
		contentValus.put("wifiVisitFlag", "0");//wifiVisitFlag: 0: same speed, 1: Telecom fast, 2: Unicom fast, default: 0
        // modify by xiaolin.he 20150804 start . change the default upload interval time to 12 hours
        // contentValus.put("pollingInterval", "24");//3: default polling interval time. default: 3 hours. unit: hours
        contentValus.put("pollingInterval", "12");
        // modify by xiaolin.he 20150804 end .
		contentValus.put("dealMsgOption", "0");//0: option open(deal push msg), 1: option close(not deal push msg), default: 0
		
		//add for OTA,because ota not clear data/data
		contentValus.put("extend1", "0");//now not use
		contentValus.put("extend2", "0");//now not use
		contentValus.put("extend3", "0");//now not use
		contentValus.put("extend4", "0");//now not use
		contentValus.put("extend5", "0");//now not use
		contentValus.put("extend6", "0");//now not use
		
		tempSQLiteDatabase.insert("YPushServiceDB", null, contentValus);
		tempSQLiteDatabase.close();
	}
		


	
	
	/**
	 * This method is used for creating/opening connection
	 * @return instance of MyDatabaseUtil
	 * @throws SQLException
	 */
	public MyDatabaseUtil open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx, YPushConfig.DATABASE_NAME);
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
	 * add by sherry for delete
	 */
	public boolean deleteEventStatistics(){
		return mDb.delete("EventStatisticsDB","mark=?",new String[]{"1"})>0;
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

	//add by sherry
	public Cursor queryMarkIsZero() throws SQLException {
		Cursor mCursor =
				mDb.query(true, "EventStatisticsDB", null, "mark=?", new String[]{"0"},
						null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
}
