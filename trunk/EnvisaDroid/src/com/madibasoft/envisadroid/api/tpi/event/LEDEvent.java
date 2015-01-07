package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;


public class LEDEvent extends GenericEvent {
	public enum Function {BACKLIGHT, FIRE, PROGRAM, TROUBLE, BYPASS, MEMORY, ARMED, READY};
	public enum State {ON, OFF, FLASH};
	private Function function;
	private State state;

	public LEDEvent(Function f, State s) {
		setFunction(f);
		setState(s);
	}
	
	public LEDEvent(JSONObject jo) throws JSONException {
		super(jo);
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function f) {
		this.function = f;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String toString() {
		return getFunction().name()+" "+getState().name();
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("function", getFunction().name());
		json.put("state", getState().name());
		json.put("eventClass", getClass().getName());
		return json;
	}
	
	@Override
	public void fromJSON(JSONObject json) throws JSONException {
		setFunction(Function.valueOf(json.getString("function")));
		setState(State.valueOf(json.getString("state")));
	}
	
}
