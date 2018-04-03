package com.ragentek.ypush.service.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.provider.Settings;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.ragentek.ypush.service.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.mediatek.telephony.TelephonyManagerEx;

import android.os.ServiceManager;

//import android.app.usage.IUsageStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import org.json.JSONException;
import org.json.JSONObject;
//add by zhengguang.yang@20160908 start for simtype unknow
import android.telephony.SubscriptionManager;
//add by zhengguang.yang end

public class CommonUtils {
    private static final String TAG = "CommonUtils";

    private static final String PREFERENCES_NAME = "ypush_data_config";
    public static final String KEY_DATA_UPLOADED_DATE = "ComRagentekYpushService_data_uploaded_date";
    public static final String FIRST_DATA_UPLOAD_TIME_SETTING = "first_data_upload_time_setting";
    public static final int FIRST_DATA_UPLOAD_TIME_DEFAULT = 120;//240;//minutes//modified by zhengguang.yang@20160517 for active
    public static final String DATA_UPLOAD_INTERVAL = "data_upload_interval";
    public static final int DATA_UPLOAD_INTERVAL_DEFAULT = 1;//default upload interval 1 day 
    
     //modified by zhengguang.yang@20160704 start for simcard type is unknow 
    public static final String CMCC_IMSI_ZERO = "46000";//China Mobile
    public static final String CMCC_IMSI_TWO = "46002";//China Mobile
    public static final String CMCC_IMSI_SEVEN = "46007";//China Mobile
    public static final String CU_IMSI_ONE = "46001";//China Unicom
    public static final String CU_IMSI_NINE = "46009";//China Unicom
    public static final String CT_IMSI_THREE = "46003";//China Telecom
    public static final String CT_IMSI_ELEVEN = "46011";//China Telecom
    public static final String CT_IMSI_TWELVE = "46012";//China Telecom
    public static final String CT_IMSI_THIRTEEN = "46013";//China Telecom
    //add by zhengguang.yang end
    
    public static final String STORED_MYUI_VERSION = "stored_myui_version";
    
    public static final int MIN_TO_MSEC = 60 * 1000;//1min = 60 * 1000 ms
    
	public  byte[] getSendByteHead(String type ,int strLen){		
		StringBuffer sb = new StringBuffer();
		sb.append(type);
		sb.append(addZeroForNum(String.valueOf(strLen),true));
		String  str = sb.toString();
		return str.getBytes();
		
	}
	
	public  String getSendStringHead(String type ,int strLen){		
		StringBuffer sb = new StringBuffer();
		sb.append(type);
		sb.append(addZeroForNum(String.valueOf(strLen),true));
		String  str = sb.toString();
		return str;		
	}
	 /*���ֲ���λ����0
	  *
	  * @param str
	  * @param strLength
	  */
	 public static String addZeroForNum(String str,boolean left) {
		 int strLen = str.length();
		 if (strLen < YPushConfig.head_len-1) {
			 while (strLen < YPushConfig.head_len-1) {
				 StringBuffer sb = new StringBuffer();
				 if(left){
					 sb.append("0").append(str);//��0
				 }else{
					 sb.append(str).append("0");//�Ҳ�0
				 }
				 strLen+=1;
				 str = sb.toString();
			 }
		 }
		 
		 return str;
	 }


	public static void main(String[] args){
//		System.out.println(addZeroForNum("1234",true));
//		System.out.println(addZeroForNum("1234",false));
		System.out.println(Integer.parseInt("0000010"));
		
	}
	
	public static boolean isRegOnceSuccess(Context context) {
	    String uploadedResult = getUploadedIntervalDate(context);
	    boolean ret = false;
        Log.i(TAG, "isRegOnceSuccess uploadedResult=" + uploadedResult);
	    if (!TextUtils.isEmpty(uploadedResult)) {
	        ret = true;
	    }
        return ret;
    }
	
	public static boolean getRegisterStatus() {
        return false;
    }
	
	public static void setRegisterStatus(boolean status) {
    }
	
//    public static void setUploadedIntervalDate(Context context) {
//        Log.i(TAG, "setUploadedIntervalDate");
//        SharedPreferences pref = context
//                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
//        Editor editor = pref.edit();
//        editor.putString(DATA_UPLOADED_DATE, getCurrentDate());
//        editor.commit();
//        return;
//    }
    
//    public static String getUploadedIntervalDate(Context context) {
//        SharedPreferences pref = context
//                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        
//        return pref.getString(DATA_UPLOADED_DATE, "");
//    }
    
//    public static void clearIntervalUploadStatus(Context context) {
//        SharedPreferences pref = context
//                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
//        Editor editor = pref.edit();
//        editor.remove(DATA_UPLOADED_DATE);
//        editor.commit();
//    }
    
