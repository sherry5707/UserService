package com.greenorange.myuiaccount.service.V2;

import com.greenorange.myuiaccount.Log;
import com.greenorange.myuiaccount.service.IRequestCallBack;
import com.greenorange.myuiaccount.service.RequestHelper;
import com.greenorange.myuiaccount.service.V2.Request.BaseRequestParam;
import com.greenorange.myuiaccount.service.V2.Request.BindLegencyAccountParam;
import com.greenorange.myuiaccount.service.V2.Request.ChangePasswordParam;
import com.greenorange.myuiaccount.service.V2.Request.CheckOldAccountStateParam;
import com.greenorange.myuiaccount.service.V2.Request.ConfigParam;
import com.greenorange.myuiaccount.service.V2.Request.GetUserParam;
import com.greenorange.myuiaccount.service.V2.Request.LogainParam;
import com.greenorange.myuiaccount.service.V2.Request.PullParam;
import com.greenorange.myuiaccount.service.V2.Request.PushBehaviorParam;
import com.greenorange.myuiaccount.service.V2.Request.PushParam;
import com.greenorange.myuiaccount.service.V2.Request.RegisterMyUIParam;
import com.greenorange.myuiaccount.service.V2.Request.RegisterParam;
import com.greenorange.myuiaccount.service.V2.Request.SendMessageParam;
import com.greenorange.myuiaccount.service.V2.Response.BaseResponse;
import com.loopj.android.http.RequestParams;
import android.text.TextUtils;
/**
 * Created by JasWorkSpace on 15/10/15.
 */
public class ServiceAPI {

