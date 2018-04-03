package com.ragentek.ypush.service.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

public class HttpUtil {
	public static final int CONNECT_TIMEOUT = 0;
	public static final int READ_TIMEOUT = 60000;
//	public int mHttpPort = 5555;
	private static final String TAG = "HttpUtil";
	private static MultipartEntity mpEntity = null;

	private static DefaultHttpClient client = null;


	public static String doHttpPost(Context context, Map<String,String> params,String postUrl, int port) throws IOException {
		Log.d(TAG, "postUrl:"+postUrl);
		initHttpClient(port);

		HttpPost post = new HttpPost(postUrl);

		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>(); 
		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			list.add(new BasicNameValuePair(key, params.get(key)));
		}
		post.setEntity(new UrlEncodedFormEntity(list,"UTF-8"));  

		String strResult = null;
		try {
			HttpResponse httpResponse = client.execute(post);
			strResult = EntityUtils.toString(httpResponse.getEntity());
			Log.d(TAG, "strResult = " + strResult);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException" + e.toString());
		} catch (IOException e) {
			Log.e(TAG, "IOException" + e.toString());
		} catch (Exception e) {
			Log.e(TAG, "Exception e: "+e);
		}finally {
			if (post != null) {
				post.abort();
			}
		}
		return strResult;
	}



	private static void initHttpClient(int httpPort) {
		// set delay time

		if (client == null) {
			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpPort));
			ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schReg);
			client = new DefaultHttpClient(connectionManager, params);
		}
	}

	public static String doStringHttpPost(Context context, String params,String postUrl, int port) throws IOException{
		HashMap<String,String> paramMap = new HashMap<String,String>();
		if(params !=null){
			paramMap.put("json_p", params);
		}
		return doHttpPost(context, paramMap, postUrl, port);
	}

	public static String doPostImage(String sUri, Map<String, String> maps, File file,boolean isFile, int httpPort) {
		try {

			initHttpClient(httpPort);

			HttpPost request = new HttpPost(sUri);
			FileBody bin = new FileBody(file);

			if (!isFile) {
				mpEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				if (maps != null) {
					for (Map.Entry<String, String> entrys : maps.entrySet()) {
						mpEntity.addPart(entrys.getKey(),new StringBody(entrys.getValue()));
					}
				}
				mpEntity.addPart("Filedata", bin);
				mpEntity.addPart("type", new StringBody("6"));
			} else {
				mpEntity = new MultipartEntity();
				if (maps != null) {
					for (Map.Entry<String, String> entrys : maps.entrySet()) {
						mpEntity.addPart(entrys.getKey(),
								new StringBody(entrys.getValue()));
					}
				}
				mpEntity.addPart("Filedata", bin);
				mpEntity.addPart("type", new StringBody("6"));
			}

			request.setEntity(mpEntity);
			HttpResponse httpResponse = client.execute(request);

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.e(TAG, "error codeStatus is "+httpResponse.getStatusLine().getStatusCode());
				return null;
			}else {
				String result = EntityUtils.toString(httpResponse.getEntity());
				Log.d(TAG, "The connection ok. result="+result);
				return result;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException" + e.toString());
		} catch (IOException e) {
			Log.e(TAG, "IOException" + e.toString());
		} catch (Exception e) {
			Log.e(TAG, "Exception e: "+e);
		} finally {
		}
		return null;
	}
}
