package com.ragentek.ypush.service.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String TAG = "YPushService.DatabaseHelper";

	private static final int VERSION = 2;

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	public DatabaseHelper(Context context, String name, int version){
		this(context,name,null,version);
	}

	public DatabaseHelper(Context context, String name){
		this(context,name,VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		System.out.println("create a database");
		Log.d(TAG, "db created");
		//db.execSQL("create table user(id int,name varchar(20))");
		//zhaoshh0320, create DB
		db.execSQL(" CREATE TABLE IF NOT EXISTS "
					+ "YPushServiceDB"
					+ " (haveInitDB VARCHAR, haveLocation VARCHAR, requestLocaton VARCHAR,"
					+ " haveResponse VARCHAR, Longitude VARCHAR, Latitude VARCHAR,"
					+ " haveResolution VARCHAR, resoWidth VARCHAR, resoHeight VARCHAR,"
					+ " imei VARCHAR, ipAddress VARCHAR, haveIpAddress VARCHAR,"
					+ " notificationId VARCHAR, curIntervalTime VARCHAR, bootConnected VARCHAR,"
					+ " wifiVisitFlag VARCHAR, pollingInterval VARCHAR, dealMsgOption VARCHAR,"
					+ " extend1 VARCHAR, extend2 VARCHAR, extend3 VARCHAR,"
					+ " extend4 VARCHAR, extend5 VARCHAR, extend6 VARCHAR)");
		// sherry, create DB
		Log.e("DatabaseHelper","create EventStatisticsDB");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + "EventStatisticsDB"
				+ "(_id INTEGER PRIMARY KEY,"
				+"deviceId VARCHAR,"
				+"romVersion VARCHAR,"
				+"productModel VARCHAR,"
				+ "ipAddress VARCHAR,"
				+"behaviorTime VARCHAR,"
				+"netType VARCHAR,"
				+ "behavior VARCHAR,"
				+"appId VARCHAR,"
				+"appSrc VARCHAR,"
				+ "packageName VARCHAR,"
				+"client VARCHAR,"
				+"listenArea VARCHAR,"
				+ "listenContextId VARCHAR,"
				+"listenContextSrc VARCHAR,"
				+"clientVersion VARCHAR,"
				+ "referenceId VARCHAR,"
				+"appName VACHAR,"
				+"appVersion VACHAR,"
				+ "mark VACHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		System.out.println("upgrade a database");
		Log.d(TAG, "onUpgrade:db created");
		db.execSQL("DROP TABLE IF EXISTS " + "YPushServiceDB");
		db.execSQL("DROP TABLE IF EXISTS " + "EventStatisticsDB");
		onCreate(db);
	}
}
