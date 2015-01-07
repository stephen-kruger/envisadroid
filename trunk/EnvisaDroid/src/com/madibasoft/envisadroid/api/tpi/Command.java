package com.madibasoft.envisadroid.api.tpi;

import java.util.Locale;


public class Command {
	public static final String POLL = "000";
	public static final String STATUS_REPORT = "001";
	public static final String NETWORK_LOGIN = "005";
	public static final String DUMP_ZONE_TIMERS = "008";
	public static final String SET_TIME_DATE = "010";
	public static final String COMMAND_OUTPUT_CONTROL = "020";
	public static final String PARTITION_ARM_CONTROL = "030";
	public static final String PARTITION_ARM_CONTROL_STAY = "031";
	public static final String PARTITION_ARM_CONTROL_ZERO_DELAY = "032";
	public static final String PARTITION_ARM_CONTROL_WITH_CODE = "033";
	public static final String PARTITION_DISARM_CONTROL = "040";
	public static final String TEMPERATURE_BROADCAST_CONTROL = "057";
	public static final String TRIGGER_PANIC_ALARM = "060";
	public static final String SEND_SINGLE_KEYSTROKE = "070";
	public static final String SEND_KEYSTROKE_STRING = "071";
	public static final String KEEP_ALIVE = "074";
	public static final String CODE_SEND = "200";

	public static final int CMD_POLL = 0;
	public static final int CMD_ACK = 500;
	public static final int CMD_ERR = 501;
	public static final int SYSTEM_ERR = 502;
	public static final int LOGIN_INTERACTION = 505;
	public static final int KEYPAD_LED_STATE = 510;
	public static final int KEYPAD_LEDFLASH_STATE = 511;
	public static final int TIME_DATE_BROADCAST = 550;
	public static final int ZONE_ALARM = 601;
	public static final int ZONE_ALARM_RESTORE = 602;
	public static final int ZONE_TAMPER = 603;
	public static final int ZONE_TAMPER_RESTORE = 604;
	public static final int ZONE_FAULT = 605;
	public static final int ZONE_FAULT_RESTORE = 606;
	public static final int ZONE_OPEN = 609;
	public static final int ZONE_RESTORED = 610;
	public static final int ZONE_TIMER_DUMP = 615;
	public static final int WIRE_SMOKE_ALARM = 631;
	public static final int WIRE_SMOKE_ALARM_RESTORE = 632;
	public static final int PARTITION_READY = 650;
	public static final int PARTITION_NOT_READY = 651;
	public static final int PARTITION_ARMED = 652;
	public static final int PARTITION_DISARMED = 655;
	public static final int PARTITION_ALARM = 654;
	public static final int EXIT_DELAY_IN_PROGRESS = 656;
	public static final int PARTITION_FAILED_TO_ARM = 659;
	public static final int CHIME_ENABLED = 663;
	public static final int CHIME_DISABLED = 664;
	public static final int INVALID_ACCESS_CODE = 670;
	public static final int FUNCTION_NOT_AVAILABLE = 671;
	public static final int FAILURE_TO_ARM = 672;
	public static final int PARTITION_BUSY = 673;
	public static final int INSTALLER_MODE = 680;
	public static final int TROUBLE_LED_ON = 840;
	public static final int TROUBLE_LED_OFF = 841;
	public static final int CODE_REQUIRED = 900;
	public static final int COMMAND_OUTPUT_PRESSED = 912;
	public static final int MASTER_CODE_REQUIRED = 921;
	public static final int INSTALLER_CODE_REQUIRED = 922;

	private String commandString = new String("");
	private String dataString = new String("");
	private String checksumString = new String("");
	private String terminationString = new String("");
	private String expectStatus;
	
	public Command(String commandString, int expectStatus) {
		terminationString = "\r\n";
		setCommand(commandString);
		this.expectStatus = Integer.toString(expectStatus);
	}


	private String getCompleteCommand() {
		recalculateChecksum();
		StringBuffer sb = new StringBuffer();

		sb.append(commandString);
		sb.append(dataString);
		sb.append(checksumString);
		sb.append(terminationString);

		return sb.toString();
	}

	public String toString() {
		return getCompleteCommand();
	}

	public void setCommand(String commandString) {
		this.commandString = commandString;
		recalculateChecksum();
	}
	
	public String getCommand() {
		return commandString;
	}

	public void setData(String dataString) {
		this.dataString = dataString;
		recalculateChecksum();
	}
	
	public String getData() {
		return dataString;
	}
	
	public String getExpectStatus() {
		return expectStatus;
	}
	
	public int getExpectStatusCode() {
		return Integer.parseInt(expectStatus);
	}

	private void recalculateChecksum() {
		int checkSum;
		int runningTotal = 0;
		checksumString = "ZZ";

		for(byte s : commandString.getBytes()) {
			runningTotal = s + runningTotal;
		}
		for(byte s : dataString.getBytes()) {
			runningTotal = s + runningTotal;
		}

		checkSum = runningTotal;
		String hexCheckSum = Integer.toHexString(checkSum);
		hexCheckSum = hexCheckSum.substring(hexCheckSum.length() - 2).toUpperCase(Locale.getDefault());

		checksumString = hexCheckSum;
	}
}
