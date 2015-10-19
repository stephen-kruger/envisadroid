package com.madibasoft.envisadroid.api;

public class EnvisaException extends Exception {

	private static final long serialVersionUID = 29382;

	private String message;
	private Exception sourceException;

	public EnvisaException(Exception sourceException, String message) {
		super(message);
		this.sourceException = sourceException;
		this.message = message;
	}

	public EnvisaException(String message) {
		super(message);
		this.message = message;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Exception Message: \n");
		sb.append(message + "\n");

		if (sourceException != null) {
			sb.append("Source Exception Message:\n");
			sb.append(sourceException.toString());
		}
		else {
			sb.append("Stack Trace:\n");
			sb.append("No source exception/stack trace.");
		}

		return sb.toString();
	}
}

