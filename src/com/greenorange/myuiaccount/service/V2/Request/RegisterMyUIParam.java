package com.greenorange.myuiaccount.service.V2.Request;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.greenorange.myuiaccount.Log;
import com.greenorange.myuiaccount.service.V2.ServiceHelper;

/**
 * Created by JasWorkSpace on 16/1/12.
 */
public class RegisterMyUIParam extends BaseRequestParam{
    public String username = "";
    public String password = "";
    public RegisterMyUIParam(){this(null);}
    public RegisterMyUIParam(RegisterMyUIParam logainParam){
        if(logainParam != null){
            username  = logainParam.username;
            password  = logainParam.password;
        }
    }
    @Override
    public boolean checkValid() {
        if(!TextUtils.isEmpty(username)
                && !TextUtils.isEmpty(password))return true;
        return false;
    }
    @Override
    public String getRequestParam() {
        try {
            RegisterMyUIParam logainParam = new RegisterMyUIParam(RegisterMyUIParam.this);
            logainParam.password = ServiceHelper.getEncrypt(logainParam.password);
            return new Gson().toJson(logainParam);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("RegisterMyUIParam getRequestParam " + e.toString());
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LogainParam{")
                .append("username="+username)
                .append(", password="+password)
                .append("}");
        return sb.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
