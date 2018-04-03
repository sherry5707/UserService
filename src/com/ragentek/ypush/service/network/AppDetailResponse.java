package com.ragentek.ypush.service.network;

import com.ragentek.ypush.service.util.AppDetailData;

public class AppDetailResponse extends BaseResponse {
	private AppDetailData data;

	public AppDetailData getData() {
		return data;
	}

	public void setData(AppDetailData data) {
		this.data = data;
	}
}
