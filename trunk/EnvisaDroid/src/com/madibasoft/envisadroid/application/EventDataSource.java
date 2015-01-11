package com.madibasoft.envisadroid.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.madibasoft.envisadroid.api.tpi.event.ChimeEvent;
import com.madibasoft.envisadroid.api.tpi.event.ErrorEvent;
import com.madibasoft.envisadroid.api.tpi.event.GenericEvent;
import com.madibasoft.envisadroid.api.tpi.event.InfoEvent;
import com.madibasoft.envisadroid.api.tpi.event.LEDEvent;
import com.madibasoft.envisadroid.api.tpi.event.LoginEvent;
import com.madibasoft.envisadroid.api.tpi.event.PanelModeEvent;
import com.madibasoft.envisadroid.api.tpi.event.PartitionEvent;
import com.madibasoft.envisadroid.api.tpi.event.SmokeEvent;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class EventDataSource {

	// Database fields
	private SQLiteDatabase database;
	private EventSQLiteHelper dbHelper;
	private String[] allColumns = { EventSQLiteHelper.COLUMN_ID, EventSQLiteHelper.COLUMN_JSON, EventSQLiteHelper.COLUMN_DATE };
	private int MAX_ROWS=1000;
	private int dirty_setting = 0;

	public EventDataSource(Context context) {
		dbHelper = new EventSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		database.close();
		dbHelper.close();
	}

	public Cursor getCursor() {
		Cursor cursor = database.rawQuery("SELECT * FROM \""+EventSQLiteHelper.TABLE_EVENTS+"\" ORDER BY "+EventSQLiteHelper.COLUMN_DATE+" DESC", 
				new String[]{});
		return cursor;
	}

	public void addEvent(GenericEvent event) {
		try {
			ContentValues values = new ContentValues();
			values.put(EventSQLiteHelper.COLUMN_DATE, new Date().getTime());
			values.put(EventSQLiteHelper.COLUMN_JSON, event.toJSON().toString());
			long insertId = database.insert(EventSQLiteHelper.TABLE_EVENTS, null, values);
			Cursor cursor = database.query(EventSQLiteHelper.TABLE_EVENTS, allColumns, EventSQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
			cursor.moveToFirst();
			cursor.close();
			trim();
		}
		catch (JSONException je) {
			je.printStackTrace();
		}
	}

	public void deleteEvent(GenericEvent ge) {
		long id = ge.getId();
		database.delete(EventSQLiteHelper.TABLE_EVENTS, EventSQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public List<GenericEvent> getAllEvents() {
		List<GenericEvent> events = new ArrayList<GenericEvent>();

		Cursor cursor = database.query(EventSQLiteHelper.TABLE_EVENTS,
				allColumns, null, null, null, null, null);
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				try {
					GenericEvent event = cursorToEvent(cursor);
					events.add(event);
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
				cursor.moveToNext();
			}
			// Make sure to close the cursor
		}
		cursor.close();
		return events;
	}

	private GenericEvent cursorToEvent(Cursor cursor) throws JSONException {
		JSONObject jo = new JSONObject(cursor.getString(1));
		GenericEvent ge = null;
		if (jo.getString("eventClass").equals(PanelModeEvent.class.getName())) {
			ge = new PanelModeEvent(jo);
		}
		if (jo.getString("eventClass").equals(ChimeEvent.class.getName())) {
			ge = new ChimeEvent(jo);
		}
		if (jo.getString("eventClass").equals(ErrorEvent.class.getName())) {
			ge = new ErrorEvent(jo);
		}
		if (jo.getString("eventClass").equals(InfoEvent.class.getName())) {
			ge = new InfoEvent(jo);
		}
		if (jo.getString("eventClass").equals(LEDEvent.class.getName())) {
			ge = new LEDEvent(jo);
		}
		if (jo.getString("eventClass").equals(LoginEvent.class.getName())) {
			ge = new LoginEvent(jo);
		}
		if (jo.getString("eventClass").equals(PartitionEvent.class.getName())) {
			ge = new PartitionEvent(jo);
		}
		if (jo.getString("eventClass").equals(SmokeEvent.class.getName())) {
			ge = new SmokeEvent(jo);
		}
		if (jo.getString("eventClass").equals(ZoneEvent.class.getName())) {
			ge = new ZoneEvent(jo);
		}
		ge.setId(cursor.getInt(0));
		return ge;
	}

	public void trim() throws JSONException {
		// don't clean every time, do it only after 10% of max events are written
		if (dirty_setting>(MAX_ROWS/10)) {
			dirty_setting = 0;
			Cursor cursor = database.query(EventSQLiteHelper.TABLE_EVENTS,
					allColumns, null, null, null, null, EventSQLiteHelper.COLUMN_DATE+" ASC");
			int rowsToDelete = cursor.getCount()-MAX_ROWS;
			cursor.moveToFirst();
			while ((rowsToDelete>0)&&(!cursor.isAfterLast())) {
				GenericEvent event = cursorToEvent(cursor);
				deleteEvent(event);
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
		database.delete(EventSQLiteHelper.TABLE_EVENTS, null, null);
		dirty_setting = 0;
	}
} 