    public static String getNextInterval(Context context) {
        String nextInterval = "";
        String uploadedDate = getUploadedIntervalDate(context);
        if (uploadedDate != null && !uploadedDate.isEmpty()) {
            int uploadedYear = Integer.parseInt(uploadedDate.substring(0, 4));
            int uploadedMonth = Integer.parseInt(uploadedDate.substring(4, 6));
            int uploadedDay = Integer.parseInt(uploadedDate.substring(6, 8));
            Calendar calendar = Calendar.getInstance();
            calendar.set(uploadedYear, uploadedMonth - 1, uploadedDay);
            calendar.add(Calendar.DAY_OF_MONTH, getUploadInterval(context));

            nextInterval = String.format("%04d%02d%02d",
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        return nextInterval;
    }
    
    private static String getCurrentDate() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String str = String.format("%04d%02d%02d", year, month, day);
        return str;
    }
    //add by zhengguang.yang@20170914 start for add query active status
    private static String getCurrentDateAndTime() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
	 int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	 int min = Calendar.getInstance().get(Calendar.MINUTE);
	 int second = Calendar.getInstance().get(Calendar.SECOND);
		
        String str = String.format("%04d%02d%02d  %02d:%02d:%02d", year, month, day,hour,min,second);
        return str;
    }
    //end by zhengguang.yang

    public static boolean isUploadedThisInterval(Context context) {
        boolean isUploadedThisInterval = false;
        String currentDay = getCurrentDate();
        String nextInterval = getNextInterval(context);
        Log.i(TAG, "isUploadedThisInterval current dayOfYear=" + currentDay
                + ",nextInterval:" + nextInterval);
        if (currentDay.compareTo(nextInterval) < 0) {
            isUploadedThisInterval = true;
        }
        Log.i(TAG, "isUploadedThisInterval interval=" + isUploadedThisInterval);
        return isUploadedThisInterval;
    }
    
    
    //first register delay time
    public static int getRegOnceDelayTime(Context context) {
        return getDataUploadTime(context) * 60 * 1000;
    }
    
    //save delay time of first register
    public static void setDataUploadTime(Context context, int time) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_APPEND);
        Editor editor = preferences.edit();
        editor.putInt(FIRST_DATA_UPLOAD_TIME_SETTING, time);
        editor.commit();
    }
    
    //get delay time of first register
    public static int getDataUploadTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_APPEND);
        int timeSetting = preferences.getInt(FIRST_DATA_UPLOAD_TIME_SETTING,
                FIRST_DATA_UPLOAD_TIME_DEFAULT);
        Log.i(TAG, "getDataUploadTime timeSetting=" + timeSetting);
        return timeSetting;
    }
    
    public static void setUploadInterval(Context context, int interval) {
        Log.i(TAG, "setUploadInterval interval=" + interval);
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_APPEND);
        Editor editor = preferences.edit();
        editor.putInt(DATA_UPLOAD_INTERVAL, interval);
        editor.commit();
    }

    public static int getUploadInterval(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_APPEND);
        int intervalSetting = preferences.getInt(DATA_UPLOAD_INTERVAL,
                DATA_UPLOAD_INTERVAL_DEFAULT);
        Log.i(TAG, "getUploadInterval intervalSetting=" + intervalSetting);
        return intervalSetting;
    }
    
    public static String getOperatorsName(Context context) {
        String providersName = "unknown";
        if (context != null) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String imsi = tm.getSubscriberId();
                if (imsi != null) {
                    if (imsi.startsWith(CMCC_IMSI_ZERO)
                            || imsi.startsWith(CMCC_IMSI_TWO)
                            || imsi.startsWith(CMCC_IMSI_SEVEN)) {//add by zhengguang.yang@20160704 for simcard type is unknow
                        providersName = context.getResources().getString(R.string.operator_cmcc);
                    } else if (imsi.startsWith(CU_IMSI_ONE)||imsi.startsWith(CU_IMSI_NINE)) {//add by zhengguang.yang@20160704 for simcard type is unknow
                        providersName = context.getResources().getString(R.string.operator_cu);
                    } else if (imsi.startsWith(CT_IMSI_THREE)||imsi.startsWith(CT_IMSI_ELEVEN)||imsi.startsWith(CT_IMSI_TWELVE)||imsi.startsWith(CT_IMSI_THIRTEEN)) {//add by zhengguang.yang@20160704 for simcard type is unknow
                        providersName = context.getResources().getString(R.string.operator_ct);
                    }
                }
            }
        }

        return providersName;
    }

