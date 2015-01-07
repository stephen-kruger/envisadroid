package com.madibasoft.envisadroid.api.tpi.event;

import org.json.JSONException;
import org.json.JSONObject;

import com.madibasoft.envisadroid.api.tpi.Message;

public class PartitionEvent extends GenericEvent {
	public enum State {READY, NOT_READY, ARMED, DISARMED, ALARM, EXIT_DELAY, ENTRY_DELAY, BUSY, FAILED_TO_ARM};
	private int partition;
	private State state;

	public PartitionEvent(Message m, State s) {
		setPartition(m.getPartition());
		setState(s);
	}
	
	public PartitionEvent(JSONObject jo) throws JSONException {
		super(jo);
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	public String toString() {
		return getPartition()+" "+getState().name();
	}
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("partition", getPartition());
		json.put("state", getState().name());
		json.put("eventClass", getClass().getName());
		return json;
	}
	
	@Override
	public void fromJSON(JSONObject json) throws JSONException {
		setPartition(json.getInt("partition"));
		setState(State.valueOf(json.getString("state")));
	}
}
