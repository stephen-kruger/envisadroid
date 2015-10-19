package com.madibasoft.envisadroid.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

import com.madibasoft.envisadroid.SettingsActivity;
import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.Session;
import com.madibasoft.envisadroid.api.tpi.TPISession;
import com.madibasoft.envisadroid.api.tpi.event.ChimeEvent;
import com.madibasoft.envisadroid.api.tpi.event.CloseEvent;
import com.madibasoft.envisadroid.api.tpi.event.ErrorEvent;
import com.madibasoft.envisadroid.api.tpi.event.GenericEvent;
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
import com.madibasoft.envisadroid.log.LogActivity;
import com.madibasoft.envisadroid.util.Util;

public class EnvisadroidApplication extends Application implements TPIListener, OnSharedPreferenceChangeListener {
	public static final String LOG = "EnvisaDroid";
	private EventDataSource eds;
	private Session session;
	public List<Integer> flashList = new ArrayList<Integer>();
	public List<Integer> ledList = new ArrayList<Integer>();

	
	public void onCreate() {
		super.onCreate();
		Log.d(LOG, "EnvisadroidApplication created");
		// listen to preference changes
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		initPanel();

		eds = new EventDataSource(this.getBaseContext());
		eds.open();
		eds.clear();

		session.addTDIListener(this);
	}

