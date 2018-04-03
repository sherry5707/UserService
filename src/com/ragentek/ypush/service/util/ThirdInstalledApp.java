package com.ragentek.ypush.service.util;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.vo.InstalledApp;

public class ThirdInstalledApp {
	
	private static final String TAG = "YPushService.ThirdInstalledApp";
	
	public ArrayList<InstalledApp> getInstalledApps(Context c ,boolean getSysPackages) {   
	      ArrayList<InstalledApp> res = new ArrayList<InstalledApp>();           
	      List<PackageInfo> packs = c.getPackageManager().getInstalledPackages(0);   
	      Log.v(TAG, "packs.size()=" + packs.size());
	      
	      for(int i=0;i<packs.size();i++) {   
	             PackageInfo p = packs.get(i);   
	             if ((!getSysPackages) && (p.versionName == null)) {   
	                    continue ;   
	             }     
	             ApplicationInfo itemInfo = p.applicationInfo; 
	             if((itemInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0){ 
	                 continue; 
	      		 }
	             String appName = p.applicationInfo.loadLabel(c.getPackageManager()).toString();
	             if(appName.equals(c.getString(R.string.app_name))){
	            	 continue;
	             }
	             
	             InstalledApp app = new InstalledApp();   
	             app.setAppName(appName);   
	             app.setPackageName(p.packageName);
	             app.setIcon(p.applicationInfo.loadIcon(c.getPackageManager()));
	             res.add(app);
	             Log.v(TAG, "app=" + app);
	      }

	      return res;    
	}
 @SuppressLint("NewApi")
  public void openApp(Context c,String packageName) throws NameNotFoundException {
  	PackageInfo pi = c.getPackageManager().getPackageInfo(packageName, 0);

  	Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
  	resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
  	resolveIntent.setPackage(pi.packageName);

  	List<ResolveInfo> apps = c.getPackageManager().queryIntentActivities(resolveIntent, 0);

  	ResolveInfo ri = apps.iterator().next();
  	if (ri != null ) {
	    	String pName = ri.activityInfo.packageName;
	    	String className = ri.activityInfo.name;
	
	    	Intent intent = new Intent(Intent.ACTION_MAIN);
	    	intent.addCategory(Intent.CATEGORY_LAUNCHER);

	    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	    	ComponentName cn = new ComponentName(pName, className);	
	    	intent.setComponent(cn);
	    	c.startActivity(intent);
  	}
  }
  @SuppressLint("NewApi")
  public void closeApp(Context c,String packageName) throws NameNotFoundException {
  	PackageInfo pi = c.getPackageManager().getPackageInfo(packageName, 0);

  	Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
  	resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
  	resolveIntent.setPackage(pi.packageName);

  	ActivityManager am = (ActivityManager) c.getSystemService(c.ACTIVITY_SERVICE);
  	List<RunningTaskInfo> list = am.getRunningTasks(100);
  	for(ActivityManager.RunningTaskInfo amTask :list){
  		if(amTask.baseActivity.getPackageName().equals(packageName)){
  			c.stopService(resolveIntent);
  			break;
  		}
  	}

  }
  @SuppressLint("NewApi")
  public void sendMsgToApp(Context c,String packageName,String msg) throws NameNotFoundException {
  	ActivityManager am = (ActivityManager) c.getSystemService(c.ACTIVITY_SERVICE);
  	List<RunningTaskInfo> list = am.getRunningTasks(100);
//  	for(ActivityManager.RunningTaskInfo amTask :list){
//  		if(amTask.baseActivity.getPackageName().equals(packageName)){

  			Log.v(TAG, "sendMsgToApp: msg=" + msg);
  			Intent broadcastIntent = new Intent(YPushConfig.broadcast_service_name);
  			broadcastIntent.putExtra("msg", msg);
  			c.sendBroadcast(broadcastIntent);
  		//	break;
//  		}
//  	}

  }
}
