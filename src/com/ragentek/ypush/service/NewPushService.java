package com.ragentek.ypush.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greenorange.myuiaccount.service.RequestCallBack;
import com.greenorange.myuiaccount.service.V2.ServiceAPI;
import com.greenorange.myuiaccount.service.V2.Response.BaseResponse;
import com.ragentek.ypush.service.db.MyDatabaseUtil;
import com.ragentek.ypush.service.network.AppDetailResponse;
import com.ragentek.ypush.service.network.HttpClient;
import com.ragentek.ypush.service.util.ACache;
import com.ragentek.ypush.service.util.CommonUtils;
import com.ragentek.ypush.service.util.PhoneInfoUtil;
import com.ragentek.ypush.service.util.SIMCardInfo;
import com.ragentek.ypush.service.util.TimeUtils;
import com.ragentek.ypush.service.util.YPushConfig;
import com.ragentek.ypush.service.vo.ByteOrStringHelper;
import com.ragentek.ypush.service.vo.commonMessage.commonMsg;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.os.SystemProperties;
import com.greenorange.myuiaccount.Util.AndroidUtil;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import android.os.Binder;
import java.util.ArrayList;
import com.greenorange.myuiaccount.service.V2.Config;
import com.ragentek.ypush.service.download.AppDownloadUtil;

import android.content.ContentValues;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ragentek.ypush.service.network.AppDetailResponse;
import com.ragentek.ypush.service.network.HttpClient;
//add by zhengguang.yang@20170802 start for get real physical screensize
import android.view.WindowManager;
import android.content.res.Configuration;
//end by zhengguang.yang


public class NewPushService extends Service{
	private final static String TAG = "yy";
	private final static String KEY_CONFIG_RESPONSE = "KEY_CONFIG_RESPONSE";
	private final static int MSG_INTERVAL_PUSH = 1;
	private final static int MSG_INTERVAL_PULL = 2;
	private final static int MSG_DO_PUSH = 3;
	private final static int MSG_DO_PULL = 4;
	private final static int MSG_NOTIFICATION_PULL = 5;
	private final static int MSG_NOTIFICATION_PUSH = 6;
	private final static int MSG_DO_ACTIVE_START = 7;
	private final static int MSG_DO_ACTIVE_END = 8;
	private final static int MSG_DO_ACTIVE_FAIL = 9;
	//add by zhengguang.yang@20160919 start for push user behavior
	private final static int MSG_DO_PUSH_BEHAVIOR = 10;
	//add by zhengguang.yang end
	// add by sherry start for EVENT_STATISTICS
	private final static int MSG_DO_EVENT_STATISTICS = 11;
	//add by sherry for delete from db
	//add by sherry start for get ip
	private final static int MSG_DO_GETIPADDRESS = 13;
	//add by sherry end
	//add by sherry for delete from db
	private final static int MSG_DO_DELETEDB = 14;
	//add by sherry end
	//add by sherry for push statistics from db
	private final static int MSG_DO_PUSHSTATISTICS_FROMDB = 15;
	//add by sherry end

	private final static int DEFAULT_DO_ACTIVE_FAIL_DELAY = 30*60*1000;//30min
	private final static int DEFAULT_PUSH_INTERVAL = 2*60*60*1000;//2 hour
	private final static int DEFAULT_PULL_INTERVAL = 2*60*60*1000;//2 hour
	private final static int DEFAULT_CONFIG_EXPIRED_TIME = 24*60*60;//1 day unit s
	private final static String KEY_ALARM_FROM = "from";
	private final static String FROM_ALARM_PUSH = "alarm_push";
	private final static String FROM_ALARM_PULL = "alarm_pull";
	//add by zhengguang.yang@20160919 start for push user behavior
	private final static String FROM_PUSH_BEHAVIOR = "push_behavior";
	//add by zhengguang.yang end
	// add by sherry start for event statistics
	private final static String FROM_EVENT_STATISTICS = "event_statistics";
	// add by sherry end
	//add by sherry start for deleteDB
	private final static String FROM_ALARM_DELETEDB = "alarm_delete_db";
	//add by sherry end
	//add by sherry start for Push from DB
	private final static String FROM_ALARM_PUSHSTSTISTICSFROMDB = "alarm_push_statistics_db";
	//add by sherry end

	private ConnectivityManager mCM;
	private ACache mCache;
	private Looper mHandlerLooper;
	private ServiceHandler mServiceHandler;
	private AlarmManager mAlarmManager = null;
	private PendingIntent mAlarmPushPendingIntent = null;
	private PendingIntent mAlarmPullPendingIntent = null;
	private ConfigResponse mConfigResponse = null;
	private Intent pushIntent;
	private Intent pullIntent;
	private Timer timer;
	
    private String IpAddress;
    private PendingIntent mAlarmDeleteDBIntent = null;
	private PendingIntent mAlarmPushEventStatistics=null;
	////////////////////////////////////
	// yang.yang2 add for push version
	private final static String PUSH_CLIENT_VERSION_CODE = "PUSH_G2_4.3.13";
	// add DB
	MyDatabaseUtil myDBUtil;
	////////////////////////////////////
	//add by sherry for autoinstall
	private AppDetailResponse res;
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_INTERVAL_PUSH:
				if(mConfigResponse != null){
					int intervalPush = mConfigResponse.getPushInterval();
					Log.i(TAG,"MSG_INTERVAL_PUSH-->intervalPush="+intervalPush);
					if(intervalPush == 0){//DEFAULT_PUSH_INTERVAL
						mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DEFAULT_PUSH_INTERVAL, DEFAULT_PUSH_INTERVAL, mAlarmPushPendingIntent);
					}else{
						mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalPush*60*60*1000, intervalPush*60*60*1000, mAlarmPushPendingIntent);
//						mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60*1000, 60*1000, mAlarmPushPendingIntent);
					}
				}
				break;
			case MSG_INTERVAL_PULL:
				if(mConfigResponse != null){
					int intervalPull = mConfigResponse.getPullInterval();
					Log.i(TAG,"MSG_INTERVAL_PULL-->intervalPull="+intervalPull);
					if(intervalPull == 0){//DEFAULT_PULL_INTERVAL
						mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DEFAULT_PULL_INTERVAL, DEFAULT_PULL_INTERVAL, mAlarmPullPendingIntent);
					}else{
						mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalPull*60*60*1000, intervalPull*60*60*1000, mAlarmPullPendingIntent);
