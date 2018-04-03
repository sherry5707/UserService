package com.greenorange.myuiaccount.service.V2;

import com.ragentek.ypush.service.MyApplication;
import com.ragentek.ypush.service.util.CommonUtils;
import android.util.Log;

/**
 * Created by JasWorkSpace on 15/10/14.
 */
public class Config {

    /////////////////////////////////////////////////
    private final static boolean DEBUG = false;
    //域名
    private final static String SERVICE_URL      = "http://apiv2.kindui.com/gateway/call";
    private final static String SERVICE_URL_TEST = "http://test.apiv2.kindui.com/gateway/call";//"http://gateway.dcv2.kindui.com/gateway/call";//"http://test.kindui.com/gateway/call";
    //channel
    private final static String CHANNEL    = "myui_userservice";//"myui_account";
    private final static String CHANNELKEY = "81x2b22A5I80e84a1U6d9f301ec6Y512";//"IGSNVI8upAPnQBCIZhVFTaNmzUKTLzYC";
    //key
    private final static String KEY = "(!@#$^*)";
    //version
    private final static String VERSION = "2.0";
    public static String getVersion(String method) {
		return VERSION;
	}
	//
    private final static String FINDPSAAWORD_URL   = "http://passport.qingcheng.com/m/password/forgetpassword";
    private final static String CHANGEUSERINFO_URL = "http://passport.qingcheng.com/m/login/index?nexturl=http://passport.qingcheng.com/m/user/info";

    private final static String METHOD_LOGIN          = "common.auth.account.login";
    private final static String METHOD_REGISTER       = "common.auth.account.register";
    private final static String METHOD_CHANGEPASSWORD = "common.auth.account.changePassword";
    private final static String METHOD_UPDATEPASSWORD = "common.auth.account.updatePassword";
    private final static String METHOD_GETUSER        = "common.auth.account.getUser";
    private final static String METHOD_SENDMESSAGE    = "common.message.sms.send";
    private final static String METHOD_CHECKLEGENCYACCOUNTSTATE = "common.auth.account.checkLegacyAccountState";
    private final static String METHOD_BINDLEGENCYACCOUNT       = "common.auth.account.bindLegacyAccount";
    private final static String METHOD_UNBINDLEGENCYACCOUNT     = "common.auth.account.unbindLegacyAccount";
    private final static String METHOD_REGISTERMYUIACCOUNT      = "common.auth.account.registerMyUIAccount";
    //add by zhengguang.yang@20160115 start for userservice new push message
    private final static String METHOD_CONFIG      	= "push.message.config.get";
    private final static String METHOD_PUSH     	= "push.message.push.do";
    private final static String METHOD_PULL      	= "push.message.pull.do";

    //add by sherry for eventstatistics
    private final static String METHOD_EVENTSTISTICS="push.behavior.upload.app";
    public static String getMethodEventstistics(){
        return METHOD_EVENTSTISTICS;
    }
    //end by sherry

    //add by sherry for ip
    private final static String METHOD_GETIPADDRESS="common.net.ip.getExternalIp";
    public static String getIPAdress(){
        return METHOD_GETIPADDRESS;
    }
    //end by sherry


    public static String getMethodConfig() {
		return METHOD_CONFIG;
	}
	public static String getMethodPush() {
		return METHOD_PUSH;
	}
	public static String getMethodPull() {
		return METHOD_PULL;
	}
    //add by zhengguang.yang end
	
	//add by zhengguang.yang@20160919 start for push user behavior 
	//modified by zhengguang.yang@20170224 start for update METHOD_PUSH_BEHAVIOR
	//private final static String METHOD_PUSH_BEHAVIOR	= "push.behavior.pull.upload";
	private final static String METHOD_PUSH_BEHAVIOR	= "push.behavior.upload.message";
	//modified by zhengguang.yang end
	
	public static String getMethodPushBehavior(){
		return METHOD_PUSH_BEHAVIOR;
	}
	//add by zhengguang.yang end
    
    
	public static String getServiceUrl(){
//        return DEBUG ? SERVICE_URL_TEST : SERVICE_URL;
		boolean isBusiServer = CommonUtils.getBusinessServerSetting(MyApplication.getInstance());
		return isBusiServer?SERVICE_URL:SERVICE_URL_TEST;
    }
    public static String getServiceTestUrl(){
        return SERVICE_URL_TEST;
    }
    public static String getChannel(){
        return CHANNEL;
    }
    public static String getChannelkey(){
        return CHANNELKEY;
    }
    public static String getKey() {
        return KEY;
    }
    public static String getMethodLogin() {
        return METHOD_LOGIN;
    }
    public static String getMethodRegister() {
        return METHOD_REGISTER;
    }
    public static String getMethodChangepassword() {
        return METHOD_CHANGEPASSWORD;
    }
    public static String getMethodUpdatepassword() {
        return METHOD_UPDATEPASSWORD;
    }
    public static String getMethodGetuser() {
        return METHOD_GETUSER;
    }
    public static String getMethodSendmessage() {
        return METHOD_SENDMESSAGE;
    }
    public static String getFindpsaawordUrl() {
        return FINDPSAAWORD_URL;
    }
    public static String getChangeuserinfoUrl() {
        return CHANGEUSERINFO_URL;
    }
    public static String getMethodChecklegencyaccountstate() {
        return METHOD_CHECKLEGENCYACCOUNTSTATE;
    }
    public static String getMethodBindlegencyaccount() {
        return METHOD_BINDLEGENCYACCOUNT;
    }
    public static String getMethodUnbindlegencyaccount() {
        return METHOD_UNBINDLEGENCYACCOUNT;
    }
    public static String getMethodRegistermyuiaccount() {
        return METHOD_REGISTERMYUIACCOUNT;
    }
}
