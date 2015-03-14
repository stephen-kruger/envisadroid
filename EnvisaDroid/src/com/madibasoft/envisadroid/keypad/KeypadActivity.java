package com.madibasoft.envisadroid.keypad;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.madibasoft.envisadroid.R;
import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.tpi.Command;
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
import com.madibasoft.envisadroid.util.Util;

public class KeypadActivity extends Activity implements TPIListener {



	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_keypad);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public void hash(View view) {
		charPress("#");
	}

	public void star(View view) {
		charPress("*");
	}

	public void send(View view) {
		String command = ((TextView)findViewById(R.id.commandString)).getText().toString();
		((TextView)findViewById(R.id.commandString)).setText("");
		if (command.length()>0) {
			try {
				Command c = new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
				c.setData(command);
				((EnvisadroidApplication)getApplication()).getSession().runCommand(c);
			} 
			catch (EnvisaException e) {
				Util.dialog(this, getString(R.string.error), e.toString());
				e.printStackTrace();
			}
		}
	}

	public void del(View view) {
		String s = ((TextView)findViewById(R.id.commandString)).getText().toString();
		if (s.length()>0)
			s = s.substring(0,s.length()-1);
		((TextView)findViewById(R.id.commandString)).setText(s);
	}

	private void charPress(String c) {
		if (((TextView)findViewById(R.id.commandString)).getText().toString().length()<6) {
			((TextView)findViewById(R.id.commandString)).setText(((TextView)findViewById(R.id.commandString)).getText()+c);
		}
	}

	public void zero(View view) {
		charPress("0");
	}

	public void one(View view) {
		charPress("1");
	}

	public void two(View view) {
		charPress("2");
	}

	public void three(View view) {
		charPress("3");
	}

	public void four(View view) {
		charPress("4");
	}

	public void five(View view) {
		charPress("5");
	}

	public void six(View view) {
		charPress("6");
	}

	public void seven(View view) {
		charPress("7");
	}

	public void eight(View view) {
		charPress("8");
	}

	public void nine(View view) {
		charPress("9");
	}

	public void panelModeEvent(PanelModeEvent panelEvent) {
		
	}

	public void zoneEvent(ZoneEvent ge) {
		
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

	public void closeEvent(CloseEvent closeEvent) {
		
	}

	public void openEvent(OpenEvent openEvent) {
		

	}

	public void panelEvent(PanelEvent panelEvent) {
		
	}

}