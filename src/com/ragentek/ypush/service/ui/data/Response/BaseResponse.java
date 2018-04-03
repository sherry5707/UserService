package com.ragentek.ypush.service.ui.data.Response;

import android.text.TextUtils;

public class BaseResponse {
	public String result;
	public String message;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean isSuccess(){
		return TextUtils.equals("success", result);
	}
}
