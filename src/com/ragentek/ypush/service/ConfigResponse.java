package com.ragentek.ypush.service;

import java.io.Serializable;

public class ConfigResponse implements Serializable{
	private int pushInterval;
	private int pullInterval;
	private int msgExpiredTime;
	public int getPushInterval() {
		return pushInterval;
	}
	public void setPushInterval(int pushInterval) {
		this.pushInterval = pushInterval;
	}
	public int getPullInterval() {
		return pullInterval;
	}
	public void setPullInterval(int pullInterval) {
		this.pullInterval = pullInterval;
	}
	public int getMsgExpiredTime() {
		return msgExpiredTime;
	}
	public void setMsgExpiredTime(int msgExpiredTime) {
		this.msgExpiredTime = msgExpiredTime;
	}
	@Override
	public String toString() {
		return "ConfigResponse [pushInterval=" + pushInterval
				+ ", pullInterval=" + pullInterval + ", msgExpiredTime="
				+ msgExpiredTime + "]";
	}
	
}
