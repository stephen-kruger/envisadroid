package com.madibasoft.envisadroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.tpi.event.ChimeEvent;
import com.madibasoft.envisadroid.api.tpi.event.ErrorEvent;
import com.madibasoft.envisadroid.api.tpi.event.InfoEvent;
import com.madibasoft.envisadroid.api.tpi.event.LEDEvent;
import com.madibasoft.envisadroid.api.tpi.event.LEDEvent.State;
import com.madibasoft.envisadroid.api.tpi.event.LoginEvent;
import com.madibasoft.envisadroid.api.tpi.event.PanelEvent;
import com.madibasoft.envisadroid.api.tpi.event.PartitionEvent;
import com.madibasoft.envisadroid.api.tpi.event.SmokeEvent;
import com.madibasoft.envisadroid.api.tpi.event.TPIListener;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;
import com.madibasoft.envisadroid.application.EnvisadroidApplication;
import com.madibasoft.envisadroid.keypad.KeypadActivity;
import com.madibasoft.envisadroid.log.LogActivity;
import com.madibasoft.envisadroid.log.LogTimer;
import com.madibasoft.envisadroid.sync.SyncHelper;
import com.madibasoft.envisadroid.util.Util;
import com.madibasoft.envisadroid.zone.ZoneDataSource;
import com.madibasoft.envisadroid.zone.ZoneExpandableListAdapter;

public class EnvisadroidActivity extends Activity implements TPIListener, OnChildClickListener {

	protected static final int LIST_CHANGED = 0;
	protected static final int LOG_CHANGED = 1;
	private Menu menu;
	private ZoneDataSource zoneDataSource;
	private ZoneExpandableListAdapter zoneAdapter;
	private FlashTask flashTask;
	private LogTimer logTimer;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(EnvisadroidApplication.LOG,"create");

		try {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		catch (NoClassDefFoundError e) {
			log("Using compat thread mode");
		}
		setContentView(R.layout.activity_envisadroid);


		// set up zone list
		ExpandableListView epView = (ExpandableListView) findViewById(R.id.zoneListView);
		zoneDataSource = new ZoneDataSource(this);
		zoneDataSource.open();
		zoneAdapter = new ZoneExpandableListAdapter(this, zoneDataSource);
		epView.setAdapter(zoneAdapter);
		epView.setOnChildClickListener(this);

		// this task runs the led flashing functions
		flashTask = new FlashTask();

		((EnvisadroidApplication) getApplication()).addTDIListener(this);

		// make log text clickable
		TextView logText  = ((TextView)findViewById(R.id.textLogView));
		if (logText!=null) {	
			logText.setOnClickListener(new View.OnClickListener() {


				public void onClick(View v) {
					EnvisadroidActivity.this.startActivity(new Intent(EnvisadroidActivity.this, LogActivity.class));
				}
			});
		}
		populateZones();
	}


	protected void onDestroy() {
		super.onDestroy();
		Log.d(EnvisadroidApplication.LOG,"destroy");
		flashTask.cancel(true);
		((EnvisadroidApplication) getApplication()).removeTDIListener(EnvisadroidActivity.this);
		zoneDataSource.close();
	}


	protected void onPause() {
		super.onPause();
		flashTask.cancel(true);
		Log.d(EnvisadroidApplication.LOG,"pause");
	}


	protected void onResume() {
		super.onResume();
		Log.d(EnvisadroidApplication.LOG,"resume");
		try {
			if (!flashTask.isRunning())
				flashTask.execute("");
		}
		catch (Throwable t) {
			Log.e(EnvisadroidApplication.LOG, "Problem starting flash task");

		}
		// in case we are resuming from a zone edit - refresh the list to
		// in case zone name was changed
		zoneAdapter.notifyDataSetChanged();
	}

