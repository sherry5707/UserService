package com.ragentek.ypush.service;

import java.io.Serializable;


public class PushBehaviorData implements Serializable{
	private String msgId;
	private String behavior;
	private String appId;
	private String packageName;
	
	
	
	public String getMsgId() {
		return msgId;
	}



	public void setMsgId(String msgId) {
		this.msgId = msgId;
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



	public String getPackageName() {
		return packageName;
	}



	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PushBehaviorData [msgId=");
		builder.append(msgId);
		builder.append(", behavior=");
		builder.append(behavior);
		builder.append(", appId=");
		builder.append(appId);
		builder.append(", packageName=");
		builder.append(packageName);
		builder.append("]");
		return builder.toString();
	}
	

}
