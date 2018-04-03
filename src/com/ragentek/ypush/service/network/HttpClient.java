package com.ragentek.ypush.service.network;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpClient {
	private static HttpClient hc;
	private static AsyncHttpClient client;
	private static Context context;
	public static long beginTime;
	public static boolean isNetSpeedSlow = false;
	
	public static HttpClient getClient(Context context){
		HttpClient.context = context;
		if(hc == null){
			hc = new HttpClient();
			client = new AsyncHttpClient();
			client.setTimeout(15000);
		}
		return hc;
	};
	
	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		beginTime = System.currentTimeMillis();
//		System.out.println("----start get-----"+url);
//		System.out.println("----phone model----"+PhoneInfoUtil.getPhoneModel());
		String packageName = context.getPackageName();
		String versionCode = "";
		try {
			versionCode = ""+context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		client.addHeader("packageName", packageName);
		client.addHeader("versionCode", versionCode);

		client.get(url,params,responseHandler);
	}
	
	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		beginTime = System.currentTimeMillis();
//		System.out.println("----start post-----"+url);
//		System.out.println("----phone model----"+PhoneInfoUtil.getPhoneModel());
		String packageName = context.getPackageName();
		String versionCode = "";
		try {
			versionCode = ""+context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		client.addHeader("packageName", packageName);
		client.addHeader("versionCode", versionCode);

		client.post(url,params,responseHandler);
	}
	
}
