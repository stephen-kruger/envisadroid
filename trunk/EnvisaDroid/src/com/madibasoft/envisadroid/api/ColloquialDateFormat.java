package com.madibasoft.envisadroid.api;

import android.annotation.SuppressLint;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;


@SuppressLint("SimpleDateFormat")
public class ColloquialDateFormat extends SimpleDateFormat {
	private static final long serialVersionUID = -5399130227441764013L;
	
	public ColloquialDateFormat() {
	}

	public String dateToCooloquial(Date created) {
		// today
		Date today = new Date();

		// how much time since (ms)
		Long duration = today.getTime() - created.getTime();

		int second = 1000;
		int minute = second * 60;
		int hour = minute * 60;
		int day = hour * 24;

		if (duration < second * 7) {
			return "right now";
		}

		if (duration < minute) {
			int n = (int) Math.floor(duration / second);
			return n+" seconds ago";
		}

		if (duration < minute * 2) {
			return "about 1 minute ago";
		}

		if (duration < hour) {
			int n = (int) Math.floor(duration / minute);
			return n+" minutes ago";
		}

		if (duration < hour * 2) {
			return "about 1 hour ago";
		}

		if (duration < day) {
			int n = (int) Math.floor(duration / hour);
			return n+" hours ago";
		}
		if (duration > day && duration < day * 2) {
			return "yesterday at "+ created;
		}

		if (duration < day * 365) {
			int n = (int) Math.floor(duration / day);
			return n+" days ago";
		} else {
			return "on "+created;
		}
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
		// custom implementation to format the date and time based on our TimeZone            
		toAppendTo.insert(pos.getBeginIndex(), dateToCooloquial(date));
		return toAppendTo; 
	}

}