    public static String API_MYUI_Logain(String username, String password) throws Exception {
        LogainParam logainParam = new LogainParam();
        logainParam.setUsername(username);
        logainParam.setPassword(password);
        return API_MYUI_Logain(logainParam);
    }
    public static String API_MYUI_Logain(LogainParam logainParam) throws Exception {
        return API_MYUI_Request(Config.getMethodLogin(), logainParam);
    }
    public static String API_MYUI_Register(String account, String realname, String password,
                                           String mobileno, String email, String validatedBy) throws Exception {
        RegisterParam registerParam = new RegisterParam();
        registerParam.setAccount(account);
        registerParam.setRealname(realname);
        registerParam.setPassword(password);
        registerParam.setMobileno(mobileno);
        registerParam.setEmail(email);
        registerParam.setValidatedBy(validatedBy);
        return API_MYUI_Register(registerParam);
    }
    public static String API_MYUI_Register(RegisterParam registerParam) throws Exception {
        return API_MYUI_Request(Config.getMethodRegister(), registerParam);
    }
    public static String API_MYUI_ChangePassword(String account, String oldPassword, String newPassword) throws Exception {
        ChangePasswordParam changePassword = new ChangePasswordParam();
        changePassword.setAccount(account);
        changePassword.setOldPassword(oldPassword);
        changePassword.setNewPassword(newPassword);
        return API_MYUI_ChangePassword(changePassword);
    }
    public static String API_MYUI_ChangePassword(ChangePasswordParam changePassword) throws Exception {
        return API_MYUI_Request(Config.getMethodChangepassword(), changePassword);
    }
    public static String API_MYUI_GetUser(String  ticket) throws Exception {
        GetUserParam getUserParam = new GetUserParam();
        getUserParam.setTicket(ticket);
        return API_MYUI_GetUser(getUserParam);
    }
    public static String API_MYUI_GetUser(GetUserParam getUserParam) throws Exception {
        return API_MYUI_Request(Config.getMethodGetuser(), getUserParam);
    }
    public static String API_MYUI_SendSMS(String mobileno, String message) throws Exception {
        SendMessageParam sendMessageParam = new SendMessageParam();
        sendMessageParam.setMobileno(mobileno);
        sendMessageParam.setSmsBody(message);
        return API_MYUI_SendSMS(sendMessageParam);
    }
    public static String API_MYUI_SendSMS(SendMessageParam param) throws Exception {
        return API_MYUI_Request(Config.getMethodSendmessage(), param);
    }
    public static String API_MYUI_CheckLegencyAccountState(String uuid) throws Exception {
        CheckOldAccountStateParam checkOldAccountStateParam = new CheckOldAccountStateParam();
        checkOldAccountStateParam.setUuid(uuid);
        return API_MYUI_CheckLegencyAccountState(checkOldAccountStateParam);
    }
    public static String API_MYUI_CheckLegencyAccountState(CheckOldAccountStateParam checkOldAccountStateParam) throws Exception {
        return API_MYUI_Request(Config.getMethodChecklegencyaccountstate(), checkOldAccountStateParam);
    }
    public static String API_MYUI_BindLegencyAccount(String uuid, String uid) throws Exception {
        BindLegencyAccountParam bindLegencyAccountParam = new BindLegencyAccountParam();
        bindLegencyAccountParam.setUuid(uuid);
        bindLegencyAccountParam.setUid(uid);
        return API_MYUI_BindLegencyAccount(bindLegencyAccountParam);
    }
    public static String API_MYUI_BindLegencyAccount(BindLegencyAccountParam param) throws Exception {
        return API_MYUI_Request(Config.getMethodBindlegencyaccount(), param);
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    public static String API_MYUI_Request(String method, BaseRequestParam param) throws Exception {
        BaseResponse baseResponse = null;
        try{//first check param
            if(param == null || !param.checkValid())throw new Exception("invalid param !!!");
        }catch (Throwable e){e.printStackTrace();   throw new Exception("invalid param !!!");}
        try{
            baseResponse = ServiceHelper.getServiceBaseResponse(method, param.getRequestParam());
            if(baseResponse != null)Log.i("baseResponse-->"+baseResponse.toString());
        }catch (Throwable e){e.printStackTrace();}
        return API_MYUI_ParserBaseResponse(baseResponse);
    }
    
    public static boolean API_MYUI_Request_Async(RequestParams requestParams, IRequestCallBack callBack) throws Exception {
        return RequestHelper.Request_Async(Config.getServiceUrl(), requestParams, callBack);
    }
    public static boolean API_MYUI_Request_Test_Async(RequestParams requestParams, IRequestCallBack callBack) throws Exception {
        return RequestHelper.Request_Async(Config.getServiceTestUrl(), requestParams, callBack);
    }
    public static boolean API_MYUI_Request_Sync(RequestParams requestParams, IRequestCallBack callBack) throws Exception {
        return RequestHelper.Request_Sync(Config.getServiceUrl(), requestParams, callBack);
    }
    public static String API_MYUI_ParserBaseResponse(BaseResponse baseResponse) throws Exception {
        if(baseResponse != null) {
            if (baseResponse.checkValid()) {
                return ServiceHelper.getDecodeParam(baseResponse.businessData);
            }else {
                throw new Exception(baseResponse.getFailMessage());
            }
        }
        throw new Exception("unknow fail");
    }
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////// Async
    ///////////////////////////////////////////////////////////////////////////////////
    public static RequestParams API_MYUI_getCheckLegencyAccountStateRequestParams(String uuid){
        try{
            CheckOldAccountStateParam checkOldAccountStateParam = new CheckOldAccountStateParam();
            checkOldAccountStateParam.setUuid(uuid);
            return ServiceHelper.getServiceRequestParams(Config.getMethodChecklegencyaccountstate(),
                        checkOldAccountStateParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getCheckLegencyAccountStateRequestParams fail" + e.toString());}
        return null;
    }
    public static RequestParams API_MYUI_getBindLegencyAccountRequestParams(String uuid, String uid){
        try{
            BindLegencyAccountParam bindLegencyAccountParam = new BindLegencyAccountParam();
            bindLegencyAccountParam.setUid(uid);
            bindLegencyAccountParam.setUuid(uuid);
            return ServiceHelper.getServiceRequestParams(Config.getMethodBindlegencyaccount(),
                            bindLegencyAccountParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getBindLegencyAccountRequestParams fail" + e.toString());}
        return null;
    }
    public static RequestParams API_MYUI_getUnBindLegencyAccountRequestParams(String uuid, String uid){
        try{
            BindLegencyAccountParam bindLegencyAccountParam = new BindLegencyAccountParam();
            bindLegencyAccountParam.setUid(uid);
            bindLegencyAccountParam.setUuid(uuid);
            return ServiceHelper.getServiceRequestParams(Config.getMethodUnbindlegencyaccount(),
                        bindLegencyAccountParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getUnBindLegencyAccountRequestParams fail" + e.toString());}
        return null;
    }
    public static RequestParams API_MYUI_getLogainRequestParams(String username, String password, String env){
        try{
            LogainParam logainParam = new LogainParam();
            logainParam.setUsername(username);
            logainParam.setPassword(password);
            logainParam.setEnv(env);
            return ServiceHelper.getServiceRequestParams(Config.getMethodLogin(),
                    logainParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getLogainRequestParams fail" + e.toString());}
        return null;
    }
    public static  RequestParams API_MYUI_getRegisterParams(String username, String password){
        return API_MYUI_getRegisterParams(username, password, RegisterParam.MODE_REGISTER_NORMAL);
    }
    public static RequestParams API_MYUI_getRegisterParams(String username, String password, int mode){
        try{
            RegisterParam registerParam = new RegisterParam();
            registerParam.setAccount(username);
            registerParam.setRealname(username);
            registerParam.setPassword(password);
            registerParam.MODE_REGISTER = mode;
            return ServiceHelper.getServiceRequestParams(Config.getMethodRegister(),
                    registerParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getLogainRequestParams fail" + e.toString());}
        return null;
    }
    public static RequestParams API_MYUI_getRegisterMyUIAccountParams(String username, String password){
        try{
            RegisterMyUIParam registerParam = new RegisterMyUIParam();
            registerParam.setPassword(password);
            registerParam.setUsername(username);
            return ServiceHelper.getServiceRequestParams(Config.getMethodRegistermyuiaccount(),
                    registerParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getRegisterMyUIAccountParams fail" + e.toString());}
        return null;
    }
    
    //add by zhengguang.yang@20160115 start for userservice new push message
    //method 1:asynchttpclient
    public static RequestParams API_MYUI_getConfigRequestParams(){
        try{
        	ConfigParam configParam = new ConfigParam();
            return ServiceHelper.getServiceRequestParams(Config.getMethodConfig(),configParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getConfigRequestParams fail" + e.toString());}
        return null;
    }
    
    public static RequestParams API_MYUI_getPushRequestParams(String data){
        try{
        	PushParam pushParam = new PushParam();
        	pushParam.setData(data);
        	pushParam.setLength(String.valueOf(data.length()));
            return ServiceHelper.getServiceRequestParams(Config.getMethodPush(),pushParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getPushRequestParams fail" + e.toString());}
        return null;
    }
    
    public static RequestParams API_MYUI_getPullRequestParams(String deviceid, String version, String model, String ipaddress){
        try{
        	PullParam pullParam = new PullParam();
        	pullParam.setDeviceid(deviceid);
        	pullParam.setPushversion(version);
        	pullParam.setModel(model);
        	pullParam.setIpaddress(ipaddress);
        	Log.i("yy","API_MYUI_getPullRequestParams-->ipaddress="+ipaddress+",pullParam="+pullParam.toString());
            return ServiceHelper.getServiceRequestParams(Config.getMethodPull(),pullParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getPullRequestParams fail" + e.toString());}
        return null;
    }
    
    //add by zhengguang.yang@20160919 start for push user behavior
    public static RequestParams API_MYUI_getPushBehaviorRequestParams(String data){
        try{
        	PushBehaviorParam pushBehaviorParam = new PushBehaviorParam();
        	pushBehaviorParam.setData(data);
        	pushBehaviorParam.setLength(String.valueOf(data.length()));
        	Log.d("API_MYUI_getPushBehaviorRequestParams pushBehaviorParam="+pushBehaviorParam.toString());
            return ServiceHelper.getServiceRequestParams(Config.getMethodPushBehavior(),pushBehaviorParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getPushBehaviorRequestParams fail" + e.toString());}
        return null;
    }
    //add by zhengguang.yang end

    //add by sherry for eventstatistics
    public static RequestParams API_MYUI_getPushEventStatisticsRequestParams(String data){
        try{
            PushBehaviorParam pushBehaviorParam = new PushBehaviorParam();
            pushBehaviorParam.setData(data);
            pushBehaviorParam.setLength(String.valueOf(data.length()));
            android.util.Log.d("API_MYUI_getPushEventStatisticsRequestParams","pushBehaviorParam="+pushBehaviorParam.toString());
            return ServiceHelper.getServiceRequestParams(Config.getMethodEventstistics(),pushBehaviorParam);
        }catch (Throwable e){e.printStackTrace();Log.d("API_MYUI_getPushBehaviorRequestParams fail" + e.toString());}
        return null;
    }


    //add by sherry for ip
    public static RequestParams API_MYUI_getIPRequestParams() {
        try {
            return ServiceHelper.getIPParams(Config.getIPAdress());
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("API_MYUI_getIPParams fail" + e.toString());
        }
        return null;
    }

    
    //method 2:HttpURLConnection
    public static String API_MYUI_Config() throws Exception {
    	ConfigParam configParam = new ConfigParam();
        return API_MYUI_Config(configParam);
    }
    public static String API_MYUI_Config(ConfigParam configParam) throws Exception {
        return API_MYUI_Request(Config.getMethodConfig(), configParam);
    }
    
    public static String API_MYUI_Pull() throws Exception {
    	PullParam pullParam = new PullParam();
    	pullParam.setDeviceid("867923020000266");
    	pullParam.setPushversion("123456");
    	pullParam.setModel("123");
        return API_MYUI_Pull(pullParam);
    }
    public static String API_MYUI_Pull(PullParam pullParam) throws Exception {
        return API_MYUI_Request(Config.getMethodPull(), pullParam);
    }
    
    public static String API_MYUI_Push() throws Exception {
    	PushParam pushParam = new PushParam();
    	String data = "1234567";
    	pushParam.setData(data);
    	pushParam.setLength(String.valueOf(data.length()));
        return API_MYUI_Push(pushParam);
    }
    public static String API_MYUI_Push(PushParam pushParam) throws Exception {
        return API_MYUI_Request(Config.getMethodPush(), pushParam);
    }
    //add by zhengguang.yang end
    
}
