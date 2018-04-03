package com.ragentek.ypush.service.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import com.ragentek.ypush.service.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

/** 
 * Copyright (C) 2012-2013, Ragentek. All rights reserved.
 *
 * @author zhouqing
 * @version 2012-11-22 上午11:50:23 
 */

public class PhoneInfoUtil {
	private static final String TAG = "YPushService.PhoneInfoUtil";
	
	private static String channelID;
	private static String clientVersion;
	private static String netType;
	
	public static String getChannelID() {
		return channelID;
	}

	public static String getClientVersion() {
		return clientVersion;
	}

	public static String getNetType() {
		return netType;
	}

	public static String getIMEI(Context context){
		TelephonyManager telephonyManager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	String imei=telephonyManager.getDeviceId();
		if(!TextUtils.isEmpty(imei)){
			return imei;
		}
    	imei= ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if(TextUtils.isEmpty(imei)){
			WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	        if(wifiMan!=null){
			  WifiInfo wifiInf = wifiMan.getConnectionInfo();			
	          imei = wifiInf.getMacAddress().replaceAll(":", "");	          
	        }		
		}	
	    return imei;
	}
	
	public static String getNetType(Context context) {
		Log.v(TAG, "enter getNetType : netType=" + netType);
		
		//modified by zhengguang.yang@20160421 for make sure nettype is current real type
//		if(TextUtils.isEmpty(netType)){
			netType = "unknown";
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			Log.v(TAG, "enter getNetType : networkInfo=" + networkInfo);
			
			if(networkInfo == null){
				return netType;
			}
			int nType = networkInfo.getType();
			Log.v(TAG, "enter getNetType : nType=" + nType);
			
			if(nType == ConnectivityManager.TYPE_MOBILE){
				//modified by zhengguang.yang@20160906 start for netType unknow
				//netType = getProvidersName(context);
				int networkType = networkInfo.getSubtype();
				netType = getProvidersName(context,networkType);
				//modified by zhengguang.yang end
			}else if (nType == ConnectivityManager.TYPE_WIFI){
				netType = "WIFI";
			}
//		}
		return netType;
	}
	//add by zhengguang.yang@20160906 start for netType unknow
	private static String getProvidersName(Context context,int networkType) {
		String ProvidersName = "unknown";
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkClass = TelephonyManager.getNetworkClass(networkType);
		if (networkClass == TelephonyManager.NETWORK_CLASS_2_G) {
			ProvidersName = context.getString(R.string.phone_info_net_type_2g);
		} else if (networkClass == TelephonyManager.NETWORK_CLASS_3_G) {
			ProvidersName = context.getString(R.string.phone_info_net_type_3g);
		} else if (networkClass == TelephonyManager.NETWORK_CLASS_4_G) {
			ProvidersName = context.getString(R.string.phone_info_net_type_4g);
		}

		return ProvidersName;
	}
	//add by zhengguang.yang end
	
	public static String getUseNetType(Context context) {
		String usenetType = "unknown";

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		Log.v(TAG, "enter getUseNetType : networkInfo=" + networkInfo);
		
		if(networkInfo == null){
			return usenetType;
		}
		int nType = networkInfo.getType();
		Log.v(TAG, "enter getUseNetType : nType=" + nType);
		
		if(nType == ConnectivityManager.TYPE_MOBILE){
			usenetType = "MOBILE";
		}else if (nType == ConnectivityManager.TYPE_WIFI){
			usenetType = "WIFI";
		}

		return usenetType;
	}
	
	@SuppressLint("NewApi")
	private static String getProvidersName(Context context) {
		String ProvidersName = "unknown";
		// modify by xiaolin.he 20151110 start . for 4G
		/*
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conMan.getActiveNetworkInfo();
		
		Log.v(TAG, "enter getProvidersName : info=" + info + " ,info.getSubtype()=" + info.getSubtype());
		if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS){
			//ProvidersName = context.getString(R.string.phone_info_net_type_gprs);
			ProvidersName = context.getString(R.string.phone_info_net_type_2g);
		} else if(info.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA){
			//ProvidersName = context.getString(R.string.phone_info_net_type_cdma);
			ProvidersName = context.getString(R.string.phone_info_net_type_2g);
		} else if(info.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE){
			//ProvidersName = context.getString(R.string.phone_info_net_type_edge);
			ProvidersName = context.getString(R.string.phone_info_net_type_2g);
		} else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_0 || info.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_A || info.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_B) {
			//ProvidersName = context.getString(R.string.phone_info_net_type_evdo);
			ProvidersName = context.getString(R.string.phone_info_net_type_3g);
		} else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_HSDPA || info.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPA
				|| info.getSubtype() == TelephonyManager.NETWORK_TYPE_HSUPA || info.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPAP) {
			//ProvidersName = context.getString(R.string.phone_info_net_type_hsdpa);
			ProvidersName = context.getString(R.string.phone_info_net_type_3g);
		} else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_UMTS) {
			//ProvidersName = context.getString(R.string.phone_info_net_type_umts);
			ProvidersName = context.getString(R.string.phone_info_net_type_3g);
		}
		*/
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = telephonyManager.getNetworkType();
		int networkClass = TelephonyManager.getNetworkClass(networkType);
		if (networkClass == TelephonyManager.NETWORK_CLASS_2_G) {
			ProvidersName = context.getString(R.string.phone_info_net_type_2g);
		} else if (networkClass == TelephonyManager.NETWORK_CLASS_3_G) {
			ProvidersName = context.getString(R.string.phone_info_net_type_3g);
		} else if (networkClass == TelephonyManager.NETWORK_CLASS_4_G) {
			ProvidersName = context.getString(R.string.phone_info_net_type_4g);
		}
		// modify by xiaolin.he 20151110 end .
		return ProvidersName;
	}
	