//						mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60*1000, 60*1000, mAlarmPullPendingIntent);
					}
				}
				break;
			case MSG_DO_PUSH:
				push();
				break;
			case MSG_DO_PULL:
				pull();
				break;
			//add by zhengguang.yang@20160517 start for active
			case MSG_DO_ACTIVE_START:
				active();
				break;
			case MSG_DO_ACTIVE_END:
				initCache();
				break;
			case MSG_DO_ACTIVE_FAIL:
				initActive();
				break;
			//add by zhengguang.yang@20160517 end for active	
			case MSG_NOTIFICATION_PULL:
				String data = (String) msg.obj;
				dealReceiveMsg(data);
				break;
			case MSG_NOTIFICATION_PUSH:
				break;
			//add by zhengguang.yang@20160919 start for push user behavior
			case MSG_DO_PUSH_BEHAVIOR:
				PushBehaviorData pushBehaviorData = (PushBehaviorData) msg.obj;
				Log.i(TAG,"MSG_DO_PUSH_BEHAVIOR-->pushBehaviorData ="+pushBehaviorData);
				pushBehavior(pushBehaviorData);
				break;
			//add by zhengguang.yang end

			// add by sherry start for push statistics event
			case MSG_DO_EVENT_STATISTICS:
				final EventStatisticsData eventStatisticsData = (EventStatisticsData) msg.obj;
				GetIPFromServer(new GetIpCallback() {
					@Override
					public void solve(String result) {
						if (result != null && eventStatisticsData != null) {
							pushStatisticsEvent(eventStatisticsData, true, -1);
						} else {
							final rowAndData rad = getPushStatisticsEventData(eventStatisticsData, true, -1);    //some data is null,insert to db,wait for push
							Log.d(TAG, "no network insert to db:" + rad.getRow());
						}
					}
				});
				break;
				//add by sheryy for delete db
			case MSG_DO_DELETEDB:
				boolean result = DeleteEventStatisticsFromDB();
				Log.d("NewPushService", "delete result:" + result);
				break;
				//add by sherry end
			//add by sherry for push from db
			case MSG_DO_PUSHSTATISTICS_FROMDB:
				PushEventStatisticsFromDB();
				break;
			//add by sherry end
			default:
				break;
			}
			
		}
	}
	
	public static Bitmap returnBitMap(String bitmapUrl) {
		String url = null;
		URL myFileUrl = null;
		Bitmap bitmap = null;

		try {
			url = new String(bitmapUrl.getBytes(), "iso8859-1");
			myFileUrl = new URL(url);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	private void asyncSetNotification(final Notification.Builder builder, final String iconUrl, final int customId) {
		final AsyncTask<Void, Void, Void> updateTask = new AsyncTask<Void, Void, Void>() {
			private Bitmap bitmap;

			@Override
			protected Void doInBackground(Void... aVoid) {
				bitmap = returnBitMap(iconUrl);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				NotificationManager promptNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				builder.setLargeIcon(bitmap);
				@SuppressWarnings("deprecation")
				Notification promptNotification = builder.getNotification();
				promptNotificationManager.notify(customId, promptNotification);
			}
		};
		updateTask.execute();
	}

	private static Map<String, Object> parseData(String data) {
		Gson gson = new Gson();
		Map<String, Object> map = gson.fromJson(data,
				new TypeToken<Map<String, Object>>() {
				}.getType());
		Log.i(TAG, "parseData map:" + map);

		return map;
	}

	private void dealReceiveMsg(String data) {
		if(TextUtils.isEmpty(data)){
			return;
		}
		
		Gson gson = new Gson();
		List<PullData> pullDatas = gson.fromJson(data,
				new TypeToken<List<PullData>>() {
				}.getType());
		if (pullDatas == null || pullDatas.size() == 0) {
			return;
		}

		for (int i = 0; i < pullDatas.size(); i++) {
			if("1".equals(pullDatas.get(i).getPushType())&&!CommonUtils.isWifi(getApplicationContext())){
				Log.i("NewPushService", "not wifi");
				return;
			}
			if ("4".equals(pullDatas.get(i).getDownloadType())&& "1".equals(pullDatas.get(i).getPushType())) {
				AutoDownLoadAndInstall(pullDatas.get(i));
			} else {
				showNotification(pullDatas.get(i));
			}
		}
	}
	
	private void showNotification(PullData pullData){
		Intent uiIntent = new Intent();
		uiIntent.setAction("android.intent.action.PUSHMSGACTIVITY");
		Bundle bundle = new Bundle();
		bundle.putString("pushType", pullData.getPushType());
//		bundle.putString("msgData", msgProtobufText);
//		bundle.putString("content", strContent);
		bundle.putString("url", pullData.getUrl());//add by zhengguang.yang for pushtype = html
		bundle.putString("title", pullData.getTitle());
		bundle.putString("msg", pullData.getDescription());
		bundle.putString("downloadType", pullData.getDownloadType());
		//add by zhengguang.yang@20160919 start for push user behavior
		bundle.putString("appId", pullData.getReferAppId());
		bundle.putString("msgId", pullData.get_id());
		//add by zhengguang.yang end
		String visitFlag = myDBUtil.myDBQuery("wifiVisitFlag");
		bundle.putString("wifiVisitFlag", visitFlag);
		
		uiIntent.putExtras(bundle);
		uiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		Log.d(TAG, "uiIntent=" + uiIntent);

		// notification bar prompt user have url to open
		// get notificationId
		String strNotificationId = myDBUtil.myDBQuery("notificationId");
		if (strNotificationId == null || ("".equals(strNotificationId))) {
			Log.v(TAG, "init NotificationId");
			strNotificationId = "0";
		}
		Log.v(TAG, "Deal self: strNotificationId=" + strNotificationId);

		int curNotificationId = new Integer(strNotificationId);
		final int customId = curNotificationId;
		if (pullData.getIcon() == null || pullData.getIcon().equals("")) {
			PendingIntent contentPendingIntent = PendingIntent.getActivity(NewPushService.this, curNotificationId, uiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			Notification promptNotification = new Notification.Builder(NewPushService.this)
			.setContentTitle(pullData.getTitle())
			.setContentText(pullData.getDescription())
			.setContentIntent(contentPendingIntent)
			.build();
			NotificationManager promptNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			promptNotification.icon = R.drawable.go_push_icon; // R.drawable.ic_launcher;
			promptNotification.tickerText = getString(R.string.push_msg_ticker);
			promptNotification.flags = Notification.FLAG_AUTO_CANCEL; // zhaoshh for after click auto cancel
			promptNotification.defaults = Notification.DEFAULT_SOUND;
			Log.v(TAG, "Deal self: promptNotification=" + promptNotification);

			promptNotificationManager.notify(curNotificationId, promptNotification);
		} else {
			Notification.Builder builder = new Notification.Builder(NewPushService.this);

			PendingIntent contentPendingIntent = PendingIntent.getActivity(NewPushService.this, curNotificationId, uiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			int iconId = R.drawable.go_push_icon;
			builder.setTicker(getResources().getString(R.string.push_msg_ticker));
			builder.setContentTitle(pullData.getTitle());
			builder.setContentText(pullData.getDescription());
			builder.setSmallIcon(iconId);

			builder.setWhen(System.currentTimeMillis());
			builder.setAutoCancel(true);
			builder.setContentIntent(contentPendingIntent);
			builder.setDefaults(Notification.DEFAULT_SOUND);

			asyncSetNotification(builder, pullData.getIcon(), curNotificationId);
		}
		curNotificationId++;
		String temp = String.valueOf(curNotificationId);
		Log.v(TAG, "Deal self: temp=" + temp);
		myDBUtil.myDBUpdate("notificationId", temp);
	}



	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service onCreate()");
		initData();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		GetIPFromServer(new GetIpCallback() {
			@Override
			public void solve(String result) {
				if (!TextUtils.isEmpty(result)) {
					ipAddress = result;
				} else {
					ipAddress = getIpAddress();
				}
			}
		});
		if(intent != null){
			String from = intent.getStringExtra(KEY_ALARM_FROM);
			Log.i(TAG,"Service onStartCommand()-->from="+from);
			if(FROM_ALARM_PUSH.equals(from)){
				mServiceHandler.sendEmptyMessage(MSG_DO_PUSH);
			}else if(FROM_ALARM_PULL.equals(from)){
				mServiceHandler.sendEmptyMessage(MSG_DO_PULL);
			}else if(FROM_PUSH_BEHAVIOR.equals(from)){
				Message msg = mServiceHandler.obtainMessage();
				msg.what = MSG_DO_PUSH_BEHAVIOR;
				Bundle bundle = intent.getExtras();
				PushBehaviorData pushBehaviorData = (PushBehaviorData) bundle.getSerializable("PushBehaviorData");
				msg.obj = pushBehaviorData;
				Log.i(TAG,"Service onStartCommand()--> pushBehaviorData="+pushBehaviorData);
				mServiceHandler.sendMessage(msg);
			} else if (FROM_EVENT_STATISTICS.equals(from)) {
				Message message = mServiceHandler.obtainMessage();
				message.what = MSG_DO_EVENT_STATISTICS;
				Bundle bundle = intent.getExtras();
				EventStatisticsData eventStatisticsData = (EventStatisticsData) bundle.getSerializable("EventStatisticsData");
				message.obj = eventStatisticsData;
				mServiceHandler.sendMessage(message);
			} else if (FROM_ALARM_DELETEDB.equals(from)) {
				mServiceHandler.sendEmptyMessage(MSG_DO_DELETEDB);
			} else if (FROM_ALARM_PUSHSTSTISTICSFROMDB.equals(from)) {
				mServiceHandler.sendEmptyMessage(MSG_DO_PUSHSTATISTICS_FROMDB);
			}
		}
	
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//add by zhengguang.yang@20160420 start for improve code
		//cancel alarm
		myDBUtil.close();
		cancelAlarm();
		
		// cancel register
		if (mNetWorkChangeBroadcastReceiver != null) {
			unregisterReceiver(mNetWorkChangeBroadcastReceiver);
			mNetWorkChangeBroadcastReceiver = null;//add by zhengguang.yang@20161216 for unregister broadcast exception
		}
		
		if(timer != null){
			timer.cancel();
		}
		Log.i(TAG, "Service onDestroy");
		//add by zhengguang.yang@20160420 end for improve code 
	}
	
	//add by zhengguang.yang@20160420 start for improve code 
	private void initAlarm(){
		mAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		
		pushIntent = new Intent(this, NewPushService.class);
		pushIntent.putExtra(KEY_ALARM_FROM, FROM_ALARM_PUSH);
		mAlarmPushPendingIntent = PendingIntent.getService(this, 0, pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		pullIntent = new Intent(this, NewPushService.class);
		pullIntent.putExtra(KEY_ALARM_FROM, FROM_ALARM_PULL);
		mAlarmPullPendingIntent = PendingIntent.getService(this, 1, pullIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		//add by sherry
		//set alarm,pendingIntent -->onstartCommand，onstartCommand-->handler
		Intent deleteIntent = new Intent(this, NewPushService.class);
		deleteIntent.putExtra(KEY_ALARM_FROM, FROM_ALARM_DELETEDB);
		mAlarmDeleteDBIntent = PendingIntent.getService(this, 2, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		int alarmType = AlarmManager.RTC_WAKEUP;
		final long intervalSec = 24 * 60 * 60 * 1000;
		long triggerAtTime = System.currentTimeMillis() + 20 * 60 * 60 * 1000;

		mAlarmManager.setRepeating(alarmType, triggerAtTime, intervalSec, mAlarmDeleteDBIntent);

		//add by sherry for push data from db which are pushed when no network
		//set alarm,pendingIntent -->onstartCommand，onstartCommand-->handler
		Intent PushEventIntent = new Intent(this, NewPushService.class);
		PushEventIntent.putExtra(KEY_ALARM_FROM, FROM_ALARM_PUSHSTSTISTICSFROMDB);
		mAlarmPushEventStatistics = PendingIntent.getService(this, 2, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		final long intervalSec2 = 12 * 60 * 60 * 1000;            //interval is 12h
		triggerAtTime = System.currentTimeMillis() + 11 * 60 * 60 * 1000;        //start time is 10:00

		mAlarmManager.setRepeating(alarmType, triggerAtTime, intervalSec2, mAlarmPushEventStatistics);
	}
	
	private void cancelAlarm() {
		if(mAlarmManager != null){
			if(mAlarmPushPendingIntent != null){
				mAlarmManager.cancel(mAlarmPushPendingIntent);
			}
			if(mAlarmPullPendingIntent != null){
				mAlarmManager.cancel(mAlarmPullPendingIntent);
			}
			
		}
		Log.i(TAG, "cancelAlarm!!");
	}
	//add by zhengguang.yang@20160420 end for improve code 
	
	private void initData() {
		// init DB
		if(myDBUtil!=null)
			myDBUtil.close();
		myDBUtil = new MyDatabaseUtil(getApplicationContext());
		try {
			if (!("1".equals(myDBUtil.myDBQuery("haveInitDB")))) {
				Log.i(TAG,"init DB try!");
				myDBUtil.initDb();
			}
		} catch (Exception e) {
			Log.i(TAG,"init DB catch!");
			myDBUtil.initDb();
		}

		// add for OTA,because ota not clear data/data, need recreate DB
		try {
			myDBUtil.myDBQuery("extend6");// notes: this column is the last column of DB
		} catch (Exception e) {
			Log.i(TAG,"init DB because extend6 catch!");
			boolean delFlag = getApplicationContext().deleteDatabase(YPushConfig.DATABASE_NAME);
			Log.i(TAG,"delete DB in catch. delFlag=" + delFlag);
			myDBUtil.initDb();
		}
		
		// start alarm for interval connect socket
		initAlarm();
		
		//init handlerThread
		HandlerThread thread = new HandlerThread("PushClient");
		thread.start();
		mHandlerLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mHandlerLooper);
		
		//add by zhengguang.yang@20160517 start for active
		mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); 
		boolean registered = CommonUtils.isRegOnceSuccess(getApplicationContext());
		Log.i(TAG,"initData-->registered="+registered+",activeFailcount="+CommonUtils.getActiveFailCount(getApplicationContext()));
		if(registered || CommonUtils.getActiveFailCount(getApplicationContext()) >= TOATAL_ACTIVE_FAIL_COUNT){
			initCache();
		}else if(CommonUtils.getActiveFailCount(getApplicationContext()) != 0){//若发现之前激活失败，延时30min再激活
			mServiceHandler.sendEmptyMessageDelayed(MSG_DO_ACTIVE_FAIL, DEFAULT_DO_ACTIVE_FAIL_DELAY);
		}else{
			initActive();
		}
		//add by zhengguang.yang@20160517 end for active
	}
	
	
	//add by zhengguang.yang@20160517 start for active
	private static final long SCAN_ADD_TIME_PEROID = 10*60*1000;//10min
	private synchronized void initActive() {
		long addTime = CommonUtils.getAddTime(getApplicationContext());
		long delayTime = CommonUtils.getRegOnceDelayTime(this);
		Log.i(TAG,"initActive-->addTime ="+addTime+",delayTime="+delayTime);
		if(addTime < delayTime){
			Log.i(TAG,"initActive-->wait");
			if(timer == null){
				timer = new Timer();
			}
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					CommonUtils.setAddTime(getApplicationContext(), CommonUtils.getAddTime(getApplicationContext())+SCAN_ADD_TIME_PEROID);
					initActive();
				}
			}, SCAN_ADD_TIME_PEROID);
		}else{
			Log.i(TAG,"initActive-->mashang");
			mServiceHandler.sendEmptyMessage(MSG_DO_ACTIVE_START);
		}
				
	}

	//del by zhengguang.yang@20170110 start for country new rule
	/*
	//检测GPRS是否打开     
    private boolean gprsIsOpenMethod(){    
        Class cmClass       = mCM.getClass();    
        Class[] argClasses  = null;    
        Object[] argObject  = null;    
            
        Boolean isOpen = false;    
        try {    
            Method method = cmClass.getMethod("getMobileDataEnabled", argClasses);    
            isOpen = (Boolean) method.invoke(mCM, argObject);    
        } catch (Exception e){    
            e.printStackTrace();    
        }    
        return isOpen;    
    }    
  
    //开启/关闭GPRS     
    private boolean setMobileDataEnable(boolean enable) {  
        //5.0以上，禁用移动网络使用TelephonyManager#setDataEnabled  
        //5.0以下，则是ConnectivityManager#setMobileDataEnabled  
        Object object = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? getSystemService(TELEPHONY_SERVICE) :  
                getSystemService(Context.CONNECTIVITY_SERVICE);  
        String methodName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "setDataEnabled" : "setMobileDataEnabled";  
        Method setMobileDataEnable;  
        try {  
            setMobileDataEnable = object.getClass().getDeclaredMethod(methodName, boolean.class);  
            setMobileDataEnable.setAccessible(true);  
            setMobileDataEnable.invoke(object, enable);  
            Log.i(TAG, "[setMobileDataEnable] normal enable="+enable);
            return true;  
        } catch (Exception e) {  
            Log.i(TAG, "[setMobileDataEnable] error,exception:" + e.toString());  
            e.printStackTrace();  
            return false;  
        }  
    } 
    
    
    private void gprsEnabled(boolean bEnable){ 
        boolean isOpen = gprsIsOpenMethod(); 
        Log.i(TAG,"gprsEnabled()-->isOpen="+isOpen+",bEnable="+bEnable);
        if(isOpen == !bEnable) { 
        	setMobileDataEnable(bEnable); 
        } 
    } 
    */
    //del by zhengguang.yang end
    
    
	private void active() {
		try{
			//add by zhengguang.yang@20160420 start for improve code
			if(!CommonUtils.hasNetWork(getApplicationContext())){
				//modified by zhengguang.yang@20170110 start for country new rule
				//gprsEnabled(true);
				//CommonUtils.setGprsOpened(getApplicationContext(), true);
				return;
				//modified by zhengguang.yang end
			}
			
			//if ipaddress is null,delay 10s,check ipaddress;until 10 times,if the last time is null,start active
			if(TextUtils.isEmpty(getIpAddress())){
				activeIdx++;
				if(activeIdx == TOATAL_COUNT){
					activeIdx = 0;
				}else{
					mServiceHandler.sendEmptyMessageDelayed(MSG_DO_ACTIVE_START, ACTIVE_PEROID);
					return;
				}
			}else{
				activeIdx = 0;
			}
			
			//if the time between first request with second request less than 30s,the second request not resolve
			long nowtime = System.currentTimeMillis();
	    	if (nowtime - lastActiveTime < NET_PERIOD) return;
	    	lastActiveTime = nowtime;
			//add by zhengguang.yang@20160420 end for improve code
			
			byte[] registerAppData = getRegisterAppData();
			String data = new String(registerAppData);
			boolean ret = ServiceAPI.API_MYUI_Request_Async(
                    ServiceAPI.API_MYUI_getPushRequestParams(data)
                    ,new RequestCallBack() {
                        @Override
                        public void onFinish() {
                            try {
                            	Log.i(TAG,"active response-->"+getResponse());
					//del by zhengguang.yang@20170110 start for country new rule 
                            	//add by zhengguang.yang@20160517 start for active
                            	//boolean isGprsOpened = CommonUtils.getGprsOpened(getApplicationContext());
                            	//Log.i(TAG,"active response-->isGprsOpened"+isGprsOpened);
                            	//if(isGprsOpened){
                            	//	gprsEnabled(false);
                            	//	CommonUtils.setGprsOpened(getApplicationContext(), false);
                            	//}
                            	//add by zhengguang.yang@20160517 end for active
                            	//del by zhengguang.yang end
                            	
                                if (!TextUtils.isEmpty(getResponse())) {
                                    BaseResponse baseResponse = getV2BaseResponse();
                                    if(baseResponse != null && baseResponse.checkValid()){
                                    	String data = baseResponse.getDecodeBusinessData();
                                    	Log.i(TAG,"active baseResponse-->data="+data);
                                    	//add by zhengguang.yang@20160517 start for active
                                    	CommonUtils.setUploadedIntervalDate(getApplicationContext());
                                    	//add by zhengguang.yang@20160517 end for active
                                    }
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                                Log.i(TAG,"active fail-->"+e.toString());
                            }
                            
                          //add by zhengguang.yang@20160517 start for active
                            activeFailIdx = CommonUtils.getActiveFailCount(getApplicationContext());
                            if(TextUtils.isEmpty(CommonUtils.getUploadedIntervalDate(getApplicationContext()))){
                				activeFailIdx++;
                				CommonUtils.setActiveFailCount(getApplicationContext(), activeFailIdx);
                				if(activeFailIdx >= TOATAL_ACTIVE_FAIL_COUNT){//每次等待两个小时，重新激活，5次失败后，走正常config流程
                					mServiceHandler.sendEmptyMessageDelayed(MSG_DO_ACTIVE_END, ACTIVE_PEROID);
                				}else{
                					mServiceHandler.sendEmptyMessageDelayed(MSG_DO_ACTIVE_START, ACTIVE_FAIL_PEROID);
                				}
                            }else{
                            	mServiceHandler.sendEmptyMessageDelayed(MSG_DO_ACTIVE_END, ACTIVE_PEROID);
                            }
                          //add by zhengguang.yang@20160517 end for active
                        }
                    });
        }catch (Throwable e){
            e.printStackTrace();
            Log.i(TAG,"fail -->"+e.toString());
        }		
	}
	//add by zhengguang.yang@20160517 end for active
	
	private void initCache(){
		//初始化cache
		mCache = ACache.get(this);
		mConfigResponse = (ConfigResponse) mCache.getAsObject(KEY_CONFIG_RESPONSE);
		if(mConfigResponse == null){
			if(CommonUtils.hasNetWork(NewPushService.this)){
				config();
			}else{
				// register the network BroadcastReceiver
				IntentFilter networkIntentFilter = new IntentFilter();
				networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
					//add by zhengguang.yang@20161216 start for unregister broadcast exception
					if(mNetWorkChangeBroadcastReceiver == null){
						mNetWorkChangeBroadcastReceiver = new NetWorkChangeBroadcastReceiver();
					}
					//add by zhengguang.yang end
				registerReceiver(mNetWorkChangeBroadcastReceiver, networkIntentFilter);
			}
		}else{
			sendIntervalMessage();
		}
	}
	
	private void sendIntervalMessage(){
		mServiceHandler.sendEmptyMessage(MSG_INTERVAL_PUSH);
		mServiceHandler.sendEmptyMessage(MSG_INTERVAL_PULL);
	}
	
	//add by zhengguang.yang@20160420 start for improve code
	private static final long NET_PERIOD = 30*1000;
	private long lastConfigTime;
	private long lastPushTime;
	private long lastPullTime;
	private long lastActiveTime;
	
	private int pullIdx = 0;
	private int pushIdx = 0;
	private int activeIdx = 0;
	private int activeFailIdx = 0;
	private static final int TOATAL_ACTIVE_FAIL_COUNT = 5;
	private static final long ACTIVE_FAIL_PEROID = 2*60*60*1000;//2hour
	
	private static final int PEROID = 2*60*1000;//2min
	private static final int ACTIVE_PEROID = 10*1000;//10s
	private static final int TOATAL_COUNT = 10;
	private String ipAddress = IP_NULL;
	//add by zhengguang.yang@20160420 end for improve code
	private void config(){
		try{
			//add by zhengguang.yang@20160420 start for improve code
			long nowtime = System.currentTimeMillis();
	    	if (nowtime - lastConfigTime < NET_PERIOD) return;
	    	lastConfigTime = nowtime;
			//add by zhengguang.yang@20160420 end for improve code
			
            boolean ret = ServiceAPI.API_MYUI_Request_Async(
                    ServiceAPI.API_MYUI_getConfigRequestParams()
                    ,new RequestCallBack() {
                        @Override
                        public void onFinish() {
                            try {
                                Log.i(TAG,"config response-->"+getResponse());
                                if (!TextUtils.isEmpty(getResponse())) {
                                    BaseResponse baseResponse = getV2BaseResponse();
                                    Log.i(TAG,"config baseResponse="+baseResponse); 
                                	String businessData = ServiceAPI.API_MYUI_ParserBaseResponse(baseResponse);
                                	Gson gson = new Gson();
                                	mConfigResponse = gson.fromJson(businessData, ConfigResponse.class);
                                	if(mConfigResponse != null){
                                		int msgExpiredTime = mConfigResponse.getMsgExpiredTime();
                                		if(msgExpiredTime == 0){
                                			mCache.put(KEY_CONFIG_RESPONSE,mConfigResponse,DEFAULT_CONFIG_EXPIRED_TIME);
                                		}else{
                                			mCache.put(KEY_CONFIG_RESPONSE,mConfigResponse,msgExpiredTime*24*60*60);
                                		}
                                		sendIntervalMessage();
                                	}
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                                Log.i(TAG,"fail-->"+e.toString());
                            }
                        }
                    });
        }catch (Throwable e){
            e.printStackTrace();
            Log.i(TAG,"bindAccount fail -->"+e.toString());
        }
	}
	
	private void pull(){
		try{
			if(!CommonUtils.hasNetWork(getApplicationContext())){
				return;
			}
			//if ipaddress is null,delay 2min,check ipaddress;until 10 times,if the last time is null,start push
			if(TextUtils.isEmpty(getIpAddress())){
				pullIdx++;
				if(pullIdx == TOATAL_COUNT){
					pullIdx = 0;
				}else{
					mServiceHandler.sendEmptyMessageDelayed(MSG_DO_PULL, PEROID);
					return;
				}
			}else{
				pullIdx = 0;
			}
			
			//if the time between first request with second request less than 30s,the second request not resolve
			long nowtime = System.currentTimeMillis();
	    	if (nowtime - lastPullTime < NET_PERIOD) return;
	    	lastPullTime = nowtime;
			//add by zhengguang.yang@20160420 end for improve code
			String ipadrs;
			if(!TextUtils.isEmpty(ipAddress)){
				ipadrs=ipAddress;
			}else {
				ipadrs=getIpAddress();
			}
            boolean ret = ServiceAPI.API_MYUI_Request_Async(
                    ServiceAPI.API_MYUI_getPullRequestParams(getDeviceId(getApplicationContext()),PUSH_CLIENT_VERSION_CODE,getProductModel(),ipadrs)
                    ,new RequestCallBack() {
                        @Override
                        public void onFinish() {
                            try {
                            	Log.d(TAG,"pull response-->"+getResponse());
                                if (!TextUtils.isEmpty(getResponse())) {
                                    BaseResponse baseResponse = getV2BaseResponse();
                                    if(baseResponse != null && baseResponse.checkValid()){
                                    	String data = baseResponse.getDecodeBusinessData();
                                    	Log.d(TAG,"pull baseResponse-->"+data);
                                    	Message msg = mServiceHandler.obtainMessage();
                                    	msg.what = MSG_NOTIFICATION_PULL;
                                    	msg.obj = data;
                                    	mServiceHandler.sendMessage(msg);
                                    }
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                                Log.i(TAG,"fail-->"+e.toString());
                            }
                        }
                    });
        }catch (Throwable e){
            e.printStackTrace();
            Log.i(TAG,"bindAccount fail -->"+e.toString());
        }
	}
	
	private void push() {
		try{
			//add by zhengguang.yang@20160420 start for improve code
			if(!CommonUtils.hasNetWork(getApplicationContext())){
				return;
			}
			
//if ipaddress is null,delay 2min,check ipaddress;until 10 times,if the last time is null,start push
			if(TextUtils.isEmpty(getIpAddress())){
				pushIdx++;
				if(pushIdx == TOATAL_COUNT){
					pushIdx = 0;
				}else{
					mServiceHandler.sendEmptyMessageDelayed(MSG_DO_PUSH, PEROID);
					return;
				}
			}else{
				pushIdx = 0;
			}
			
			//if the time between first request with second request less than 30s,the second request not resolve
			long nowtime = System.currentTimeMillis();
	    	if (nowtime - lastPushTime < NET_PERIOD) return;
	    	lastPushTime = nowtime;
			//add by zhengguang.yang@20160420 end for improve code
			
			byte[] registerAppData = getRegisterAppData();
//			String data = new String(buildSendByte(registerAppData, YPushConfig.register_update));
			String data = new String(registerAppData);
			boolean ret = ServiceAPI.API_MYUI_Request_Async(
                    ServiceAPI.API_MYUI_getPushRequestParams(data)
                    ,new RequestCallBack() {
                        @Override
                        public void onFinish() {
                            try {
                            	Log.i(TAG,"push response-->"+getResponse());
                                if (!TextUtils.isEmpty(getResponse())) {
                                    BaseResponse baseResponse = getV2BaseResponse();
                                    if(baseResponse != null && baseResponse.checkValid()){
                                    	String data = baseResponse.getDecodeBusinessData();
                                    	Log.i(TAG,"push baseResponse-->data="+data);
                                    	//add by zhengguang.yang@20160517 start for active
                                    	CommonUtils.setUploadedIntervalDate(getApplicationContext());
                                    	//add by zhengguang.yang@20160517 end for active
                                    }
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                                Log.i(TAG,"fail-->"+e.toString());
                            }
                        }
                    });
        }catch (Throwable e){
            e.printStackTrace();
            Log.i(TAG,"bindAccount fail -->"+e.toString());
        }		
	}
	
	
	public byte[] getRegisterAppData() throws RemoteException {
		byte[] registerAppData = null;
		boolean protocolbuf = false;// remove protocalbuf

		if (protocolbuf) {
			Context c = getApplicationContext();
			Map dmap = getAllDetail(c);
			JSONObject textjs = new JSONObject(dmap);
			String registerstr = textjs.toString();
			Log.i(TAG,"protocol buf registerstr:" + registerstr);
			byte[] cInfoRegister = generateCommonBody(registerstr);

			Log.i(TAG,"protocol buf len:" + cInfoRegister.length);

			return cInfoRegister;
		} else {
			Context c = getApplicationContext();
			Map dmap = getAllDetail(c);
			JSONObject cmdtextjs = new JSONObject(dmap);
			String registerstr = cmdtextjs.toString();
			Log.i(TAG,"str registerstr:" + registerstr);
			byte[] cInfoRegister = ByteOrStringHelper.StringToByte(registerstr);

			Log.i(TAG,"str len:" + cInfoRegister.length);

			return cInfoRegister;
		}

		// return registerAppData;
	}
	
	
	public byte[] buildSendByte(byte[] sendByte, String type) {
		String headS = new CommonUtils().getSendStringHead(type, sendByte.length);
		StringBuffer buffer = new StringBuffer();
		buffer.append(headS);
		buffer.append(new String(sendByte));
		Log.i(TAG,"str buffer:" + buffer.toString());

		byte[] headByte = headS.getBytes();
		byte[] newData = new byte[headS.getBytes().length + sendByte.length];

		System.arraycopy(headByte, 0, newData, 0, headByte.length);
		System.arraycopy(sendByte, 0, newData, headByte.length, sendByte.length);
		return newData;
	}
	//add by zhengguang.yang@20160122 start for new push service
	private String getDeviceId(Context c){
		SIMCardInfo si = new SIMCardInfo(c);
		String imei = si.getIMEI();
		if(imei == null){
			imei = "";
		}
		Log.i(TAG,"imei:"+imei);
		return imei;
	}
	//modified by zhengguang.yang@20160421 start for not make sure real time,only have value
	private synchronized String getIpAddress(){
		Log.i(TAG,"getIpAddress-->ipAddress="+ipAddress);
		if(TextUtils.isEmpty(ipAddress)){
			ipAddress = IP_NULL;
			for (String ipaddr : IP_ADDRS) {
				ipAddress = GetNetIp(ipaddr);
				if (!IP_NULL.equals(ipAddress)) break;
			}
		}
		return ipAddress;
	}
	//modified by zhengguang.yang@20160421 end for not make sure real time,only have value
	//add by zhengguang.yang end
	
	public Map getAllDetail(Context c) {
		SIMCardInfo si = new SIMCardInfo(c);
		String imei = si.getIMEI();
		// String simd = si.getSIMID();

		// String line1Number = si.getLine1Number();

		// add for service
		if (imei == null) {
			imei = "";
		}

		// if (line1Number == null) {
		// line1Number = "";
		// }

		// zhaoshh0325,get location
		String longitudeLatitude;
		if ("1".equals(myDBUtil.myDBQuery("haveLocation"))) {
			String Longitude = myDBUtil.myDBQuery("Longitude");
			String Latitude = myDBUtil.myDBQuery("Latitude");
			Log.i(TAG,"getAllDetail: Longitude=" + Longitude + " ,Latitude=" + Latitude);
			StringBuffer lalo = new StringBuffer();
			lalo.append(Longitude);
			lalo.append(",");
			lalo.append(Latitude);
			longitudeLatitude = lalo.toString();
		} else {
			longitudeLatitude = "";
		}

		DisplayMetrics dm = new DisplayMetrics();
		//modified by zhengguang.yang@20170802 start for get real physical screenSize
		//dm = getApplicationContext().getResources().getDisplayMetrics();
		WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE); 
		windowManager.getDefaultDisplay().getRealMetrics(dm);
		//end by zhengguang.yang
		int resoWidth = dm.widthPixels;
		int resoHeight = dm.heightPixels;
		Log.i(TAG,"resoWidth =" + resoWidth + " ,resoHeight=" + resoHeight);
		//modified by zhengguang.yang@20170802 start for get real physical screenSize
		/*
		float curDensity = dm.density;
		int curDensityDpi = dm.densityDpi;
		Log.i(TAG,"curDensity=" + curDensity + " ,curDensityDpi=" + curDensityDpi);

		double diagonalPixels = Math.sqrt(Math.pow(resoWidth, 2) + Math.pow(resoHeight, 2));
		float screenSize = (float) diagonalPixels / (160 * curDensity);
		Log.i(TAG,"diagonalPixels=" + diagonalPixels + " ,screenSize=" + screenSize);
		*/
		Configuration config = getResources().getConfiguration();
		if(config.orientation == Configuration.ORIENTATION_LANDSCAPE){
			resoWidth = dm.heightPixels;
			resoHeight = dm.widthPixels;
			Log.i(TAG,"resoWidth change=" + resoWidth + " ,resoHeight=" + resoHeight);
		}
	    float screenSize = (float)Math.sqrt(Math.pow(resoWidth/dm.xdpi, 2) + Math.pow(resoHeight/dm.ydpi, 2));  
	    Log.i(TAG, "screenSize : " + screenSize+",xdpi="+dm.xdpi+",ydpi="+dm.ydpi);
		//end by zhengguang.yang
		String resolution = Integer.toString(resoWidth) + "*" + Integer.toString(resoHeight);
		// add by xiaolin.he 20150915 start . for test 
//		String strIpAddress = IP_NULL;
//		for (String ipaddr : IP_ADDRS) {
//			strIpAddress = GetNetIp(ipaddr);
//			if (!IP_NULL.equals(strIpAddress)) break;
//		}
		String strIpAddress;//modified by zhengguang.yang@20160421 for ipaddress is null when first push
		if(!TextUtils.isEmpty(ipAddress)){
			strIpAddress=ipAddress;
		} else {
			strIpAddress=getIpAddress();
		}
		// add by xiaolin.he 20150915 end .

		// List apps = new ArrayList();
		// ThirdInstalledApp ta = new ThirdInstalledApp();
		// ArrayList<InstalledApp> list = new ArrayList<InstalledApp>();
		// list = ta.getInstalledApps(c, false);
		// for (InstalledApp iApp : list) {
		// apps.add(iApp.getPackageName());
		// }
		String netType = PhoneInfoUtil.getNetType(c);
		// yang.yang2 add for push version
		String versionCode = PUSH_CLIENT_VERSION_CODE;
		String romVersion = getMyUIVersion();
		String productModel = getProductModel();
		String aliRomVersion = getAliRomVersion();
		//add by zhengguang.yang@20160607 start for add displayVersion
		String displayVersion = getDisplayVersion();
		//add by zhengguang.yang@20160607 start for add displayVersion
		//add by zhengguang.yang@20170410 start for romchannel
		String romchannel = SystemProperties.get("ro.build.myui.romchannel");
		//end by zhengguang.yang 
		//add by zhengguang.yang@20170731 start for add mhlSn
		String mhlSn = SystemProperties.get("sys.mhl.sn", "");
		//end by zhengguang.yang

		List<JSONObject> appInfoList = CommonUtils.getMyPhoneAppList(c);

		Map reMap = new HashMap();

		reMap.put("deviceId", imei);
		reMap.put("netType", netType);
		// reMap.put("simSn", simd);

		if (isMtkRom() && (CommonUtils.hasIccCardMtk(0) && CommonUtils.hasIccCardMtk(1))) {
			String providersType = CommonUtils.getOperatorsName(c, 0);
			String providersType2 = CommonUtils.getOperatorsName(c, 1);
			if (providersType == null) {
				providersType = "unknow";
			}
			if (providersType2 == null) {
				providersType2 = "unknow";
			}
			reMap.put("simcardType", providersType);
			reMap.put("simcardType2", providersType2);
		} else {
			String providersType = CommonUtils.getOperatorsName(c);
			if (providersType == null) {
				providersType = "";
			}
			reMap.put("simcardType", providersType);
		}
		reMap.put("longitudeLatitude", longitudeLatitude);
		// reMap.put("area", line1Number);
		reMap.put("channel", "GoMsgClient");
		reMap.put("ipAddress", strIpAddress); // server do not need this any more . 20150701
		reMap.put("resolution", resolution);
		reMap.put("screenSize", String.valueOf(screenSize));
		// reMap.put("apps", apps);
		reMap.put("versionCode", versionCode);
		reMap.put("romVersion", romVersion);
		reMap.put("productModel", productModel);
		reMap.put("aliRomVersion", aliRomVersion);
		//add by zhengguang.yang@20160607 start for add displayVersion
		reMap.put("displayVersion", displayVersion);
		//add by zhengguang.yang@20160607 end for add displayVersion
		//add by zhengguang.yang@20170410 start for romchannel
		reMap.put("romchannel", romchannel);
		//end by zhengguang.yang end
		
		//add by zhengguang.yang@20170731 start for add mhlSn
		reMap.put("mhlSn",mhlSn);
		//end by zhengguang.yang
		

		if (isMtkRom()) {
			if (CommonUtils.hasIccCardMtk(0) && CommonUtils.hasIccCardMtk(1)) {
				String cellId1 = String.valueOf(CommonUtils.getCellId(0));
				reMap.put("simCellId", cellId1);
				String cellId2 = String.valueOf(CommonUtils.getCellId(1));
				reMap.put("sim2CellId", cellId2);
			} else {
				String cellId1 = String.valueOf(CommonUtils.getCellId(c));
				reMap.put("simCellId", cellId1);
			}
		} else {
			if (CommonUtils.hasIccCard(c)) {
				String cellId1 = String.valueOf(CommonUtils.getCellId(c));
				reMap.put("simCellId", cellId1);
			}
		}
		reMap.put("appInfoList", appInfoList);
		Log.i(TAG,"getAllDetail reMap:" + reMap);

		return reMap;
	}
	
	
	public byte[] generateCommonBody(String strJson) {
		Log.i(TAG,"generateCommonBody str:" + strJson);
		commonMsg.Builder commBulider = commonMsg.newBuilder();
		commBulider.setCommText(strJson);
		commonMsg cMsg = commBulider.build();
		int len = cMsg.getSerializedSize();
		Log.i(TAG,"generateCommonBody len:" + len);
		byte[] buf = cMsg.toByteArray();

		return buf;
	}

	//add by zhengguang.yang@20160607 start for add displayVersion
	public static String getDisplayVersion() {
		String version = Build.DISPLAY;
		return version.trim();
	}
	//add by zhengguang.yang@20160607 end for add displayVersion
	
	
	// yang.yang2@qingcheng.com add for get MyUI version
	public static String getMyUIVersion() {
		String myUIVersion = "";
		if (isMtkRom()) {
			myUIVersion = SystemProperties.get("ro.build.myui.id", "");
		} else {
			myUIVersion = Build.DISPLAY;
		}
		return myUIVersion.trim();
	}
	
	// yang.yang2@qingcheng.com add for get ProductModel
	public static String getProductModel() {
		String productName = "";
		productName = Build.MODEL;
		return productName.trim();
	}

	// yang.yang2@qingcheng.com add for get AliRomVersion
	public static String getAliRomVersion() {
		String aliVersion = "";
		aliVersion = SystemProperties.get("ro.yunos.update.version", "");
		return aliVersion.trim();
	}
	
	private static boolean isMtkRom() {
		if(getProductModel().equals("GO M2S") || getProductModel().equals("GO M3")){
			return false;
		}else {
			return true;
		}
	}
	
	
	// add by xiaolin.he 20150922 start . get the default active IP address
		private static final String IP_NULL = "";
		private static final String IP_PATTERN = "\\b([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}\\b";
		private static final String [] IP_ADDRS = {
			"http://www.qingcheng.com/plugin/getip", 
			"http://ip.taobao.com/service/getIpInfo2.php?ip=myip", 
			"http://city.ip138.com/ip2city.asp"};

		private String GetNetIp(String ipaddr) {
			URL infoUrl = null;
			InputStream inStream = null;
			try {
				infoUrl = new URL(ipaddr);
				URLConnection connection = infoUrl.openConnection();
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				int responseCode = httpConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					inStream = httpConnection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "gb2312"));
					StringBuilder strber = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						strber.append(line + "\n");
					}
					inStream.close();
					Log.i(TAG,"GetNetIp(" + ipaddr + ") == " + strber.toString());
					Pattern pattern = Pattern.compile(IP_PATTERN);
					Matcher matcher = pattern.matcher(strber);
					if (matcher.find()) {
						String matcherIp = matcher.group();
						Log.i(TAG,"matcher.find() true matched ip = " + matcherIp);
						if (!isInnerIP(matcherIp)) {
							return matcherIp;
						}
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return IP_NULL;
		}

		public static boolean isInnerIP(String ipAddress) {
			boolean isInnerIp = false;
			long ipNum = getIpNum(ipAddress);
			// 私有IP：A类 10.0.0.0-10.255.255.255 B类 172.16.0.0-172.31.255.255 C类 192.168.0.0-192.168.255.255 当然，还有127这个网段是环回地址
			long aBegin = getIpNum("10.0.0.0");
			long aEnd = getIpNum("10.255.255.255");
			long bBegin = getIpNum("172.16.0.0");
			long bEnd = getIpNum("172.31.255.255");
			long cBegin = getIpNum("192.168.0.0");
			long cEnd = getIpNum("192.168.255.255");
			isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || ipAddress.equals("127.0.0.1");
			Log.i(TAG,ipAddress + " is inner ip ? " + isInnerIp);
			return isInnerIp;
		}

		private static long getIpNum(String ipAddress) {
			String[] ip = ipAddress.split("\\.");
			long a = Integer.parseInt(ip[0]);
			long b = Integer.parseInt(ip[1]);
			long c = Integer.parseInt(ip[2]);
			long d = Integer.parseInt(ip[3]);

			long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
			return ipNum;
		}

		private static boolean isInner(long userIp, long begin, long end) {
			return (userIp >= begin) && (userIp <= end);
		}
		// add by xiaolin.he 20150922 end .
		
		//add by zhengguang.yang@20161216 start for broadcast unregister exception
		/*
		BroadcastReceiver mNetWorkChangeBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "mNetWorkChangeBroadcastReceiver onReceive, intent=" + intent);

				if (intent == null || intent.getAction() == null) {
					return;
				}

				if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					//add by zhengguang.yang@20160420 start for improve code 
					if(mCache != null){
						mConfigResponse = (ConfigResponse) mCache.getAsObject(KEY_CONFIG_RESPONSE);
					}
					//add by zhengguang.yang@20160420 end for improve code
					if(CommonUtils.hasNetWork(NewPushService.this)&&mConfigResponse == null) {
						config();
					}
				}
			}

	};*/
	NetWorkChangeBroadcastReceiver mNetWorkChangeBroadcastReceiver;
	class NetWorkChangeBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "NetWorkChangeBroadcastReceiver onReceive, intent=" + intent);

			if (intent == null || intent.getAction() == null) {
				return;
			}

			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				//add by zhengguang.yang@20160420 start for improve code 
				if(mCache != null){
					mConfigResponse = (ConfigResponse) mCache.getAsObject(KEY_CONFIG_RESPONSE);
				}
				//add by zhengguang.yang@20160420 end for improve code
				if(CommonUtils.hasNetWork(NewPushService.this)&&mConfigResponse == null) {
					config();
				}
			}
		}
		
	}
	//add by zhengguang.yang end
	
	
	//add by zhengguang.yang@20160919 start for push user behavior
	public String getPushBehaviorData(PushBehaviorData pushBehaviorData){
		SIMCardInfo si = new SIMCardInfo(getApplicationContext());
		String imei = si.getIMEI();
		if (imei == null) {
			imei = "";
		}
		String ipadrs;
		if(!TextUtils.isEmpty(ipAddress)){
			ipadrs=ipAddress;
		}else {
			ipadrs=getIpAddress();
		}
		Context c = getApplicationContext();
		Map map = new HashMap();
		map.put("msgId", pushBehaviorData.getMsgId());
		map.put("behavior", pushBehaviorData.getBehavior());
		map.put("behaviorTime", TimeUtils.getCurrentTime());
		map.put("deviceId", imei);
		map.put("productModel", getProductModel());
		map.put("pushVersion", PUSH_CLIENT_VERSION_CODE);
		map.put("romVersion", getMyUIVersion());
		map.put("appId", pushBehaviorData.getAppId());
		map.put("packageName", pushBehaviorData.getPackageName());
		map.put("ipAddress", ipadrs);
		//add by zhengguang.yang@20170224 start for update METHOD_PUSH_BEHAVIOR
		map.put("netType",PhoneInfoUtil.getNetType(getApplicationContext()) );
		map.put("client", "UserService");
		map.put("clientVersion", AndroidUtil.getClientVersion(getApplicationContext()));
		//add by zhengguang.yang end

		JSONObject cmdtextjs = new JSONObject(map);
		String registerstr = cmdtextjs.toString();
		Log.i(TAG,"000 registerstr:" + registerstr);
		byte[] cInfoRegister = ByteOrStringHelper.StringToByte(registerstr);
		Log.i(TAG,"111 len:" + cInfoRegister.length);
		String data = new String(cInfoRegister);
		Log.i(TAG,"222 data:"+data);
		return data;
	}
	
	private void pushBehavior(PushBehaviorData pushBehaviorData) {
		try{
			//add by zhengguang.yang@20160420 start for improve code
			if(!CommonUtils.hasNetWork(getApplicationContext())){
				return;
			}
			
			String data = getPushBehaviorData(pushBehaviorData);
			boolean ret = ServiceAPI.API_MYUI_Request_Async(
                    ServiceAPI.API_MYUI_getPushBehaviorRequestParams(data)
                    ,new RequestCallBack() {
                        @Override
                        public void onFinish() {
                            try {
                            	Log.i(TAG,"pushBehavior response-->"+getResponse());
                                if (!TextUtils.isEmpty(getResponse())) {
                                    BaseResponse baseResponse = getV2BaseResponse();
                                    if(baseResponse != null && baseResponse.checkValid()){
                                    	String data = baseResponse.getDecodeBusinessData();
                                    	Log.i(TAG,"pushBehavior baseResponse-->data="+data);
                                    }
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                                Log.i(TAG,"pushBehavior fail-->"+e.toString());
                            }
                        }
                    });
        }catch (Throwable e){
            e.printStackTrace();
            Log.i(TAG,"pushBehavior fail -->"+e.toString());
        }		
	}
	
	//add by zhengguang.yang

	// add by sherry start
	public rowAndData getPushStatisticsEventData(EventStatisticsData eventStatisticsData, boolean flag, int id) {
		SIMCardInfo si = new SIMCardInfo(getApplicationContext());
		String imei = si.getIMEI();
		if (imei == null) {
			imei = "";
		}

		Context c = getApplicationContext();

		Map map = new HashMap();
		//insert cv
		ContentValues cv = new ContentValues();

		map.put("dv", imei);
		map.put("rv", getMyUIVersion());
		map.put("pm", getProductModel());
		Log.d("NewPushService", "ipAddress:" + IpAddress);
		map.put("ip", IpAddress);
		map.put("nt", PhoneInfoUtil.getNetType(getApplicationContext()));
		map.put("bht", TimeUtils.getCurrentTime());

		cv.put("deviceId", imei);
		cv.put("romVersion", getMyUIVersion());
		cv.put("productModel", getProductModel());

		cv.put("ipAddress", TextUtils.isEmpty(IpAddress) ? "0" : IpAddress);
		cv.put("netType", PhoneInfoUtil.getNetType(getApplicationContext()));
		cv.put("behaviorTime", TimeUtils.getCurrentTime());

		if (!TextUtils.isEmpty(eventStatisticsData.getBehavior())) {
			map.put("bh", eventStatisticsData.getBehavior());
			cv.put("behavior", eventStatisticsData.getBehavior());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getAppId())) {
			map.put("apid", eventStatisticsData.getAppId());
			cv.put("appId", eventStatisticsData.getAppId());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getAppSrc())) {
			map.put("apsr", eventStatisticsData.getAppSrc());
			cv.put("appSrc", eventStatisticsData.getAppSrc());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getPackageName())) {
			map.put("pk", eventStatisticsData.getPackageName());
			cv.put("packageName", eventStatisticsData.getPackageName());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getClient())) {
			map.put("cli", eventStatisticsData.getClient());
			cv.put("client", eventStatisticsData.getClient());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getListenArea())) {
			map.put("lsar", eventStatisticsData.getListenArea());
			cv.put("listenArea", eventStatisticsData.getListenArea());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getListenContextId())) {
			map.put("lscid", eventStatisticsData.getListenContextId());
			cv.put("listenContextId", eventStatisticsData.getListenContextId());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getListenContextSrc())) {
			map.put("lscsrc", eventStatisticsData.getListenContextSrc());
			cv.put("listenContextSrc", eventStatisticsData.getListenContextSrc());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getClientVersion())) {
			map.put("cliv", eventStatisticsData.getClientVersion());
			cv.put("clientVersion", eventStatisticsData.getClientVersion());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getReferenceId())) {
			map.put("ref", eventStatisticsData.getReferenceId());
			cv.put("referenceId", eventStatisticsData.getReferenceId());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getAppName())) {
			map.put("apnm", eventStatisticsData.getAppName());
			cv.put("appName", eventStatisticsData.getAppName());
		}
		if (!TextUtils.isEmpty(eventStatisticsData.getAppVersion())) {
			map.put("apvn", eventStatisticsData.getAppVersion());
			cv.put("appVersion", eventStatisticsData.getAppVersion());
		}
		cv.put("mark", "0");

		long row = -1;
		if (flag == true) {
			//insert to db
			try {
				myDBUtil = myDBUtil.open();
				row = myDBUtil.add("EventStatisticsDB", cv);
				myDBUtil.close();
				Log.d("NewPushService", "insert row is" + row);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("NewPushService", "inserto db fail");
			}
		} else {
			row = id;
		}

		JSONObject cmdtextjs = new JSONObject(map);
		String registerstr = cmdtextjs.toString();
		Log.d(TAG, "000 JSONObject to String:" + registerstr);
		byte[] cInfoRegister = ByteOrStringHelper.StringToByte(registerstr);
		Log.d(TAG, "111 String to byte;s length:" + cInfoRegister.length);
		String data = new String(cInfoRegister);
		Log.d(TAG, "222 byte to string:" + data);

		return new rowAndData(row, data);
	}


	/**
	 * @param eventStatisticsData
	 * @param flag                whether insert to db
	 * @param id                  if doesn't need to insert to db,whitch id needs to update
	 */
	private void pushStatisticsEvent(EventStatisticsData eventStatisticsData, boolean flag, int id) {
		try {
			//ready to set data to db,if push data successfully,mark this data choose a right time to delete the data
			//which has been marked
			//should check whether there are some datas in db

			if (!CommonUtils.hasNetWork(getApplicationContext())) {
				final rowAndData rad = getPushStatisticsEventData(eventStatisticsData, true, -1);    //some data is null,insert to db,wait for push
				Log.d(TAG, "no network insert to db:" + rad.getRow());
				return;
			}
			final rowAndData rad = getPushStatisticsEventData(eventStatisticsData, flag, id);
			boolean ret = ServiceAPI.API_MYUI_Request_Async(
					ServiceAPI.API_MYUI_getPushEventStatisticsRequestParams(rad.getData())
					, new RequestCallBack() {
						@Override
						public void onFinish() {
							try {
								if (!TextUtils.isEmpty(getResponse())) {
									BaseResponse baseResponse = getV2BaseResponse();
									if (baseResponse != null && baseResponse.checkValid()) {
										String result = baseResponse.gwResult;
										Log.d("NewPushService", "StatisticsEvent result=" + result);
										if (rad.getRow() > -1) {//result.equals("sucess")&&rad.getRow()>-1){
											ContentValues contentValues = new ContentValues();
											contentValues.put("mark", "1");
											try {
												Log.d("NewPushService", "update db id:" + rad.getRow());
												myDBUtil = myDBUtil.open();
												myDBUtil.update("EventStatisticsDB", (int) rad.getRow(), contentValues);
												myDBUtil.close();
											} catch (Exception e) {
												e.printStackTrace();
												Log.d("NewPushService", "update db fail");
											}
										}
									}
								}
							} catch (Throwable e) {
								e.printStackTrace();
								Log.d("NewPushService", "pushBehavior fail-->" + e.toString());
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("NewPushService", "pushStatisticsEvent fail -->" + e.toString());
		}

	}

	public class rowAndData {
		long row;
		String data;

		public rowAndData(long row, String data) {
			super();
			this.row = row;
			this.data = data;
		}

		public long getRow() {
			return row;
		}

		public String getData() {
			return data;
		}

	}

	public interface GetIpCallback {
		public void solve(String result);
	}

	public void GetIPFromServer(final GetIpCallback callback) {
		try {
			//add by zhengguang.yang@20160420 start for improve code
			if (!CommonUtils.hasNetWork(getApplicationContext())) {
				callback.solve(null);
				return;
			}
			boolean ret = ServiceAPI.API_MYUI_Request_Async(
					ServiceAPI.API_MYUI_getIPRequestParams()
					, new RequestCallBack() {
						@Override
						public void onFinish() {
							try {
								Log.d(TAG, "getip response-->" + getResponse());
								if (!TextUtils.isEmpty(getResponse())) {
									BaseResponse baseResponse = getV2BaseResponse();
									if (baseResponse != null && baseResponse.checkValid()) {
										String data = baseResponse.getDecodeBusinessData();
										Log.d(TAG, "getip baseResponse-->data=" + data);
										IpAddress = data;
										callback.solve(data);
									}
								}
							} catch (Throwable e) {
								e.printStackTrace();
								Log.d(TAG, "getip fail-->" + e.toString());
							}
						}
					});
		} catch (Throwable e) {
			e.printStackTrace();
			Log.d(TAG, "getip fail -->" + e.toString());
		}
	}

	//for delete
	public boolean DeleteEventStatisticsFromDB() {
		myDBUtil.open();
		boolean result = myDBUtil.deleteEventStatistics();
		myDBUtil.close();
		return result;
	}

	//for push statistics which ipAddress is null and mark is 0
	public void PushEventStatisticsFromDB() {
		myDBUtil.open();
		Cursor mCursor = myDBUtil.queryMarkIsZero();
		mCursor.moveToFirst();
		do {
			final EventStatisticsData eventStatisticsData = new EventStatisticsData();
			String[] colums = new String[]{
					"behavior", "appId", "appSrc", "packageName", "client", "listenArea",
					"listenContextId", "listenContextSrc", "clientVersion", "referenceId",
					"appName", "appVersion"};
			int index;
			String value;
			for (int i = 0; i < colums.length; i++) {
				index = mCursor.getColumnIndex(colums[i]);
				value = mCursor.getString(index);
				eventStatisticsData.SetParamFromApp(i, value);
			}
			index = mCursor.getColumnIndex("_id");
			final int id = mCursor.getInt(index);
			GetIPFromServer(new GetIpCallback() {
				@Override
				public void solve(String result) {
					if (result != null && eventStatisticsData != null) {
						pushStatisticsEvent(eventStatisticsData, false, id);
						Log.d(TAG, "im here don't insert to db,just update");
					}
				}
			});

		} while (mCursor.moveToNext());
		mCursor.close();
		myDBUtil.close();
	}

	//add by sherry for autoDownLoadInstall
	public void AutoDownLoadAndInstall(PullData pd) {
		RequestParams params = new RequestParams();
		params.put("_id", pd.getReferAppId());

		HttpClient.getClient(getApplicationContext()).get(YPushConfig.HTTP_HEAD +
				YPushConfig.API_APP_DETAIL_URL, params, getHandler(AppDetailResponse.class, pd.get_id(), pd.getReferAppId()));

	}

	private AutoInstallResponseHandler getHandler(Class<?> classzz, final String msgId, final String appId) {
		AutoInstallResponseHandler handler = new AutoInstallResponseHandler(classzz, msgId, appId) {
			@Override
			public void onSuccess(int arg0, cz.msebera.android.httpclient.Header[] arg1, byte[] arg2) {
				super.onSuccess(arg0, arg1, arg2);
				if (getDataSuccess) {
					if (arg2 != null) {
						String result = new String(arg2);
						System.out.println("----result----" + result);
						if (response instanceof AppDetailResponse) {
							res = (AppDetailResponse) response;
							Log.i(TAG, "AutoInstallResponseHandler onSuccess：res" + res);

							res.getData().setAppId(appId);
							res.getData().setMsgId(msgId);
							AppDownloadUtil.sendPushBehavior(NewPushService.this, YPushConfig.VIEWMSG, msgId, appId, res.getData().getPackageName());
							AppDownloadUtil.createDownloadListner(NewPushService.this, res.getData());
						}
					}
				}

			}
		};
		return handler;
	}

	private class AutoInstallResponseHandler extends AsyncHttpResponseHandler {
		private static final String TAG = "AutoInstallResponseHandler";
		protected com.ragentek.ypush.service.network.BaseResponse response;
		protected Gson gson = new Gson();
		protected Class<?> classzz;
		protected boolean getDataSuccess = false;
		private String msgId;
		private String appId;

		public AutoInstallResponseHandler(Class<?> classzz, String msgId, String appId) {
			super();
			this.classzz = classzz;
			this.msgId = msgId;
			this.appId = appId;
		}

		@Override
		public void onSuccess(int arg0, cz.msebera.android.httpclient.Header[] arg1, byte[] arg2) {
			if (System.currentTimeMillis() - HttpClient.beginTime > 5000) {
				HttpClient.isNetSpeedSlow = true;
			} else {
				HttpClient.isNetSpeedSlow = false;
			}
			if (arg2 != null) {
				String result = new String(arg2);
				System.out.println("---result success---" + result);
				try {
					response = (com.ragentek.ypush.service.network.BaseResponse) gson.fromJson(result, classzz);
					if (response.getStatus() == null || !response.getStatus().equals("1")) {
						getDataSuccess = false;
						showNetworkErrorMsg();
					} else {
						getDataSuccess = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					getDataSuccess = false;
				}
			}
		}

		@Override
		public void onFailure(int arg0, cz.msebera.android.httpclient.Header[] arg1, byte[] arg2, Throwable arg3) {
			if (arg2 != null) {
				String result = new String(arg2);
				System.out.println("---result failure---" + result);
				showNetworkErrorMsg();
			}
		}

		private void showNetworkErrorMsg() {
			Log.i(TAG, "showNetworkErrorMsg");
		}
	}
}
