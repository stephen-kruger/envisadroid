package com.madibasoft.envisadroid.zone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseIntArray;

import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;

public class ZoneDataSource {

	// Database fields
	private SQLiteDatabase database;
	private ZoneSQLiteHelper dbHelper;

	private SparseIntArray cacheMap = new SparseIntArray();

	public ZoneDataSource(Context context) {
		dbHelper = new ZoneSQLiteHelper(context);
	}

	public boolean isOpen() {
		return (database!=null);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		database.close();
		dbHelper.close();
		database = null;
		dbHelper = null;
	}

	public void clear() {
		database.delete(ZoneSQLiteHelper.TABLES_ZONES, null, null);
	}

	public Cursor getGroupChildrenCursor(ZoneEvent.State state) {
		Cursor cursor = database.query(ZoneSQLiteHelper.TABLES_ZONES, ZoneSQLiteHelper.allColumns, 
				ZoneSQLiteHelper.COLUMN_STATE+"=?", 
				new String[]{Integer.toString(state.ordinal())}, 
				null, 
				null, 
				ZoneSQLiteHelper.COLUMN_ZONE);
		return cursor;
	}

	public int getChildCount(ZoneEvent.State state) {
		Cursor mCount= database.rawQuery("select count(*) from \""+ZoneSQLiteHelper.TABLES_ZONES+"\" where "+ZoneSQLiteHelper.COLUMN_STATE+"=?", 
				new String[]{Integer.toString(state.ordinal())});
		int count=0;
		if (mCount.moveToFirst())
			count= mCount.getInt(0);
		mCount.close();
		return count;
	}

	public Cursor getGroupCursor() {
		Cursor cursor = database.query (true, ZoneSQLiteHelper.TABLES_ZONES, 
				ZoneSQLiteHelper.allColumns,// columns
				null,// selection, 
				null,//String[] selectionArgs, 
				ZoneSQLiteHelper.COLUMN_STATE,// groupBy, 
				null,// having, 
				ZoneSQLiteHelper.COLUMN_STATE,// orderBy, 
				null);// limit
		return cursor;
	}

	public int getGroupCount() {
		int count=0;
		try {

			Cursor mCount= database.rawQuery("select count(DISTINCT "+ZoneSQLiteHelper.COLUMN_STATE+") FROM \""+ZoneSQLiteHelper.TABLES_ZONES+"\"", 
					null);

			if (mCount.moveToFirst())
				count=mCount.getInt(0);
			mCount.close();
		}
		catch (Throwable t) {
			t.printStackTrace();
			Log.e("xxx", t.getMessage());
		}
		return count;
	}

	public ZoneEvent createZone(ZoneEvent zone) {
		deleteZone(zone);
		ContentValues values = new ContentValues();
		values.put(ZoneSQLiteHelper.COLUMN_ZONE, zone.getZone());
		values.put(ZoneSQLiteHelper.COLUMN_PARTITION, zone.getPartition());
		values.put(ZoneSQLiteHelper.COLUMN_STATE, zone.getState().ordinal());
		values.put(ZoneSQLiteHelper.COLUMN_EVENT_DATE, zone.getEventDate().getTime());
		values.put(ZoneSQLiteHelper.COLUMN_ZONE_DATE, zone.getZoneDate().getTime());
		database.insert(ZoneSQLiteHelper.TABLES_ZONES, ZoneSQLiteHelper.COLUMN_STATE, values);
		//		String whereClause = ZoneSQLiteHelper.COLUMN_ZONE+"=? AND "+ZoneSQLiteHelper.COLUMN_PARTITION+"=?";
		//		database.update(ZoneSQLiteHelper.TABLES_ZONES, values, whereClause, new String[]{Integer.toString(zone.getZone()),""+zone.getPartition()});
		cacheMap.clear();
		return zone;
	}

