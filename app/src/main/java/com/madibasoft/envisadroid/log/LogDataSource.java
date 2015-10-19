package com.madibasoft.envisadroid.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LogDataSource {

	// Database fields
	private SQLiteDatabase database;
	private LogSQLiteHelper dbHelper;
	private String[] allColumns = { LogSQLiteHelper.COLUMN_ID, LogSQLiteHelper.COLUMN_LOG, LogSQLiteHelper.COLUMN_DATE };
	private int MAX_ROWS=1000;
	private int dirty_setting = 0;

	public LogDataSource(Context context) {
		dbHelper = new LogSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		database.close();
		dbHelper.close();
	}

	public Cursor getCursor() {
		Cursor cursor = database.rawQuery("SELECT * FROM \""+LogSQLiteHelper.TABLE_LOGS+"\" ORDER BY "+LogSQLiteHelper.COLUMN_DATE+" DESC", 
				new String[]{});
		return cursor;
	}

	public Log createLog(String log) {
		ContentValues values = new ContentValues();
		values.put(LogSQLiteHelper.COLUMN_DATE, new Date().getTime());
		values.put(LogSQLiteHelper.COLUMN_LOG, log);
		long insertId = database.insert(LogSQLiteHelper.TABLE_LOGS, null, values);
		Cursor cursor = database.query(LogSQLiteHelper.TABLE_LOGS, allColumns, LogSQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		Log newLog = cursorToLog(cursor);
		cursor.close();
		trim();
		return newLog;
	}

	public void deleteLog(Log Log) {
		long id = Log.getId();
		database.delete(LogSQLiteHelper.TABLE_LOGS, LogSQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public List<Log> getAllLogs() {
		List<Log> Logs = new ArrayList<Log>();

		Cursor cursor = database.query(LogSQLiteHelper.TABLE_LOGS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log Log = cursorToLog(cursor);
			Logs.add(Log);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return Logs;
	}

	private Log cursorToLog(Cursor cursor) {
		Log log = new Log();
		log.setId(cursor.getLong(0));
		log.setLog(cursor.getString(1));
		log.setDate(cursor.getLong(2));
		return log;
	}

	public void trim() {
		// don't clean every time, do it only after 10% of max events are written
		if (dirty_setting>(MAX_ROWS/10)) {
			dirty_setting = 0;
			Cursor cursor = database.query(LogSQLiteHelper.TABLE_LOGS,
					allColumns, null, null, null, null, LogSQLiteHelper.COLUMN_DATE+" ASC");
			int rowsToDelete = cursor.getCount()-MAX_ROWS;
			cursor.moveToFirst();
			while ((rowsToDelete>0)&&(!cursor.isAfterLast())) {
				Log Log = cursorToLog(cursor);
				deleteLog(Log);
				cursor.moveToNext();
				rowsToDelete--;
			}
			// Make sure to close the cursor
			cursor.close();

		}
		else {
			dirty_setting++;
		}
	}

	public void clear() {
		database.delete(LogSQLiteHelper.TABLE_LOGS, null, null);
		dirty_setting = 0;
	}
} 
