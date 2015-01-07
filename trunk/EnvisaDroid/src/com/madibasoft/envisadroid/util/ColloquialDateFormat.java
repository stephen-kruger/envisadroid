package com.madibasoft.envisadroid.util;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

import com.madibasoft.envisadroid.R;

@SuppressLint("SimpleDateFormat")
public class ColloquialDateFormat extends SimpleDateFormat {
	private static final long serialVersionUID = -5399130227441764013L;
	private Context c;
	
	public ColloquialDateFormat(Context c) {
		this.c = c;
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
			return c.getString(R.string.time_now);//"right now";
		}

		if (duration < minute) {
			int n = (int) Math.floor(duration / second);
			return MessageFormat.format(c.getString(R.string.time_seconds), n);
		}

		if (duration < minute * 2) {
			return c.getString(R.string.time_minute);//"about 1 minute ago";
		}

		if (duration < hour) {
			int n = (int) Math.floor(duration / minute);
			return MessageFormat.format(c.getString(R.string.time_minutes), n);
		}

		if (duration < hour * 2) {
			return c.getString(R.string.time_hour);//"about 1 hour ago";
		}

		if (duration < day) {
			int n = (int) Math.floor(duration / hour);
			return MessageFormat.format(c.getString(R.string.time_hours), n);
		}
		if (duration > day && duration < day * 2) {
			return MessageFormat.format(c.getString(R.string.time_yesterday), created);
		}

		if (duration < day * 365) {
			int n = (int) Math.floor(duration / day);
			return MessageFormat.format(c.getString(R.string.time_days), n);
		} else {
			return MessageFormat.format(c.getString(R.string.time_on), created);
		}
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
		// custom implementation to format the date and time based on our TimeZone            
		toAppendTo.insert(pos.getBeginIndex(), dateToCooloquial(date));
		return toAppendTo; 
	}

}
