package com.madibasoft.envisadroid.api.tpi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.Session;
import com.madibasoft.envisadroid.api.tpi.event.ChimeEvent;
import com.madibasoft.envisadroid.api.tpi.event.ErrorEvent;
import com.madibasoft.envisadroid.api.tpi.event.InfoEvent;
import com.madibasoft.envisadroid.api.tpi.event.LEDEvent;
import com.madibasoft.envisadroid.api.tpi.event.LEDEvent.Function;
import com.madibasoft.envisadroid.api.tpi.event.LoginEvent;
import com.madibasoft.envisadroid.api.tpi.event.PartitionEvent;
import com.madibasoft.envisadroid.api.tpi.event.SmokeEvent;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;


public class TPISession extends Session {
	private boolean loggedIn=false, installerMode=false;
	private int pollCount=0;
	private int expectingRetryCount=0;
	LinkedList<Command> commandStack=new LinkedList<Command>();
	LinkedList<Command> expectingStack=new LinkedList<Command>();
	private Command lastCommand;
	private static TPIConnection connection = null;

	public TPISession(String server, int port, String username, String password, String passcode) {
		super (server,port,passcode,username,password);
	}

	public void run() {
		setConnected(true);
		try {
			loggedIn = false;
			setConnected(true);
			connection = new TPIConnection();
			connection.open(getServer(), getPort(), 10000);
		} 
		catch (Throwable e) {
			setConnected(false);
			e.printStackTrace();
			loginEvent(new LoginEvent(LoginEvent.State.CONNECTION_FAIL));
		}
		while (isConnected()) {
			tickEvents();	
			tickCommands();
			try {
				tickPoll();
			} catch (EnvisaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void close() {
		close(false);
	}

	public void close(boolean quiet) {
		setConnected(false);
		loggedIn = false;
		if (connection!=null)
			connection.close();
		if (!quiet) 
			loginEvent(new LoginEvent(LoginEvent.State.LOGGEDOUT));
		// clear pending commands
		pollCount=0;
		lastCommand=null;
		commandStack.clear();
		expectingStack.clear();
	}

	public boolean isInstallerMode() {
		return installerMode;
	}

	public void setInstallerMode(boolean installerMode) {
		this.installerMode = installerMode;
	}

	public void processMessage(Message message) {
		switch (message.getCode()) {
		case Command.TROUBLE_LED_ON : 
			logd("TROUBLE LED ON:"+message);
			ledEvent(new LEDEvent(LEDEvent.Function.TROUBLE,LEDEvent.State.ON));
			break;
		case Command.TROUBLE_LED_OFF : 
			logd("TROUBLE LED OFF:"+message);
			ledEvent(new LEDEvent(LEDEvent.Function.TROUBLE,LEDEvent.State.OFF));
			break;
		case Command.KEYPAD_LED_STATE : 
			logd("KEYPAD_LED_STATE:"+message);
			String value1 = convertToBinaryString(message.getGeneralData().substring(0, 1));
			String value2 = convertToBinaryString(message.getGeneralData().substring(1, 2));
			String ledArray = value1+value2;
			for (int i = 0; i < ledArray.length();i++) {
				if (ledArray.charAt(i)=='1')
					ledEvent(new LEDEvent(Function.values()[i],LEDEvent.State.ON));
				else
					ledEvent(new LEDEvent(Function.values()[i],LEDEvent.State.OFF));
			}
			break;
		case Command.KEYPAD_LEDFLASH_STATE : 
			logd("KEYPAD_LEDFLASH_STATE:"+message);
			String ledArray2 = convertToBinaryString(message.getGeneralData().substring(0, 1)) +
					convertToBinaryString(message.getGeneralData().substring(1, 2));
			for (int i = 0; i < ledArray2.length();i++) {
				if (ledArray2.charAt(i)=='1')
					ledEvent(new LEDEvent(Function.values()[i],LEDEvent.State.FLASH));
				else
					ledEvent(new LEDEvent(Function.values()[i],LEDEvent.State.OFF));
			}
			break;	
		case Command.TIME_DATE_BROADCAST :
			logi("TIME_DATE_BROADCAST:"+message);
			infoEvent(new InfoEvent("TIME_DATE_BROADCAST:"+message));
			break;	
		case Command.ZONE_ALARM : 
			logi("ZONE_ALARM:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Alarm));
			break;	
		case Command.ZONE_ALARM_RESTORE : 
			logi("ZONE_ALARM_RESTORE:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Alarm_Restore));
			break;	
		case Command.ZONE_TAMPER : 
			logi("ZONE_TAMPER:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Tamper));
			break;	
		case Command.ZONE_TAMPER_RESTORE : 
			logi("ZONE_TAMPER_RESTORE:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Tamper_Restore));
			break;	
		case Command.ZONE_OPEN : 
			logd("ZONE_OPEN:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Open));
			break;	
		case Command.ZONE_FAULT : 
			logd("ZONE_FAULT:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Fault));
			break;
		case Command.ZONE_FAULT_RESTORE : 
			logi("ZONE_FAULT_RESTORE:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Fault_Restored));
			break;
		case Command.WIRE_SMOKE_ALARM : 
			logi("WIRE_SMOKE_ALARM:"+message);
			smokeEvent(new SmokeEvent(message,SmokeEvent.State.Alarm));
			break;
		case Command.WIRE_SMOKE_ALARM_RESTORE : 
			logi("WIRE_SMOKE_ALARM_RESTORE:"+message);
			smokeEvent(new SmokeEvent(message,SmokeEvent.State.Restore));
			break;
		case Command.PARTITION_BUSY : 
			logd("PARTITION_BUSY:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.BUSY));
			break;
		case Command.ZONE_RESTORED : 
			logd("ZONE_RESTORED:"+message);
			zoneEvent(new ZoneEvent(message,ZoneEvent.State.Restored));
			break;	
		case Command.ZONE_TIMER_DUMP : 
			//			The dump is a 256 character packed HEX string representing 64 UINT16 
			//			(little endian) zone timers. Zone timers count down from 0xFFFF (zone is 
			//			open) to 0x0000 (zone is closed too long ago to remember). Each “tick” of
			//			the zone time is actually 5 seconds so a zone timer of 0xFFFE means “5 
			//			seconds ago”. Remember, the zone timers are LITTLE ENDIAN so the 
			//			above example would be transmitted as FEFF.
			logd("ZONE_TIMER_DUMP:"+message);
			String zoneTicks;
			int zoneId = 1;
			ZoneEvent ze;
			for (int i = 0; i < 256; i+=4) {
				zoneTicks = message.getGeneralData().substring(i,i+4);
				if (zoneTicks.equals("FFFF")) {
					ze = new ZoneEvent(1,zoneId++,ZoneEvent.State.Open);
					ze.setZoneDate(new Date());
				}
				else {
					long seconds = hexToTicks(zoneTicks);
					ze = new ZoneEvent(1,zoneId++,ZoneEvent.State.Restored);
					// calculate the time the event occurred
					long timeMs = new Date().getTime()-(seconds*1000);
					ze.setZoneDate(new Date(timeMs));
				}
				zoneEvent(ze);
			}
			break;	
		case Command.PARTITION_READY : 
			logd("PARTITION_READY:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.READY));
			break;
		case Command.PARTITION_NOT_READY : 
			logd("PARTITION_NOT_READY:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.NOT_READY));
			break;
		case Command.PARTITION_ARMED : 
			logd("PARTITION_ARMED:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.ARMED));
			break;
		case Command.PARTITION_DISARMED : 
			logd("PARTITION_DISARMED:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.DISARMED));
			break;
		case Command.PARTITION_ALARM : 
			logd("PARTITION_ALARM:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.ALARM));
			break;
		case Command.EXIT_DELAY_IN_PROGRESS : 
			logd("EXIT_DELAY_IN_PROGRESS:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.EXIT_DELAY));
			break;
		case Command.PARTITION_FAILED_TO_ARM : 
			logd("PARTITION_FAILED_TO_ARM:"+message);
			partitionEvent(new PartitionEvent(message,PartitionEvent.State.FAILED_TO_ARM));
			break;
		case Command.CHIME_ENABLED : 
			logd("CHIME_ENABLED:"+message);
			chimeEvent(new ChimeEvent(message,ChimeEvent.State.ENABLED));
			break;
		case Command.CHIME_DISABLED : 
			logd("CHIME_DISABLED:"+message);
			chimeEvent(new ChimeEvent(message,ChimeEvent.State.DISABLED));
			break;
		case Command.INSTALLER_MODE : 
			logi("INSTALLER_MODE:The whole system is in installers mode. If you did not enter Installers through the TPI, you will be locked out of most options :"+message);
			infoEvent(new InfoEvent("INSTALLER_MODE:"+message));
			setInstallerMode(true);
			break;
		case Command.CMD_ERR : 
			logi("CMD_ERR:"+message);
			expectFound(message);
			errorEvent(new ErrorEvent(message,ErrorEvent.Type.CMD_ERR, ErrorEvent.Detail.GENERAL));
			break;
		case Command.SYSTEM_ERR : 
			switch (Integer.parseInt(message.getGeneralData())) {
			case 0 : 
				logi("Error - none");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.GENERAL));
				break;
			case 1 : 
				logi("Error - Receive Buffer Overrun (a command is received while another is still being processed)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.RECEIVE_BUFFER_OVERRUN));
				break;
			case 2 : 
				logi("Error - Receive Buffer Overflow");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.RECEIVE_BUFFER_OVERFLOW));
				break;	
			case 3 : 
				logi("Error - Transmit Buffer Overflow");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.TRANSMIT_BUFFER_OVERFLOW));
				runCommandFirst(lastCommand);
				break;	
			case 10 : 
				logi("Error - Keybus Transmit Buffer Overrun");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_TRANSMIT_BUFFER_OVERRUN));
				runCommandFirst(lastCommand);
				break;	
			case 11 : 
				logw("Error - Keybus Transmit Time Timeout");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_TRANSMIT_TIMEOUT));
				break;	
			case 12 : 
				logw("Error - Keybus Transmit Mode Timeout");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_TRANSMIT_MODE_TIMEOUT));
				break;
			case 13 : 
				logw("Error - Keybus Transmit Keystring Timeout");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_TRANSMIT_KEYSTRING_TIMEOUT));
				break;
			case 14 : 
				logw("Error - Keybus Interface Not Functioning (the TPI cannot communicate with the security system)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_NOT_FUNCTIONING));
				break;
			case 15 : 
				logw("Error - Keybus Busy (Attempting to Disarm or Arm with user code)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_BUSY));
				break;
			case 16 : 
				logw("Error - Keybus Busy – Lockout (The panel is currently in Keypad Lockout – too many disarm attempts)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_BUSY_LOCKOUT));
				break;
			case 17 : 
				logw("Error - Keybus Busy – Installers Mode (Panel is in installers mode, most functions are unavailable)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_BUSY_INSTALLERS_MODE));
				break;
			case 18 : 
				logw("Error - Keybus Busy – General Busy (The requested partition is busy)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.KEYBUS_BUSY));
				break;
			case 20 : 
				logw("Error - API Command Syntax Error");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_COMMAND_SYNTAX));
				break;
			case 21 : 
				logw("Error - API Command Partition Error (Requested Partition is out of bounds)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_COMMAND_PARTITION_ERROR));
				break;
			case 22 : 
				logw("Error - API Command Not Supported");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_COMMAND_NOT_SUPPORTED));
				break;
			case 23 : 
				logw("Error - API System Not Armed (sent in response to a disarm command)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_SYSTEM_NOT_ARMED));
				break;
			case 24 : 
				logw("Error - API System Not Ready to Arm (system is either not-secure, in exit-delay, or already armed)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_SYSTEM_NOT_READY_TO_ARM));
				break;
			case 25 : 
				logw("Error - API Command Invalid Length");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_COMMAND_INVALID_LENGTH));
				break;
			case 26 : 
				logw("Error - API User Code not Required");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_USER_CODE_NOT_REQUIRED));
				break;
			case 27 : 
				logw("Error - API Invalid Characters in Command (no alpha characters are allowed except for checksum)");
				errorEvent(new ErrorEvent(message,ErrorEvent.Type.SYSTEM_ERR, ErrorEvent.Detail.API_INVALID_CHARACTERS));
				break;
			default : logw("Unknown error receieved:"+message);
			}
			expectFound(message);
			break;
		case Command.INVALID_ACCESS_CODE :
			logw("INVALID_ACCESS_CODE:"+message);
			errorEvent(new ErrorEvent(message,ErrorEvent.Type.CMD_ERR, ErrorEvent.Detail.INVALID_ACCESS_CODE));
			break;
		case Command.FUNCTION_NOT_AVAILABLE :
			logw("FUNCTION_NOT_AVAILABLE:"+message);
			errorEvent(new ErrorEvent(message,ErrorEvent.Type.CMD_ERR, ErrorEvent.Detail.FUNCTION_NOT_AVAILABLE));
			break;
		case Command.FAILURE_TO_ARM :
			logw("FAILURE_TO_ARM:"+message);
			errorEvent(new ErrorEvent(message,ErrorEvent.Type.CMD_ERR, ErrorEvent.Detail.FAILURE_TO_ARM));
			break;
		case Command.CMD_ACK : 
			logd("CMD_ACK:"+message);
			expectFound(message);
			break;
		case Command.CMD_POLL : 
			logi("CMD_POLL:"+message);
			infoEvent(new InfoEvent("CMD_POLL:"+message));
			break;
		case Command.LOGIN_INTERACTION : 
			switch (Integer.parseInt(message.getGeneralData())) {
			case 0 : 
				logw("Login failed :"+message.getOriginalMessage());
				loggedIn = false;
				loginEvent(new LoginEvent(LoginEvent.State.FAILED));
				break;
			case 1 : 
				logi("Login success");
				loggedIn = true;
				loginEvent(new LoginEvent(LoginEvent.State.LOGGEDIN));
				break;				
			case 2 : 
				logw("Login timed out");
				loginEvent(new LoginEvent(LoginEvent.State.TIMEDOUT));
				break;
			case 3 : 
				logi("Login requested - sending creds");
				try {				
					loginEvent(new LoginEvent(LoginEvent.State.REQUESTING));
					Command command = new Command(Command.NETWORK_LOGIN,Command.CMD_ACK);
					command.setData(getPassword());
					connection.write(command.toString());
					expect(command);
				}
				catch (Throwable t) {
					loge(t.getMessage());
				}
				break;			
			default : logw("Unknown:"+message);
			}
			break;
		case Command.CODE_REQUIRED : 
			logi("CODE_REQUIRED:"+message);
			infoEvent(new InfoEvent("CODE_REQUIRED:"+message));
			//			Command command = new Command(Command.CODE_SEND,Command.CMD_ACK);
			//			command.setData(passcode);
			//			runCommandFirst(command);
			try {
				codeSend(getPasscode());
			} catch (EnvisaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;	
		case Command.INSTALLER_CODE_REQUIRED : 
			logi("INSTALLER_CODE_REQUIRED:"+message);
			infoEvent(new InfoEvent("INSTALLER_CODE_REQUIRED:"+message));
			Command c = new Command(Command.CODE_SEND,Command.CMD_ACK);
			c.setData(getPasscode());
			runCommandFirst(c);
			break;				
		default : loge(">>>>>>>>>>>>>>>>>>Unsupported message code:"+message.getCode()+" "+message);
		}
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

	public void tickEvents() {
		try {
			String messageString;
			do {
				messageString = connection.readLine();
				if (messageString!=null) {
					Message message = new Message(messageString);
					processMessage(message);
				}
				else {
					logd("No pending messages");
					pollCount++;
					// sleep(2000);
				}
			} while (messageString!=null);
		}
		catch (Throwable t) {
			t.printStackTrace();
			loge("Error:"+t.getMessage());
		}		
	}

	public void tickCommands() {
		if (loggedIn) {
			if (!isExpecting()) {
				if (commandStack.size()>0) {
					try {
						Command command = commandStack.removeFirst();
						lastCommand = command;
						logi("Executing command "+command.getCommand()+" "+command.getData());
						connection.write(command.toString());
						// check if # entered to cancel installer mode
						if (command.toString().indexOf('#')>=0)
							setInstallerMode(false);
						expect(command);
					} 
					catch (EnvisaException e) {
						e.printStackTrace();
					}
				}
				else {
					logd("No commands to run");
				}
			}
			else {
				logd("Waiting for previous command acknowledgment");
				sleep(500);
				expectingRetryCount++;
				if (expectingRetryCount>20) {
					expectNotFound();
				}
			}
		}
		else {
			logd("Waiting for login");
			// ensure our loop doesn't go mad
			sleep(500);
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

	private boolean isExpecting() {
		return expectingStack.size()>0;	
	}

	private void expect(Command command) throws EnvisaException {
		expectingStack.add(command);
	}

	private void expectFound(Message message)  {
		if (expectingStack.size()>0) {
			// now response is ready
			Command expectingCommand = expectingStack.removeFirst();
			if (Integer.toString(message.getCode()).equals(expectingCommand.getExpectStatus())) {
				logi("Command succeeded");
			}
			else {
				logw("Command failed "+message.getCode()+" "+message.getOriginalMessage());
			}
		}
	}

	private void expectNotFound() {
		logw("Command expect never received - abandoning : stack size="+expectingStack.size()+" expectingRetryCount="+expectingRetryCount);
		expectingRetryCount=0;
		expectingStack.removeFirst();
	}

	public void poll() throws EnvisaException {
		Command command = new Command(Command.POLL,Command.CMD_ACK);
		runCommand(command);
	}

	public void dumpZoneTimers() throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.DUMP_ZONE_TIMERS,Command.CMD_ACK);
		runCommand(command);
	}

	public void reboot() throws EnvisaException {
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(getUsername(), getPassword()));
		
		DefaultHttpClient http = new DefaultHttpClient();
		http.setCredentialsProvider(credProvider);
		HttpGet get = new HttpGet("http://"+getServer()+"/3?A=2");
		try {
			HttpResponse response = http.execute(get);
			Log.d(Session.LOG, "This is what we get back:"+response.getStatusLine().toString()+", "+response.getEntity().toString());
		}
		catch (Throwable t) {
			throw new EnvisaException(t.getMessage());
		}
	}

	public void setDateAndTime() throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.SET_TIME_DATE,Command.CMD_ACK);
		//hhmmMMDDYY
		SimpleDateFormat formatter = new SimpleDateFormat("hhmmMMddyy",Locale.getDefault());
		String dtStr = formatter.format(new Date());
		command.setData(dtStr);
		runCommand(command);
	}

	public void getStatusReport() throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.STATUS_REPORT,Command.CMD_ACK);
		runCommand(command);
	}

	public void arm(int partition) throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.PARTITION_ARM_CONTROL,Command.CMD_ACK);
		command.setData(Integer.toString(partition));
		runCommand(command);	
	}

	public void armStay(int partition) throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.PARTITION_ARM_CONTROL_STAY,Command.CMD_ACK);
		command.setData(Integer.toString(partition));
		runCommand(command);
	}

	public void armWithCode(int partition) throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.PARTITION_ARM_CONTROL_WITH_CODE,Command.CMD_ACK);
		command.setData(partition+getPasscode());
		runCommand(command);
	}

	public void disarm(int partition) throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.PARTITION_DISARM_CONTROL,Command.CMD_ACK);
		command.setData(partition+getPasscode());
		runCommand(command);
	}

	private void checkLogin() throws EnvisaException {
		if (!this.loggedIn) {
			throw new EnvisaException("Not logged in");
		}
	}

	public void toggleChime(int partition) throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+"*4");
		runCommand(command);
	}

	public void bypass(int partition, int zone) throws EnvisaException {
		bypass(partition, new int[]{zone});
	}

	private String padInt(int i) {
		// pad with leading zero
		String str = "0"+i;
		str = str.substring(str.length()-2);
		return str;
	}

	public void bypass(int partition, int[] zone) throws EnvisaException {
		checkLogin();
		Command command= new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+"*1"+getPasscode());
		runCommand(command);

		for (int i = 0; i < zone.length; i++) {
			// pad with leading zero
			String zoneStr = padInt(zone[i]);
			command= new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
			command.setData(partition+zoneStr);
			runCommand(command);
		}

		command= new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+"#");
		runCommand(command);
	}

	public void clearBypass(int partition, int zone) throws EnvisaException {
		checkLogin();
		Command command= new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+"*1"+zone+getPasscode());
		runCommand(command);

		command= new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+"00#");
		runCommand(command);
	}

	private void codeSend(String code) throws EnvisaException {
		Command command= new Command(Command.CODE_SEND,Command.CMD_ACK);
		command.setData(code);
		runCommand(command);
	}

	public void testSystem(String partition) throws EnvisaException {
		Command command = new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+"*6");
		runCommand(command);

		command = new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+getPasscode());
		runCommand(command);

		command = new Command(Command.SEND_KEYSTROKE_STRING,Command.CMD_ACK);
		command.setData(partition+"4#");
		runCommand(command);
	}

	public void sendTemperatureBroadcast(boolean on) throws EnvisaException {
		checkLogin();
		Command command = new Command(Command.TEMPERATURE_BROADCAST_CONTROL,Command.CMD_ACK);
		if (on)
			command.setData("1");
		else
			command.setData("0");
		runCommand(command);
	}


	public void runCommandFirst(Command command) {
		pollCount=0;
		commandStack.add(0,command);
	}

	public void runCommand(Command command) throws EnvisaException {
		if (!isConnected()) {
			throw new EnvisaException("Not connected");
		}
		pollCount=0;
		commandStack.add(command);
	}

	public boolean commandQueued() {
		return commandStack.size()>0;
	}

	private String convertToBinaryString(String data) {
		String value1 = Integer.toBinaryString(Integer.parseInt(data, 16));
		if (value1.length() == 1) {
			value1 = "000" + value1;
		}
		else if (value1.length() == 2) {
			value1 = "00" + value1;
		}
		else if (value1.length() == 3) {
			value1 = "0" + value1;
		}

		return value1;
	}

	public static long hexToTicks(String zone) {
		long ticks = 5*(Long.decode("0xFFFF")-Long.decode("0x"+zone.substring(2)+zone.substring(0,2)));
		//		System.out.println("A>>>>>>>>>>>"+Long.decode("0xFFFF"));
		//		System.out.println("B>>>>>>>>>>>"+Long.decode("0x"+zone.substring(2)+zone.substring(0,2)));
		//		System.out.println(">>>>>>>>>>>"+zone.substring(2));
		//		System.out.println(">>>>>>>>>>>"+zone.substring(0,2));
		//		System.out.println(">>>>>>>>>>>"+ticks);
		return ticks;
	}


}

