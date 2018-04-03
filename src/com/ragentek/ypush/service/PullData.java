package com.ragentek.ypush.service;

public class PullData {
	private String createdDate;
    private String createdOn;
    private String description;
    private String deviceIds;
    private String downloadType;
    private String downloadUrl;
    private String endDate;
    private String icon;
    private String pushType;
    private String receivedCount;
    private String startDate;
    private String title;
    private String totalCount;
    private String updatedDate;
    private String updatedOn;
    private String pushVersion;
    private String model;
    private String _id;
    private String runtimeDeviceId;
    private String runtimePushVersion;
    private String runtimeModel;
    private String url;
    private String referAppId;
    
    
    
    
    
	public String getCreatedDate() {
		return createdDate;
	}





	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}





	public String getCreatedOn() {
		return createdOn;
	}





	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}





	public String getDescription() {
		return description;
	}





	public void setDescription(String description) {
		this.description = description;
	}





	public String getDeviceIds() {
		return deviceIds;
	}





	public void setDeviceIds(String deviceIds) {
		this.deviceIds = deviceIds;
	}





	public String getDownloadType() {
		return downloadType;
	}





	public void setDownloadType(String downloadType) {
		this.downloadType = downloadType;
	}





	public String getDownloadUrl() {
		return downloadUrl;
	}





	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}





	public String getEndDate() {
		return endDate;
	}





	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}





	public String getIcon() {
		return icon;
	}





	public void setIcon(String icon) {
		this.icon = icon;
	}





	public String getPushType() {
		return pushType;
	}





	public void setPushType(String pushType) {
		this.pushType = pushType;
	}





	public String getReceivedCount() {
		return receivedCount;
	}





	public void setReceivedCount(String receivedCount) {
		this.receivedCount = receivedCount;
	}





	public String getStartDate() {
		return startDate;
	}





	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}





	public String getTitle() {
		return title;
	}





	public void setTitle(String title) {
		this.title = title;
	}





	public String getTotalCount() {
		return totalCount;
	}





	public void setTotalCount(String totalCount) {
		this.totalCount = totalCount;
	}





	public String getUpdatedDate() {
		return updatedDate;
	}





	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}





	public String getUpdatedOn() {
		return updatedOn;
	}





	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}





	public String getPushVersion() {
		return pushVersion;
	}





	public void setPushVersion(String pushVersion) {
		this.pushVersion = pushVersion;
	}





	public String getModel() {
		return model;
	}





	public void setModel(String model) {
		this.model = model;
	}





	public String get_id() {
		return _id;
	}





	public void set_id(String _id) {
		this._id = _id;
	}





	public String getRuntimeDeviceId() {
		return runtimeDeviceId;
	}





	public void setRuntimeDeviceId(String runtimeDeviceId) {
		this.runtimeDeviceId = runtimeDeviceId;
	}





	public String getRuntimePushVersion() {
		return runtimePushVersion;
	}





	public void setRuntimePushVersion(String runtimePushVersion) {
		this.runtimePushVersion = runtimePushVersion;
	}





	public String getRuntimeModel() {
		return runtimeModel;
	}





	public void setRuntimeModel(String runtimeModel) {
		this.runtimeModel = runtimeModel;
	}





	public String getUrl() {
		return url;
	}





	public void setUrl(String url) {
		this.url = url;
	}





	public String getReferAppId() {
		return referAppId;
	}





	public void setReferAppId(String referAppId) {
		this.referAppId = referAppId;
	}





	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PullData [createdDate=");
		builder.append(createdDate);
		builder.append(", createdOn=");
		builder.append(createdOn);
		builder.append(", description=");
		builder.append(description);
		builder.append(", deviceIds=");
		builder.append(deviceIds);
		builder.append(", downloadType=");
		builder.append(downloadType);
		builder.append(", downloadUrl=");
		builder.append(downloadUrl);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", icon=");
		builder.append(icon);
		builder.append(", pushType=");
		builder.append(pushType);
		builder.append(", receivedCount=");
		builder.append(receivedCount);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", title=");
		builder.append(title);
		builder.append(", totalCount=");
		builder.append(totalCount);
		builder.append(", updatedDate=");
		builder.append(updatedDate);
		builder.append(", updatedOn=");
		builder.append(updatedOn);
		builder.append(", pushVersion=");
		builder.append(pushVersion);
		builder.append(", model=");
		builder.append(model);
		builder.append(", _id=");
		builder.append(_id);
		builder.append(", runtimeDeviceId=");
		builder.append(runtimeDeviceId);
		builder.append(", runtimePushVersion=");
		builder.append(runtimePushVersion);
		builder.append(", runtimeModel=");
		builder.append(runtimeModel);
		builder.append(", url=");
		builder.append(url);
		builder.append(", referAppId=");
		builder.append(referAppId);
		builder.append("]");
		return builder.toString();
	}
    
}
