package com.ragentek.ypush.service.util;

import android.graphics.drawable.Drawable;

public class AppInstalledData extends AppData{
	private Drawable appIcon;

	public AppInstalledData() {
		super();
	}
	public AppInstalledData(AppData data) {
		super();
		set_id(data.get_id());
		setDescription(data.getDescription());
		setDeveloper(data.getDeveloper());
		setDownloadUrl(data.getDownloadUrl());
		setLogoUrl(data.getLogoUrl());
		setName(data.getName());
		setPackageName(data.getPackageName());
		setScore(data.getScore());
		setSize(data.getSize());
		setVersionCode(data.getVersionCode());
		setVersionName(data.getVersionName());
		setDownloadStatus(data.getDownloadStatus());
		setNotificationId(data.getNotificationId());
		setTotalDownloadTimes(data.getTotalDownloadTimes());
		setNeedUpdated(data.isNeedUpdated());
		setApkFilePath(data.getApkFilePath());
		setDlProgress(data.getDlProgress());
		setOldVersionName(data.getOldVersionName());
		setOldVersionCode(data.getOldVersionCode());
		setSourceFlag(data.getSourceFlag());
	}
	public Drawable getAppIcon() {
		return appIcon;
	}
	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}
}
