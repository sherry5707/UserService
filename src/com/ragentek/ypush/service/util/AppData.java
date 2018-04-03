package com.ragentek.ypush.service.util;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "appdata")
public class AppData {
	@DatabaseField
	private String _id;
	@DatabaseField
	private String description;
	@DatabaseField
	private String developer;
	@DatabaseField(id=true)
	private String downloadUrl;
	@DatabaseField
	private String logoUrl;
	@DatabaseField
	private String name;
	@DatabaseField
	private String packageName;
	@DatabaseField
	private String score;
	@DatabaseField
	private String size;
	@DatabaseField
	private String versionCode;
	@DatabaseField
	private String versionName;
	@DatabaseField
	private int downloadStatus;
	@DatabaseField
	private int notificationId;
	@DatabaseField
	private int totalDownloadTimes;
	@DatabaseField
	private boolean needUpdated;
	@DatabaseField
	private String apkFilePath;
	@DatabaseField
	private int dlProgress;
	@DatabaseField
	private String oldVersionName;
	@DatabaseField
	private String oldVersionCode;
	@DatabaseField
	private String sourceFlag;
	//add by zhengguang.yang@20160919 start for push user behavior
	@DatabaseField
	private String msgId;
	@DatabaseField
	private String appId;
	
	public String getSourceFlag() {
		return sourceFlag;
	}
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	//add by zhengguang.yang end
	
	public void setSourceFlag(String sourceFlag) {
		this.sourceFlag = sourceFlag;
	}
	public String getOldVersionName() {
		return oldVersionName;
	}
	public void setOldVersionName(String oldVersionName) {
		this.oldVersionName = oldVersionName;
	}
	public String getOldVersionCode() {
		return oldVersionCode;
	}
	public void setOldVersionCode(String oldVersionCode) {
		this.oldVersionCode = oldVersionCode;
	}
	public int getDlProgress() {
		return dlProgress;
	}
	public void setDlProgress(int dlProgress) {
		this.dlProgress = dlProgress;
	}
	public String getApkFilePath() {
		return apkFilePath;
	}
	public void setApkFilePath(String apkFilePath) {
		this.apkFilePath = apkFilePath;
	}
	public boolean isNeedUpdated() {
		return needUpdated;
	}
	public void setNeedUpdated(boolean needUpdated) {
		this.needUpdated = needUpdated;
	}
	public int getTotalDownloadTimes() {
		return totalDownloadTimes;
	}
	public void setTotalDownloadTimes(int totalDownloadTimes) {
		this.totalDownloadTimes = totalDownloadTimes;
	}
	public int getDownloadStatus() {
		return downloadStatus;
	}
	public void setDownloadStatus(int downloadStatus) {
		this.downloadStatus = downloadStatus;
	}
	public int getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDeveloper() {
		return developer;
	}
	public void setDeveloper(String developer) {
		this.developer = developer;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getLogoUrl() {
		return logoUrl;
	}
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
}

