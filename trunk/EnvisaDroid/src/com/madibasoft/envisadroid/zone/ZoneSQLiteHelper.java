package com.madibasoft.envisadroid.zone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ZoneSQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLES_ZONES = "zones";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ZONE = "zone";
	public static final String COLUMN_PARTITION = "partition";
	public static final String COLUMN_STATE = "state";
	public static final String COLUMN_EVENT_DATE = "event_date";// date this event was received from Envisa
	public static final String COLUMN_ZONE_DATE = "zone_date"; // timer data as reported by Envisa
	public static final String[] allColumns = { 
			COLUMN_ID, 
			COLUMN_ZONE, 
			COLUMN_PARTITION, 
			COLUMN_STATE, 
			COLUMN_EVENT_DATE, 
			COLUMN_ZONE_DATE };
	
	private static final String DATABASE_NAME = "zones.db";
	private static final int DATABASE_VERSION = 6;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLES_ZONES + "(" +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_ZONE + " integer not null, " +
			COLUMN_PARTITION + " integer not null, "+
			COLUMN_STATE + " integer not null, "+
			COLUMN_EVENT_DATE + " int not null, "+
			COLUMN_ZONE_DATE + " int);";

	public ZoneSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		// now create the indexes
		database.execSQL("CREATE INDEX "+COLUMN_ZONE+"_idx ON "+TABLES_ZONES+"("+COLUMN_ZONE+")");
		database.execSQL("CREATE INDEX "+COLUMN_PARTITION+"_idx ON "+TABLES_ZONES+"("+COLUMN_PARTITION+")");
		database.execSQL("CREATE INDEX "+COLUMN_STATE+"_idx ON "+TABLES_ZONES+"("+COLUMN_STATE+")");
		database.execSQL("CREATE INDEX "+COLUMN_EVENT_DATE+"_idx ON "+TABLES_ZONES+"("+COLUMN_EVENT_DATE+")");
		database.execSQL("CREATE INDEX "+COLUMN_ZONE_DATE+"_idx ON "+TABLES_ZONES+"("+COLUMN_ZONE_DATE+")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ZoneSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLES_ZONES);
		onCreate(db);
	}

} 
