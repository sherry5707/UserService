package com.ragentek.ypush.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;


public class GpsBaseStationService extends Service {
	
	private static final String TAG = "YPushService.GpsBaseStationService";
	
	public static final int DO_APN = 0;
	public static final int DO_WIFI = 1;
	public static final int DO_GPS = 2;
	
	@Override
	public IBinder onBind(Intent arg0) {		
		return null;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		stopService(new Intent(this, GpsBaseStationService.class));
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");
		try {
			String result="";
			
			long startTime = new Date().getTime();
			
			if(isConnect(getApplicationContext()))
		 	{
				if(CheckNetworkState(getApplicationContext()).equals("wifi")){
					Log.d(TAG, "wifi base station");
					result= transResponse(execute(doWifi(this),DO_WIFI));					
				}
				else if(CheckNetworkState(getApplicationContext()).equals("mobile")){
					Log.d(TAG, "mobile base station");
					result= transResponse(execute(doApn(this),DO_APN));
				}
		 	}
			
			Log.d(TAG, "result="+result);
			
			long endTime = new Date().getTime();
			long interval = endTime - startTime;
			Log.d(TAG, "jizhan get location: startTime=" + startTime + " ,endTime=" + endTime + " ,interval=" + interval);

			if(!result.equals("")){				
				if(result.indexOf(",") > -1){
					String [] strResu=result.split(",");
					String lat = strResu[0];
					String lng = strResu[1];
					Log.d(TAG, "lat=" + lat + " ,lng=" + lng);
					//setParameter(getApplicationContext(), "ecat_lon",strResu[0]+"");
					//setParameter(getApplicationContext(), "ecat_lat",strResu[1]+"");		
					stopService(new Intent(this, GpsBaseStationService.class));
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "onStart Error:"+e.toString());
			stopService(new Intent(this, GpsBaseStationService.class));
		}
	}
	

	private JSONObject doApn(Context context) throws Exception {
		JSONObject holder = new JSONObject();
		holder.put("version", "1.1.0");
		holder.put("host", "maps.google.com");
		holder.put("address_language", "zh_CN");
		holder.put("request_address", true);
		
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		GsmCellLocation gcl = (GsmCellLocation) tm.getCellLocation();
		int cid = gcl.getCid();
		int lac = gcl.getLac();
		int mcc = Integer.valueOf(tm.getNetworkOperator().substring(0,
				3));
		int mnc = Integer.valueOf(tm.getNetworkOperator().substring(3,
				5));
		
		Log.d(TAG, "cid=" + cid + " ,lac=" + lac);
		Log.d(TAG, "mcc=" + mcc + " ,mnc=" + mnc);
		
		JSONArray array = new JSONArray();
		JSONObject data = new JSONObject();
		data.put("cell_id", cid);
		data.put("location_area_code", lac);
		data.put("mobile_country_code", mcc);
		data.put("mobile_network_code", mnc);
		array.put(data);
		holder.put("cell_towers", array);
		
		Log.d(TAG, "doApn exit");
		return holder;
	}
	

	private JSONObject doWifi(Context context) throws Exception {
		JSONObject holder = new JSONObject();
		holder.put("version", "1.1.0");
		holder.put("host", "maps.google.com");
		holder.put("address_language", "zh_CN");
		holder.put("request_address", true);
		
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		if(wifiManager.getConnectionInfo().getBSSID() == null) {
			throw new RuntimeException("bssid is null");
		}
		
		JSONArray array = new JSONArray();
		JSONObject data = new JSONObject();
		data.put("mac_address", wifiManager.getConnectionInfo().getBSSID());  
        data.put("signal_strength", 8);  
        data.put("age", 0);  
		array.put(data);
		holder.put("wifi_towers", array);
		
		Log.d(TAG, "doWifi exit");
		return holder;
	}
	
	public HttpResponse execute(JSONObject params,int postType) throws Exception {
		
		String htpUrL = getAnalysisServerURL();
		
		HttpClient httpClient = new DefaultHttpClient();

		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				20 * 1000);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 20 * 1000);
		
		HttpPost post = new HttpPost(htpUrL);
		Log.d(TAG, "execute post=" + post);
		

