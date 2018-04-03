package com.ragentek.ypush.service.listen;

import com.ragentek.ypush.service.YPushService;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;


public class ListenNetStateService extends Service{
	
	private static final String TAG = "YPushService.ListenNetStateService";

	private ConnectivityManager connectivityManager;
    private NetworkInfo info;
    private boolean isCreatedInstances = true;
    private BroadcastReceiver netReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	System.out.println("-------------------------");
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                connectivityManager = (ConnectivityManager)  getSystemService(Context.CONNECTIVITY_SERVICE);
                info = connectivityManager.getActiveNetworkInfo();  
                if(info != null && info.isAvailable()) {
                    String name = info.getTypeName();

                    if(isCreatedInstances){
                    	Intent serviceIntent = new Intent(context, YPushService.class);  
                        context.startService(serviceIntent);  
                        isCreatedInstances = false;
                    }
                    
                } else {

                    if(!isCreatedInstances){
                    	Intent serviceIntent = new Intent(context, YPushService.class);  
                        context.stopService(serviceIntent);
                    }                    
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netReceiver, mFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netReceiver);
    }


	@SuppressLint("NewApi")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);
    }
}
