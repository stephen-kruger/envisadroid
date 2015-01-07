package com.madibasoft.envisadroid.api.tpi.event;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.madibasoft.envisadroid.api.tpi.Message;

public class ZoneEvent extends GenericEvent implements Comparable<ZoneEvent> {
	public enum State {Restored, Fault_Restored, Open, Fault, Tamper, Tamper_Restore, Alarm, Alarm_Restore};
	private int partition, zone;
	private State state;
	private Date zoneDate, eventDate;

	private ZoneEvent() {
		setZoneDate(new Date());
		setEventDate(new Date());
	}
	
	public ZoneEvent(Message m, State s) {
		this();
		setPartition(m.getPartition());
		setZone(m.getZone());
		setState(s);
	}

	public ZoneEvent(int partition, int zone, State s) {
		this();
		setPartition(partition);
		setZone(zone);
		setState(s);
	}

	public ZoneEvent(JSONObject jo) throws JSONException {
		super(jo);
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
		if (this.partition<=0)
			this.partition=1;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String toString() {
		return getZone()+" "+getState().name()+" "+getZoneDate();
	}


	public Date getZoneDate() {
		return zoneDate;
	}

	public void setZoneDate(Date zoneDate) {
		this.zoneDate = zoneDate;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	
	
	public boolean equals(Object o) {
		if (o instanceof ZoneEvent) {
			ZoneEvent z = (ZoneEvent)o;
			return ((z.getPartition()==getPartition())&&(z.getZone()==getZone()));
		}
		return false;
	}

	
	public int compareTo(ZoneEvent object) {
		if (object instanceof ZoneEvent)
			return Integer.valueOf(getZone()).compareTo(Integer.valueOf(((ZoneEvent)object).getZone()));
		return 0;
	}

	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("partition", getPartition());
		json.put("zone", getZone());
		json.put("zoneDate", getZoneDate().getTime());
		json.put("eventDate", getEventDate().getTime());
		json.put("state", getState().name());
		json.put("eventClass", getClass().getName());
		return json;
	}
	
	
	public void fromJSON(JSONObject json) throws JSONException {
		setPartition(json.getInt("partition"));
		setZone(json.getInt("zone"));
		setZoneDate(new Date(json.getLong("zoneDate")));
		setEventDate(new Date(json.getLong("eventDate")));
		setState(State.valueOf(json.getString("state")));
	}


}
