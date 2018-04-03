package com.ragentek.ypush.service.vo;

import android.graphics.drawable.Drawable;

public class InstalledApp {
	private String appName;
	private String packageName;
	private Drawable icon;
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	
}
