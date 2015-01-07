package com.madibasoft.envisadroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;
import com.madibasoft.envisadroid.application.EnvisadroidApplication;
import com.madibasoft.envisadroid.sync.SyncHelper;
import com.madibasoft.envisadroid.util.Util;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ZoneEditActivity extends Activity {

	public static final String EXTRA_ZONE_STATE = "com.madibasoft.envisadroid.ZoneState";
	public static final String EXTRA_ZONE_NAME = "com.madibasoft.envisadroid.ZoneName";
	public static final String EXTRA_ZONE_ID = "com.madibasoft.envisadroid.ZoneID";
	public static final String EXTRA_ZONE_PARTITION = "com.madibasoft.envisadroid.ZonePartition";

	// Values for email and password at the time of the login attempt.
	private String zoneName;
	private ZoneEvent.State zoneState;
	int zonePartition, zoneId;

	// UI references.
	private EditText mZoneNameView;


	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_zone_edit);

		// Set up the login form.
		zoneName = getIntent().getStringExtra(EXTRA_ZONE_NAME);
		zoneId = getIntent().getIntExtra(EXTRA_ZONE_ID,1);
		zonePartition = getIntent().getIntExtra(EXTRA_ZONE_PARTITION,1);
		zoneState = ZoneEvent.State.valueOf(getIntent().getStringExtra(EXTRA_ZONE_STATE));
		mZoneNameView = (EditText) findViewById(R.id.zoneName);
		mZoneNameView.setText(zoneName);
		((TextView)findViewById(R.id.partitionText)).setText("Partition:"+zonePartition);
		((TextView)findViewById(R.id.zoneText)).setText("Zone:"+zoneId);
		Button bypassOnButton = (Button) findViewById(R.id.bypassOnAction);
		Button bypassOffButton = (Button) findViewById(R.id.bypassOffAction);

		switch (zoneState) {
		case Restored:
			bypassOnButton.setEnabled(false);
			bypassOffButton.setEnabled(false);
			break;
		case Fault_Restored:
			bypassOnButton.setEnabled(false);
			bypassOffButton.setEnabled(false);
			break;
		case Open:
			bypassOnButton.setEnabled(true);
			bypassOffButton.setEnabled(true);
			break;
		case Fault:
			bypassOnButton.setEnabled(true);
			bypassOffButton.setEnabled(true);
			break;
		case Tamper:
			bypassOnButton.setEnabled(true);
			bypassOffButton.setEnabled(true);
			break;
		case Tamper_Restore:
			bypassOnButton.setEnabled(false);
			bypassOffButton.setEnabled(false);
			break;
		case Alarm:
			bypassOnButton.setEnabled(false);
			bypassOffButton.setEnabled(false);
			break;
		case Alarm_Restore :
			bypassOnButton.setEnabled(false);
			bypassOffButton.setEnabled(false);
			break;
		}
		
		bypassOnButton.setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
			            try {
							((EnvisadroidApplication)getApplication()).getSession().clearBypass(zonePartition, zoneId);
						} 
			            catch (EnvisaException e) {
							e.printStackTrace();
							Util.dialog(ZoneEditActivity.this,"Info",e.getMessage());
						}
						finish();
					}
				});
		bypassOffButton.setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
			            try {
							((EnvisadroidApplication)getApplication()).getSession().clearBypass(zonePartition, zoneId);
						} 
			            catch (EnvisaException e) {
							e.printStackTrace();
						}
						finish();
					}
				});
		
		findViewById(R.id.save).setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
						zoneName = mZoneNameView.getText().toString();
						SyncHelper syncHelper = new SyncHelper(ZoneEditActivity.this,Util.getPreference(ZoneEditActivity.this,SettingsActivity.HOSTNAME,"192.168.1.25"));
						syncHelper.setZoneName(ZoneEditActivity.this, zonePartition, zoneId, zoneName);
						finish();
					}
				});
		
		findViewById(R.id.cancel).setOnClickListener(
				new View.OnClickListener() {
					
					public void onClick(View view) {
						finish();
					}
				});
	}

}
