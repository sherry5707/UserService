package com.ragentek.ypush.service;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import java.util.List;

import com.ragentek.ypush.service.util.CommonUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class YPushServiceReceiver extends BroadcastReceiver {  
	
	private static final String TAG = "yy";
	
//	private static String sLastNetWorkName = "";
	
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	final String action = intent.getAction();     
        Log.e(TAG, "onReceive action=" + action);
        //del by zhengguang.yang@20160225 start for replace YPushService to NewPushService
    	//Intent serviceIntent = new Intent(context, YPushService.class);
    	//serviceIntent.setAction(action);
        //context.startService(serviceIntent);     
        //del by zhengguang.yang end
        
        //add by zhengguang.yang@20160122 start for new push service
//        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
//        	long firstBootTimeStamp = CommonUtils.getFirstBootTimeStamp(context);
//        	Log.i(TAG,"YPushServiceReceiver-->ACTION_BOOT_COMPLETED firstBootTimeStamp="+firstBootTimeStamp);
//        	if(firstBootTimeStamp == 0){
//        		CommonUtils.setFirstBootTimeStamp(context, System.currentTimeMillis());
//        	}
//        }
        
        Intent newServiceIntent = new Intent(context,NewPushService.class);
        newServiceIntent.setAction(action);
        context.startService(newServiceIntent);
        //add by zhengguang.yang end
        
        /*
        boolean serviceRunningFlag = false;
        serviceRunningFlag = isMyServiceRun(context);
		Log.d(TAG, "serviceRunningFlag = " + serviceRunningFlag);
        
		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			Log.d(TAG, "BOOT_COMPLETED");
			
			ConnectivityManager connectivityManager;
            NetworkInfo info;
            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            info = connectivityManager.getActiveNetworkInfo();  
            
            if(info != null && info.isAvailable()) {
            	Intent serviceIntent = new Intent(context, YPushService.class);  
            	Log.d(TAG, "BOOT_COMPLETED: startService ");
                context.startService(serviceIntent); 
            }
            
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d(TAG, "CONNECTIVITY_ACTION");
            
            ConnectivityManager connectivityManager;
            NetworkInfo info;
            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            info = connectivityManager.getActiveNetworkInfo();  
            
            if(info != null && info.isAvailable()) {
                String name = info.getTypeName();
                Log.d(TAG, "network name ��ǰ������ƣ�" + name); 
                Log.d(TAG, "sLastNetWorkName=" + sLastNetWorkName);
                
                if (sLastNetWorkName == null || "".equals(sLastNetWorkName)) {
                	Intent serviceIntent = new Intent(context, YPushService.class);  
	                context.startService(serviceIntent);
                } else {
                	if (sLastNetWorkName.equals(name)) {
                		//if (!serviceRunningFlag) {
	    	            	Intent serviceIntent = new Intent(context, YPushService.class);  
	    	                context.startService(serviceIntent);
	            		//}
                	} else {
                		//restart service
                		Intent serviceIntent = new Intent(context, YPushService.class); 
                        context.stopService(serviceIntent); 
                        
                        Log.e(TAG, "netork changed,restart service");
                        
                        Intent restartServiceIntent = new Intent(context, YPushService.class);  
    	                context.startService(restartServiceIntent);
                	}
                }
                
                sLastNetWorkName = name;//save last net work

            } else {
            	Log.d(TAG, "û�п�������,ֹͣ����");
            	Intent serviceIntent = new Intent(context, YPushService.class); 
                context.stopService(serviceIntent);                           
            } 
            
        } else if (action.equals("android.intent.action.PACKAGE_REPLACED")){
        	Log.d(TAG, "PACKAGE_REPLACED"); 
        	
	        final boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);  
	        final String packageName = intent.getData().getSchemeSpecificPart();
	        Log.d(TAG, "replace=" + replace + " ,packageName=" + packageName);
	        
	        // do some thing you want.  
	    	if (packageName.equals("com.ragentek.ypush.service")) {    		
	    		ConnectivityManager connectivityManager;
	            NetworkInfo info;
	            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	            info = connectivityManager.getActiveNetworkInfo();  
	            
	            if(info != null && info.isAvailable()) {
	            	String name = info.getTypeName();
	            	Log.d(TAG, "network name=" + name); 
	            	
	            	//if (!serviceRunningFlag) {
		    			Intent serviceIntent = new Intent(context, YPushService.class);  
			            Log.d(TAG, "PACKAGE_REPLACED: serviceIntent = " + serviceIntent);
		                context.startService(serviceIntent); 
		    		//}
	            }
	        }
	    	
	    } else if (action.equals("android.intent.action.PACKAGE_ADDED")){
        	Log.d(TAG, "PACKAGE_ADDED"); 
        	
	        final boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);  
	        final String packageName = intent.getData().getSchemeSpecificPart();
	        Log.d(TAG, "replace=" + replace + " ,packageName=" + packageName);
	        
	        // do some thing you want.  
	    	if (packageName.equals("com.ragentek.ypush.service")) {
	    		ConnectivityManager connectivityManager;
	            NetworkInfo info;
	            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	            info = connectivityManager.getActiveNetworkInfo();  
	            
	            if(info != null && info.isAvailable()) {
	            	String name = info.getTypeName();
	            	Log.d(TAG, "network name=" + name); 
	            	
	            	//if (!serviceRunningFlag) {
		    			Intent serviceIntent = new Intent(context, YPushService.class);  
			            Log.d(TAG, "PACKAGE_ADDED: serviceIntent = " + serviceIntent);
		                context.startService(serviceIntent); 
		    		//}
	            }
	        }
	    	
	    } else if(action.equals("android.intent.action.TIME_TICK")){
        	Log.d(TAG, "TIME_TICK"); 

	        // do some thing you want.  
    		if (!serviceRunningFlag) {
    			ConnectivityManager connectivityManager;
                NetworkInfo info;
    			connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                info = connectivityManager.getActiveNetworkInfo();  
                
                if(info != null && info.isAvailable()) {
                	String name = info.getTypeName();
                    Log.d(TAG, "��ǰ������ƣ�" + name); 
                    
                    Intent serviceIntent = new Intent(context, YPushService.class);  
	                context.startService(serviceIntent); 
                }	
    		}
	    } else if(action.equals("android.intent.action.assist")){
        	Log.d(TAG, "assist: APKAutoRunReceiver");

            boolean replace = intent.getBooleanExtra("replace", false);  
            final String packageName = intent.getPackage();
            Log.d(TAG, "replace=" + replace + " packageName=" + packageName);
            // do some thing you want.  

        	if (packageName.equals("com.ragentek.ypush.service")) {
	        	Intent intent1 = new Intent();
            	PackageManager pm = context.getPackageManager();
            	intent1 = pm.getLaunchIntentForPackage(packageName);
            	Log.d(TAG, "intent1 = " + intent1);
            	
	            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            context.startService(intent1); 
	        }
        }
        */
        
    }
    
    private boolean isMyServiceRun(Context context) {
    	Log.d(TAG, "isMyServiceRun start");
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> list = am.getRunningServices(30);
        
        for (RunningServiceInfo info : list) {
        	Log.d(TAG, "info.service.getClassName()=" + info.service.getClassName());
            if(info.service.getClassName().equals("com.ragentek.ypush.service.YPushService")) {
                 return true;
            }
        }
        
        Log.d(TAG, "isMyServiceRun end");
        
        return false;
    }
}
