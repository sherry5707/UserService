package com.ragentek.ypush.service.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @version
 */
public class IpAddressHelper {

	private static final String TAG = "YPushService.IpAddressHelper";
	private final static int UPDATE_IP_ADDRESS_TO_DB = 2;

	private Handler myHandler;
	private String mDomain;

	public IpAddressHelper(String domain, Handler handler) {
		this.mDomain = domain;
		this.myHandler = handler;
		// get ip from server
//		myGetIpFromHttpThread();
	}

	private void myGetIpFromHttpThread() {
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "myGetIpFromHttpThread start. mDomain=" + mDomain);
				String uriIpAddress = "http://push1.qingcheng.com/ypush/statisticalDevice.action?method=getNetWorkIp";
				if ("0".equals(mDomain)) {
					Log.i(TAG, "default use Telecom service!");
				} else if ("1".equals(mDomain)) {
					Log.i(TAG, "use Telecom service!");
				} else if ("2".equals(mDomain)) {
					Log.i(TAG, "use Unicom service!");
					uriIpAddress = "http://push2.qingcheng.com/ypush/statisticalDevice.action?method=getNetWorkIp";
				}
				HttpPost httpPost = new HttpPost(uriIpAddress);
				String postParams = "json_p={\"status\":\"%s\",\"nwIp\":\"%s\"}";
				try {
					httpPost.setEntity(new StringEntity(postParams, HTTP.UTF_8));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				DefaultHttpClient defClient = new DefaultHttpClient();
				defClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
				defClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
				try {
					HttpResponse httpResponse = defClient.execute(httpPost);
					String strResult = "";
					if (httpResponse != null) {
						if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							// get the post status from server success
							strResult = EntityUtils.toString(httpResponse.getEntity());
							Log.i(TAG, "get post success! strResult=" + strResult);
							updateIpAddress2DB(strResult);
						} else {
							Log.e(TAG, "get the post error status code=" + httpResponse.getStatusLine().getStatusCode());
						}
					}
				} catch (ClientProtocolException e) {
					Log.i(TAG, "ClientProtocolException e=" + e);
					e.printStackTrace();
				} catch (IOException e) {
					Log.i(TAG, "IOException e=" + e);
					e.printStackTrace();
				} catch (Exception e) {
					Log.i(TAG, "Exception e=" + e);
					e.printStackTrace();
				} finally {
					if (httpPost != null) {
						httpPost.abort();
					}
				}
				return;
			}
		}.start();
	}

	private void updateIpAddress2DB(String postResult) {
		if (postResult == null || ("".equals(postResult))) {
			Log.i(TAG, "get post result from server error!");
			return;
		}
		try {
			Gson gson = new Gson();
			Map<String, Object> postMap = gson.fromJson(postResult, new TypeToken<Map<String, Object>>() {
			}.getType());
			Log.i(TAG, "postMap:" + postMap);
			// postMap data
			String status = (String) postMap.get("status");
			String nwIp = (String) postMap.get("nwIp");
			Log.i(TAG, "status=" + status + " ,nwIp=" + nwIp);
			if (nwIp != null) {
				Message message = myHandler.obtainMessage();
				message.what = UPDATE_IP_ADDRESS_TO_DB;
				message.obj = nwIp;
				myHandler.sendMessage(message);
			}
		} catch (Exception e) {
			Log.v(TAG, "Exception e=" + e);
			e.printStackTrace();
		}
	}
}