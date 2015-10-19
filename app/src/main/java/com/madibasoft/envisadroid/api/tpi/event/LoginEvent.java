package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginEvent extends GenericEvent {
	public enum State {FAILED, TIMEDOUT, LOGGEDIN, LOGGEDOUT, REQUESTING, CONNECTION_FAIL}

	private State state;

	public LoginEvent(State s) {
		setState(s);
	}
	
	public LoginEvent(JSONObject jo) throws JSONException {
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
