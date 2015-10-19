package com.madibasoft.envisadroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.tpi.event.ChimeEvent;
import com.madibasoft.envisadroid.api.tpi.event.CloseEvent;
import com.madibasoft.envisadroid.api.tpi.event.ErrorEvent;
import com.madibasoft.envisadroid.api.tpi.event.InfoEvent;
import com.madibasoft.envisadroid.api.tpi.event.LEDEvent;
import com.madibasoft.envisadroid.api.tpi.event.LoginEvent;
import com.madibasoft.envisadroid.api.tpi.event.OpenEvent;
import com.madibasoft.envisadroid.api.tpi.event.PanelEvent;
import com.madibasoft.envisadroid.api.tpi.event.PanelModeEvent;
import com.madibasoft.envisadroid.api.tpi.event.PartitionEvent;
import com.madibasoft.envisadroid.api.tpi.event.SmokeEvent;
import com.madibasoft.envisadroid.api.tpi.event.TPIListener;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;
import com.madibasoft.envisadroid.application.EnvisadroidApplication;
import com.madibasoft.envisadroid.log.LogActivity;
import com.madibasoft.envisadroid.sync.SyncActivity;
import com.madibasoft.envisadroid.util.Util;

public class ToolsActivity extends Activity implements TPIListener {

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tools);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		findViewById(R.id.syncButton).setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
						Intent myIntent = new Intent(ToolsActivity.this, SyncActivity.class);
						ToolsActivity.this.startActivity(myIntent);
					}
				});

		Button settings = (Button)findViewById(R.id.settingsButton);
		settings.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				Intent myIntent = new Intent(ToolsActivity.this, SettingsActivity.class);
				ToolsActivity.this.startActivity(myIntent);
			}
		});

		Button date = (Button)findViewById(R.id.timeButton);
		date.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				try {
					((EnvisadroidApplication)getApplication()).getSession().setDateAndTime();
					Util.dialog(ToolsActivity.this,"Info","Sent date request");
				}
				catch (Throwable e) {
					dialog(ToolsActivity.this,R.string.error,e.getMessage());
				}
			}
		});

		findViewById(R.id.logButton).setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
						Intent myIntent = new Intent(ToolsActivity.this, LogActivity.class);
						ToolsActivity.this.startActivity(myIntent);
					}
				});

		Button armButton = (Button)findViewById(R.id.armToolButton);
		armButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				try {
					((EnvisadroidApplication)getApplication()).getSession().armWithCode(1);
					AlertDialog.Builder builder = new AlertDialog.Builder(ToolsActivity.this);
					builder.setMessage("Arm requested")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}
				catch (Throwable e) {
					dialog(ToolsActivity.this,R.string.error,e.getMessage());
				}
			}
		});

		Button stayButton = (Button)findViewById(R.id.stayToolButton);
		stayButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ToolsActivity.this);
				builder.setMessage("Arm stay requested")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						try {
							((EnvisadroidApplication)getApplication()).getSession().armStay(1);
						} 
						catch (Throwable e) {
							dialog(ToolsActivity.this,R.string.error,e.getMessage());
							e.printStackTrace();
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});

		// disarm
		Button disarmButton = (Button)findViewById(R.id.disarmToolButton);
		disarmButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {

				try {
					((EnvisadroidApplication)getApplication()).getSession().disarm(1);
					dialog(ToolsActivity.this,R.string.success,R.string.disarm);
				} 
				catch (Throwable e) {
					dialog(ToolsActivity.this,R.string.error,e.getMessage());
				}
			}
		});

		// chime
		Button chimeButton = (Button)findViewById(R.id.chimeButton);
		chimeButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				try {
					((EnvisadroidApplication)getApplication()).getSession().toggleChime(1);
					dialog(ToolsActivity.this,R.string.success,R.string.chime_toggle_clicked);
				} 
				catch (Throwable e) {
					dialog(ToolsActivity.this,R.string.error,e.getMessage());
				}
			}
		});

		// timers
		Button timerButton = (Button)findViewById(R.id.timersButton);
		timerButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				try {
					((EnvisadroidApplication)getApplication()).getSession().dumpZoneTimers();
					dialog(ToolsActivity.this,R.string.success,R.string.timers);
				} 
				catch (Throwable e) {
					dialog(ToolsActivity.this,R.string.error,e.getMessage());
				}
			}
		});

		// report
		Button reportButton = (Button)findViewById(R.id.reportButton);
		reportButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				try {
					((EnvisadroidApplication)getApplication()).getSession().getStatusReport();
					dialog(ToolsActivity.this,R.string.success,R.string.report);
				} 
				catch (Throwable e) {
					dialog(ToolsActivity.this,R.string.error,e.getMessage());
				}
			}
		});

		// reboot
		Button rebootButton = (Button)findViewById(R.id.rebootButton);
		rebootButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View vw) {
				try {
					((EnvisadroidApplication)getApplication()).getSession().reboot();
					dialog(ToolsActivity.this,R.string.success,R.string.reboot);
				} 
				catch (Throwable e) {
					dialog(ToolsActivity.this,R.string.error,e.getMessage());
				}
			}
		});
		
		((EnvisadroidApplication) getApplication()).addTDIListener(this);
	}



	
	protected void onDestroy() {
		super.onDestroy();
		((EnvisadroidApplication) getApplication()).removeTDIListener(this);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tools, menu);
		return true;
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
//			NavUtils.navigateUpFromSameTask(this);
			navigateUpTo(this.getParentActivityIntent());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void dialog(Context c, int titleResource, int msgResource) {
		Util.dialog(c,c.getString(titleResource),c.getString(msgResource));
	}

	public static void dialog(Context c, int titleResource, String msg) {
		Util.dialog(c,c.getString(titleResource),msg);
	}



	
	public void panelModeEvent(PanelModeEvent panelEvent) {
		// disable / enable buttons based on select panel type
		switch (panelEvent.getMode()) {
		case TPI : 
			findViewById(R.id.timeButton).setEnabled(true);
			findViewById(R.id.chimeButton).setEnabled(true);
			break;
		case NONTPI : 
			findViewById(R.id.timeButton).setEnabled(false);
			findViewById(R.id.chimeButton).setEnabled(false);
			break;
		default :
		}
		
	}
	
	public void ledEvent(LEDEvent ledEvent) {
		
	}
	
	public void partitionEvent(PartitionEvent partitionEvent) {
		
	}
	
	public void loginEvent(LoginEvent loginEvent) {
		
	}
	
	public void errorEvent(ErrorEvent errorEvent) {
		
	}
	
	public void infoEvent(InfoEvent infoEvent) {
		
	}

	public void chimeEvent(ChimeEvent chimeEvent) {
		
	}
	
	public void smokeEvent(SmokeEvent smokeEvent) {
		
	}
	
	public void zoneEvent(ZoneEvent ge) {
		
	}

	public void closeEvent(CloseEvent closeEvent) {
		
		
	}

	public void openEvent(OpenEvent openEvent) {
				
	}

	public void panelEvent(PanelEvent panelEvent) {
		
	}

}