//add by zhengguang.yang@20160908 start for simtype unknow
    private static int getSubIdBySlot(int slot) {
        int [] subId = SubscriptionManager.getSubId(slot);
        Log.d(TAG, "getSubIdBySlot, simId " + slot +
                "subId " + ((subId != null) ? subId[0] : "invalid!"));
        return (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubId();
    }
//add by zhengguang.yang end
	
    
    public static String getOperatorsName(Context context, int simId) {
        String providersName = "unknown";
        if (context != null) {
		//modified by zhengguang.yang@20160908 start for simcard type is unknow
            //TelephonyManagerEx tm = TelephonyManagerEx.getDefault();
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		//modified by zhengguang.yang end
            if (tm != null) {
			//modified by zhengguang.yang@20160908 start for simcard type is unknow
                //String imsi = tm.getSubscriberId(simId);
				String imsi = tm.getSubscriberId(getSubIdBySlot(simId));
			//modified by zhengguang.yang end
                if (imsi != null) {
                    if (imsi.startsWith(CMCC_IMSI_ZERO)
                            || imsi.startsWith(CMCC_IMSI_TWO)
                            || imsi.startsWith(CMCC_IMSI_SEVEN)) {//add by zhengguang.yang@20160704 for simcard type is unknow
                        providersName = context.getResources().getString(R.string.operator_cmcc);
                    } else if (imsi.startsWith(CU_IMSI_ONE)||imsi.startsWith(CU_IMSI_NINE)) {//add by zhengguang.yang@20160704 for simcard type is unknow
                        providersName = context.getResources().getString(R.string.operator_cu);
                    } else if (imsi.startsWith(CT_IMSI_THREE)||imsi.startsWith(CT_IMSI_ELEVEN)||imsi.startsWith(CT_IMSI_TWELVE)||imsi.startsWith(CT_IMSI_THIRTEEN)) {//add by zhengguang.yang@20160704 for simcard type is unknow
                        providersName = context.getResources().getString(R.string.operator_ct);
                    }
                }
            }
        }

        return providersName;
    }
    
    public static String getCurrentMyUIVersion() {
        String KEY_MYUI_VERSION_PATH="ro.build.myui.id";
        //if get myui version failed, use build version as default value
        String myUIVersion; 
        myUIVersion = SystemProperties.get(KEY_MYUI_VERSION_PATH, "");
        return myUIVersion;
    }
    
    public static String getStoredMyUIVersion(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_APPEND);
        String myuiVersion = preferences.getString(STORED_MYUI_VERSION, "");
        return myuiVersion;
    }

    public static void storeMyUIVersion(Context context, String version) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_APPEND);
        Editor editor = preferences.edit();
        editor.putString(STORED_MYUI_VERSION, version);
        editor.commit();
    }
    
    public static boolean isMyUIChanged(Context context) {
        String currentVersion = getCurrentMyUIVersion();
        String storedVersion = getStoredMyUIVersion(context);
        
        boolean result = false;
        if (!currentVersion.isEmpty() && !storedVersion.isEmpty()
                && !currentVersion.equals(storedVersion)) {
            result = true;
        }
        Log.i(TAG, "isMyUIChanged result=" + result);
        return result;
    }
    
    public static int getCellId(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int cellId = 0;
        if (telephonyManager != null) {
            CellLocation cl = telephonyManager.getCellLocation();
            Log.i(TAG, "cl:" + cl);
            if (cl instanceof GsmCellLocation) {
                cellId = ((GsmCellLocation)cl).getCid();
                Log.i(TAG, "gsm cell id=" + cellId);
            } else if (cl instanceof CdmaCellLocation) {
                cellId = ((CdmaCellLocation)cl).getNetworkId();
                Log.i(TAG, "cdma cell id=" + cellId);
            }
        }
        
        return cellId;
    }
    
    public static int getCellId(int simId) {
        int cellId = 0;
        try {
        TelephonyManagerEx telephonyManager =
                TelephonyManagerEx.getDefault();
        if (telephonyManager != null) {
            CellLocation cl = telephonyManager.getCellLocation(simId);
            Log.i(TAG, "cl:" + cl);
            if (cl instanceof GsmCellLocation) {
                cellId = ((GsmCellLocation)cl).getCid();
                Log.i(TAG, "gsm cell id=" + cellId);
            } else if (cl instanceof CdmaCellLocation) {
                cellId = ((CdmaCellLocation)cl).getNetworkId();
                Log.i(TAG, "cdma cell id=" + cellId);
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return cellId;
    }
    public static boolean hasIccCard( Context context) {
        boolean result = false;
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            result = telephonyManager.hasIccCard();
        }
        return result;
    }
    public static boolean hasIccCardMtk(int simId) {
        boolean result = false;
        try {
            TelephonyManagerEx telephonyManager =
                    TelephonyManagerEx.getDefault();
            result = telephonyManager.hasIccCard(simId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    // add by xiaolin.he . this is used in android lollipop .
	public static ArrayList<JSONObject> getMyPhoneAppList(Context context) {
		Log.e(TAG, "getMyPhoneAppList.begin");
		ArrayList<JSONObject> appList = new ArrayList<JSONObject>();
		
		String permission = "android.permission.PACKAGE_USAGE_STATS";
		boolean isGranted = context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
		Log.e(TAG, "getMyPhoneAppList.isGranted == " + isGranted);
		
		UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
		Log.e(TAG, "mUsageStatsManager == " + mUsageStatsManager.toString());
		Calendar cal = Calendar.getInstance();
		long endTime = cal.getTimeInMillis();
		cal.add(Calendar.YEAR, -1);
		long startTime = cal.getTimeInMillis();
		final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, startTime, endTime);
		Log.e(TAG, "getMyPhoneAppList.stats.size() == " + stats.size());
		if (stats == null) {
			return appList;
		}
		try {
			for (UsageStats pkgStats : stats) {
				//modified by zhengguang.yang@20161226 start for filter com.android&com.mediatek
				String packageName = pkgStats.getPackageName();
				if(!packageName.startsWith("com.android")&&!packageName.startsWith("com.mediatek")){	
					JSONObject app = new JSONObject();
					app.put("packageName", pkgStats.getPackageName());
					app.put("launchCount", pkgStats.mLaunchCount);
					app.put("usageTime", pkgStats.getTotalTimeInForeground());
					Log.e(TAG, app.toString());
					appList.add(app);
				}
				//modified by zhengguang.yang end
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			return appList;
		}
    }
    
    /*public static ArrayList<JSONObject> getMyPhoneAppList(Context context) {
        Log.i(TAG, "getMyPhoneAppList");
        try {
            IUsageStatsManager usageStatsService = IUsageStatsManager.Stub
                    .asInterface(ServiceManager.getService(Context.USAGE_STATS_SERVICE));
            if (usageStatsService == null) {
                Log.e(TAG, "getMyPhoneAppList usageStatsService null");
                return new ArrayList<JSONObject>();
            }

            PackageUsageStats[] allPkgUsageStats = usageStatsService
                    .getAllPkgUsageStats();
            if (allPkgUsageStats == null) {
                Log.e(TAG, "getMyPhoneAppList usageStatsService allPkgUsageStats");
                return new ArrayList<JSONObject>();
            }

            ArrayList<JSONObject> appList = new ArrayList<JSONObject>();
            PackageManager pm = context.getPackageManager();
            for (PackageUsageStats pkgUsageStats : allPkgUsageStats) {
                int appFlags = pm.getApplicationInfo(pkgUsageStats.packageName,
                        0).flags;
//                if ((appFlags & ApplicationInfo.FLAG_SYSTEM) == 0
//                        && (appFlags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                    JSONObject app = new JSONObject();
                    app.put("packageName", pkgUsageStats.packageName);
                    app.put("launchCount", pkgUsageStats.launchCount);
                    app.put("usageTime", pkgUsageStats.usageTime);
                    appList.add(app);
//                     Log.d(TAG, "packageName=" + app.packageName +
//                             " launchCount=" + app.launchCount);
//                }
            }
            return appList;
        } catch (RemoteException e) {
            Log.e(TAG, "Could not query launch counts", e);
            return new ArrayList<JSONObject>();
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Could not find the package", e);
            return new ArrayList<JSONObject>();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ArrayList<JSONObject>();
        } catch (Exception e) {
        	return new ArrayList<JSONObject>();
        }
    }*/
    
    public static boolean isNeedUploadData(Context context) {
        boolean result = false;
        boolean isMyUIChanged = CommonUtils.isMyUIChanged(context);
        if (!CommonUtils.isUploadedThisInterval(context) || isMyUIChanged) {
            result = true;
        }
        return result;
    }


    public static boolean hasNetWork(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo!=null && networkInfo.isAvailable());
	}
    
    public static String getCurrentTimeStamp(){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String timestamp = sdf.format(System.currentTimeMillis());
		return timestamp;
    }
    
    //add by zhengguang.yang@20160308 start for relase/debug switch
    public static boolean getBusinessServerSetting(Context context) {
        String isBusinessServer = Settings.System
                .getString(
                        context.getContentResolver(),
                        /* Settings.System.QINGCHENG_BUSINESS_SERVER_CONFIG */"set_qingcheng_server_config");
        boolean result = (Boolean.parseBoolean(isBusinessServer) || (isBusinessServer == null));
        Log.i("gg", "getBusinessServerSetting isBusinessServer=" +
                isBusinessServer + ",result=" + result);
        return result;
    }
    //add by zhengguang.yang end
    
    public static final String KEY_NEED_OPEN_GPRS = "need_open_gprs";
    public static final String KEY_ADD_TIME = "ComRagentekYpushService_boot_add_time";
    public static void setGprsOpened(Context context,boolean enable) {
        Log.i(TAG, "setNeedOpenGPRS");
        SharedPreferences pref = context
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putBoolean(KEY_NEED_OPEN_GPRS, enable);
        editor.commit();
        return;
    }
    public static boolean getGprsOpened(Context context) {
        SharedPreferences pref = context
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return pref.getBoolean(KEY_NEED_OPEN_GPRS, false);
    }
    public static void setAddTime(Context context,long timeStamp) {
//    	 Log.i(TAG, "setAddTime timeStamp=" + timeStamp);
         boolean setResult = Settings.Global.putLong(context.getContentResolver(), KEY_ADD_TIME, timeStamp);
//         Log.i(TAG, "setAddTime setResult=" + setResult);
    }
    public static long getAddTime(Context context) {
        long addTime = Settings.Global.getLong(context.getContentResolver(), KEY_ADD_TIME, 0);
//    	Log.i(TAG, "getAddTime addTime=" +addTime);
        return addTime;
    }
    public static void clearAddTime(Context context) {
    	setAddTime(context, 0);
    }
    public static void setUploadedIntervalDate(Context context) {
//        Log.i(TAG, "setUploadedIntervalDate");
	//add by zhengguang.yang@20170914 start for add query active status
        //boolean setResult = Settings.Global.putString(context.getContentResolver(), KEY_DATA_UPLOADED_DATE, getCurrentDate());
        boolean setResult = Settings.Global.putString(context.getContentResolver(), KEY_DATA_UPLOADED_DATE, getCurrentDateAndTime());
	//end by zhengguang.yang
//        Log.i(TAG, "setUploadedIntervalDate setResult=" + setResult);
    }
    public static String getUploadedIntervalDate(Context context) {
        String date = Settings.Global.getString(context.getContentResolver(), KEY_DATA_UPLOADED_DATE);
//        Log.i(TAG, "getLeijiTime leijiTime=" +date);
        return date;
    }
    public static void clearIntervalUploadStatus(Context context) {
//    	Log.i(TAG, "clearIntervalUploadStatus");
        boolean setResult = Settings.Global.putString(context.getContentResolver(), KEY_DATA_UPLOADED_DATE, "");
//        Log.i(TAG, "clearIntervalUploadStatus setResult=" + setResult);
    }
    public static final String KEY_ACTIVE_FAIL_COUNT = "ComRagentekYpushService_active_fail_count";
    public static void setActiveFailCount(Context context,int count) {
//		Log.i(TAG, "setActiveFailCount count="+count);
        boolean setResult = Settings.Global.putInt(context.getContentResolver(), KEY_ACTIVE_FAIL_COUNT, count);
//   		Log.i(TAG, "setActiveFailCount setResult=" + setResult);
   }
   public static int getActiveFailCount(Context context) {
       int count = Settings.Global.getInt(context.getContentResolver(), KEY_ACTIVE_FAIL_COUNT, 0);
//       Log.i(TAG, "getActiveFailCount count="+count);
	   return count;
   }

    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            int netType = networkInfo.getType();
            if (netType == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
