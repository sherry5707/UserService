package com.greenorange.myuiaccount.service;

import android.text.TextUtils;
import com.greenorange.myuiaccount.Log;
import com.greenorange.myuiaccount.service.V1.Request.BaseRequestParam;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.Header;

/**
 * Created by JasWorkSpace on 15/11/2.
 */
public class RequestHelper {
    private static AsyncHttpClient client = new AsyncHttpClient();
	//add by zhengguang.yang@20160920 start for socket timeout exception
	static{
		client.setTimeout(30000);
		client.setConnectTimeout(30000);
		client.setResponseTimeout(30000);
	}
	//add by zhengguang.yang end

    public static boolean Request_Sync(String url, BaseRequestParam baseRequestParam, final IRequestCallBack callBack) throws Exception {
    	//modified by zhengguang.yang 2016.02.18 start. for android6.0 make asynchttpclient use. 
//        return Request_Async(new SyncHttpClient() {
//            @Override
//            public String onRequestFailed(Throwable throwable, String s) {
//                if (callBack != null) {
//                    callBack.onFailure(throwable, null);
//                }
//                return null;
//            }
//    }, url, baseRequestParam, callBack);
        return Request_Async(new SyncHttpClient(), url, baseRequestParam, callBack);
        //modified by zhengguang.yang 2016.02.18 end.
    }
    public static boolean Request_Sync(String url, RequestParams baseRequestParam, final IRequestCallBack callBack) throws Exception{
    	//modified by zhengguang.yang 2016.02.18 start. for android6.0 make asynchttpclient use. 
//    	return Request_Async(new SyncHttpClient() {
//            @Override
//            public String onRequestFailed(Throwable throwable, String s) {
//                if (callBack != null) {
//                    callBack.onFailure(throwable, null);
//                }
//                return null;
//            }
//        }, url, baseRequestParam, callBack);
        return Request_Async(new SyncHttpClient(), url, baseRequestParam, callBack);
        //modified by zhengguang.yang 2016.02.18 end. 
    }
    public static boolean Request_Async(String url, BaseRequestParam baseRequestParam, final IRequestCallBack callBack) throws Exception {
        return Request_Async(client, url, baseRequestParam, callBack);
    }
    public static boolean Request_Async(AsyncHttpClient asyncHttpClient, String url, BaseRequestParam baseRequestParam, final IRequestCallBack callBack) throws Exception {
        try{
            if(baseRequestParam == null || !baseRequestParam.checkValid()){
                throw new Exception("invalid param !!!");
            }
            return Request_Async(asyncHttpClient, url, baseRequestParam.getRequestParam(), callBack);
        }catch (Throwable e){e.printStackTrace();
            Log.d("RequestHelper  Request_Async fail -->" + e.toString());
        }
        throw new Exception("unknow fail");
    }
    public static boolean Request_Async(String url, RequestParams baseRequestParam, final IRequestCallBack callBack) throws Exception{
        return Request_Async(client, url, baseRequestParam, callBack);
    }
    public static boolean Request_Async(AsyncHttpClient asyncHttpClient, String url, RequestParams baseRequestParam, final IRequestCallBack callBack) throws Exception {
        try{
            if(TextUtils.isEmpty(url)){
                //if(!URLUtil.isNetworkUrl(url))throw new Exception("invalid url !!!");//only judge it none.
                throw new Exception("invalid url !!!");
            }
            Log.d("Request_Async-->" + url + (baseRequestParam == null ? "" : ("?" + baseRequestParam.toString())));
            String requestUrl = url + (baseRequestParam == null ? "" : ("?" + baseRequestParam.toString()));
            
            asyncHttpClient.post(url, baseRequestParam, new AsyncHttpResponseHandler(){
            	//modified by zhengguang.yang 2016.02.18 start. for android6.0 make asynchttpclient use. 
//                @Override
//                public void onSuccess(String s) {
//                    super.onSuccess(s);
//                    Log.d("Request_Async onSuccess()--> s="+s);
//                    if(callBack != null){
//                        callBack.onSuccess(s);
//                    }
//                }
//                
//                @Override
//                public void onFailure(Throwable throwable, String s) {
//                    super.onFailure(throwable, s);
//                    Log.d("Request_Async onFailure()");
//                    if(callBack != null){
//                        callBack.onFailure(throwable, s);
//                    }
//                }
            	
            	@Override
        		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
        				Throwable throwable) {
//                  Log.d("Request_Async onFailure()");
                  if(arg2 != null){
                	  String s = new String(arg2);
                	  if(callBack != null){
                		  callBack.onFailure(throwable, s);
                	  }        			
                  }
        		}
            	@Override
            	public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
            	  String s = new String(arg2);
//                  Log.d("Request_Async onSuccess()--> s="+s);
                  if(callBack != null){
                      callBack.onSuccess(s);
                  }            		
            	}
              //modified by zhengguang.yang 2016.02.18 end. 
                
                @Override
                public void onStart() {
                    super.onStart();
//                    Log.d("Request_Async onStart()");
                    if(callBack != null){
                        callBack.onStart();
                    }
                }
                @Override
                public void onFinish() {
                    super.onFinish();
//                    Log.d("Request_Async onFinish()");
                    if(callBack != null){
                        callBack.onFinish();
                    }
                }
            });
            return true;
        }catch (Throwable e){e.printStackTrace();
            Log.d("RequestHelper  Request_Async fail -->" + e.toString());
        }
        throw new Exception("unknow fail");
    }

}
