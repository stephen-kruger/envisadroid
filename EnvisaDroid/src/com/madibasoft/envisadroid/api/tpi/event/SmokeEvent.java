package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;

import com.madibasoft.envisadroid.api.tpi.Message;

public class SmokeEvent extends GenericEvent {
	public enum State {Alarm,Restore};
	private State state;

	public SmokeEvent(Message m, State s) {
		setState(s);
	}
	
	public SmokeEvent(JSONObject jo) throws JSONException {
		super(jo);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	public String toString() {
		return getState().name();
	}
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("state", getState().name());
		json.put("eventClass", getClass().getName());
		return json;
	}
	
	@Override
	public void fromJSON(JSONObject json) throws JSONException {
		setState(State.valueOf(json.getString("state")));
	}
}
