package com.ragentek.ypush.service.db;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SQLiteActivity extends Activity {
	/** Called when the activity is first created. */
	private Button createDatabaseButton = null;
	private Button updateDatabaseButton = null;
	private Button insertButton = null;
	private Button updateButton = null;
	private Button selectButton = null;
	private Button deleteButton = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
	
		
		createDatabaseButton = (Button) findViewById(0);
		updateDatabaseButton = (Button) findViewById(0);
		insertButton = (Button) findViewById(0);
		updateButton = (Button) findViewById(0);
		selectButton = (Button) findViewById(0);
		deleteButton = (Button) findViewById(0);
		createDatabaseButton
				.setOnClickListener(new CreateDatabaseOnClickListener());
		updateDatabaseButton
				.setOnClickListener(new UpdateDatabaseOnClickListener());
		insertButton.setOnClickListener(new InsertOnClickListener());
		updateButton.setOnClickListener(new UpdateOnClickListener());
		selectButton.setOnClickListener(new SelectOnClickListener());
		deleteButton.setOnClickListener(new DeleteOnClickListener());
	}

	class CreateDatabaseOnClickListener implements OnClickListener {
		public void onClick(View v) {
			DatabaseHelper dbHelper = new DatabaseHelper(SQLiteActivity.this,
					"test_yangyz_db");
			SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
		}
	}

	class UpdateDatabaseOnClickListener implements OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			DatabaseHelper dbHelper = new DatabaseHelper(SQLiteActivity.this,
					"test_yangyz_db", 2);
			SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
		}

	}

	class InsertOnClickListener implements OnClickListener {
		public void onClick(View v) {
			ContentValues values = new ContentValues();
			values.put("id", 1);
			values.put("name", "yangyz");
			DatabaseHelper dbHelper = new DatabaseHelper(SQLiteActivity.this,
					"test_yangyz_db", 2);
			SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
			sqliteDatabase.insert("user", null, values);
		}
	}

	class UpdateOnClickListener implements OnClickListener {
		public void onClick(View v) {
			DatabaseHelper dbHelper = new DatabaseHelper(SQLiteActivity.this,
					"test_yangyz_db", 2);
			SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("name", "zhangsan");
			sqliteDatabase.update("user", values, "id=?", new String[] { "1" });
			System.out.println("-----------update------------");
		}
	}

	class SelectOnClickListener implements OnClickListener {
		public void onClick(View v) {
			String id = null;
			String name = null;
			DatabaseHelper dbHelper = new DatabaseHelper(SQLiteActivity.this,
					"test_yangyz_db", 2);
			SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
			Cursor cursor = sqliteDatabase.query("user", new String[] { "id",
					"name" }, "id=?", new String[] { "1" }, null, null, null);
			while (cursor.moveToNext()) {
				id = cursor.getString(cursor.getColumnIndex("id"));
				name = cursor.getString(cursor.getColumnIndex("name"));
			}
			System.out.println("-------------select------------");
			System.out.println("id: "+id);
			System.out.println("name: "+name);
		}
	}

	class DeleteOnClickListener implements OnClickListener {
		public void onClick(View v) {
			DatabaseHelper dbHelper = new DatabaseHelper(SQLiteActivity.this,"test_yangyz_db",2);
			SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
			sqliteDatabase.delete("user", "id=?", new String[]{"1"});
			System.out.println("----------delete----------");
		}
	}
}