package com.madibasoft.envisadroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.madibasoft.envisadroid.api.Session;

public class EnvisaHandler extends Handler {
	private Session session;

	private EnvisaHandler() {
		final Message message = this.obtainMessage();
		message.getData().putString("msg", "tick");
		postDelayed(new Runnable() {

			
			public void run() {
				sendMessage(message);
			}

		}, 1000);
	}

	
	public void handleMessage(final Message msg) {
		super.handleMessage(msg);
		Bundle bundle = msg.getData();
		if (bundle.containsKey("msg")) {
			if (bundle.getString("msg").equals("tick")) {
				final Message message = obtainMessage();
				message.getData().putString("msg", "tick");
				postDelayed(new Runnable() {

					
					public void run() {
						sendMessage(message);
					}

				}, 1000);
			}
		}
	}

	public Session getSession() {
		return session;
	}

}
