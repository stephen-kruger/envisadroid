package com.madibasoft.envisadroid.log;

public class Log {
	private long id;
	private String log;
	private long date;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String comment) {
		this.log = comment;
	}
	
	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return log;
	}
} 
