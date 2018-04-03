package com.ragentek.ypush.service;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.util.Log;


public class YPushServerSettingReceiver extends BroadcastReceiver {
    private static final String TAG = "YPushServerSettingReceiver";

    private static final String DATA_COLLECTION_REG_SETTING_ACTION =
            "android.provider.Telephony.SECRET_CODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive start");
        Log.i(TAG, "action = " + intent.getAction());

        if (intent.getAction().equals(DATA_COLLECTION_REG_SETTING_ACTION)) {
            Intent localIntent = new Intent(context, MainActivity.class);
            localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(localIntent);
        }
    }
}

