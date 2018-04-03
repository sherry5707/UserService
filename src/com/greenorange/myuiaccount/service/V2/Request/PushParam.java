package com.greenorange.myuiaccount.service.V2.Request;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.greenorange.myuiaccount.Log;
import com.greenorange.myuiaccount.service.V2.ServiceHelper;

public class PushParam extends BaseRequestParam{
	private String data;
	private String length;
	
	public String getData() {
		return data;
	}


	public void setData(String data) {
		this.data = data;
	}


	public String getLength() {
		return length;
	}


	public void setLength(String length) {
		this.length = length;
	}


	@Override
	public boolean checkValid() {
        if(!TextUtils.isEmpty(data)
                && !TextUtils.isEmpty(length))return true;
        return false;
    }
	

	 public PushParam(){this(null);}


	public PushParam(PushParam pushParam) {
        if(pushParam != null){
            data  = pushParam.data;
            length  = pushParam.length;
        }
    }


	@Override
	public String getRequestParam() {
		// TODO Auto-generated method stub
		try {
			PushParam pushParam = new PushParam(PushParam.this);
			pushParam.data = ServiceHelper.getEncrypt(ServiceHelper.getEncodeParam(pushParam.data));
            pushParam.length = String.valueOf(pushParam.data.length());
			return new Gson().toJson(pushParam);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ConfigParam getRequestParam " + e.toString());
        }
        return null;
	}


	@Override
	public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LogainParam{")
                .append("data="+data)
                .append(", length="+length)
                .append("}");
        return sb.toString();
    }
	
	

}
