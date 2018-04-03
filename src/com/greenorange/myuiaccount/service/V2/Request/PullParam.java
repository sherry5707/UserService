package com.greenorange.myuiaccount.service.V2.Request;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.greenorange.myuiaccount.Log;
import com.greenorange.myuiaccount.service.V2.ServiceHelper;

public class PullParam extends BaseRequestParam{
	String deviceid;
	String pushversion;
	String model;
	String ipaddress;
	
	
	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

	public String getPushversion() {
		return pushversion;
	}

	public void setPushversion(String pushversion) {
		this.pushversion = pushversion;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public boolean checkValid() {
        if(!TextUtils.isEmpty(deviceid)
                && !TextUtils.isEmpty(pushversion)
                && !TextUtils.isEmpty(ipaddress)
                && !TextUtils.isEmpty(model))return true;
        return false;
    }

	
	public PullParam() {
		this(null);
	}
	public PullParam(PullParam pullParam) {
        if(pullParam != null){
        	deviceid  		= pullParam.deviceid;
        	pushversion  	= pullParam.pushversion;
        	ipaddress 		= pullParam.ipaddress;
        	model       	= pullParam.model;
        }
	}

	@Override
	public String getRequestParam() {
		// TODO Auto-generated method stub
		try {
			PullParam pullParam = new PullParam(PullParam.this);
            return new Gson().toJson(pullParam);
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
                .append("deviceid="+deviceid)
                .append(", pushversion="+pushversion)
                .append(", model="+model)
                .append(", ipaddress="+ipaddress)
                .append("}");
        return sb.toString();
    }
	
	

}
