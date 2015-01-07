package com.madibasoft.envisadroid.log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LogSQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_LOGS = "logs";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_LOG = "log";

	private static final String DATABASE_NAME = "logs.db";
	private static final int DATABASE_VERSION = 3;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_LOGS + "(" +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_LOG + " text not null, "+
			COLUMN_DATE + " int);";

	public LogSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		// now create the indexes
		database.execSQL("CREATE INDEX "+COLUMN_DATE+"_idx ON "+TABLE_LOGS+"("+COLUMN_DATE+")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LogSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
		onCreate(db);
	}

} 
