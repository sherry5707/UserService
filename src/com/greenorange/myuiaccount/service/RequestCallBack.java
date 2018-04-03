package com.greenorange.myuiaccount.service;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.greenorange.myuiaccount.Log;
import com.greenorange.myuiaccount.service.V2.Response.BaseResponse;
import com.greenorange.myuiaccount.service.V2.ServiceAPI;

/**
 * Created by JasWorkSpace on 15/11/26.
 */
public class RequestCallBack implements IRequestCallBack {
    private Gson   gson = new Gson();
    private String response = "";
    private final static boolean DEBUG = false;
    @Override
    public void onStart() {
        if(DEBUG) Log.d("onStart");
    }
    @Override
    public void onSuccess(String s) {
        if(DEBUG)Log.d("onSuccess-->"+s);
        response = s;
    }
    @Override
    public void onFailure(Throwable throwable, String s) {
        throwable.printStackTrace();
        Log.i("RequestCallBack fail-->"+throwable.toString());
        if(DEBUG)Log.d("onFailure -->" + s);
    }
    @Override
    public void onFinish() {
        if(DEBUG)Log.d("onFinish");
    }
    public String getResponse(){
        return response;
    }

    ///////////////////////////////
    public String getV2Response() throws Exception {
        if(!TextUtils.isEmpty(getResponse())) {
            return ServiceAPI.API_MYUI_ParserBaseResponse(getV2BaseResponse());
        }
        return null;
    }

    public BaseResponse getV2BaseResponse(){
        try{
            if(!TextUtils.isEmpty(getResponse())) {
                return gson.fromJson(getResponse(), BaseResponse.class);
            }
        }catch (Throwable e){
            e.printStackTrace();
            Log.d("getV2BaseResponse fail-->"+e.toString());
        }
        return null;
    }
    
}
