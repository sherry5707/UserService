package com.ragentek.ypush.service.ui.data.Response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.ragentek.ypush.service.ui.data.ActivityInfo;
import com.ragentek.ypush.service.ui.data.AdsInfo;

public class ResponseMessage implements Serializable{
	public ArrayList<AdsInfo> Ads;
	public ArrayList<ActivityInfo> Activities;
	public ArrayList<AdsInfo> getAds() {
		return Ads;
	}
	public void setAds(ArrayList<AdsInfo> ads) {
		Ads = ads;
	}
	public ArrayList<ActivityInfo> getActivities() {
		return Activities;
	}
	public void setActivities(ArrayList<ActivityInfo> activities) {
		Activities = activities;
	}
	/**
	 * 冒泡排序，按sortNo从小到大排序
	 */
	public void sort(){
		Collections.sort(Ads);
		Collections.sort(Activities);

	}
	
	@Override
	public String toString() {
		return "ResponseMessage [Ads=" + Ads + ", Activities=" + Activities
				+ "]";
	}
	
}
