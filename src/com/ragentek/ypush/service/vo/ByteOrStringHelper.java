package com.ragentek.ypush.service.vo;

import java.io.UnsupportedEncodingException;

import android.util.Log;
public class ByteOrStringHelper {

	private static final String TAG = "YPushService.ByteOrStringHelper";
	private static String CHAR_ENCODE = "UTF-8";
	public static void configCharEncode(String charEncode){
		CHAR_ENCODE = charEncode;
	}

	public static byte[] StringToByte(String str) {
		return StringToByte(str,CHAR_ENCODE);
	}

	public static String ByteToString(byte[] srcObj) {
		return ByteToString(srcObj,CHAR_ENCODE);
	}

	public static byte[] StringToByte(String str,String charEncode) {
		byte[] destObj = null;
		try {
		if(null == str || str.trim().equals("")){ 
			destObj = new byte[0]; 
			return destObj; 
		}else{ 
			destObj = str.getBytes(charEncode);
		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return destObj;
	 }
	public static String ByteToString(byte[] srcObj,String charEncode) {
		String destObj = null;
		try {
			destObj = new String(srcObj,charEncode);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		Log.v(TAG, "destObj:" + destObj);
		return destObj.replaceAll("\0"," ");
	}

 }