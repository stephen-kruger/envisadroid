package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;

import com.madibasoft.envisadroid.api.Session;
import com.madibasoft.envisadroid.api.tpi.TPISession;

public class PanelModeEvent extends GenericEvent {

	public Session.Mode mode;

	public PanelModeEvent(Session.Mode m) {
		this.mode = m;
	}
	
	public PanelModeEvent(JSONObject jo) throws JSONException {
		fromJSON(jo);
	}

	public TPISession.Mode getMode() {
		return mode;
	}

	public void setMode(TPISession.Mode mode) {
		this.mode = mode;
	}


	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("mode", getMode().name());
		json.put("eventClass", getClass().getName());
		return json;
	}

	@Override
	public void fromJSON(JSONObject json) throws JSONException {
		setMode(TPISession.Mode.valueOf(json.getString("mode")));		
	}

}
