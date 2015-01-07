package com.madibasoft.envisadroid.sync;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.madibasoft.envisadroid.R;
import com.madibasoft.envisadroid.SettingsActivity;
import com.madibasoft.envisadroid.application.EnvisadroidApplication;
import com.madibasoft.envisadroid.log.LogActivity;
import com.madibasoft.envisadroid.util.Util;

@SuppressLint("DefaultLocale")
public class SyncActivity extends Activity {

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sync);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		((ProgressBar)findViewById(R.id.syncProgress)).setVisibility(View.INVISIBLE);

		//		final EditText ipAddress = new EditText(this);

		//		ipAddress.setText(Util.getMACAddress());
		findViewById(R.id.sendButton).setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
						new AlertDialog.Builder(SyncActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.sync_title)
						.setMessage(R.string.really_send)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

							
							public void onClick(DialogInterface dialog, int which) {
								SendTask task = new SendTask();
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
									task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
								else
									task.execute("");
								dialog.dismiss();
							}

						})
						.setNegativeButton(R.string.no, null)
						.show();
					}
				});

		findViewById(R.id.receiveButton).setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
						new AlertDialog.Builder(SyncActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.sync_title)
						.setMessage(R.string.really_receive)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

							
							public void onClick(DialogInterface dialog, int which) {
								Log.d(EnvisadroidApplication.LOG,"Starting receieve task1") ;
								dialog.dismiss();
								Log.d(EnvisadroidApplication.LOG,"Starting receieve task2") ;
								ReceiveTask task = new ReceiveTask();
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
									task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
								else
									task.execute("");
								Log.d(EnvisadroidApplication.LOG,"Starting receieve task3") ;
							}

						})
						.setNegativeButton(R.string.no, null)
						.show();

					}
				});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sync, menu);
		return true;
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
//			NavUtils.navigateUpFromSameTask(this);
			navigateUpTo(this.getParentActivityIntent());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class SendTask extends AsyncTask<String, String, String> {


		protected String doInBackground(String... params) {
			String message = "OK";
			try {
				publishProgress("Preparing settings","10");
				SyncHelper syncHelper = new SyncHelper(SyncActivity.this,Util.getPreference(SyncActivity.this,SettingsActivity.HOSTNAME,"192.168.1.25"));
				publishProgress("Collating settings","40");
				JSONObject zoneData = syncHelper.getSettingsData(SyncActivity.this);
				
				publishProgress("Storing settings","85");				
				syncHelper.sendSettings(SyncActivity.this,zoneData);
				publishProgress("Stored settings","100");	
			}
			catch (Throwable se) {
				LogActivity.log(SyncActivity.this, "Unexpected error sending zone settings ("+se+")");
				message = "Error:"+se.getMessage();
				se.printStackTrace();
			}
			return message;
		}

		protected void onProgressUpdate(String... progress) {
			Log.d(EnvisadroidApplication.LOG,progress[0]+" "+progress[1]) ;
			((ProgressBar)findViewById(R.id.syncProgress)).setVisibility(View.VISIBLE);
			((ProgressBar)findViewById(R.id.syncProgress)).setProgress(Integer.parseInt(progress[1]));
			((TextView)findViewById(R.id.syncStatus)).setText(progress[0]);
			((Button)findViewById(R.id.receiveButton)).setEnabled(false);
			((Button)findViewById(R.id.sendButton)).setEnabled(false);
		}

		protected void onPostExecute(String result) {
			((TextView)findViewById(R.id.syncStatus)).setText(result);
			((ProgressBar)findViewById(R.id.syncProgress)).setVisibility(View.INVISIBLE);
			((ProgressBar)findViewById(R.id.syncProgress)).setProgress(100);
			((Button)findViewById(R.id.receiveButton)).setEnabled(true);
			((Button)findViewById(R.id.sendButton)).setEnabled(true);
		}
	}

	public class ReceiveTask extends AsyncTask<String, String, String> {

		protected String doInBackground(String... params) {
			Log.d(EnvisadroidApplication.LOG,"Into receive task") ;
			try {
				SyncHelper syncHelper = new SyncHelper(SyncActivity.this,Util.getPreference(SyncActivity.this,SettingsActivity.HOSTNAME,"192.168.1.25"));

				// now read contents
				publishProgress("Searching for settings","10");
				JSONObject zoneData = syncHelper.receiveSettings(SyncActivity.this);
				publishProgress("Received settings bundle","20");
				String result = syncHelper.setSettingsData(SyncActivity.this,zoneData);
				publishProgress("Loaded zones","100");
				return result;
			}
			catch (Throwable se) {
				LogActivity.log(SyncActivity.this, "Problem recieving ("+se+")");
				se.printStackTrace();
				return "Error:"+se.getMessage();
			} 
			//			Log.d(EnvisadroidApplication.LOG,"Done receieve task") ;
			//			return "Successfully received zone names";
		}

		protected void onProgressUpdate(String... progress) {
			Log.d(EnvisadroidApplication.LOG,progress[0]+" "+progress[1]) ;
			((ProgressBar)findViewById(R.id.syncProgress)).setVisibility(View.VISIBLE);
			((ProgressBar)findViewById(R.id.syncProgress)).setProgress(Integer.parseInt(progress[1]));
			((TextView)findViewById(R.id.syncStatus)).setText(progress[0]);
			((Button)findViewById(R.id.receiveButton)).setEnabled(false);
			((Button)findViewById(R.id.sendButton)).setEnabled(false);
		}

		protected void onPostExecute(String result) {
			((TextView)findViewById(R.id.syncStatus)).setText(result);
			((ProgressBar)findViewById(R.id.syncProgress)).setVisibility(View.INVISIBLE);
			((Button)findViewById(R.id.receiveButton)).setEnabled(true);
			((Button)findViewById(R.id.sendButton)).setEnabled(true);
		}
	}


}