	public void updateZone(ZoneEvent zone) {
		ContentValues values = new ContentValues();
		values.put(ZoneSQLiteHelper.COLUMN_ZONE, zone.getZone());
		values.put(ZoneSQLiteHelper.COLUMN_PARTITION, zone.getPartition());
		values.put(ZoneSQLiteHelper.COLUMN_STATE, zone.getState().ordinal());
		values.put(ZoneSQLiteHelper.COLUMN_EVENT_DATE, zone.getEventDate().getTime());
		values.put(ZoneSQLiteHelper.COLUMN_ZONE_DATE, zone.getZoneDate().getTime());
		String whereClause = ZoneSQLiteHelper.COLUMN_ZONE+"=? AND "+ZoneSQLiteHelper.COLUMN_PARTITION+"=?";
		int rowsChanged = database.update(ZoneSQLiteHelper.TABLES_ZONES, values, whereClause, 
				new String[]{Integer.toString(zone.getZone()),Integer.toString(zone.getPartition())});
		cacheMap.clear();
		if (rowsChanged!=1) {
			Log.e("xxx","Bad thing happened");
		}
	}

	public void deleteZone(ZoneEvent zone) {
		database.delete(ZoneSQLiteHelper.TABLES_ZONES, 
				ZoneSQLiteHelper.COLUMN_ZONE+ "=? AND " + ZoneSQLiteHelper.COLUMN_PARTITION+"=?", 
				new String[]{""+zone.getZone(),""+zone.getPartition()});
		cacheMap.clear();
	}

	public List<ZoneEvent> getAllZones(ZoneEvent.State state) {
		List<ZoneEvent> zones = new ArrayList<ZoneEvent>();
		Cursor cursor = getGroupChildrenCursor(state);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			zones.add(cursorToZone(cursor));
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return zones;
	}

	public ZoneEvent cursorToZone(Cursor cursor) {
		try {
			ZoneEvent zoneEvent = new ZoneEvent(
					cursor.getInt(cursor.getColumnIndex(ZoneSQLiteHelper.COLUMN_PARTITION)),
					cursor.getInt(cursor.getColumnIndex(ZoneSQLiteHelper.COLUMN_ZONE)),
					ZoneEvent.State.values()[cursor.getInt(cursor.getColumnIndex(ZoneSQLiteHelper.COLUMN_STATE))]);
			zoneEvent.setEventDate(new Date(cursor.getLong(cursor.getColumnIndex(ZoneSQLiteHelper.COLUMN_EVENT_DATE))));
			zoneEvent.setZoneDate(new Date(cursor.getLong(cursor.getColumnIndex(ZoneSQLiteHelper.COLUMN_ZONE_DATE))));
			return zoneEvent;
		}
		catch (CursorIndexOutOfBoundsException cioob) {
			Log.e("ZoneDataSource", cioob.toString());
			return null;
		}
	}

	/*
	 * Utility method for the list adapter
	 */
	public ZoneEvent.State getGroupState(int groupPosition) {

		int groupState = cacheMap.get(groupPosition,ZoneEvent.State.Open.ordinal());
		ZoneEvent group = getGroup(groupPosition);
		if (group!=null) {
			groupState = group.getState().ordinal();
			cacheMap.put(groupPosition, groupState);
		}
		return ZoneEvent.State.values()[groupState];
	}

	/*
	 * Utility method for the list adapter
	 */
	public ZoneEvent getGroup(int groupPosition) {
		Cursor cursor = getGroupCursor();
		cursor.move(groupPosition+1);
		ZoneEvent zoneEvent = cursorToZone(cursor);
		cursor.close();
		return zoneEvent;
	}

	/*
	 * Used by the list view to retrieve a particular child of a list group
	 */
	public ZoneEvent getZoneAt(ZoneEvent.State state, int childPosition) {
		ZoneEvent zone = null;
		Cursor cursor = getGroupChildrenCursor(state);
		cursor.moveToFirst();
		if (cursor.move(childPosition)) {
			zone = cursorToZone(cursor);
		}
		cursor.close();
		return zone;
	}
} 
