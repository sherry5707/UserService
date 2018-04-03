package com.ragentek.ypush.service;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.util.Log;


public class YPushServerPushReceiver extends BroadcastReceiver {
    private static final String TAG = "yy";
	private final static String KEY_ALARM_FROM = "from";
	private final static String FROM_ALARM_PUSH = "alarm_push";
	private final static String FROM_ALARM_PULL = "alarm_pull";
	
    private static final String DATA_COLLECTION_REG_SETTING_ACTION =
            "android.provider.Telephony.SECRET_CODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive start");
        Log.i(TAG, "action = " + intent.getAction());
	 String action = intent.getAction();    
        if (intent.getAction().equals(DATA_COLLECTION_REG_SETTING_ACTION)) {
            Intent newServiceIntent = new Intent(context,NewPushService.class);
			newServiceIntent.setAction(action);
			newServiceIntent.putExtra(KEY_ALARM_FROM, FROM_ALARM_PUSH);
			context.startService(newServiceIntent);
        }
    }
}

