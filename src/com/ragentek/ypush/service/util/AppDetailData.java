package com.ragentek.ypush.service.util;

public class AppDetailData extends AppData{
	private String adminComment;
	private int appGroupAmount;
	private long createdOn;
	private String fee;
	private int isRecommend;
	private int onLine;
	private String requiredSystem;
	private long updatedOn;
	private String[] category;
	private String[] screenShotMobile;
	private String[] screenShotPc;
	
	public String getAdminComment() {
		return adminComment;
	}
	public void setAdminComment(String adminComment) {
		this.adminComment = adminComment;
	}
	public int getAppGroupAmount() {
		return appGroupAmount;
	}
	public void setAppGroupAmount(int appGroupAmount) {
		this.appGroupAmount = appGroupAmount;
	}
	public long getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}
	public String getFee() {
		return fee;
	}
	public void setFee(String fee) {
		this.fee = fee;
	}
	public int getIsRecommend() {
		return isRecommend;
	}
	public void setIsRecommend(int isRecommend) {
		this.isRecommend = isRecommend;
	}
	public int getOnLine() {
		return onLine;
	}
	public void setOnLine(int onLine) {
		this.onLine = onLine;
	}
	public String getRequiredSystem() {
		return requiredSystem;
	}
	public void setRequiredSystem(String requiredSystem) {
		this.requiredSystem = requiredSystem;
	}
	public long getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(long updatedOn) {
		this.updatedOn = updatedOn;
	}
	public String[] getCategory() {
		return category;
	}
	public void setCategory(String[] category) {
		this.category = category;
	}
	public String[] getScreenShotMobile() {
		return screenShotMobile;
	}
	public void setScreenShotMobile(String[] screenShotMobile) {
		this.screenShotMobile = screenShotMobile;
	}
	public String[] getScreenShotPc() {
		return screenShotPc;
	}
	public void setScreenShotPc(String[] screenShotPc) {
		this.screenShotPc = screenShotPc;
	}
	
}
