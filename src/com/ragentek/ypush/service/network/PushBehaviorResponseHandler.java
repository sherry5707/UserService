package com.ragentek.ypush.service.network;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ragentek.ypush.service.R;

import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class PushBehaviorResponseHandler extends AsyncHttpResponseHandler {
	protected Gson gson = new Gson();
	protected Class<?> classzz;
	protected BaseResponse response;
	protected boolean getDataSuccess = false;
	private Activity activity;
	private String msgId;
	private String appId;
	
	public PushBehaviorResponseHandler(Activity activity, Class<?> classzz,String msgId,String appId) {
		super();
		this.activity = activity;
		this.classzz = classzz;
		this.msgId = msgId;
		this.appId = appId;
	}

    @Override
	public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
		if(System.currentTimeMillis()-HttpClient.beginTime>5000){
			HttpClient.isNetSpeedSlow = true;
		}else{
			HttpClient.isNetSpeedSlow = false;
		}
		if(arg2 != null){
			String result = new String(arg2);
			System.out.println("---result success---"+result);
			View progress = activity.findViewById(R.id.progressBar);
			if(progress!=null){
				progress.setVisibility(View.GONE);
			}
			try{
				response = (BaseResponse)gson.fromJson(result, classzz);
				if(response.getStatus()==null || !response.getStatus().equals("1")){
					getDataSuccess = false;
					showNetworkErrorMsg();
				}else{
					getDataSuccess = true;
				}
			}catch(Exception e){
				e.printStackTrace();
				getDataSuccess = false;
			}
		}
	}
	
	@Override
	public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
		if(arg2 != null){
			String result = new String(arg2);
			System.out.println("---result failure---"+result);
			View progress = activity.findViewById(R.id.progressBar);
			if(progress!=null){
				progress.setVisibility(View.GONE);
			}
			showNetworkErrorMsg();
		}
	}
	//modified by zhengguang.yang 2015.11.20 start. for android6.0 make asynchttpclient use. 

	private void showNetworkErrorMsg(){
		Toast.makeText(activity, activity.getString(R.string.tips_network_error),Toast.LENGTH_SHORT).show();
	}
	
}