	private void initPanel() {
		Session.Mode mode;
		List<TPIListener>listeners = new ArrayList<TPIListener>();

		try {
			mode = Session.Mode.valueOf(Util.getPreference(this,"panelPref", Session.Mode.TPI.name()));
		}
		catch (Throwable t) {
			t.printStackTrace();
			mode = Session.Mode.TPI;
		}
		if (session!=null) {
			disconnect();
			// save listeners
			listeners = session.getTDIListeners();
		}
		session = Session.getInstance(
				mode,
				Util.getPreference(this,SettingsActivity.HOSTNAME,"192.168.1.25"),
				Util.getIntPreference(this,SettingsActivity.PORT,4025),
				Util.getPreference(this,SettingsActivity.ENVISAUSER,"user"),
				Util.getPreference(this,SettingsActivity.ENVISAPASSWORD,"user"),
				Util.getPreference(this,SettingsActivity.PASSCODE,"1234"));

		// if we changed underlying session, read-add all registered listeners
		for (TPIListener l : listeners) {
			session.addTDIListener(l);
		}

		session.panelEvent(new PanelModeEvent(mode));

		if (Util.getBoolPreference(this, SettingsActivity.AUTOCONNECT)) {
			try {
				connect();
			} 
			catch (Throwable e) {
				LogActivity.log(this, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	
	public void onTerminate() {
		super.onTerminate();
		Log.d(LOG, "EnvisadroidApplication terminated");
		try {
			if (session!=null) {
				session.removeTDIListener(this);
				session.close();
			}
		} 
		catch (Throwable e) {
			e.printStackTrace();
		}
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		eds.close();
	}

	public void connect() {
		LogActivity.log(this, "Connecting to "+Util.getPreference(this,SettingsActivity.HOSTNAME,"192.168.1.25")+":"+Util.getIntPreference(this,SettingsActivity.PORT,4025));
		try {
			session.close(true);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}

		// re-init the session object parameters in case settings changed
		session.setServer(Util.getPreference(this,SettingsActivity.HOSTNAME,"192.168.1.25"));
		session.setPort(Util.getIntPreference(this,SettingsActivity.PORT,4025));
		session.setUsername(Util.getPreference(this,SettingsActivity.ENVISAUSER,"user"));
		session.setPassword(Util.getPreference(this,SettingsActivity.ENVISAPASSWORD,"user"));
		session.setPasscode(Util.getPreference(this,SettingsActivity.PASSCODE,"1234"));

		try {
			new Thread(session).start();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			LogActivity.log(this, "Disconnecting stale session");
			session.close();
			//			lastLoginEvent = null;
			//			lastChimeEvent = null;
			//			lastSmokeEvent = null;
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public boolean isConnected() {
		return session.isConnected();	
	}

	public void addTDIListener(TPIListener listener) {
		session.addTDIListener(listener);
		if (session instanceof TPISession) {
			listener.panelModeEvent(new PanelModeEvent(Session.Mode.TPI));			
		}
		else {
			listener.panelModeEvent(new PanelModeEvent(Session.Mode.NONTPI));						
		}
		// send newly registered listener last 100 past events
		List<GenericEvent> events = eds.getAllEvents();
		for (GenericEvent ge : events) {
			if (ge instanceof ZoneEvent) {
				listener.zoneEvent((ZoneEvent)ge);
			} else if (ge instanceof ChimeEvent) {
				listener.chimeEvent((ChimeEvent)ge);
			} else if (ge instanceof ErrorEvent) {
				listener.errorEvent((ErrorEvent)ge);
			} else if (ge instanceof InfoEvent) {
				listener.infoEvent((InfoEvent)ge);
			} else if (ge instanceof LEDEvent) {
				listener.ledEvent((LEDEvent)ge);
			} else if (ge instanceof LoginEvent) {
				listener.loginEvent((LoginEvent)ge);
			} else if (ge instanceof PartitionEvent) {
				listener.partitionEvent((PartitionEvent)ge);
			} else if (ge instanceof SmokeEvent) {
				listener.smokeEvent((SmokeEvent)ge);
			}
		}
	}

	public void removeTDIListener(TPIListener listener) {
		session.removeTDIListener(listener);
	}

	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SettingsActivity.PANEL)) {
			initPanel();
		}

		if (key.equals(SettingsActivity.HOSTNAME)) {
			initPanel();
		}

		if (key.equals(SettingsActivity.PORT)) {
			initPanel();
		}

		if (key.equals(SettingsActivity.ENVISAPASSWORD)) {
			initPanel();
		}

		if (key.equals(SettingsActivity.PASSCODE)) {
			session.setPasscode(Util.getPreference(this,SettingsActivity.PASSCODE,"1234"));
		}
	}

	public void setLed(int ledId, LEDEvent.State state) {
		removeFlash(ledId);
		ledOff(ledId);

		switch (state) {
		case ON:
			ledOn(ledId);
			break;
		case OFF :
			break;
		case FLASH :
			addFlash(ledId);
			break;
		}
	}

	public List<Integer> getFlashList() {
		return Collections.unmodifiableList(flashList);
	}

	private void addFlash(int flashId) {
		synchronized (flashList) {
			flashList.add(flashId);
		}
	}

	private void removeFlash(int flashId) {
		synchronized (flashList) {
			try {
				while (flashList.remove(Integer.valueOf(flashId)));
			}
			catch (Throwable t) {
				// no biggie
			}
		}
	}

	private void clearFlash() {
		synchronized (flashList) {
			flashList.clear();
		}
	}

	public List<Integer> getLedList() {
		return Collections.unmodifiableList(ledList);
	}

	private void ledOn(final int ledId) {
		synchronized (ledList) {
			ledList.add(ledId);
		}
	}

	private void ledOff(final int ledId) {
		synchronized (ledList) {
			try {
				while (ledList.remove(Integer.valueOf(ledId)));
			}
			catch (Throwable t) {
				// no biggie
			}
		}
	}

	public void ledsOff() {
		clearFlash();
		clearLeds();
	}

	private void clearLeds() {
		synchronized (ledList) {
			ledList.clear();
		}
	}

	public void clearPersistedEvents() {
		eds.clear();
	}

	public void persistEvent(GenericEvent ge) {
		eds.addEvent(ge);
	}

	
	public void zoneEvent(ZoneEvent ze) {
		LogActivity.log(this,"Zone event :"+ze.toString());
		//		persistEvent(ze);	
	}

	
	public void ledEvent(LEDEvent ledEvent) {
		persistEvent(ledEvent);
	}

	
	public void partitionEvent(PartitionEvent partitionEvent) {
		persistEvent(partitionEvent);
	}

	
	public void loginEvent(LoginEvent loginEvent) {
		persistEvent(loginEvent);
		//		lastLoginEvent = loginEvent;	
		switch (loginEvent.getState()) {
		case TIMEDOUT :
			break;
		case FAILED :
			break;
		case CONNECTION_FAIL :
			break;
		case LOGGEDOUT :
			clearPersistedEvents();
			break;
		case LOGGEDIN :
			// need this to get led status
			// session.getStatusReport();	
			// need this to get close times
			try {
				session.getStatusReport();
				session.dumpZoneTimers();
			} 
			catch (EnvisaException e) {
				e.printStackTrace();
			}
			break;
		case REQUESTING :
			break;
		}

	}

	
	public void errorEvent(ErrorEvent errorEvent) {
		persistEvent(errorEvent);
	}

	
	public void infoEvent(InfoEvent infoEvent) {
		persistEvent(infoEvent);
	}

	
	public void chimeEvent(ChimeEvent chimeEvent) {
		persistEvent(chimeEvent);
	}

	public void closeEvent(CloseEvent closeEvent) {
		persistEvent(closeEvent);		
	}

	public void openEvent(OpenEvent openEvent) {
		persistEvent(openEvent);		
	}
	
	public void panelEvent(PanelEvent panelEvent) {
		persistEvent(panelEvent);	
	}
	
	public void smokeEvent(SmokeEvent smokeEvent) {
		persistEvent(smokeEvent);
	}

	
	public void panelModeEvent(PanelModeEvent panelEvent) {
		persistEvent(panelEvent);
	}

	public Session getSession() {
		return session;
	}

}
