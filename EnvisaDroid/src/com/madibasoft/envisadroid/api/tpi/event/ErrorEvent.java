package com.madibasoft.envisadroid.api.tpi.event;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.madibasoft.envisadroid.api.tpi.Message;

public class ErrorEvent extends GenericEvent {
	public enum Type {CMD_ERR,SYSTEM_ERR};
	public enum Detail {
		GENERAL, RECEIVE_BUFFER_OVERRUN, RECEIVE_BUFFER_OVERFLOW, 
		TRANSMIT_BUFFER_OVERFLOW, KEYBUS_TRANSMIT_BUFFER_OVERRUN, KEYBUS_TRANSMIT_TIMEOUT, 
		KEYBUS_TRANSMIT_MODE_TIMEOUT, KEYBUS_TRANSMIT_KEYSTRING_TIMEOUT, KEYBUS_NOT_FUNCTIONING, 
		KEYBUS_BUSY, KEYBUS_BUSY_LOCKOUT, KEYBUS_BUSY_INSTALLERS_MODE, API_COMMAND_SYNTAX, 
		API_COMMAND_PARTITION_ERROR, API_COMMAND_NOT_SUPPORTED, API_SYSTEM_NOT_ARMED, 
		API_SYSTEM_NOT_READY_TO_ARM, API_COMMAND_INVALID_LENGTH, API_USER_CODE_NOT_REQUIRED, API_INVALID_CHARACTERS, INVALID_ACCESS_CODE, FAILURE_TO_ARM, FUNCTION_NOT_AVAILABLE};
		private Type type;
		private Detail detail;

		public ErrorEvent(Message m, Type s, Detail detail) {
			setType(s);
			setDetail(detail);
		}

		public ErrorEvent(JSONObject jo) throws JSONException {
			super(jo);
		}

		public Type getType() {
			return type;
		}

		public void setType(Type state) {
			this.type = state;
		}

		public Detail getDetail() {
			return detail;
		}

		public void setDetail(Detail detail) {
			this.detail = detail;
		}

		public String toString() {
			return toCamelCase(getType().name()+": "+getDetail().name());
		}

		static String toCamelCase(String s){
			String[] parts = s.split("_");
			String camelCaseString = "";
			for (String part : parts){
				camelCaseString = camelCaseString + ' '+ toProperCase(part);
			}
			return camelCaseString;
		}

		static String toProperCase(String s) {
			Locale l = Locale.getDefault();
			return s.substring(0, 1).toUpperCase(l) + s.substring(1).toLowerCase(l);
		}
		
		@Override
		public JSONObject toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("type", getType().name());
			json.put("detail", getDetail().name());
			json.put("eventClass", getClass().getName());
			return json;
		}
		
		@Override
		public void fromJSON(JSONObject json) throws JSONException {
			setType(Type.valueOf(json.getString("type")));
			setDetail(Detail.valueOf(json.getString("detail")));
		}
}
