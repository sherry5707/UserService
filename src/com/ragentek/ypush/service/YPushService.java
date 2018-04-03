package com.ragentek.ypush.service;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.james.mime4j.util.StringArrayMap;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ragentek.ypush.service.socket.SocketClient;
import com.ragentek.ypush.service.util.CommonUtils;
import com.ragentek.ypush.service.util.CoordinateHelper;
import com.ragentek.ypush.service.util.IpAddressHelper;
import com.ragentek.ypush.service.util.PhoneInfoUtil;
import com.ragentek.ypush.service.util.SIMCardInfo;
import com.ragentek.ypush.service.util.ThirdInstalledApp;
import com.ragentek.ypush.service.util.YPushConfig;
import com.ragentek.ypush.service.vo.ByteOrStringHelper;
import com.ragentek.ypush.service.vo.InstalledApp;
import com.ragentek.ypush.service.vo.commonMessage.commonMsg;
import com.ragentek.ypush.service.vo.hbpMessage.hbpMsg;
import com.ragentek.ypush.service.db.MyDatabaseUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;

import android.provider.Settings;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;

import java.net.MalformedURLException;

import android.os.AsyncTask;

public class YPushService extends Service {
	private static final String TAG = "YPushService";

	private SocketClient socketClient;

	private ReceiveThread receiveThread;

	private boolean ifSocketConn = false;

	private boolean recTFlag = true;

	private static long lLastCommDate = new Date().getTime();

	// add DB
	MyDatabaseUtil myDBUtil;

	private final static int UPDATE_LOCATION_TO_DB = 1;

	private final static int UPDATE_IP_ADDRESS_TO_DB = 2;

	private final static int BOOT_CONNECT_SOCKETCLIENT = 3;

	private final static int INTERVAL_CONNECT_SOCKETCLIENT = 4;

	private final static int MSG_DEFAULT_MSG_ID = 10;

    // modify by xiaolin.he 20150804 start . delay the first upload time since start or reboot
    // private final static long FROM_BOOT_COMPLETE_TIME = 10; // 10min--yangyang to be modify
    private final static long FROM_BOOT_COMPLETE_TIME = 16;
    // modify by xiaolin.he 20150804 end .

	private static long intervalTime = 0;

    // modify by xiaolin.he 20150804 start . change the default upload interval time to 12 hours
    // private static final long DEFAULT_INTERVAL_TIME = 24 * 60; // 24 hours
    private static final long DEFAULT_INTERVAL_TIME = 12 * 60;
    // modify by xiaolin.he 20150804 end .

	private AlarmManager mAlarmManager = null;

	private PendingIntent mAlarmPendingIntent = null;

    // modify by xiaolin.he 20150802 start . reverse the value of PERIOD from 24 hours to 10 minutes .
    private static final long PERIOD = 10*60*1000;//TODO llk 10 * 60 * 1000; // 10 minutes //yang.yang to modify
    // modify by xiaolin.he 20150802 end .

	private static boolean bootStayConnectStatus = false;

	private static boolean intervalStayConnectStatus = false;

	private static boolean bootHaveRandomFlag = false;

	private static boolean haveRandomFlag = false;

	private static int bootRandomTime = 5;

	private static int intervalRandomTime = 5;

	private static int bootConnectCount = 0;

	private static int intervalConnectCount = 0;

	private static String sLastNetWorkName = "";

	private static final long ONE_HOUR_DELAY = 1 * 60 * 60 * 1000;// 20 * 1000;
	public static final String ACTION_ONLY_ONE_REG = "android.intent.action.ACTION_ONLY_ONE_REG";
	public static final String ACTION_UPLOAD_BY_INTERVAL = "android.intent.action.UPLOAD_BY_INTERVAL";

	private static final String DATA_TYPE = "data_type";
	private static final String INTERVAL_DATA_UPLOAD = "interval_data_upload";

	public static final String ACTION_UPLOAD_RETRY = "android.intent.action.UPLOAD_RETRY";
	public static final String ACTION_RGK_LAUNCHER_DOWNLOAD_THEME = "com.ragentek.greenorangehome.theme_downloaded";

	// private final Intent mRegister = new Intent(ACTION_ONLY_ONE_REG);
	private final Intent mUploadByInterval = new Intent(
			ACTION_UPLOAD_BY_INTERVAL);
	private final Intent mUploadRetry = new Intent(ACTION_UPLOAD_RETRY);

	private int mRetryTimes;
	private static final int UPLOAD_DATA_MAX = 3;

	// zhaoshh0424 add handler in thread
	private Looper mHandlerLooper;

	// zhaoshh0424 add handler in thread
	private ServiceHandler mServiceHandler;

	// add have receive msg count
	private static int mReceiveMsgCount = 0;

	// yang.yang2 add for push version
	public final static String PUSH_CLIENT_VERSION_CODE = "PUSH_G2_4.3.13";//modified by zhengguang.yang@20160505 for add new feedback param

	// private Bitmap mIconBitmap;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.v(TAG, "onBind() called");

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate start");

		// init DB
		myDBUtil = new MyDatabaseUtil(getApplicationContext());
		try {
			if (!("1".equals(myDBUtil.myDBQuery("haveInitDB")))) {
				Log.v(TAG, "init DB try!");
				myDBUtil.initDb();
			}
		} catch (Exception e) {
			Log.v(TAG, "init DB catch!");
			myDBUtil.initDb();
		}

		// add for OTA,because ota not clear data/data, need recreate DB
		try {
			myDBUtil.myDBQuery("extend6");// notes: this column is the last column of DB
		} catch (Exception e) {
			Log.v(TAG, "init DB because extend6 catch!");
			boolean delFlag = getApplicationContext().deleteDatabase(YPushConfig.DATABASE_NAME);
			Log.v(TAG, "delete DB in catch. delFlag=" + delFlag);
			myDBUtil.initDb();
		}

//		// get location and update to DB
//		if (!("1".equals(myDBUtil.myDBQuery("haveLocation")))) {
//			Log.v(TAG, "to update location to DB!");
//			CoordinateHelper ch = new CoordinateHelper(getApplicationContext(),
//					mHandler);
//		}

		// test service is online
		// myServiceTestThread();

