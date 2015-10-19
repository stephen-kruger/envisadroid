package com.madibasoft.envisadroid.log;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

public class LogTimer extends CountDownTimer {

	private TextView logText;

	public LogTimer(TextView logText,long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.logText = logText;
	}
	
	@Override
	public void onFinish() {
		logText.setVisibility(View.VISIBLE);		
		logText.setText("");
	}

	@Override
	public void onTick(long millisUntilFinished) {
		logText.setVisibility(View.VISIBLE);		
	}

}
