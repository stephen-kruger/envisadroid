package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;


public class InfoEvent extends GenericEvent {

	private String message;


	public InfoEvent(String message) {
		setMessage(message);
	}

	public InfoEvent(JSONObject jo) throws JSONException {
		super(jo);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String toString() {
		return getMessage();
	}
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("message", getMessage());
		json.put("eventClass", getClass().getName());
		return json;
	}
	
	@Override
	public void fromJSON(JSONObject json) throws JSONException {
		setMessage(json.getString("message"));
	}


}
