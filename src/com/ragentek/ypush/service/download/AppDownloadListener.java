package com.ragentek.ypush.service.download;

public abstract class AppDownloadListener {
	public abstract void onDownloading(int progress);
	public abstract void onDownloadSuccess();
	public abstract void onDownloadFailed();
}