		if (postType == DO_APN) {
			String proxyHost = Proxy.getDefaultHost();
			if(proxyHost != null) {
				HttpHost proxy = new HttpHost(proxyHost, 80);
				Log.d(TAG, "ConnRouteParams.DEFAULT_PROXY=" + ConnRouteParams.DEFAULT_PROXY + " ,proxy=" + proxy);
				httpClient.getParams().setParameter(
						ConnRouteParams.DEFAULT_PROXY, proxy);
			}
		}
		
		StringEntity se = new StringEntity(params.toString());
		Log.d(TAG, "execute params.toString()=" + params.toString());
		post.setEntity(se);
		Log.d(TAG, "execute after post.setEntity(se)");
		HttpResponse response = httpClient.execute(post);
		Log.d(TAG, "execute response=" + response);
		
		Log.d(TAG, "execute exit");
		return response;
	}
	
	private String transResponse(HttpResponse response) {
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			BufferedReader br;
			String Longitude="";
			String Latitude="";
			try {
				br = new BufferedReader(new InputStreamReader(
						entity.getContent()));
				StringBuffer sb = new StringBuffer();
				String result = br.readLine();
				while (result != null) {
					sb.append(result);
					result = br.readLine();
				}
				JSONObject json = new JSONObject(sb.toString());
				JSONObject lca = json.getJSONObject("location");
				if (lca != null) {
					if(lca.has("longitude"))
						Longitude =String.valueOf(lca.getDouble("longitude"));
					if(lca.has("latitude"))
						Latitude =String.valueOf(lca.getDouble("latitude"));
					
					Log.d(TAG, "Longitude=" + Longitude + " ,Latitude=" + Latitude);
					return Longitude+","+Latitude;
				}
				return "";
			} catch (Exception e) {
				Log.e(TAG, "transResponse(HttpResponse response) Error:"+e.toString());
				return "";
			}
		}
		
		Log.d(TAG, "transResponse exit");
		return "";
	}
	
	public static String getAnalysisServerURL(){
		String servicePath="http://public.51greenorange.com/googletest.aspx"; 
		String returnUrL="http://74.125.71.147/loc/json";
		try
		{
			StringBuffer response=new StringBuffer();
			HttpGet get = new HttpGet(servicePath);
			HttpClient httpClient= new DefaultHttpClient();
			
			HttpResponse httpResponse = httpClient.execute(get);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null)
			{
			
				BufferedReader br = new BufferedReader(
					new InputStreamReader(entity.getContent()));
				String line = null;
				while ((line = br.readLine()) != null)
				{	
					
					response.append(line + "\n");
				}
				returnUrL=response.toString().trim();
				Log.d(TAG, "get url="+returnUrL);
			}
			return returnUrL;
		}
		catch (Exception e)
		{
			returnUrL = "http://74.125.71.147/loc/json";
			Log.e(TAG, ""+e.toString());				
		}
		
		return returnUrL;
	}
	
	
	public static boolean isConnect(Context context) { 

	    try { 
	        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
	        if (connectivity != null) { 

	            NetworkInfo info = connectivity.getActiveNetworkInfo(); 
	            if (info != null&& info.isConnected()) { 

	                if (info.getState() == NetworkInfo.State.CONNECTED) { 
	                    return true; 
	                } 
	            } 
	        } 
	    } catch (Exception e) { 
	    	Log.v("error",e.toString()); 
	    } 
        return false; 
    }
	
	public static String getNetType(Context context) {
		String netType = "unknown";
		if(TextUtils.isEmpty(netType)){
			netType = "unknown";
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if(networkInfo == null){
				return netType;
			}
			int nType = networkInfo.getType();
			if(nType == ConnectivityManager.TYPE_MOBILE){
				netType = "mobile";//getProvidersName(context);
			}else if (nType == ConnectivityManager.TYPE_WIFI){
				netType = "WIFI";
			}
		}
		return netType;
	}
	

	public static String CheckNetworkState(Context contx){      
		ConnectivityManager manager=(ConnectivityManager) contx.getSystemService(Context.CONNECTIVITY_SERVICE);      
		State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(mobile==State.CONNECTED || mobile==State.CONNECTING){
			return "mobile";
		}
		
		if(wifi==State.CONNECTED || wifi==State.CONNECTING){
			return "wifi";
		}
		return "";
	
	}
}
