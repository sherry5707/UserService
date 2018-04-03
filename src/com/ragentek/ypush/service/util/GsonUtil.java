package com.ragentek.ypush.service.util;

import com.google.gson.Gson;

public class GsonUtil {

	public static Object convertObject(Object data, Class<?> to) {
		Gson gson = new Gson();
		//String json = gson.toJson(data);
		//return gson.fromJson(json, to);
		return gson.fromJson((String)data, to);
	}
	
	public static String convertJson(Object data) {
		Gson gson = new Gson();
		return gson.toJson(data);
	}
}