	public boolean onCreateOptionsMenu(Menu menu) {		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		// check if we are logged in, set the Connect option accordingly
		if (((EnvisadroidApplication) getApplication()).isConnected()) {
			((MenuItem)menu.findItem(R.id.menu_action_connect)).setEnabled(true);
			((MenuItem)menu.findItem(R.id.menu_action_connect)).setTitle(R.string.disconnect);
		}
		this.menu = menu;
		MenuItem switchMenu = menu.findItem(R.id.menu_action_connect);
		Switch switchItem = (Switch) switchMenu.getActionView().findViewById(R.id.switchAB);
		// orientation change causes new activity - might already be logged in
		if (((EnvisadroidApplication)getApplication()).isConnected()) {
			switchItem.setChecked(true);
		}
		switchItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// connect
					try {
						setLed(R.id.ledConnection,LEDEvent.State.FLASH);
						((EnvisadroidApplication)getApplication()).connect();
					} 
					catch (Throwable e1) {
						log( "Error connecting :"+e1.getMessage());
						e1.printStackTrace();
					}	
				}
				else {
					// disconnect
					try {
						((EnvisadroidApplication)getApplication()).disconnect();
						ledsOff();
					} 
					catch (Throwable e1) {
						log( "Error disconnecting :"+e1.getMessage());
						e1.printStackTrace();
					}	
				}
			}
		});

		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_action_connect :
			if (((MenuItem)menu.findItem(R.id.menu_action_connect)).getTitle()==getString(R.string.connect)) {
				try {
					if (menu!=null) {
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setEnabled(true);
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setTitle(R.string.disconnect);
					}

					setLed(R.id.ledConnection,LEDEvent.State.FLASH);
					((EnvisadroidApplication)getApplication()).connect();
				} 
				catch (Throwable e1) {
					log( "Error connecting :"+e1.getMessage());
					e1.printStackTrace();
				}				
			}
			else {
				try {
					((EnvisadroidApplication)getApplication()).disconnect();
					ledsOff();
					if (menu!=null) {
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setEnabled(true);
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setTitle(R.string.connect);
					}
				} 
				catch (Throwable e1) {
					log( "Error disconnecting :"+e1.getMessage());
					e1.printStackTrace();
				}	
			}

			break;
			//		case R.id.menu_action_arm :
			//			try {
			//				((EnvisadroidApplication)getApplication()).getSession().armWithCode(1);
			//			} 
			//			catch (EnvisaException e) {
			//				ToolsActivity.dialog(this,R.string.error,e.getMessage());
			//				e.printStackTrace();
			//			}
			//			break;
			//		case R.id.menu_action_log :
			//			EnvisadroidActivity.this.startActivity(new Intent(EnvisadroidActivity.this, LogActivity.class));
			//			break;
			//		case R.id.menu_action_tools :
			//			EnvisadroidActivity.this.startActivity(new Intent(EnvisadroidActivity.this, ToolsActivity.class));
			//			break;
		case R.id.menu_action_settings:
			EnvisadroidActivity.this.startActivity(new Intent(EnvisadroidActivity.this.getBaseContext(), SettingsActivity.class));
			break;
		case R.id.menu_action_refresh:
			try {
				((EnvisadroidApplication)getApplication()).getSession().dumpZoneTimers();
			} 
			catch (EnvisaException e) {
				ToolsActivity.dialog(this,R.string.error,e.getMessage());
				e.printStackTrace();
			}
			break;			
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	public void zoneClicked(View view,final ZoneEvent ze) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton("Bypass", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					((EnvisadroidApplication)getApplication()).getSession().bypass(ze.getPartition(), ze.getZone());
				} 
				catch (Throwable e) {
					e.printStackTrace();
					Log.e(EnvisadroidApplication.LOG, "Bypass failed", e);
				}
			}
		});
		builder.setNegativeButton("Done", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});
		// Set other dialog properties
		builder.setTitle("Zone Detail ("+ze.getPartition()+":"+ze.getZone()+")");

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@SuppressLint("HandlerLeak")
	protected Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LIST_CHANGED : 
				zoneAdapter.notifyDataSetChanged();
				break;
			case LOG_CHANGED : 
				TextView logText  = ((TextView)findViewById(R.id.textLogView));
				if (logText!=null) {
					resetLogTimer(logText);
					logText.setText(msg.obj.toString());
				}
				break;
			}
		}
	};

	public void keypad(View view) {
		try {
			startActivity(new Intent(this, KeypadActivity.class));
		}
		catch (Throwable e) {
			ToolsActivity.dialog(EnvisadroidActivity.this,R.string.error,e.getMessage());
		}
	}

	public void tools(View view) {
		try {
			EnvisadroidActivity.this.startActivity(new Intent(EnvisadroidActivity.this, ToolsActivity.class));
		}
		catch (Throwable e) {
			ToolsActivity.dialog(EnvisadroidActivity.this,R.string.error,e.getMessage());
		}
	}

	public void armdisarm(View view) {
		try {
			if (((Button)findViewById(R.id.armButton)).getText().equals(EnvisadroidActivity.this.getString(R.string.arm))) {
				((EnvisadroidApplication)getApplication()).getSession().armWithCode(1);
			}
			else {
				((EnvisadroidApplication)getApplication()).getSession().disarm(1);
			}
		}
		catch (Throwable e) {
			ToolsActivity.dialog(EnvisadroidActivity.this,R.string.error,e.getMessage());
		}
	}

	public void zoneEvent(ZoneEvent zoneEvent) {
		zoneDataSource.updateZone(zoneEvent);
		log("Zone event :"+zoneEvent.toString());
		if (zoneDataSource.isOpen()) {
			Message msg = new Message();
			msg.what=LIST_CHANGED;
			handler.sendMessage(msg);
		}		
	}

	private void resetLogTimer(TextView logText) {
		if (logTimer==null) {
			logTimer = new LogTimer(logText,10000,5000);
		}
		logTimer.cancel();
		logTimer.start();
	}

	private void log(final String message) {
		//		if (logText!=null) {		
		Message msg = new Message();
		msg.what=LOG_CHANGED;
		msg.obj = message;
		handler.sendMessage(msg );
		//			runOnUiThread(new Runnable() {
		//				public void run() {
		//					resetLogTimer(logText);
		//
		//					logText.setText(message);
		//				}
		//			});
		//		}
	}


	public void ledEvent(final LEDEvent e) {
		log("LED Event :"+e.toString());
		switch (e.getFunction()) {
		case BACKLIGHT :
			setLed(R.id.ledBacklight,e.getState());
			break;
		case FIRE :
			setLed(R.id.ledFire,e.getState());
			break;
		case PROGRAM :
			setLed(R.id.ledProgram,e.getState());
			break;
		case TROUBLE :
			setLed(R.id.ledTrouble,e.getState());
			break;
		case BYPASS :
			setLed(R.id.ledBypass,e.getState());
			break;
		case MEMORY :
			setLed(R.id.ledMem,e.getState());
			break;
		case ARMED :
			setLed(R.id.ledArmed,e.getState());
			break;
		case READY :
			setLed(R.id.ledReady,e.getState());					
			break;
		default : log("Invalid LED Event :"+e.toString());
		}
	}


	public void partitionEvent(final PartitionEvent e) {

		runOnUiThread(new Runnable() {
			public void run() {
				if (menu==null)
					return;
				if (e.getPartition()==1) {
					switch (e.getState()) {
					case READY :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setTitle(R.string.arm);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setIcon(R.drawable.device_access_secure);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setEnabled(true);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.arm);
						((Button)findViewById(R.id.armButton)).setEnabled(true);
						((Button)findViewById(R.id.keypadButton)).setEnabled(true);
						log("Ready");
						break;
					case NOT_READY :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setEnabled(false);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.arm);
						((Button)findViewById(R.id.armButton)).setEnabled(false);
						((Button)findViewById(R.id.keypadButton)).setEnabled(false);
						log("Not ready");
						break;
					case ARMED :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setTitle(R.string.disarm);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setIcon(R.drawable.device_access_not_secure);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setEnabled(true);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.disarm);
						((Button)findViewById(R.id.armButton)).setEnabled(true);
						log("Armed");
						break;
					case DISARMED :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setTitle(R.string.arm);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setIcon(R.drawable.device_access_secure);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.arm);
						((Button)findViewById(R.id.armButton)).setEnabled(true);
						log("Disarmed");
						break;
					case ALARM :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setTitle(R.string.disarm);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setIcon(R.drawable.device_access_not_secure);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setEnabled(true);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.disarm);
						((Button)findViewById(R.id.armButton)).setEnabled(true);
						log("Alarm");
						break;
					case EXIT_DELAY :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setTitle(R.string.disarm);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setIcon(R.drawable.device_access_not_secure);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setEnabled(true);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.disarm);
						((Button)findViewById(R.id.armButton)).setEnabled(true);
						log("Exit delay");
						break;
					case ENTRY_DELAY :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setTitle(R.string.disarm);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setIcon(R.drawable.device_access_secure);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setEnabled(true);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.disarm);
						((Button)findViewById(R.id.armButton)).setEnabled(true);
						log("Entry delay");
						break;
					case BUSY :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setEnabled(false);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText("");
						((Button)findViewById(R.id.armButton)).setEnabled(false);
						log("Busy");
						break;
					case FAILED_TO_ARM :
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setTitle(R.string.arm);
						//						((MenuItem)menu.findItem(R.id.menu_action_arm)).setIcon(R.drawable.device_access_secure);
						// toolbar
						((Button)findViewById(R.id.armButton)).setText(R.string.arm);
						((Button)findViewById(R.id.armButton)).setEnabled(true);
						log("Failed to arm");
						break;
					}
				}
			}
		});
	}


	public void loginEvent(LoginEvent loginEvent) {
		switch (loginEvent.getState()) {
		case TIMEDOUT :
			ledsOff();
			log("Login timeout");
		case FAILED :
			ledsOff();
			log("Login failed");
		case CONNECTION_FAIL :
			ledsOff();
			log("Connection fail");
		case LOGGEDOUT :
			ledsOff();
			setLed(R.id.ledConnection,LEDEvent.State.OFF);
			log("Logged out");
			runOnUiThread(new Runnable() {
				public void run() {
					if (menu!=null) {
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setEnabled(true);
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setTitle(R.string.connect);
					}
					((Button)findViewById(R.id.keypadButton)).setEnabled(false);
				}
			});
			break;
		case LOGGEDIN :
			setLed(R.id.ledConnection,LEDEvent.State.ON);
			log("Logged in");
			runOnUiThread(new Runnable() {
				public void run() {
					if (menu!=null) {
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setEnabled(true);
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setTitle(R.string.disconnect);
					}
					((Button)findViewById(R.id.keypadButton)).setEnabled(true);
				}
			});
			break;
		case REQUESTING :
			setLed(R.id.ledConnection,LEDEvent.State.FLASH);
			log("Login requested");
			runOnUiThread(new Runnable() {
				public void run() {
					if (menu!=null)
						((MenuItem)menu.findItem(R.id.menu_action_connect)).setEnabled(false);
				}
			});
			break;
		}
	}


	public void infoEvent(InfoEvent infoEvent) {
		log("Info :"+infoEvent.toString());
	}


	public void errorEvent(ErrorEvent errorEvent) {
		log("Error :"+errorEvent.toString());
		sendNotification(this.getBaseContext(),errorEvent.toString());
	}


	public void chimeEvent(final ChimeEvent chimeEvent) {
		log("Chime :"+chimeEvent.toString());
		runOnUiThread(new Runnable() {
			public void run() {
				((ImageView)findViewById(R.id.chimeIcon)).setEnabled(true);
				if (chimeEvent.getState().equals(ChimeEvent.State.ENABLED)) {
					((ImageView)findViewById(R.id.chimeIcon)).setImageResource(R.drawable.ic_action_volume_on);
				}
				else {
					((ImageView)findViewById(R.id.chimeIcon)).setImageResource(R.drawable.ic_action_volume_off);
				}
			}
		});
	}


	public void smokeEvent(final SmokeEvent smokeEvent) {
		log("Smoke :"+smokeEvent.toString());
		runOnUiThread(new Runnable() {
			public void run() {
				((ImageView)findViewById(R.id.chimeIcon)).setEnabled(true);
				if (smokeEvent.getState().equals(SmokeEvent.State.Alarm)) {
					((ImageView)findViewById(R.id.fireIcon)).setImageResource(R.drawable.ic_action_smoking);
				}
				else {
					((ImageView)findViewById(R.id.fireIcon)).setImageResource(R.drawable.ic_action_nosmoking);
				}		
			}
		});
	}


	public void panelEvent(PanelEvent panelEvent) {		
	}


	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		ZoneEvent ze = (ZoneEvent) zoneAdapter.getChild(groupPosition, childPosition);
		Intent zoneEdit = new Intent(EnvisadroidActivity.this.getBaseContext(), ZoneEditActivity.class);
		SyncHelper syncHelper = new SyncHelper(this,Util.getPreference(this,SettingsActivity.HOSTNAME,"192.168.1.25"));
		zoneEdit.putExtra(ZoneEditActivity.EXTRA_ZONE_NAME,syncHelper.getZoneName(this,ze));
		zoneEdit.putExtra(ZoneEditActivity.EXTRA_ZONE_ID,ze.getZone());
		zoneEdit.putExtra(ZoneEditActivity.EXTRA_ZONE_STATE,ze.getState().name());
		zoneEdit.putExtra(ZoneEditActivity.EXTRA_ZONE_PARTITION,ze.getPartition());
		EnvisadroidActivity.this.startActivity(zoneEdit);
		return true;
	}

	@SuppressWarnings("deprecation")
	public void sendNotification(Context context, String msg) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		CharSequence tickerText = msg;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.ic_error, tickerText, when);

		// send android notification
		CharSequence contentTitle = context.getString(R.string.app_name);
		CharSequence contentText = msg;
		Intent notificationIntent = new Intent(context, EnvisadroidActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(6758, notification);
	}

	public void ledsOff() {
		((EnvisadroidApplication)getApplication()).ledsOff();
	}

	private void setLed(int ledId, State state) {
		((EnvisadroidApplication)getApplication()).setLed(ledId,state);
	}

	private class FlashTask extends AsyncTask<String, String, String> {
		public static final String LOG = "Envisadroid";
		private boolean checked = true, running = false;

		protected boolean isRunning() {
			return running;
		}


		protected void onCancelled() {
			super.onCancelled();
			running = false;
		}


		protected String doInBackground(String... arg0) {
			running = true;
			while(running) {
				try {
					checked = !checked;
					publishProgress("");
					Thread.sleep(450);
				} 
				catch (InterruptedException e) {
					running = false;
					Log.w(LOG, "Flash thread interrupted");
				}
			}
			return arg0[0];
		}

		protected void onProgressUpdate(String... progress) {
			EnvisadroidActivity act = EnvisadroidActivity.this;
			EnvisadroidApplication app = ((EnvisadroidApplication)act.getApplication());


			((RadioButton)findViewById(R.id.ledArmed)).setChecked(false);
			((RadioButton)findViewById(R.id.ledBacklight)).setChecked(false);
			((RadioButton)findViewById(R.id.ledBypass)).setChecked(false);
			((RadioButton)findViewById(R.id.ledConnection)).setChecked(false);
			((RadioButton)findViewById(R.id.ledFire)).setChecked(false);
			((RadioButton)findViewById(R.id.ledMem)).setChecked(false);
			((RadioButton)findViewById(R.id.ledProgram)).setChecked(false);
			((RadioButton)findViewById(R.id.ledReady)).setChecked(false);
			((RadioButton)findViewById(R.id.ledTrouble)).setChecked(false);

			synchronized (app.getLedList()) {
				for (int ledId : app.getLedList()) {
					((RadioButton)findViewById(ledId)).setChecked(true);
				}
			}

			synchronized (app.getFlashList()) {
				for (int flashId : app.getFlashList()) {
					((RadioButton)findViewById(flashId)).setChecked(checked);
				}
			}
		}

	}

	public void populateZones() {
		if (zoneDataSource.getGroupCount()<=0) {
			for (int i = 1; i < 65;i++) {
				zoneDataSource.createZone(new ZoneEvent(1,i,ZoneEvent.State.Restored));
			}
		}
		//		String jsonStr = "{'state':'ON','function':'FIRE'}";
		//		try {
		//			LEDEvent led = new LEDEvent(new JSONObject(jsonStr));
		//			System.out.println("1>>>>>>"+led.toString());
		//			led.fromJSON(new JSONObject(jsonStr));
		//			System.out.println("2>>>>>>"+led.toString());
		//		} catch (JSONException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		};
		//		setLed(R.id.ledReady, LEDEvent.State.ON);
		//		setLed(R.id.ledFire, LEDEvent.State.FLASH);
	}



}
