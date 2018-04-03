package com.ragentek.ypush.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;

public class EventStatisticsData implements Serializable{
	/*private String deviceId;
	private String romVersion;
	private String productModel;
	private String ipAddress;
	private String behaviorTime;*/

	//private String ;
	private String behavior;
    private String appId;
    private String appSrc;
    private String packageName;
    private String client;
    private String listenArea;
    private String listenContextId;
    private String listenContextSrc;
    private String clientVersion;
    private String referenceId;
	private String appName;
	private String appVersion;

	public EventStatisticsData(){}

	public EventStatisticsData(String appdata){
		Log.e("EventStatisticsData","appdata:"+appdata);
		String[] s1 = appdata.split(";");
		int i = 0;
		for (String s2 : s1) {
			String[] keyValue = s2.split(",");
			if(keyValue.length>1){
				SetParamFromApp(i, keyValue[1]);
			}
			i++;
		}
	}

    public void SetParamFromApp(int i,String param){
		if(TextUtils.isEmpty(param)){
			return;
		}
		switch (i) {
			case 0:
				setBehavior(param);
				break;
			case 1:
				setAppId(param);
				break;
			case 2:
				setAppSrc(param);
				break;
			case 3:
				setPackageName(param);
				break;
			case 4:
				setClient(param);
				break;
			case 5:
				setListenArea(param);
				break;
			case 6:
				setListenContextId(param);
				break;
			case 7:
				setListenContextSrc(param);
				break;
			case 8:
				setClientVersion(param);
				break;
			case 9:
				setReferenceId(param);
				break;
			case 10:
				setAppName(param);
				break;
			case 11:
				setAppVersion(param);
				break;
			default:
				break;
		}
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}


	public String getBehavior() {
		return behavior;
	}
	public void setBehavior(String behavior) {
		this.behavior = behavior;
	}

	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppSrc() {
		return appSrc;
	}
	public void setAppSrc(String appSrc) {
		this.appSrc = appSrc;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getListenArea() {
		return listenArea;
	}
	public void setListenArea(String listenArea) {
		this.listenArea = listenArea;
	}
	public String getListenContextId() {
		return listenContextId;
	}
	public void setListenContextId(String listenContextId) {
		this.listenContextId = listenContextId;
	}
	public String getListenContextSrc() {
		return listenContextSrc;
	}
	public void setListenContextSrc(String listenContextSrc) {
		this.listenContextSrc = listenContextSrc;
	}
	public String getClientVersion() {
		return clientVersion;
	}
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}
	public String getReferenceId() {
		return referenceId;
	}
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	@Override
	public String toString() {
		return "EventStatisticsData{" +
				"appId='" + appId + '\'' +
				", behavior='" + behavior + '\'' +
				", appSrc='" + appSrc + '\'' +
				", packageName='" + packageName + '\'' +
				", client='" + client + '\'' +
				", listenArea='" + listenArea + '\'' +
				", listenContextId='" + listenContextId + '\'' +
				", listenContextSrc='" + listenContextSrc + '\'' +
				", clientVersion='" + clientVersion + '\'' +
				", referenceId='" + referenceId + '\'' +
				", appName='" + appName + '\'' +
				", appVersion='" + appVersion + '\'' +
				'}';
	}
}
