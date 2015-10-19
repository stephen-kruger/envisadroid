package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class GenericEvent {
	
	private int id;
	
	protected GenericEvent() {
		
	}
	
	public GenericEvent(JSONObject jo) throws JSONException {
		fromJSON(jo);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public abstract JSONObject toJSON() throws JSONException;

	public abstract void fromJSON(JSONObject json) throws JSONException;

}
