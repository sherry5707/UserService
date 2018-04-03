package com.ragentek.ypush.service.ui.data;

import java.io.Serializable;

public class ActivityInfo implements Serializable,Comparable{
	public String activityId;//活动的id
	public String activityTitle;//主标题
	public String activityDesc;//副标题，描述
	public String activityDetailUrl;//点击后将要跳转的界面地址
	public String thumbnailUrl;//活动对应图片的url
	public int sortNo;//活动编号,此标识唯一
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public String getActivityTitle() {
		return activityTitle;
	}
	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}
	public String getActivityDesc() {
		return activityDesc;
	}
	public void setActivityDesc(String activityDesc) {
		this.activityDesc = activityDesc;
	}
	public String getActivityDetailUrl() {
		return activityDetailUrl;
	}
	public void setActivityDetailUrl(String activityDetailUrl) {
		this.activityDetailUrl = activityDetailUrl;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public int getSortNo() {
		return sortNo;
	}
	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}
	@Override
	public String toString() {
		return "ActivityInfo [activityId=" + activityId + ", activityTitle="
				+ activityTitle + ", activityDesc=" + activityDesc
				+ ", activityDetailUrl=" + activityDetailUrl
				+ ", thumbnailUrl=" + thumbnailUrl + ", sortNo=" + sortNo + "]";
	}
	@Override
	public int compareTo(Object another) {
		return this.sortNo -((ActivityInfo)another).getSortNo();
	}
	
	
	
}