		// register the network BroadcastReceiver
		IntentFilter networkIntentFilter = new IntentFilter();
		networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetWorkChangeBroadcastReceiver, networkIntentFilter);

		// zhaoshh0424 add handler in thread
		HandlerThread thread = new HandlerThread("PushClient");
		thread.start();
		mHandlerLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mHandlerLooper);

		// boot or restart, set bootConnected flag
		myDBUtil.myDBUpdate("bootConnected", "0");

		// start alarm for interval connect socket
		mAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent toIntent = new Intent(this, YPushService.class);
		toIntent.putExtra("from", "alarm");
		mAlarmPendingIntent = PendingIntent.getService(this, 0, toIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + PERIOD, PERIOD, mAlarmPendingIntent);

		Log.d(TAG, "YPushService onCreate end");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		Log.d(TAG, "onStartCommand, intent = " + intent);
		if (intent == null) {
			return START_STICKY;
		}

        // delete by xiaolin.he 20150802 start . server do not need ip address any more
        /*
		// if have network, get ip address and update to DB
		if (!("1".equals(myDBUtil.myDBQuery("haveIpAddress")))) {
			ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connectivityManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				Log.v(TAG, "to get ip to DB!");
				String curDomain = correctCurrentDomain();
				IpAddressHelper iph = new IpAddressHelper(curDomain, mHandler);
			}
		}
        */
        // delete by xiaolin.he 20150802 end .


		Message msg = mServiceHandler.obtainMessage(MSG_DEFAULT_MSG_ID);
		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);

		return START_REDELIVER_INTENT;
	}

	// add by xiaolin.he 20150922 start . get the default active IP address
	private static final String IP_NULL = "";
	private static final String IP_PATTERN = "\\b([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}\\b";
	private static final String [] IP_ADDRS = {
		"http://www.qingcheng.com/plugin/getip", 
		"http://ip.taobao.com/service/getIpInfo2.php?ip=myip", 
		"http://city.ip138.com/ip2city.asp"};

	private static String GetNetIp(String ipaddr) {
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
				Log.i(TAG, "GetNetIp(" + ipaddr + ") == " + strber.toString());
				Pattern pattern = Pattern.compile(IP_PATTERN);
				Matcher matcher = pattern.matcher(strber);
				if (matcher.find()) {
					String matcherIp = matcher.group();
					Log.i(TAG, "matcher.find() true matched ip = " + matcherIp);
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
		Log.i(TAG, ipAddress + " is inner ip ? " + isInnerIp);
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

	@Override
	public void onDestroy() {
		super.onDestroy();

		shutDown();

		// cancel register
		if (mNetWorkChangeBroadcastReceiver != null) {
			unregisterReceiver(mNetWorkChangeBroadcastReceiver);
		}

		Log.d(TAG, "YPushService onDestroy");
	}

	BroadcastReceiver mNetWorkChangeBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(TAG, "mNetWorkChangeBroadcastReceiver onReceive, intent=" + intent);

			if (intent == null || intent.getAction() == null) {
				return;
			}

			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager connectivityManager;
				NetworkInfo info;
				connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo();

				if (info != null && info.isAvailable()) {
					String name = info.getTypeName();
					Log.d(TAG, "network name=" + name + " ,bootStayConnectStatus=" + bootStayConnectStatus + " ,intervalStayConnectStatus="
							+ intervalStayConnectStatus);

					if (bootStayConnectStatus) {
						// create socketClient in handler
						// zhaoshh0424 add handler in thread
						Message message = mServiceHandler.obtainMessage();
						message.what = BOOT_CONNECT_SOCKETCLIENT;
						// zhaoshh0424 add handler in thread
						mServiceHandler.sendMessage(message);
					} else {
						if (intervalStayConnectStatus) {
							// create socketClient in handler
							// zhaoshh0424 add handler in thread
							Message message = mServiceHandler.obtainMessage();
							message.what = INTERVAL_CONNECT_SOCKETCLIENT;
							// zhaoshh0424 add handler in thread
							mServiceHandler.sendMessage(message);
						}
					}

					Log.d(TAG, "sLastNetWorkName=" + sLastNetWorkName);

					if (sLastNetWorkName == null || "".equals(sLastNetWorkName)) {
						// if net work is wifi save visit flag to DB
						if ("WIFI".equals(name)) {
							Log.d(TAG, "current network is wifi, last network is null");
							// begin yang.yang2 add for delete ping action in
							// push client
							// myPingPushTimeThread();
							// end
						}
					} else {
						if (!(sLastNetWorkName.equals("WIFI"))) {
							// if net work is wifi save visit flag to DB
							if ("WIFI".equals(name)) {
								Log.d(TAG, "current network is wifi, last network is not wifi");
								// begin yang.yang2 add for delete ping action
								// in push client
								// myPingPushTimeThread();
								// end
							}
						}
					}

					sLastNetWorkName = name;// save last net work

				}
			}
		}

	};

	private void myServiceTestThread() {
		new Thread() {
			@Override
			public void run() {
				Log.v(TAG, "myServiceTestThread: bootStayConnectStatus=" + bootStayConnectStatus + ",intervalStayConnectStatus=" + intervalStayConnectStatus);

				while (true) {
					try {
						long tempTime = new Date().getTime();
						sleep(60000);
						long tempinterval = new Date().getTime() - tempTime;
						Log.v(TAG, "myServiceTestThread: mReceiveMsgCount=" + mReceiveMsgCount + " ,tempTime=" + tempTime + " ,tempinterval="
								+ tempinterval);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		}.start();
	}

	private void myPingPushTimeThread() {
		new Thread() {
			@Override
			public void run() {
				Log.v(TAG, "myPingPushTimeThread: start");
				int pingTime1 = 0;
				int pingTime2 = 0;

				// get push1 time
				try {
					String strLine1 = null;
					Process process1;

					long startTime1 = new Date().getTime();
					Log.v(TAG, "start ping push1");
					process1 = Runtime.getRuntime().exec("ping push1.qingcheng.com");
					long interval1 = new Date().getTime() - startTime1;
					Log.v(TAG, "startTime1=" + startTime1 + " ,interval1=" + interval1);

					BufferedReader br1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
					String line1 = br1.readLine();
					Log.v(TAG, "line1=" + line1);
					strLine1 = line1;

					for (; line1 != null;) {
						String nextLine1 = br1.readLine();
						line1 = nextLine1;
						Log.v(TAG, "myPingPushTimeThread: line1=" + line1);
						strLine1 = strLine1 + line1;
						if (strLine1.contains("time")) {
							Log.v(TAG, "have get the time. strLine1=" + strLine1);
							break;
						}
					}

					br1.close();

					String conntime1 = strLine1.substring(strLine1.lastIndexOf("time=") + 5, strLine1.lastIndexOf("ms") - 1);

					try {
						if (conntime1.contains(".")) {
							Log.v(TAG, "conntime is float time. ");
							float time1 = new Float(conntime1);
							pingTime1 = (int) time1;
							Log.v(TAG, "float time1=" + time1 + " ,pingTime1=" + pingTime1);
						} else {
							Log.v(TAG, "conntime is int time. ");
							pingTime1 = new Integer(conntime1);
						}
					} catch (Exception e) {
						Log.v(TAG, "conntime format is error. e=" + e);
					}

					Log.v(TAG, "strLine1=" + strLine1 + " ,conntime1=" + conntime1);

					// process1.waitFor();
					process1.destroy();
					Log.v(TAG, "end ping push1");
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				// get push2 time
				try {
					String strLine2 = null;
					Process process2;

					long startTime2 = new Date().getTime();
					Log.v(TAG, "start ping push2");
					process2 = Runtime.getRuntime().exec("ping push2.qingcheng.com");
					long interval2 = new Date().getTime() - startTime2;
					Log.v(TAG, "startTime2=" + startTime2 + " ,interval2=" + interval2);

					BufferedReader br2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
					String line2 = br2.readLine();
					Log.v(TAG, "line2=" + line2);
					strLine2 = line2;

					for (; line2 != null;) {
						String nextLine2 = br2.readLine();
						line2 = nextLine2;
						Log.v(TAG, "myPingPushTimeThread: line2=" + line2);
						strLine2 = strLine2 + line2;
						if (strLine2.contains("time")) {
							Log.v(TAG, "have get the time. strLine2=" + strLine2);
							break;
						}
					}

					br2.close();

					String conntime2 = strLine2.substring(strLine2.lastIndexOf("time=") + 5, strLine2.lastIndexOf("ms") - 1);

					try {
						if (conntime2.contains(".")) {
							Log.v(TAG, "conntime is float time. ");
							float time2 = new Float(conntime2);
							pingTime2 = (int) time2;
							Log.v(TAG, "float time2=" + time2 + " ,pingTime2=" + pingTime2);
						} else {
							Log.v(TAG, "conntime is int time. ");
							pingTime2 = new Integer(conntime2);
						}
					} catch (Exception e) {
						Log.v(TAG, "conntime format is error. e=" + e);
					}

					Log.v(TAG, "strLine2=" + strLine2 + " ,conntime2=" + conntime2);

					// process2.waitFor();
					process2.destroy();
					Log.v(TAG, "end ping push2");

				} catch (Exception ex) {
					ex.printStackTrace();
				}

				// wifiVisitFlag: 0: same speed, 1: Telecom fast, 2: Unicom fast
				String wifiVisitFlag = "0";
				if (pingTime1 == pingTime2) {
					wifiVisitFlag = "0";
				} else if (pingTime1 < pingTime2) {
					wifiVisitFlag = "1";
				} else if (pingTime1 > pingTime2) {
					wifiVisitFlag = "2";
				}

				updateWifivISITNetwork2DB(wifiVisitFlag);
				Log.v(TAG, "myPingPushTimeThread: end");
			}
		}.start();
	}

	private void updateWifivISITNetwork2DB(String visitFlag) {

		if (visitFlag == null || ("".equals(visitFlag))) {
			Log.v(TAG, "not get visitFlag!");
			return;
		}

		Log.v(TAG, "updateWifivISITNetwork2DB: visitFlag=" + visitFlag);
		myDBUtil.myDBUpdate("wifiVisitFlag", visitFlag);
	}

	public static boolean isConnect(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v(TAG, e.toString());
		}
		return false;
	}

	public static boolean haveInternet(Context context) {
		NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}

		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable internet while roaming, just return false
			return true;
		}
		return true;
	}

	public void sendMsgToApp(String msg, String packageName) {
		Log.v(TAG, "broadcast sendMsgToApp()" + packageName);
		ThirdInstalledApp ta = new ThirdInstalledApp();
		try {
			ta.sendMsgToApp(getApplicationContext(), packageName, msg);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.v(TAG, "broadcast sendMsgToApp()" + packageName + "not found");
		}
	}

	public String correctCurrentDomain() {
		Log.v(TAG, "correctCurrentDomain start");

		String visitDominFlag = "0";
		// begin yang.yang2 add for delete ping action in push client
		/*
		 * String useNetType =
		 * PhoneInfoUtil.getUseNetType(getApplicationContext()); Log.v(TAG,
		 * "str useNetType:" + useNetType); if ("unknown".equals(useNetType)) {
		 * Log.v(TAG, "unknown network use default service!");
		 * 
		 * } else if ("WIFI".equals(useNetType)) { Log.v(TAG,
		 * "WIFI network use fastest service!"); String visitFlag =
		 * myDBUtil.myDBQuery("wifiVisitFlag");
		 * 
		 * visitDominFlag = visitFlag; } else if ("MOBILE".equals(useNetType)) {
		 * Log.v(TAG, "MOBILE network use correct service!");
		 * 
		 * SIMCardInfo si = new SIMCardInfo(getApplicationContext()); String
		 * providersType = si.getProvidersType();
		 * 
		 * if (providersType == null) { Log.v(TAG, "providersType=" +
		 * providersType);
		 * 
		 * } else { if ("1".equals(providersType)) { Log.v(TAG,
		 * "China Mobile network use Telecom service!");
		 * 
		 * visitDominFlag = "0"; } else if ("2".equals(providersType)) {
		 * Log.v(TAG, "China Unicom network use Unicom service!");
		 * 
		 * visitDominFlag = "2"; } else if ("3".equals(providersType)) {
		 * Log.v(TAG, "China Telecom network use Telecom service!");
		 * 
		 * visitDominFlag = "1"; } } }
		 */
		// end yang.yang2 add for delete ping action in push client
		return visitDominFlag;
	}

	// zhaoshh0424 add handler in thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

			Log.v(TAG, "handleMessage incoming message. what=" + msg.what);

			// process incoming messages here
			switch (msg.what) {
			case 0:
				// tView.append((String) msg.obj);
				Log.v(TAG, "handleMessage do some thing" + (String) msg.obj);
				return;// break;

			case BOOT_CONNECT_SOCKETCLIENT:
				Log.v(TAG, "BOOT_CONNECT: Create socketClient bootStayConnectStatus=" + bootStayConnectStatus);

				if (bootStayConnectStatus) {
					ConnectivityManager connectivityManager;
					NetworkInfo info;
					connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
					info = connectivityManager.getActiveNetworkInfo();

					if (info != null && info.isAvailable()) {
						try {
							String curDomain = correctCurrentDomain();
							boolean isBusiServer = getBusinessServerSetting(getApplicationContext());
							Log.v(TAG, "BOOT_CONNECT:isBusiServer=" + isBusiServer);
							socketClient = new SocketClient(curDomain, isBusiServer);
							Log.v(TAG, "BOOT_CONNECT: handler socketClient=" + socketClient);

							if (socketClient != null || !socketClient.socket.isClosed()) {
								if (socketClient.scFlag) {
									Log.d(TAG, "BOOT_CONNECT: create socketClient success: socketClient.scFlag ifSocketConn");
									ifSocketConn = true;
									bootStayConnectStatus = false;
									intervalStayConnectStatus = false;
									bootConnectCount = 0;
									// boot or restart, set bootConnected flag
									myDBUtil.myDBUpdate("bootConnected", "1");
									prepare();
								}
							}

						} catch (Exception e) {
							Log.v(TAG, "BOOT_CONNECT: create socketClient Exception");
							e.printStackTrace();
						}

						// reconnect times 3
						if ((bootConnectCount < 3) && //
								(socketClient == null || socketClient.socket == null || !(socketClient.socket.isConnected() && !socketClient.socket.isClosed()))) {
							Log.v(TAG, "BOOT_CONNECT: create socketClient is fail, reconnect socketClient. bootConnectCount=" + bootConnectCount);

							// create socketClient in handler
							// zhaoshh0424 add handler in thread
							Message message = mServiceHandler.obtainMessage();
							message.what = BOOT_CONNECT_SOCKETCLIENT;
							if (bootConnectCount == 0) {
								// zhaoshh0424 add handler in thread
								mServiceHandler.sendMessageDelayed(message, 60000 * 3);
							} else if (bootConnectCount == 1) {
								// zhaoshh0424 add handler in thread
								mServiceHandler.sendMessageDelayed(message, 60000 * 5);
							} else if (bootConnectCount == 2) {
								// zhaoshh0424 add handler in thread
								mServiceHandler.sendMessageDelayed(message, 60000 * 10);
							}

							bootConnectCount++;
						}
					}
				}

				return;// break;
			case INTERVAL_CONNECT_SOCKETCLIENT:
				Log.v(TAG, "INTERVAL_CONNECT: Create socketClient intervalStayConnectStatus=" + intervalStayConnectStatus);

				if (intervalStayConnectStatus) {
					ConnectivityManager connectivityManager;
					NetworkInfo info;
					connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
					info = connectivityManager.getActiveNetworkInfo();

					if (info != null && info.isAvailable()) {
						if ((ifSocketConn == true) && (socketClient != null) && (!socketClient.socket.isClosed())) {
							Log.v(TAG, "INTERVAL_CONNECT: socketClient current is connected, not need reconnect");

							return;// break;
						} else {
							try {
								String curDomain = correctCurrentDomain();
								boolean isBusiServer = getBusinessServerSetting(getApplicationContext());
								Log.v(TAG, "INTERVAL_CONNECT:isBusiServer=" + isBusiServer);
								socketClient = new SocketClient(curDomain, isBusiServer);
								Log.v(TAG, "INTERVAL_CONNECT: handler socketClient=" + socketClient);

								if (socketClient != null || !socketClient.socket.isClosed()) {
									if (socketClient.scFlag) {
										Log.d(TAG, "INTERVAL_CONNECT: create socketClient success: socketClient.scFlag ifSocketConn");
										ifSocketConn = true;
										bootStayConnectStatus = false;
										intervalStayConnectStatus = false;
										intervalConnectCount = 0;
										// boot or restart, set bootConnected
										// flag
										myDBUtil.myDBUpdate("bootConnected", "1");
										prepare();
									}
								}

							} catch (Exception e) {
								Log.v(TAG, "INTERVAL_CONNECT: create socketClient Exception");
								e.printStackTrace();
							}

							if ((intervalConnectCount < 3) && (socketClient == null//
									|| socketClient.socket == null//
							|| !(socketClient.socket.isConnected() && !socketClient.socket.isClosed()))) {
								Log.v(TAG, "INTERVAL_CONNECT: create socketClient is fial, reconnect socketClient. intervalConnectCount="
										+ intervalConnectCount);

								// create socketClient in handler
								// zhaoshh0424 add handler in thread
								Message message = mServiceHandler.obtainMessage();

								message.what = INTERVAL_CONNECT_SOCKETCLIENT;
								if (intervalConnectCount == 0) {
									// zhaoshh0424 add handler in thread
									mServiceHandler.sendMessageDelayed(message, 60000 * 3);
								} else if (intervalConnectCount == 1) {
									// zhaoshh0424 add handler in thread
									mServiceHandler.sendMessageDelayed(message, 60000 * 5);
								} else if (intervalConnectCount == 2) {
									// zhaoshh0424 add handler in thread
									mServiceHandler.sendMessageDelayed(message, 60000 * 10);
								}

								intervalConnectCount++;
							}
						}
					}
				}

				return;// break;

			case MSG_DEFAULT_MSG_ID:
				Intent intent = (Intent) msg.obj;
				handleDifferentAction(intent);
				break;

			default:
				Log.v(TAG, "handleMessage what=" + msg.what);
				return;
			}
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// process incoming messages here
			switch (msg.what) {
			case 0:
				// tView.append((String) msg.obj);
				Log.v(TAG, "handleMessage do some thing" + (String) msg.obj);
				break;
			case UPDATE_LOCATION_TO_DB:
				Log.v(TAG, "UPDATE_LOCATION_TO_DB");

				String longitudeLatitude = "";
				longitudeLatitude = (String) msg.obj;
				Log.d(TAG, "longitudeLatitude= " + longitudeLatitude);

				if (longitudeLatitude == null || "".equals(longitudeLatitude)) {
					Log.d(TAG, "not get longitudeLatitude");
				} else {
					if ("1".equals(myDBUtil.myDBQuery("haveLocation"))) {
						Log.v(TAG, "location have updated to DB!");
						break;
					}

					String[] strResu = longitudeLatitude.split(",");
					String lat = strResu[0];
					String lng = strResu[1];
					Log.d(TAG, "lat=" + lat + " ,lng=" + lng);

					myDBUtil.myDBUpdate("haveLocation", "1");
					myDBUtil.myDBUpdate("Longitude", lng);
					myDBUtil.myDBUpdate("Latitude", lat);
				}

				break;
			case UPDATE_IP_ADDRESS_TO_DB:
				Log.v(TAG, "UPDATE_IP_ADDRESS_TO_DB");

				String ipAddress = "";
				ipAddress = (String) msg.obj;
				Log.d(TAG, "ipAddress= " + ipAddress);

				if (ipAddress == null || "".equals(ipAddress)) {
					Log.d(TAG, "not get longitudeLatitude");
				} else {
					if ("1".equals(myDBUtil.myDBQuery("haveIpAddress"))) {
						Log.v(TAG, "ipAddress have updated to DB!");
						break;
					}

					myDBUtil.myDBUpdate("haveIpAddress", "1");
					myDBUtil.myDBUpdate("ipAddress", ipAddress);
				}

				break;

			}
			super.handleMessage(msg);
		}
	};

	public void prepare() {
		Log.v(TAG, "register App data");
		try {
			byte[] registerAppData = getRegisterAppData();
			RegisterAppDataThread registerThread = new RegisterAppDataThread(socketClient, buildSendByte(registerAppData, YPushConfig.register_update));
			registerThread.start();
		} catch (RemoteException e) {
			Log.v(TAG, "get details hava RemoteException");
			e.printStackTrace();
		}

		receiveThread = new ReceiveThread(socketClient);
		receiveThread.start();
		recTFlag = true;// zhaoshh for reconnect, activate receiveThread

		listenTimeOutThread();

		lLastCommDate = new Date().getTime();
	}

	public void shutDown() {
		Log.v(TAG, "shutDown");

		recTFlag = false;
		ifSocketConn = false;
		receiveThread = null;

		if (socketClient != null) {
			socketClient.close();
			socketClient = null;
		}

		// polling
		bootStayConnectStatus = false;
		intervalStayConnectStatus = false;
		intervalTime = 0;
		haveRandomFlag = false;
		bootHaveRandomFlag = false;
		// myDBUtil.myDBUpdate("curIntervalTime", "0");
	}

	private void closeSocket() {
		Log.d(TAG, "to closeSocket");

		recTFlag = false;
		ifSocketConn = false;
		receiveThread = null;

		if (socketClient != null) {
			socketClient.close();
			socketClient = null;
		}

		// polling
		bootStayConnectStatus = false;
		intervalStayConnectStatus = false;
		intervalTime = 0;
		haveRandomFlag = false;
		bootHaveRandomFlag = false;
		myDBUtil.myDBUpdate("curIntervalTime", "0");

	}

	public byte[] getRegisterAppData() throws RemoteException {
		byte[] registerAppData = null;
		boolean protocolbuf = false;// remove protocalbuf

		if (protocolbuf) {
			Context c = getApplicationContext();
			Map dmap = getAllDetail(c);
			JSONObject textjs = new JSONObject(dmap);
			String registerstr = textjs.toString();
			Log.v(TAG, "protocol buf registerstr:" + registerstr);
			byte[] cInfoRegister = generateCommonBody(registerstr);

			Log.v(TAG, "protocol buf len:" + cInfoRegister.length);

			return cInfoRegister;
		} else {
			Context c = getApplicationContext();
			Map dmap = getAllDetail(c);
			JSONObject cmdtextjs = new JSONObject(dmap);
			String registerstr = cmdtextjs.toString();
			Log.v(TAG, "str registerstr:" + registerstr);
			byte[] cInfoRegister = ByteOrStringHelper.StringToByte(registerstr);

			Log.v(TAG, "str len:" + cInfoRegister.length);

			return cInfoRegister;
		}

		// return registerAppData;
	}

	class RegisterAppDataThread extends Thread {
		private SocketClient tempsocketClient;
		private byte[] registerByte;
		private int mPushDataType;
		public static final int DEFAULT_PUSH_DATA = 0;
		public static final int INTERVAL_UPLOAD_PUSH_DATA = 1;

		public RegisterAppDataThread(SocketClient socketClient, byte[] sendByte) {
			this(socketClient, sendByte, DEFAULT_PUSH_DATA);
		}

		public RegisterAppDataThread(SocketClient socketClient,
				byte[] sendByte, int dataType) {
			this.tempsocketClient = socketClient;
			this.registerByte = sendByte;
			mPushDataType = dataType;
		}

		@Override
		public void run() {
			// Looper.prepare();
			try {
				socketClient.dout.write(registerByte);
				socketClient.dout.flush();
				Log.v(TAG, "RegisterAppDataThread registerByte :"
						+ registerByte);

				finishUploadData(mPushDataType);

				return;
				// Looper.loop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void finishUploadData(int dataType) {
//            if (RegisterAppDataThread.INTERVAL_UPLOAD_PUSH_DATA == dataType) {
				Context context = getApplicationContext();

				CommonUtils.setUploadedIntervalDate(context);
				mRetryTimes = 0;

				String curVer = CommonUtils.getCurrentMyUIVersion();
				String storedVer = CommonUtils.getStoredMyUIVersion(context);
				boolean isMyUIChanged = CommonUtils.isMyUIChanged(context);
				if (isMyUIChanged || storedVer.isEmpty()) {
					CommonUtils.storeMyUIVersion(context, curVer);
				}
//            }
		}
	}

	public byte[] buildSendByte(byte[] sendByte, String type) {
		String headS = new CommonUtils().getSendStringHead(type, sendByte.length);
		StringBuffer buffer = new StringBuffer();
		buffer.append(headS);
		buffer.append(new String(sendByte));
		Log.v(TAG, "str buffer:" + buffer.toString());

		byte[] headByte = headS.getBytes();
		byte[] newData = new byte[headS.getBytes().length + sendByte.length];

		System.arraycopy(headByte, 0, newData, 0, headByte.length);
		System.arraycopy(sendByte, 0, newData, headByte.length, sendByte.length);
		return newData;
	}

	public byte[] generateHeartbeatPacket(String deviceId, String netType) {
		hbpMsg.Builder hpBulider = hbpMsg.newBuilder();
		hpBulider.setDeviceId(deviceId);
		hpBulider.setNetType(netType);
		hbpMsg hbb = hpBulider.build();
		int len = hbb.getSerializedSize();
		byte[] buf = hbb.toByteArray();
		
//		try {
//			hbpMsg hbb2 = hbpMsg.parseFrom(buf);
//
//			System.out.println("Result deviceId = " + hbb2.getDeviceId() + " netType = " + hbb2.getNetType());
//		} catch (InvalidProtocolBufferException e) {
//			e.printStackTrace();
//		}
		return buf;
	}

	public byte[] generateCommonBody(String strJson) {
		Log.v(TAG, "generateCommonBody str:" + strJson);
		commonMsg.Builder commBulider = commonMsg.newBuilder();
		commBulider.setCommText(strJson);
		commonMsg cMsg = commBulider.build();
		int len = cMsg.getSerializedSize();
		Log.v(TAG, "generateCommonBody len:" + len);
		byte[] buf = cMsg.toByteArray();

		return buf;
	}

	public void callStartApp(String packageName) {
		ThirdInstalledApp ta = new ThirdInstalledApp();

		try {
			ta.openApp(getApplicationContext(), packageName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.v(TAG, "callStartApp()" + packageName + "not found");
		}
	}

	public void callStopApp(String packageName) {
		ThirdInstalledApp ta = new ThirdInstalledApp();

		try {
			ta.closeApp(getApplicationContext(), packageName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.v(TAG, "callStartApp()" + packageName + "not found");
		}
	}

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
			Log.v(TAG, "getAllDetail: Longitude=" + Longitude + " ,Latitude=" + Latitude);
			StringBuffer lalo = new StringBuffer();
			lalo.append(Longitude);
			lalo.append(",");
			lalo.append(Latitude);
			longitudeLatitude = lalo.toString();
		} else {
			longitudeLatitude = "";
		}

		DisplayMetrics dm = new DisplayMetrics();
		dm = getApplicationContext().getResources().getDisplayMetrics();
		int resoWidth = dm.widthPixels;
		int resoHeight = dm.heightPixels;
		Log.d(TAG, "resoWidth=" + resoWidth + " ,resoHeight=" + resoHeight);
		float curDensity = dm.density;
		int curDensityDpi = dm.densityDpi;
		Log.d(TAG, "curDensity=" + curDensity + " ,curDensityDpi=" + curDensityDpi);

		double diagonalPixels = Math.sqrt(Math.pow(resoWidth, 2) + Math.pow(resoHeight, 2));
		float screenSize = (float) diagonalPixels / (160 * curDensity);
		Log.d(TAG, "diagonalPixels=" + diagonalPixels + " ,screenSize=" + screenSize);
		String resolution = Integer.toString(resoWidth) + "*" + Integer.toString(resoHeight);
		// add by xiaolin.he 20150915 start . for test 
		String strIpAddress = IP_NULL;
		for (String ipaddr : IP_ADDRS) {
			strIpAddress = GetNetIp(ipaddr);
			if (!IP_NULL.equals(strIpAddress)) break;
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
		Log.v(TAG, "getAllDetail reMap:" + reMap);

		return reMap;
	}

	/**
	 * listen time out thread
	 */
	private void listenTimeOutThread() {
		new Thread() {
			@Override
			public void run() {
				Log.v(TAG, "listenTimeOutThread: ifSocketConn=" + ifSocketConn);

				while (ifSocketConn) {
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					long interval = new Date().getTime() - lLastCommDate;
					Log.v(TAG, "listenTimeOutThread: interval=" + interval + " lLastCommDate=" + lLastCommDate);

					// for 3 minutes not receive msg, to close socket
					if (interval > YPushConfig.receive_msg_time_out) {
						Log.v(TAG, "3 minutes not receive msg, to close socket");
						closeSocket();

						return;
					}

				}
			}
		}.start();
	}

	public class MsgSendThread {

		private SocketClient tempsocketClient;
		private byte[] sendByte;

		public MsgSendThread(SocketClient socketClient, byte[] sendByte) {
			this.tempsocketClient = socketClient;
			this.sendByte = sendByte;
		}

		public void sendData() {
			try {
				Log.d(TAG, "MsgSendThread tempsocketClient=" + tempsocketClient + " ,socketClient=" + socketClient);
				if (tempsocketClient == null || tempsocketClient.socket == null || !(tempsocketClient.socket.isConnected() && !tempsocketClient.socket.isClosed())) {
					Log.v(TAG, "MsgSendThread socketClient is not OK, restart socketClient");
				}

				String strsendByte = ByteOrStringHelper.ByteToString(sendByte);
				Log.v(TAG, "MsgSendThread strsendByte=" + strsendByte);

				socketClient.dout.write(sendByte);
				socketClient.dout.flush();
				Log.v(TAG, "MsgSendThread send :" + sendByte);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressLint("UseValueOf")
	class ReceiveThread extends Thread {
		private SocketClient tempsocketClient;
		long sleepTime = 500;

		public ReceiveThread(SocketClient socketClient) {
			this.tempsocketClient = socketClient;
		}

		@Override
		public void run() {
			Log.v(TAG, "ReceiveThread Id=" + this.getId() + " ,tempsocketClient=" + tempsocketClient + " ,socketClient=" + socketClient);

			try {
				while (recTFlag) {
					Log.v(TAG, "ReceiveThread activity. lLastCommDate=" + lLastCommDate);

					sleep(sleepTime);
					if (tempsocketClient.socket == null || !(tempsocketClient.socket.isConnected() && !tempsocketClient.socket.isClosed())) {
						Log.v(TAG, "ReceiveThread socketClient is not OK, socketClient=" + socketClient);

						if (socketClient != null && socketClient.scFlag) {
							continue;
						}
					}

					byte[] sbBody = new byte[4096];
					StringBuffer sbHead = new StringBuffer();
					int count = 0;
					int index = -1;
					int streamLen = 0;
					int headLen = YPushConfig.head_len;
					String type = "0";

					while ((index = socketClient.din.read()) != -1) {
						// Log.v(TAG,
						// "ReceiveThread index:"+index+"-->"+(char)index);
						count++;
						// Log.v(TAG, "ReceiveThread count:"+count);

						if (count == 1) {
							char cType = (char) index;
							type = String.valueOf(cType);
							Log.v(TAG, "ReceiveThread type:" + type);

							sbHead.append(cType);
							if (type.equals(YPushConfig.heart_beat_packet) || type.equals(YPushConfig.command) || type.equals(YPushConfig.msg_type)
									|| type.equals(YPushConfig.notify_send_complete_type)) {
								lLastCommDate = new Date().getTime();
								Log.v(TAG, "ReceiveThread receive new msg update time. lLastCommDate=" + lLastCommDate);
							}
							if (type.equals(YPushConfig.heart_beat_packet)) {
								Log.v(TAG, "ReceiveThread type is heart beat break");
								break;
							}
						}
						if (count > 1 && count <= headLen) {
							sbHead.append((char) index);
						}
						if (count == headLen) {
							String dataLen = sbHead.substring(1);
							streamLen = new Integer(dataLen);
							Log.v(TAG, "ReceiveThread streamLen:" + streamLen + " ,dataLen:" + dataLen);

							if (!type.equals(YPushConfig.heart_beat_packet) && streamLen > 0) {
								continue;
							} else {
								break;
							}
						}

						if (count > headLen) {
							sbBody[count - headLen - 1] = (byte) index;
							// sbData.append((char)index);
						}

						if (count == (streamLen + headLen)) {
							if (!type.equals(YPushConfig.heart_beat_packet)) {
								byte[] byteBody = new byte[streamLen];
								System.arraycopy(sbBody, 0, byteBody, 0, byteBody.length);

								dealReceiveMsg(type, streamLen, byteBody);
								mReceiveMsgCount++;
								Log.v(TAG, "ReceiveThread msg have receive completed. count:" + count + " ,mReceiveMsgCount=" + mReceiveMsgCount);
								count = 0;// add zhaoshh for next msg
							}
							break;
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static Map<String, Object> parseData(String data) {
		// GsonBuilder gb = new GsonBuilder();
		// Gson g = gb.create();
		Gson gson = new Gson();
		Map<String, Object> map = gson.fromJson(data, new TypeToken<Map<String, Object>>() {
		}.getType());
		Log.v(TAG, "parseData map:" + map);

		return map;
	}

	@SuppressLint("UseValueOf")
	public void dealReceiveMsg(final String type, final int len,
			final byte[] bodyByte) {

		new Thread() {
			public void run() {
				Log.v(TAG, "dealReceiveMsg start");
				if (0 == len || null == bodyByte) {
					return;
				}
				Log.v(TAG, "dealReceiveMsg data:" + bodyByte + " ,type=" + type + " ,len=" + len);
				String data = ByteOrStringHelper.ByteToString(bodyByte);
				Log.v(TAG, "dealReceiveMsg data:" + data + " ,type=" + type + " ,len=" + len);

				try {

					Gson gson = new Gson();

					if (type.equals(YPushConfig.heart_beat_packet)) {
						Log.v(TAG, "YPushConfig.heart_beat_packet");

					} else if (type.equals(YPushConfig.msg_type)) {
						Log.v(TAG, "YPushConfig.msg_type");

						// add set push parameter(interval;option)
						if (len <= 5) {// if len is 3, will set push parameter
							String setCmdType = data.substring(0, 2);
							String setCmdValue = data.substring(2);
							Log.v(TAG, "setCmdType:" + setCmdType + " ,setCmdValue=" + setCmdValue);

							if (setCmdType.equals("01")) {// set interval
								// push msg to set polling interval
								myDBUtil.myDBUpdate("pollingInterval", setCmdValue);
								int pollingDays = Integer.parseInt(setCmdValue) / 24;
								CommonUtils.setUploadInterval(getApplicationContext(), pollingDays);

							} else if (setCmdType.equals("02")) {// set deal push msg option 0:open(deal) 1:close(not deal)
								// push msg to set deal push msg option
								myDBUtil.myDBUpdate("dealMsgOption", setCmdValue);

							}

							Log.v(TAG, "dealReceiveMsg set push parameter return.");
							return;
						}

						String msgProtobufText = data;

						Map<String, Object> msgMap = parseData(msgProtobufText);

						// msg data
						String cmdType = (String) msgMap.get("cmdType");
						Map<String, Object> content = (Map) msgMap.get("content");
						String appId = (String) msgMap.get("appId");
						Log.v(TAG, "cmdType=" + cmdType + " ,content=" + content + " ,appId=" + appId);

						// content data
						String icon = (String) content.get("icon");
						String pushType = (String) content.get("pushType");
						String downloadType = (String) content.get("downloadType");
						String downloadUrl = (String) content.get("downloadUrl");
						String canPause = (String) content.get("canPause");
						String isCommand = (String) content.get("isCommand");
						String title = (String) content.get("title");
						String msgType = (String) content.get("msgType");
						String msg = (String) content.get("msg");
						List<Map<String, Object>> urlList = new ArrayList<Map<String, Object>>();
						urlList = (ArrayList) content.get("urlList");
						String version = (String) content.get("version");
						String vDownUrl = (String) content.get("vDownUrl");
						String downloadAppId = (String) content.get("referAppId");

						ArrayList<String> screenShotMobile = (ArrayList<String>) content.get("screenShotMobile");

						Log.v(TAG, "icon=" + icon + " , referAppId= " + downloadAppId);
						Log.v(TAG, "pushType=" + pushType + " ,downloadType=" + downloadType + " ,canPause=" + canPause);
						Log.v(TAG, "downloadUrl=" + downloadUrl);
						Log.v(TAG, "isCommand=" + isCommand + " ,title=" + title + " ,msgType=" + msgType);
						Log.v(TAG, "msg=" + msg + " ,urlList=" + urlList + " ,version=" + version + " ,vDownUrl=" + vDownUrl);

						// urlList data
						// List<Map<String, Object>> list = new
						// ArrayList<Map<String,Object>>();
						if (urlList != null) {
							int listSize = urlList.size();
							for (int i = 0; i < listSize; i++) {
								Map<String, Object> temp = urlList.get(i);
								String Name = (String) temp.get("Name");
								String url = (String) temp.get("url");
								Log.v(TAG, "temp=" + temp + " ,Name=" + Name + " ,url=" + url);
							}
						}

						JSONObject contentjs = new JSONObject(content);
						String strContent = contentjs.toString();
						Log.v(TAG, "strContent=" + strContent);

						String dealOption = myDBUtil.myDBQuery("dealMsgOption");
						Log.v(TAG, "dealReceiveMsg dealOption:" + dealOption);
						if (!("1".equals(dealOption))) {
							if (cmdType.equals("01")) {// Deal self

								if (cmdType.equals("01") && isCommand.equals("1")) {// Update
															// client
									Log.v(TAG, "Update client start");

									ConnectivityManager connectivityManager;
									NetworkInfo info;
									connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
									info = connectivityManager.getActiveNetworkInfo();

									if (info != null && info.isAvailable()) {
										String netName = info.getTypeName();

										// only network is wifi update
										// pushClient
										if ("WIFI".equals(netName)) {
											// zhaoshh0323,add download Service
											boolean isUpdateClient = true;
											String fileName = "";
											int beginIndex = vDownUrl.lastIndexOf('/');
											fileName = vDownUrl.substring(beginIndex);
											Log.d(TAG, "get fileName: beginIndex=" + beginIndex + " ,fileName=" + fileName);

											if ("YPush_service.apk".equals(fileName)) {
												Intent serviceIntent = new Intent(getApplicationContext(), DownloadService.class);
												Bundle bundle = new Bundle();
												bundle.putString("url", vDownUrl);
												bundle.putString("fileName", fileName);
												bundle.putString("version", version);
												bundle.putBoolean("isUpdateClient", isUpdateClient);
												serviceIntent.putExtras(bundle);
												Log.d(TAG, " DownLoadService start Intent: serviceIntent=" + serviceIntent);
												getApplicationContext().startService(serviceIntent);
											} else {
												Log.v(TAG, "the service file is not client apk");
											}

										}
									}

									return;
								}

								Intent uiIntent = new Intent();
								uiIntent.setAction("android.intent.action.PUSHMSGACTIVITY");
								Bundle bundle = new Bundle();
								bundle.putString("pushType", pushType);
								bundle.putString("msgData", msgProtobufText);
								bundle.putString("content", strContent);
								bundle.putString("title", title);
								bundle.putString("msg", msg);
								bundle.putString("downloadType", downloadType);
								bundle.putString("_id", downloadAppId);
								String visitFlag = myDBUtil.myDBQuery("wifiVisitFlag");
								bundle.putString("wifiVisitFlag", visitFlag);

								uiIntent.putExtras(bundle);

								Log.d(TAG, "msgType=" + msgType + " ,strContent=" + strContent);
								// uiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								uiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
								Log.d(TAG, "uiIntent=" + uiIntent);

								// notification bar prompt user have url to open
								// zhaoshh0322,for notification
								// get notificationId
								String strNotificationId = myDBUtil.myDBQuery("notificationId");
								if (strNotificationId == null || ("".equals(strNotificationId))) {
									Log.v(TAG, "init NotificationId");
									strNotificationId = "0";
								}
								Log.v(TAG, "Deal self: strNotificationId=" + strNotificationId);

								int curNotificationId = new Integer(strNotificationId);
								final int customId = curNotificationId;
								if (icon == null || icon.equals("")) {
									//modified by yangzg 2015.11.27 start. for use new notification method
									PendingIntent contentPendingIntent = PendingIntent.getActivity(YPushService.this, curNotificationId, uiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
									Notification promptNotification = new Notification.Builder(YPushService.this)
									.setContentTitle(title)
									.setContentText(msg)
									.setContentIntent(contentPendingIntent)
									.build();
//									Notification promptNotification = new Notification();
									NotificationManager promptNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
									promptNotification.icon = R.drawable.go_push_icon; // R.drawable.ic_launcher;
									// promptNotification.tickerText =
									// getString(R.string.push_msg_open_url_ticker);
									promptNotification.tickerText = getString(R.string.push_msg_ticker);

									promptNotification.flags = Notification.FLAG_AUTO_CANCEL; // zhaoshh for after click auto cancel
									// PendingIntent contentPendingIntent = PendingIntent.getActivity(YPushService.this, 0, uiIntent, 0);

									promptNotification.defaults = Notification.DEFAULT_SOUND;// 锟斤拷锟斤拷锟斤拷锟斤拷
//									promptNotification.setLatestEventInfo(YPushService.this, title, msg, contentPendingIntent);
									// promptNotification.setLatestEventInfo(YPushService.this, getString(R.string.push_msg_open_url_title),
									// getString(R.string.push_msg_open_url_summary), contentPendingIntent);
									//modified by yangzg 2015.11.27 end. for use new notification method
									Log.v(TAG, "Deal self: promptNotification=" + promptNotification);

									promptNotificationManager.notify(curNotificationId, promptNotification);
								} else {
									Notification.Builder builder = new Notification.Builder(YPushService.this);

									PendingIntent contentPendingIntent = PendingIntent.getActivity(YPushService.this, curNotificationId, uiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
									int iconId = R.drawable.go_push_icon;
									builder.setTicker(getResources().getString(R.string.push_msg_ticker));
									builder.setContentTitle(title);
									builder.setContentText(msg);
									builder.setSmallIcon(iconId);

									builder.setWhen(System.currentTimeMillis());
									builder.setAutoCancel(true);
									builder.setContentIntent(contentPendingIntent);
									builder.setDefaults(Notification.DEFAULT_SOUND);

									asyncSetNotification(builder, icon, curNotificationId);
								}
								curNotificationId++;
								String temp = String.valueOf(curNotificationId);
								Log.v(TAG, "Deal self: temp=" + temp);
								myDBUtil.myDBUpdate("notificationId", temp);

							} else if (cmdType.equals("02")) {// Forward to app
								Log.v(TAG, "Forward to app start");

								String pacName = appId;
								// String customVers =
								// getInternalVersion();//yang.yang2 modify
								String customVers = getMyUIVersion();
								Log.v(TAG, "dealReceiveMsg customVers:" + customVers + " ,version=" + version);
								if ((pacName != null) && (pacName.equals("com.android.ota.UpdateSystem"))) {
									Log.v(TAG, "is OTA");

									if ((version == null) || ("".equals(version))) {
										Log.v(TAG, "OTA version is null, forward to OTA");

										Intent otaService = new Intent(Intent.ACTION_RUN);
										otaService.setAction("com.android.BootService");
										getApplicationContext().startService(otaService);

										return;
									}

									// add for special version, contain
									// "otaVersion"
									if (version.contains("otaVersion")) {
										Log.v(TAG, "OTA version contain otaVersion, forward to OTA");

										Intent otaService = new Intent(Intent.ACTION_RUN);
										otaService.setAction("com.android.BootService");
										getApplicationContext().startService(otaService);

										return;
									}

									if ((version != null) && (customVers != null) && (version.equals(customVers))) {
										Log.v(TAG, "OTA version can upgrade");

										Intent otaService = new Intent(Intent.ACTION_RUN);
										otaService.setAction("com.android.BootService");
										getApplicationContext().startService(otaService);

										return;
									}

								} else {
									Log.v(TAG, "dealReceiveMsg pacName:" + pacName);
									sendMsgToApp(msgProtobufText, pacName);
								}

							}

						} else {// deal option not control OTA
							if (cmdType.equals("02")) {// Forward to app
								Log.v(TAG, "Forward to app start");

								String pacName = appId;
								// String customVers =
								// getInternalVersion();//yang.yang2 modify
								String customVers = getMyUIVersion();
								Log.v(TAG, "dealReceiveMsg customVers:" + customVers + " ,version=" + version);
								if ((pacName != null) && (pacName.equals("com.android.ota.UpdateSystem"))) {
									Log.v(TAG, "is OTA");

									if ((version == null) || ("".equals(version))) {
										Log.v(TAG, "OTA version is null, forward to OTA");

										Intent otaService = new Intent(Intent.ACTION_RUN);
										otaService.setAction("com.android.BootService");
										getApplicationContext().startService(otaService);

										return;
									}

									// add for special version, contain
									// "otaVersion"
									if (version.contains("otaVersion")) {
										Log.v(TAG, "OTA version contain otaVersion, forward to OTA");

										Intent otaService = new Intent(Intent.ACTION_RUN);
										otaService.setAction("com.android.BootService");
										getApplicationContext().startService(otaService);

										return;
									}

									if ((version != null) && (customVers != null) && (version.equals(customVers))) {
										Log.v(TAG, "OTA version can upgrade");

										Intent otaService = new Intent(Intent.ACTION_RUN);
										otaService.setAction("com.android.BootService");
										getApplicationContext().startService(otaService);

										return;
									}
								}
							}
						}

					} else if (type.equals(YPushConfig.command)) { // 锟斤拷锟秸碉拷command
						Log.v(TAG, "YPushConfig.command");
						// do some thing

					} else if (type.equals(YPushConfig.register_update)) { // app锟斤拷息注锟斤拷透锟斤拷拢锟酵拷锟斤拷锟絘pp锟斤拷锟斤拷
						Log.v(TAG, "YPushConfig.register_update");

					} else if (type.equals(YPushConfig.notify_send_complete_type)) { // msg receiver complete
						Log.v(TAG, "YPushConfig.notify_send_complete_type");

						// msg receiver complete, to close socket
						closeSocket();

					}

				} catch (Exception e) {
					Log.e(TAG, "dealReceiveMsg has Exception");
					e.printStackTrace();

					return;
				}

			}

		}.start();
	}

	protected static ImageView GetImaeView(View paramView) {
		if (paramView instanceof ImageView)
			return (ImageView) paramView;
		if (paramView instanceof ViewGroup)
			for (int i = 0; i < ((ViewGroup) paramView).getChildCount(); ++i) {
				View localView = ((ViewGroup) paramView).getChildAt(i);
				if (localView instanceof ImageView)
					return (ImageView) localView;
				if (localView instanceof ViewGroup)
					return GetImaeView(localView);
			}
		return null;
	}

	public static String getInternalVersion() {
		String internalVersion = "";
		internalVersion = SystemProperties.get("ro.internal.version", "");
		if (null == internalVersion || "".equals(internalVersion)) {
			internalVersion = SystemProperties.get("ro.custom.build.version", "");
		}

		return internalVersion.trim();
	}

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
	//add by zhengguang.yang@20160512 start for get ipAddress
	
	public static String getIpAddress(){
		String ipAddress = IP_NULL;
		if(TextUtils.isEmpty(ipAddress)){
			ipAddress = IP_NULL;
			for (String ipaddr : IP_ADDRS) {
				ipAddress = GetNetIp(ipaddr);
				if (!IP_NULL.equals(ipAddress)) break;
			}
		}
		return ipAddress;
	}
	//add by zhengguang.yang@20160512 end for get ipAddress

	private String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
	}

	private String getLocalIpAddress() {

		try {
			Log.d(TAG, "getLocalIpAddress  enter");
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();

				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						Log.d(TAG, "getLocalIpAddress  return");
						return inetAddress.getHostAddress().toString();
					}
				}
			}

		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}

		return null;
	}

	// return. true: business server. false: test server
	public boolean getBusinessServerSetting(Context context) {
		boolean isBusinessServer = true;
		try {
			String strBusinessServer = Settings.System.getString(context.getContentResolver(),
			/* Settings.System.QINGCHENG_BUSINESS_SERVER_CONFIG */"set_qingcheng_server_config");
			isBusinessServer = Boolean.parseBoolean(strBusinessServer) || (strBusinessServer == null);

		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG, "getBusinessServerSetting have exception e=" + e);
		}
		return isBusinessServer;
	}

	// begin yang.yang2 add for download icon from url
	public static Bitmap returnBitMap(String bitmapUrl) {
		String url = null;
		URL myFileUrl = null;
		Bitmap bitmap = null;

		try {
			url = new String(bitmapUrl.getBytes(), "iso8859-1");
			myFileUrl = new URL(url);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
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

	// end yang.yang2 add for download icon from url

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
				Notification promptNotification = builder.getNotification();
				promptNotificationManager.notify(customId, promptNotification);
			}
		};
		updateTask.execute();
	}

	private void setAlarm(long delayTime, Intent intent) {
		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, new Intent(intent), PendingIntent.FLAG_ONE_SHOT);
		mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, (SystemClock.elapsedRealtime() + delayTime), pIntent);
		Log.v(TAG, "setAlarm: " + delayTime);
	}

	private void cancelAlarm(Intent intent) {
		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, new Intent(intent), PendingIntent.FLAG_ONE_SHOT);
		mAlarmManager.cancel(pIntent);
		Log.v(TAG, "cancelAlarm!!");
	}

	private void uploadPhoneData() {
		Log.i(TAG, "uploadPhoneData");
		cancelAlarm(mUploadByInterval);
		Context context = getApplicationContext();
		boolean isMyUIChanged = CommonUtils.isMyUIChanged(context);

		if (!CommonUtils.isUploadedThisInterval(context) || isMyUIChanged) {
			if (!PhoneInfoUtil.isOnline(this)) {
				Intent intent = new Intent(ACTION_UPLOAD_RETRY);
				intent.putExtra(DATA_TYPE, INTERVAL_DATA_UPLOAD);
				sendBroadcast(intent);
				return;
			}

			sendData();
			setAlarm(calculateNextIntervalTime(), mUploadByInterval);
		}
	}

	// private void uploadData(Object data, String dataType) {
	// String url = getDataUploadUrl(getApplicationContext());
	// int port = getDataUploadPort(getApplicationContext());
	// Map<String, String> map = new HashMap<String, String>();
	// map.put("json_p", GsonUtil.convertJson(data));
	// map.put(DATA_UPLOAD_TYPE, GsonUtil.convertJson(data));
	// Log.i(TAG, "uploadData map=" + map);
	// String results = null;
	//
	// try {
	// results = HttpUtil.doHttpPost(this, map, url, port);
	// int status = 0;
	// String statusString = new String("\"status\":");
	// if (results != null && results.contains(statusString)) {
	// int index = results.indexOf(statusString) + statusString.length() + 1;
	// status = Integer.parseInt(results.charAt(index)+"");
	// }
	//
	// //status = 0;
	// Log.i(TAG, "uploadData--status=" + status);
	// /*
	// if (status != 1) {
	// Intent intent = new Intent(YPushService.ACTION_UPLOAD_RETRY);
	// intent.putExtra("url", url);
	// intent.putExtra(DATA_TYPE, dataType);
	// sendBroadcast(intent);
	// } else
	// */
	// {
	// if (WEEKLY_DATA_UPLOAD.equals(dataType)) {
	// CommonUtils.setUploadedWeekly(getApplicationContext());
	// mWeeklyRetryTimes = 0;
	// }
	// }
	//
	// } catch (Exception e) {
	// Log.e(TAG, String.format("Exception happened when call API [%s]",url),
	// e);
	// }
	//
	// }

	private long calculateNextIntervalTime() {
		Calendar calendar = Calendar.getInstance();
		final long currentTime = System.currentTimeMillis();
		calendar.setTimeInMillis(currentTime);
		final int intervalTime = CommonUtils.getUploadInterval(getApplicationContext());
		calendar.add(Calendar.DAY_OF_MONTH, intervalTime);
		final long delayTime = calendar.getTimeInMillis() - currentTime;

		return delayTime;
	}

	private void uploadDataRetry() {
		if (mRetryTimes < UPLOAD_DATA_MAX) {
			++mRetryTimes;
			setAlarm(ONE_HOUR_DELAY, mUploadByInterval);
		} else if (mRetryTimes == UPLOAD_DATA_MAX) {
			Log.i(TAG, "data send failed 3,clear");
			mRetryTimes = 0;// reset resent-times for next interval
		}
	}

	private void sendData() {
		String curDomain = correctCurrentDomain();
		boolean isBusiServer = getBusinessServerSetting(getApplicationContext());
		socketClient = new SocketClient(curDomain, isBusiServer);
		Log.v(TAG, "sendData: handler socketClient=" + socketClient);

		if (socketClient != null || !socketClient.socket.isClosed()) {
			if (socketClient.scFlag) {
				try {
					byte[] registerAppData = getRegisterAppData();
					RegisterAppDataThread registerThread = new RegisterAppDataThread(socketClient, buildSendByte(registerAppData, YPushConfig.register_update),
							RegisterAppDataThread.INTERVAL_UPLOAD_PUSH_DATA);
					registerThread.start();
				} catch (RemoteException e) {
					Log.v(TAG, "send data RemoteException");
					e.printStackTrace();
				}
			}
		}
	}

	private void handleBootComplete() {
		Context context = getApplicationContext();
		boolean isRegSuccess = CommonUtils.isRegOnceSuccess(context);

		String storedVer = CommonUtils.getStoredMyUIVersion(context);
		boolean isMyUIChanged = CommonUtils.isMyUIChanged(context);

		if (!isRegSuccess) {
			// setAlarm(4 * ONE_HOUR_DELAY, mRegister);
			long delayTime = CommonUtils.getRegOnceDelayTime(context);
			setAlarm(delayTime, mUploadByInterval);
		} else {
			uploadPhoneData();
		}
	}

	private void handleDifferentAction(Intent intent) {
		Log.i(TAG, "handleDifferentAction intent:" + intent);
		if (intent != null) {
			String action = intent.getAction();
			// if ((ACTION_BOOT_COMPLETED.equals(action)
			// || Intent.ACTION_TIMEZONE_CHANGED.equals(action))) {
			// handleBootComplete();
			// } else if (ACTION_UPLOAD_BY_INTERVAL.equals(action)) {
			// uploadPhoneData();
			// } else if (ACTION_UPLOAD_RETRY.equals(action)) {
			// uploadDataRetry();
			Log.i(TAG, intent.getStringExtra("from") + "");
			// if ("alarm".equals(intent.getStringExtra("from"))) {
			// if (CommonUtils.isNeedUploadData(getApplicationContext())) {
			handleIntervalConnect();
			// }
			// }
		}
	}

	private void handleIntervalConnect() {
		// add into db
		String strdbTime = myDBUtil.myDBQuery("curIntervalTime");
		int dbCurIntervalTime = Integer.parseInt(strdbTime);

		intervalTime = dbCurIntervalTime + PERIOD / 1000 / 60; // intervalTime + PERIOD/1000/60; //minute
		Log.d(TAG, "onStart, intervalTime=" + intervalTime + " ,dbCurIntervalTime=" + dbCurIntervalTime);

		// set interval time to db
		myDBUtil.myDBUpdate("curIntervalTime", String.valueOf(intervalTime));

		// boot connect socket
		if (!("1".equals(myDBUtil.myDBQuery("bootConnected")))) {
			Log.v(TAG, "BOOT_CONNECT boot or restart have not connected socket.");

			if (intervalTime >= FROM_BOOT_COMPLETE_TIME) {
				if (!bootHaveRandomFlag) {
					Random random = new Random();
					int tempRand = random.nextInt(20);
					bootRandomTime = Math.abs(tempRand) + 5; // yangyang to be modify
					// bootRandomTime = 2;
					Log.v(TAG, "BOOT_CONNECT get random: tempRand=" + tempRand + " ,bootRandomTime=" + bootRandomTime);

					bootHaveRandomFlag = true;
				}

				Log.v(TAG, "BOOT_CONNECT bootRandomTime=" + bootRandomTime);

				if (intervalTime >= FROM_BOOT_COMPLETE_TIME + bootRandomTime) {// minute
					Log.v(TAG, "BOOT_CONNECT time is OK to connect socket! bootStayConnectStatus=" + bootStayConnectStatus);
					bootStayConnectStatus = true;

					// create socketClient in handler
					// zhaoshh0424 add handler in thread
					Message message = mServiceHandler.obtainMessage();
					message.what = BOOT_CONNECT_SOCKETCLIENT;
					// zhaoshh0424 add handler in thread
					mServiceHandler.sendMessage(message);
				}
			}

		} else {
			Log.v(TAG, "INTERVAL_CONNECT time start.");

			// get the polling interval time from DB, or not use the default 3
			// hours
			String dbInterval = myDBUtil.myDBQuery("pollingInterval");
			long dbIntervalTime;
			if ((dbInterval == null) || ("".equals(dbInterval))) {
				dbIntervalTime = DEFAULT_INTERVAL_TIME;// minute
			} else {
				int intValue = Integer.parseInt(dbInterval);
				dbIntervalTime = intValue * 60; // minute
			}
			Log.v(TAG, "dbInterval=" + dbInterval + " ,dbIntervalTime=" + dbIntervalTime);

            // add by xiaolin.he 20150803 start . if wifi , set upload interval time to 2 hours .
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable() && "WIFI".equals(info.getTypeName())) {
                dbIntervalTime = 2 * 60; // 2 hours
            }
            Log.v(TAG, "finally : dbIntervalTime=" + dbIntervalTime);
            // add by xiaolin.he 20150803 end .

			if (intervalTime >= dbIntervalTime) {
				if (!haveRandomFlag) {
					Random random = new Random();
					int tempRand = random.nextInt(25);
					intervalRandomTime = Math.abs(tempRand) + 5;
					Log.v(TAG, "INTERVAL_CONNECT get random: tempRand=" + tempRand + " ,intervalRandomTime=" + intervalRandomTime);

					haveRandomFlag = true;
				}

				Log.v(TAG, "INTERVAL_CONNECT intervalRandomTime=" + intervalRandomTime);

				if (intervalTime >= dbIntervalTime + intervalRandomTime) {
					Log.v(TAG, "INTERVAL_CONNECT time is OK to connect socket! intervalStayConnectStatus=" + intervalStayConnectStatus);
					intervalStayConnectStatus = true;

					// create socketClient in handler
					// zhaoshh0424 add handler in thread
					Message message = mServiceHandler.obtainMessage();
					message.what = INTERVAL_CONNECT_SOCKETCLIENT;
					// zhaoshh0424 add handler in thread
					mServiceHandler.sendMessage(message);
				}
			}
		}
	}
	
	private static boolean isMtkRom() {
		if(getProductModel().equals("GO M2S") || getProductModel().equals("GO M3")){
			return false;
		}else {
			return true;
		}
	}
}
