package com.greenorange.myuiaccount.service.V2.Request;

import com.google.gson.Gson;
import com.greenorange.myuiaccount.Log;

public class ConfigParam extends BaseRequestParam{

	@Override
	public boolean checkValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getRequestParam() {
		// TODO Auto-generated method stub
		try {
            return new Gson().toJson(ConfigParam.this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ConfigParam getRequestParam " + e.toString());
        }
        return null;
	}

}