	public static long getFreeMem(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Activity.ACTIVITY_SERVICE);
		MemoryInfo info = new MemoryInfo();
		manager.getMemoryInfo(info);
		long free = info.availMem / 1024 / 1024;
		return free;
	}

	public static long getTotalMem(Context context) {
		try {
			FileReader fr = new FileReader("/proc/meminfo");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split("\\s+");
			return Long.valueOf(array[1]) / 1024;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	@SuppressLint("NewApi")
	public static int getSysVersion() {
		return Build.VERSION.SDK_INT;
	}
	
	public static boolean isOnline(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		}
		return false;
	}
	
	public static String getCpuInfo() {
		try {
			FileReader fr = new FileReader("/proc/cpuinfo");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			return array[1];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressLint("NewApi")
	public static String getSensorNames (Context context) {
		String allSensorName = "";
		SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		for (int i = 0; i < allSensors.size(); i++) {
			allSensorName += allSensors.get(i).getName()+",";
		}
		return allSensorName.substring(0,allSensorName.length()-1);
	}
	
	public static String getDisplaysMetrics(Activity activity){
		DisplayMetrics displaysMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
		return displaysMetrics.widthPixels+"*"+displaysMetrics.heightPixels;
	}
	
	public static String getROMStorage(){
        String path=Environment.getDataDirectory().getPath();
        StatFs statFs=new StatFs(path);
        long blockSize=statFs.getBlockSize();
        long availableBlocks=statFs.getAvailableBlocks();
        String[] available=fileSize(availableBlocks*blockSize);
        return (available[0]+available[1]);
    }
	
	public static String getTotalStorage(){
        String path=Environment.getDataDirectory().getPath();
        StatFs statFs=new StatFs(path);
        long blockSize=statFs.getBlockSize();
        long totalBlocks=statFs.getBlockCount();
        String[] total=fileSize(totalBlocks*blockSize);
        return (total[0]+total[1]);
    }
	
	private static String[] fileSize(long size){
        String str="";
        if(size>=1024){
            str="KB";
            size/=1024;
            if(size>=1024){
                str="MB";
                size/=1024;
                if(size>=1024){
                    str="GB";
                    size/=1024;
                }
            }
        }
        DecimalFormat formatter=new DecimalFormat();
        formatter.setGroupingSize(3);
        String[] result=new String[2];
        result[0]=formatter.format(size);
        result[1]=str;
        return result;
    }

	public static boolean isDeviceRooted() {
	    String buildTags = android.os.Build.TAGS;
	    if (buildTags != null && buildTags.contains("test-keys")) {
	    	return true;
	    }
	    try {
	    	File file = new File("/system/app/Superuser.apk");
		    if (file.exists()) {
		      return true;
		    }
	    } catch (Throwable e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	public static String getWifiMac(Context context){
		String imei = "unknown";
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiMan!=null){
		  WifiInfo wifiInf = wifiMan.getConnectionInfo();	
		  if(!TextUtils.isEmpty(wifiInf.getMacAddress())){
			  imei = wifiInf.getMacAddress().replaceAll(":", "");
		  }
        }
        return imei;
	}
	
	@SuppressLint("NewApi")
	public static String getBTMac(){
		String imei = "unknown";
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter != null){
			if (mBluetoothAdapter.isEnabled()) {
				imei = mBluetoothAdapter.getAddress();
			}
		}
		return imei;
	}
	
	public static String getChengyouVersion(Context context){
		if(TextUtils.isEmpty(clientVersion)){
			clientVersion = "unknown";
			PackageInfo	PkgInfo = null;
			try {
				PkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				clientVersion = PkgInfo.versionName;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return clientVersion;
	}
	
	public static String getChannelID(Context context){
		if(TextUtils.isEmpty(channelID)){
			channelID = "unknown";
			ApplicationInfo Info = null;
			try {
				Info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
				channelID = Info.metaData.getString("channel_id");//(Constants.CHANNEL_ID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return channelID;
	}
	
		public static String getChannelName(Context context){
			String channelName = "unknown";
			ApplicationInfo Info = null;
			try {
				Info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
				channelName = Info.metaData.getString("channel_name");//(Constants.CHANNEL_NAME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return channelName;
		}
	
	public static String getSimSerialNumber(Context context){
		TelephonyManager tm= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String simNum = tm.getSimSerialNumber();
		if(TextUtils.isEmpty(simNum)){
			return "unknown";
		}else{
			return simNum;
		}
	}
	
}
