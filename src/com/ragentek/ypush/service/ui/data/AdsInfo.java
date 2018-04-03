package com.ragentek.ypush.service.ui.data;

import java.io.Serializable;

/**
 * 广告，轮播位置显示
 * @author zhengguang.yang
 *
 */
public class AdsInfo implements Serializable,Comparable{
	public String adTitle;//广告的标题
	public String adDetailUrl;//详情url，点击图片后跳转的网页
	public String bannerImgUrl;//广告图片的url，在轮播界面显示
	public String adDesc;//描述
	public int sortNo;//广告编号，此标识唯一，不会重复
	public String getAdTitle() {
		return adTitle;
	}
	public void setAdTitle(String adTitle) {
		this.adTitle = adTitle;
	}
	public String getAdDetailUrl() {
		return adDetailUrl;
	}
	public void setAdDetailUrl(String adDetailUrl) {
		this.adDetailUrl = adDetailUrl;
	}
	public String getBannerImgUrl() {
		return bannerImgUrl;
	}
	public void setBannerImgUrl(String bannerImgUrl) {
		this.bannerImgUrl = bannerImgUrl;
	}
	public String getAdDesc() {
		return adDesc;
	}
	public void setAdDesc(String adDesc) {
		this.adDesc = adDesc;
	}
	public int getSortNo() {
		return sortNo;
	}
	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}
	@Override
	public String toString() {
		return "AdsInfo [adTitle=" + adTitle + ", adDetailUrl=" + adDetailUrl
				+ ", bannerImgUrl=" + bannerImgUrl + ", adDesc=" + adDesc
				+ ", sortNo=" + sortNo + "]";
	}
	@Override
	public int compareTo(Object another) {
		return this.sortNo - ((AdsInfo)another).getSortNo();
	}
	
	
}
