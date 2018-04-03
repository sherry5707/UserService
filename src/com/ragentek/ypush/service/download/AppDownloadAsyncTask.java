package com.ragentek.ypush.service.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.ragentek.ypush.service.util.AppData;
import com.ragentek.ypush.service.ui.DownloadListActivity;
import com.loopj.android.http.RequestParams;
import com.ragentek.ypush.service.R;

public class AppDownloadAsyncTask extends AsyncTask<Map<String, String>, Void, Void> {
	private NotificationManager notificationManager;
	private Context context;
	private AppData myapp;
	private AppDownloadListener listener;
	public boolean isStop = false;
	public boolean isDeleted = false;
	private long beginTime = System.currentTimeMillis();
	private int dlNotifyIdx = 0;
	private long notifyTime;
	private boolean isShowNotification=true;

	public AppData getMyapp() {
		return myapp;
	}

	public void setListener(AppDownloadListener listener) {
		this.listener = listener;
	}

	public AppDownloadAsyncTask(Context context, AppData myapp,AppDownloadListener listener) {
		this.context = context;
		this.myapp = myapp;
		this.listener = listener;
		
		notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public AppDownloadAsyncTask(Context context, AppData myapp, AppDownloadListener listener, boolean isShowNotification) {
		this.context = context;
		this.myapp = myapp;
		this.listener = listener;
		this.isShowNotification = isShowNotification;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	@Override
	protected Void doInBackground(Map<String, String>... arg0) {
		downloadFile();
		return null;
	}

	public void downloadFile() {
		HttpURLConnection conn = null;
		RandomAccessFile out = null;
		InputStream in = null;
		
		int apkLength = AppDownloadUtil.getAppContentLength(myapp.getDownloadUrl());
		File apkFile = new File(myapp.getApkFilePath());
		System.out.println("-----AppDownloadAsyncTask downloadUrl-----"+myapp.getDownloadUrl());
		try {
			URL downloadURL = new URL(myapp.getDownloadUrl());
//			if(myapp.getDownloadUrl().contains("http://m.baidu.com/api?")){
//				String urlTmp = myapp.getDownloadUrl().replace("http://m.baidu.com/api?", "");
//				downloadURL = new URL("http://m.baidu.com/api?"+URLEncoder.encode(urlTmp));
//			}
			System.out.println("-----downloadURL-----"+downloadURL.toString());
			out = new RandomAccessFile(apkFile, "rw");
			long fileLength = 0;
			conn = (HttpURLConnection) downloadURL.openConnection();
			if (apkFile.exists()) {
				fileLength = apkFile.length();

				if (apkLength > 0 && apkLength > fileLength) {
					conn.setAllowUserInteraction(true);
					conn.setRequestProperty("Range", "bytes=" + fileLength + "-");
					out.seek(fileLength);
				} else if (apkLength > 0 && apkLength == fileLength) {
					AppDownloadUtil.downloadTasks.remove(myapp.getDownloadUrl());
					myapp.setDlProgress(100);
					myapp.setDownloadStatus(AppDownloadUtil.STATUS_COMPLETED);
					saveOrUpdate();
					listener.onDownloadSuccess();
					return;
				}
			}
			
			int statucode = conn.getResponseCode();
			if (statucode == HttpURLConnection.HTTP_OK || statucode == HttpURLConnection.HTTP_PARTIAL) {
				System.out.println("-----------save db STATUS_NEW----------");
//				if(myapp.getDownloadStatus()!=AppDownloadUtil.STATUS_NEED_UPDATE){
					myapp.setDownloadStatus(AppDownloadUtil.STATUS_DOWLOADING);
					saveOrUpdate();
//				}
				
				in = conn.getInputStream();
				byte[] buffer = new byte[1024];
				int length;
				long progress = fileLength;
				int send = 0;
				int prg = (int) (((float) progress / apkLength) * 100);
				while (!isStop && (length = in.read(buffer)) != -1) {
					out.write(buffer, 0, length);
					if (apkLength > -1) {
						progress += length;
						prg = (int) (((float) progress / apkLength) * 100);
						if (prg > send) {
							send = prg;
							listener.onDownloading(prg);
							//show notification
							if(myapp.getNotificationId()==-1){
								myapp.setNotificationId(new Random().nextInt());
							}
							if(prg>=100){
								dlNotifyIdx = 100;
							}
						}
						long nowTime = System.currentTimeMillis();
						if(nowTime-notifyTime>500){
							if (isShowNotification)
								showNotification(myapp.getNotificationId(), myapp.getName(), prg);
							notifyTime = nowTime;
						}
					}
				}
			}
			
			if (!isStop) {
				System.out.println("--------save db STATUS_COMPLETED------------");
				myapp.setDownloadStatus(AppDownloadUtil.STATUS_COMPLETED);
				saveOrUpdate();
				
				apkFile.renameTo(new File(apkFile.getAbsolutePath().replace(".tmp", ".apk")));
//				AppDownloadUtil.uploadUserBehavior(myapp, "DOWNLOAD_APP", context);
				listener.onDownloadSuccess();
				
				//appBehaviorUpload(context, myapp, beginTime);
			}else{
				if(isDeleted){
					isDeleted = false;
				}else{
					System.out.println("--------save db STATUS_PAUSED------------");
					myapp.setDownloadStatus(AppDownloadUtil.STATUS_PAUSED);
					saveOrUpdate();
				}
			}
			AppDownloadUtil.downloadTasks.remove(myapp.getDownloadUrl());
//			//cancel notification
			notificationManager.cancel(myapp.getNotificationId());
		} catch (Exception e) {
			e.printStackTrace();
			AppDownloadUtil.downloadTasks.remove(myapp.getDownloadUrl());
			if(myapp.getDownloadStatus()!=AppDownloadUtil.STATUS_NEED_UPDATE){
				myapp.setDownloadStatus(AppDownloadUtil.STATUS_FAILED);
				saveOrUpdate();
			}
			listener.onDownloadFailed();
			//cancel notification
			notificationManager.cancel(myapp.getNotificationId());
		} finally {
			try {

				if (out != null) {
					out.close();
				}

				if (in != null) {
					in.close();
				}

				if (conn != null) {
					conn.disconnect();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	private void saveOrUpdate(){
		if(!myapp.getPackageName().equals(context.getPackageName())){
			AppDownloadUtil.saveOrUpdateMyAppInDb(context, myapp);
		}
	}

	private void showNotification(int notificationId,String gameName,int progress){
		int notifyIcon = getNotificationIcon();
    	Notification notification=new Notification(notifyIcon,gameName,System.currentTimeMillis());
    	PendingIntent pt=PendingIntent.getActivity(context, 0, new Intent(context,DownloadListActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
		notification.contentIntent = pt;
		notification.tickerText = "正在下载\""+gameName+"\"";
		notification.icon = notifyIcon;

		final String packageName = getContext().getPackageName();
		RemoteViews contentView = new RemoteViews(packageName,R.layout.include_app_download_notification_view);
		if(progress>0){
			contentView.setProgressBar(R.id.progressBar, 100, progress, false);
		}
		contentView.setTextViewText(R.id.app_name, "正在下载：“"+gameName+"”");
		notification.contentView = contentView;

    	notificationManager.notify(notificationId, notification);
    }
	
//	private void appBehaviorUpload(Context context,AppData data,long beginTime){
//		AppBehavior req = new AppBehavior();
//		req.setAppId(data.get_id());
//		req.setPackageName(data.getPackageName());
//		req.setCostTime(System.currentTimeMillis()-beginTime);
//		req.setBehaviorType("DOWNLOAD_APP");
//		req.setNetworkType(PhoneInfoUtil.getNetType(context));
//		req.setSourceFlag(data.getSourceFlag());
//		req.setVersionCode(data.getVersionCode());
//		//get data
//		RequestParams params = new RequestParams();
//		params.put("json_p", req.toJsonString());
//		HttpClient.getClient(context).post(Config.HTTP_HEAD+Config.API_APP_BEHAVIOR_UPLOAD_URL,params, null);
//	}
	
	private int getNotificationIcon(){
		if(dlNotifyIdx==0){
			dlNotifyIdx = 1;
			return R.drawable.notification_status_0;
		}else if(dlNotifyIdx==1){
			dlNotifyIdx = 2;
			return R.drawable.notification_status_1;
		}else if(dlNotifyIdx==2){
			dlNotifyIdx = 3;
			return R.drawable.notification_status_2;
		}else if(dlNotifyIdx==3){
			dlNotifyIdx = 0;
			return R.drawable.notification_status_3;
		}
		return R.drawable.notification_status_4;
	}
	
}
