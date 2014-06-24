package com.relayr.core.activity;

import com.relayr.Relayr_Application;
import com.relayr.Relayr_SDK;
import com.relayr.core.settings.Relayr_ActivityStack;
import com.relayr.core.settings.Relayr_SDKStatus;

import android.app.Activity;

public class Relayr_Activity extends Activity {

	@Override
	protected void onResume() {
		super.onResume();
		Relayr_Application.setCurrentActivity(Relayr_Activity.this);
		try {
			Relayr_SDK.init();
			Relayr_ActivityStack.addStackCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (Relayr_SDKStatus.isActive()) {
			Relayr_ActivityStack.eraseStackCall();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (Relayr_SDKStatus.isActive()) {
			if (Relayr_ActivityStack.isCallStackEmpty()) {
				Relayr_SDK.stop();
			}
		}
	}
}