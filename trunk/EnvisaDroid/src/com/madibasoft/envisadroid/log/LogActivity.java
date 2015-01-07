package com.madibasoft.envisadroid.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
//import android.support.v4.app.NavUtils;
//import android.support.v4.widget.SimpleCursorAdapter;
//import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.madibasoft.envisadroid.R;
import com.madibasoft.envisadroid.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class LogActivity extends ListActivity {

	private static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm",Locale.getDefault());
	private static final String DB_CHANGED = "DB_CHANGED";
	private LogDataSource datasource;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.changeCursor(datasource.getCursor());
		}
	};
	
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);

		datasource = new LogDataSource(this);
		datasource.open();
		cursor = datasource.getCursor();
		//		ArrayAdapter<Log> adapter = new ArrayAdapter<Log>(this, R.layout.log_listitem, values);		
		adapter = new SimpleCursorAdapter(this, R.layout.log_listitem, cursor, 
				new String[] { LogSQLiteHelper.COLUMN_DATE, LogSQLiteHelper.COLUMN_LOG }, 
				new int[] { R.id.logDate, R.id.logMessage});

		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

				if (aColumnIndex == 2) {
					String date = aCursor.getString(aColumnIndex);
					TextView textView = (TextView) aView;
					try {
						textView.setText(formatter.format(new Date(Long.parseLong(date))));
					}
					catch (Throwable t) {
						textView.setText("??:??");
					}
					return true;
				}

				return false;
			}
		});
		setListAdapter(adapter);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(DB_CHANGED);
		registerReceiver(this.receiver, filter);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_log, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cursor.close();
		datasource.close();
		unregisterReceiver(this.receiver);
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
//			NavUtils.navigateUpFromSameTask(this);
			navigateUpTo(this.getParentActivityIntent());
			return true;
		case R.id.menu_action_clear_log:
			datasource.clear();
			cursor.close();
			adapter.changeCursor(cursor = datasource.getCursor());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static void log(Context c, String string) {
		// Save the new comment to the database
		LogDataSource datasource = new LogDataSource(c);
		datasource.open();
		datasource.createLog(string);
		datasource.close();
		// notify cursor refresh
		Intent intent = new Intent(LogActivity.DB_CHANGED);
        c.sendBroadcast(intent);
	}

}
