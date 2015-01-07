package com.madibasoft.envisadroid.api.web;

import java.util.Date;
import java.util.StringTokenizer;

import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.Session;
import com.madibasoft.envisadroid.api.tpi.Command;
import com.madibasoft.envisadroid.api.tpi.event.LEDEvent;
import com.madibasoft.envisadroid.api.tpi.event.LoginEvent;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;


public class WebSession extends Session {
	private static final String NSE = "Not supported in non-TPI mode";
	private boolean installerMode=false;
	private int pollCount=0;

	private static WebConnection connection = null;

	public WebSession(String server, int port, String username, String password, String passcode) {
		super (server,port,passcode,username,password);
	}

	public void run() {
		setConnected(true);
		try {
			setConnected(true);
			loginEvent(new LoginEvent(LoginEvent.State.REQUESTING));
			connection = new WebConnection();
			while (isConnected()) {
				loginEvent(new LoginEvent(LoginEvent.State.LOGGEDIN));
				sleep(120000);
			}
		} 
		catch (Throwable e) {
			setConnected(false);
			e.printStackTrace();
			loginEvent(new LoginEvent(LoginEvent.State.CONNECTION_FAIL));
		}

	}

	private void parsePage(String homePage) throws EnvisaException {
		try {
			ledEvent(new LEDEvent(LEDEvent.Function.READY,LEDEvent.State.FLASH));
			parseStatus(homePage);
			// parse the zone events
			String zoneTable = "<TABLE BORDER=2 CLASS=keypad><TR><TD BGCOLOR=#000000>";
			homePage = homePage.substring(homePage.indexOf(zoneTable)+zoneTable.length());
			String zoneStartString = "<SPAN TITLE=";
			String zoneEndString = "</SPAN>";
			int zoneIndex;
			ZoneEvent.State zoneState;
			//			System.out.println(">>>>start"+homePage);
			while ((zoneIndex=homePage.indexOf(zoneStartString))>=0) {
				// <SPAN TITLE=\"CLOSED: \">1</SPAN>
				homePage = homePage.substring(zoneIndex);
				int endIndex;
				String zoneString = homePage.substring(0,endIndex=(homePage.indexOf(zoneEndString)+zoneEndString.length()));
				//				System.out.println(zoneString);
				if (zoneString.indexOf("CLOSED")>=0) {
					zoneState = ZoneEvent.State.Restored;
				}
				else if (zoneString.indexOf("OPEN")>=0) {
					zoneState = ZoneEvent.State.Open;
				} else {
					zoneState = ZoneEvent.State.Alarm;
				}
				int timeStart = zoneString.indexOf(":")+1;
				String timeString = zoneString.substring(timeStart,zoneString.indexOf('"',timeStart)).trim();
				//				System.out.println("Time:"+timeString);
				String zoneZoneString =zoneString.substring(zoneString.indexOf('>')+1,zoneString.indexOf(zoneEndString));
				//				System.out.println(zoneZoneString+" "+zoneState);
				ZoneEvent zoneEvent = new ZoneEvent(1,Integer.parseInt(zoneZoneString), zoneState);
				zoneEvent.setZoneDate(parseDate(timeString));
				zoneEvent(zoneEvent);
				homePage = homePage.substring(endIndex);
			}
		}
		catch (Exception e) {
			ledEvent(new LEDEvent(LEDEvent.Function.READY,LEDEvent.State.OFF));
			e.printStackTrace();
			loginEvent(new LoginEvent(LoginEvent.State.LOGGEDOUT));
			throw new EnvisaException("Invalid content");
		}		
	}

	private void parseStatus(String homePage) {
		// <TR><TD>System</TD><TD BGCOLOR=\"LIME\">Ready </TD>
		String startTag = "<TR><TD>System</TD>";
		String endTag = "</TD>";
		homePage = homePage.substring(homePage.indexOf(startTag)+startTag.length());
		homePage = homePage.substring(0,homePage.indexOf(endTag));
		String status = homePage.substring(homePage.indexOf(">")).trim();
		if (status.indexOf("Not Ready")>=0) {
			ledEvent(new LEDEvent(LEDEvent.Function.READY,LEDEvent.State.OFF));
		}
		else if (status.indexOf("Ready")>=0) {
			ledEvent(new LEDEvent(LEDEvent.Function.READY,LEDEvent.State.ON));
		} else {
			ledEvent(new LEDEvent(LEDEvent.Function.READY,LEDEvent.State.OFF));			
		}

	}

