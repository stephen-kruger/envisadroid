package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;

import com.madibasoft.envisadroid.api.tpi.Message;

public class OpenEvent extends GenericEvent {
	public enum Type {User, Special}

	private int partition;
	private Type type;

	public OpenEvent(Message m, Type s) {
		setPartition(m.getPartition());
		setType(s);
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type Type) {
		this.type = Type;
	}
	
	public String toString() {
		return "Open Event Partition="+getPartition()+" Type="+getType().name();
	}
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("partition", getPartition());
		json.put("type", getType().name());
		json.put("eventClass", getClass().getName());
		return json;
	}
	
	@Override
	public void fromJSON(JSONObject json) throws JSONException {
		setPartition(json.getInt("partition"));
		setType(Type.valueOf(json.getString("type")));
	}
}
