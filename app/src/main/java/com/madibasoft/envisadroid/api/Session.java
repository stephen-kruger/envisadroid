package com.madibasoft.envisadroid.api;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.madibasoft.envisadroid.api.tpi.Command;
import com.madibasoft.envisadroid.api.tpi.TPISession;
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
import com.madibasoft.envisadroid.api.web.WebSession;


public abstract class Session implements Runnable {
	public static final String LOG = "EnvisadroidSession";
	private static Logger log = Logger.getLogger(Session.LOG);
	private boolean running=false, installerMode=false;
	public enum Mode {TPI,NONTPI}

	private String passcode,username,password;
	private LinkedList<TPIListener> tdiListeners = new LinkedList<TPIListener>();
	private String server;
	private static Session sessionInstance=null;

	protected Session(String server, int port, String username, String password, String passcode) {
		this.server = server;
		this.port = port;
		this.passcode = passcode;
		this.username = username;
		this.password = password;
	}

	public static Session getInstance(Mode mode, String server, int port, String username, String password, String passcode) {
		logi("Setting panel mode to "+mode.name());

		// if panel mode changed, delete old instance
		if (sessionInstance!=null) {
			switch (mode) {
			case TPI :if (!(sessionInstance instanceof TPISession))
				sessionInstance=null;break;
			case NONTPI :if (!(sessionInstance instanceof WebSession))
				sessionInstance=null;break;
			}
		}
		if (sessionInstance==null) {
			if (mode==Mode.TPI) {
				return new TPISession(server, port, username, password, passcode);
			}
			else if (mode==Mode.NONTPI) {
				return new WebSession(server, port, username, password, passcode);
			}
			else {
				return new WebSession(server, port, username, password, passcode);
			}
		}
		else {
			return sessionInstance;
		}
	}

	public abstract void run();

	public boolean isConnected() {
		return running;
	}

	public void setConnected(boolean running) {
		this.running = running;
	}

	public String getPasscode() {
		return passcode;
	}

	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private int port;

	public static void loge(String msg) {
		//		Log.e(Session.LOG, msg);
		log.severe(msg);
	}

	public static void logi(String msg) {
		//		Log.i(Session.LOG, msg);
		log.info(msg);
	}

	public static void logw(String msg) {
		//		Log.w(Session.LOG, msg);
		log.info(msg);
	}

	public static void logd(String msg) {
		//		Log.d(Session.LOG, msg);
		log.info(msg);
	}

	public void addTDIListener(TPIListener listener){
		tdiListeners.add(listener);
	}

	public boolean removeTDIListener(TPIListener listener){
		return tdiListeners.remove(listener);
	}

	public List<TPIListener> getTDIListeners() {
		return tdiListeners;
	}

	protected void zoneEvent(ZoneEvent event){
		for(TPIListener t: getTDIListeners()){
			t.zoneEvent(event);
		}
	}

	public void panelEvent(PanelModeEvent event){
		for(TPIListener t: getTDIListeners()){
			t.panelModeEvent(event);
		}
	}


	//	protected void zoneEvent(List<ZoneEvent> zones){
	//		for(TPIListener t: getTDIListeners()){
	//			t.zoneEvent(zones);
	//		}
	//	}

	protected void ledEvent(LEDEvent z){
		for(TPIListener t: getTDIListeners()){
			t.ledEvent(z);
		}
	}

	protected void partitionEvent(PartitionEvent p){
		for(TPIListener t: getTDIListeners()){
			t.partitionEvent(p);
		}
	}

	protected void chimeEvent(ChimeEvent p){
		for(TPIListener t: getTDIListeners()){
			t.chimeEvent(p);
		}
	}

	protected void smokeEvent(SmokeEvent smokeEvent) {
		for(TPIListener t: getTDIListeners()){
			t.smokeEvent(smokeEvent);
		}		
	}

	protected void loginEvent(LoginEvent loginEvent) {
		for(TPIListener t: getTDIListeners()){
			t.loginEvent(loginEvent);
		}
	}

	protected void errorEvent(ErrorEvent errorEvent) {
		for(TPIListener t: getTDIListeners()){
			t.errorEvent(errorEvent);
		}
	}

	protected void infoEvent(InfoEvent infoEvent) {
		for(TPIListener t: getTDIListeners()){
			t.infoEvent(infoEvent);
		}
	}
	
	protected void closeEvent(CloseEvent infoEvent) {
		for(TPIListener t: getTDIListeners()){
			t.closeEvent(infoEvent);
		}
	}
	
	protected void openEvent(OpenEvent infoEvent) {
		for(TPIListener t: getTDIListeners()){
			t.openEvent(infoEvent);
		}
	}
	
	protected void panelEvent(PanelEvent panelEvent) {
		for(TPIListener t: getTDIListeners()){
			t.panelEvent(panelEvent);
		}
	}

	public void close() {
		close(false);
	}

	public abstract void close(boolean quiet);

	public boolean isInstallerMode() {
		return installerMode;
	}

	public void setInstallerMode(boolean installerMode) {
		this.installerMode = installerMode;
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public abstract void poll() throws EnvisaException;

	public abstract void dumpZoneTimers() throws EnvisaException;

	public abstract void reboot() throws EnvisaException;

	public abstract void setDateAndTime() throws EnvisaException;

	public abstract void getStatusReport() throws EnvisaException;

	public abstract void arm(int partition) throws EnvisaException;

	public abstract void armStay(int partition) throws EnvisaException;

	public abstract void armWithCode(int partition) throws EnvisaException;

	public abstract void disarm(int partition) throws EnvisaException;

	public abstract void toggleChime(int partition) throws EnvisaException;

	public abstract void bypass(int partition, int zone) throws EnvisaException;

	public abstract void runCommand(Command c) throws EnvisaException;

	public abstract void bypass(int partition, int[] zone) throws EnvisaException;

	public abstract void clearBypass(int partition, int zone) throws EnvisaException;

	public abstract void sendTemperatureBroadcast(boolean on) throws EnvisaException;

}