	private Date parseDate(String timeString) {
		// 9 Hours Ago
		// 14 Minutes Ago
		// 10 Seconds Ago
		StringTokenizer tok = new StringTokenizer(timeString," ");
		if (tok.hasMoreTokens()) {
			String value = tok.nextToken().trim();
			String unit = tok.nextToken().trim();
			if (unit.indexOf("Hours")>=0) {
				return new Date(new Date().getTime()-(Integer.parseInt(value)*60*60*1000));
			} else if (unit.indexOf("Minutes")>=0) {
				return new Date(new Date().getTime()-(Integer.parseInt(value)*60*1000));
			} else if (unit.indexOf("Seconds")>=0) {
				return new Date(new Date().getTime()-(Integer.parseInt(value)*1000));
			}
			return new Date();
		}
		else {
			return new Date();
		}
	}

	public void close() {
		close(false);
	}

	public void close(boolean quiet) {
		setConnected(false);
		if (connection!=null)
			connection.close();
		if (!quiet) 
			loginEvent(new LoginEvent(LoginEvent.State.LOGGEDOUT));
		// clear pending commands
		pollCount=0;
	}

	public boolean isInstallerMode() {
		return installerMode;
	}

	public void setInstallerMode(boolean installerMode) {
		this.installerMode = installerMode;
	}

	public void tickPoll() throws EnvisaException {
		// do a poll every now and then
		if (pollCount>40) {
			poll();
		}
		else {
			if (pollCount>0) {
				sleep(4500);
			}
		}
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void poll() throws EnvisaException {
		throw new EnvisaException(NSE);

	}

	public void dumpZoneTimers() throws EnvisaException {
		parsePage(connection.getHomePage(getServer(),getUsername(),getPassword()));
	}

	public void reboot() throws EnvisaException {
		connection.execute(getServer(),getUsername(), getPassword(),"/3?A=2");
	}

	public void setDateAndTime() throws EnvisaException {
		throw new EnvisaException(NSE);

	}

	public void getStatusReport() throws EnvisaException {
		parsePage(connection.getHomePage(getServer(),getUsername(),getPassword()));
	}

	public void arm(int partition) throws EnvisaException {
		// http://192.168.xxx.xxx/2?A=3&p=1&X=1234
		connection.execute(getServer(),getUsername(), getPassword(),"/2?A=3&p="+partition+"&X="+getPasscode());
	}

	public void armStay(int partition) throws EnvisaException {
		connection.execute(getServer(),getUsername(), getPassword(),"/2?A=3&p="+partition+"&X="+getPasscode());
	}

	public void armWithCode(int partition) throws EnvisaException {
		connection.execute(getServer(),getUsername(), getPassword(),"/2?A=3&p="+partition+"&X="+getPasscode());
	}

	public void disarm(int partition) throws EnvisaException {
		connection.execute(getServer(),getUsername(), getPassword(),"/2?A=4&p="+partition+"&X="+getPasscode());
	}

	public void toggleChime(int partition) throws EnvisaException {
		throw new EnvisaException("Not supported in Web mode");
	}

	public void bypass(int partition, int zone) throws EnvisaException {
		bypass(partition, new int[]{zone});
	}

	public void bypass(int partition, int[] zone) throws EnvisaException {
		throw new EnvisaException(NSE);
	}

	public void clearBypass(int partition, int zone) throws EnvisaException {
		throw new EnvisaException("Not supported in Web mode");
	}

	public void sendTemperatureBroadcast(boolean on) throws EnvisaException {
		throw new EnvisaException("Not supported in Web mode");
	}

	@Override
	public void runCommand(Command c) throws EnvisaException {
		throw new EnvisaException("Not supported in Web mode");		
	}

}

