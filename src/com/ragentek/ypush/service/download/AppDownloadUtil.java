package com.ragentek.ypush.service.download;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.net.URLDecoder;
import com.ragentek.ypush.service.EventStatisticsData;
import com.ragentek.ypush.service.NewPushService;
import com.ragentek.ypush.service.PushBehaviorData;
import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.db.DBUtil;
import com.ragentek.ypush.service.ui.PushMsgActivity;
import com.ragentek.ypush.service.util.AppData;
import com.ragentek.ypush.service.util.AppDetailData;
import com.ragentek.ypush.service.util.AppInstalledData;
import com.ragentek.ypush.service.util.YPushConfig;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import android.content.pm.PackageManager.NameNotFoundException;//add by zhengguang.yang@20160406 for versioncode compare

public class AppDownloadUtil {

	public static final Map<String, AppDownloadAsyncTask> downloadTasks = new HashMap<String, AppDownloadAsyncTask>();
	public static final String TAG = "AppDownloadUtil";
	
	public static int STATUS_NEW=0;
	public static int STATUS_DOWLOADING = 1;
	public static int STATUS_PAUSED = 2;
	public static int STATUS_COMPLETED = 3;
	public static int STATUS_FAILED = 4;
	public static int STATUS_INSTALLED = 5;
	public static int STATUS_NEED_UPDATE = 6;
	public static int STATUS_NEED_UNINSTALL = 7;
	
	public static void startDownload(Context context, AppData detail, AppDownloadListener listener) {
		if (getDownloadDir().exists()) {
			downloadApp(context, detail, listener);
		}else{
			Toast.makeText(context, R.string.no_sd_card, Toast.LENGTH_LONG).show();
		}
	}
	//add by sherry
	public static void startAutoDownload(Context context, AppData detail, AppDownloadListener listener) {
		if (getDownloadDir().exists()) {
			if (downloadTasks.containsKey(detail.getDownloadUrl())) {
				Log.i(TAG,"all ready start download apk,checkout in downloadTask Manager");
				return;
			}
			detail.setNotificationId(5000 + new Random().nextInt(2000));
			detail.setApkFilePath(getDownloadFilePath(detail));
			detail.setDownloadStatus(STATUS_DOWLOADING);
			AppDownloadAsyncTask task = (AppDownloadAsyncTask) new AppDownloadAsyncTask(context, detail, listener,false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
			downloadTasks.put(detail.getDownloadUrl(), task);
		} else {
			Toast.makeText(context, R.string.no_sd_card, Toast.LENGTH_LONG).show();
		}
	}
	private static void downloadApp(Context context, AppData detail,AppDownloadListener listener) {
		if(downloadTasks.containsKey(detail.getDownloadUrl())){
			Toast.makeText(context, R.string.begin_download_game, Toast.LENGTH_SHORT).show();
			return;
		}
//		Map<String, Object> queryMap = new HashMap<String, Object>();
//		queryMap.put("downloadUrl", detail.getDownloadUrl());
//		List<MyApp> results = (List<MyApp>)DBUtil.queryForFieldValues(context, MyApp.class, queryMap);
//		if(results!=null && results.size()>0){
//			MyApp myApp = (MyApp)results.get(0);
//			myApp.setDownloadStatus(STATUS_DOWLOADING);
//			AppDownloadAsyncTask task = (AppDownloadAsyncTask) new AppDownloadAsyncTask(context, myApp, listener).execute();
//			downloadTasks.put(detail.getDownloadUrl(), task);
//		}else{
//			MyApp myApp = (MyApp) GsonUtil.convertObject(detail, AppData.class, MyApp.class);
			detail.setNotificationId(5000+new Random().nextInt(2000));
			detail.setApkFilePath(getDownloadFilePath(detail));
			detail.setDownloadStatus(STATUS_DOWLOADING);
			AppDownloadAsyncTask task = (AppDownloadAsyncTask) new AppDownloadAsyncTask(context, detail, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
			downloadTasks.put(detail.getDownloadUrl(), task);
//		}
	}
	
	public static void createDownloadListner(final Context context, final Button button, final TextView statusText,final AppData detail,final int type, final boolean startDown) {
		Log.d(TAG, "createDownloadListner enter , status is " + detail.getDownloadStatus());
		Log.d(TAG, "createDownloadListner enter , startDown is " + startDown);
		if(downloadTasks.containsKey(detail.getDownloadUrl())){
			detail.setDownloadStatus(STATUS_DOWLOADING);
			final Handler appDownloadHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					int progress = 0;
					if(msg.obj!=null){
						progress = Integer.parseInt(msg.obj.toString());
						detail.setDlProgress(progress);
					}
					if(msg.what==1){
						statusText.setText(msg.obj+"%");
//						if(progressBar!=null){
//							progressBar.setVisibility(View.VISIBLE);
//							progressBar.setProgress(detail.getDlProgress());
//						}
						if(type==1){
							setButtonDownloadStatus(context, button, progress,detail.getDownloadStatus());
						}else{
							setButtonDownloadStatus2(context, button, progress);
						}
					}else if(msg.what==2){
//						if(progressBar!=null){
//							progressBar.setVisibility(View.GONE);
//						}
						detail.setDownloadStatus(STATUS_COMPLETED);
						setButtonDownloadStr(context,statusText,detail.getDownloadStatus());
						File dlFile = new File(getDownloadFilePath(detail).replace("tmp", "apk"));
						if(dlFile.exists()){
							AppDownloadUtil.installApp(context, dlFile);
						}
						showInstallNotification(context, detail);
					}else if(msg.what==3){
//						if(progressBar!=null){
//							progressBar.setVisibility(View.GONE);
//						}
						detail.setDownloadStatus(STATUS_FAILED);
						setButtonDownloadStr(context,statusText,detail.getDownloadStatus());
					}
				}
			};
			downloadTasks.get(detail.getDownloadUrl()).setListener(new AppDownloadListener(){
				@Override
				public void onDownloading(int progress) {
					// TODO Auto-generated method stub
//					System.out.println("-----dlBtn2-----"+button.getTag().toString());
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						Message msg = new Message();
						msg.what=1;
						msg.obj = progress;
						appDownloadHandler.sendMessage(msg);
					}
				}

				@Override
				public void onDownloadSuccess() {
					// TODO Auto-generated method stub
					System.out.println("----------onDownloadSuccess----------");
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(2);
					}
				}

				@Override
				public void onDownloadFailed() {
					// TODO Auto-generated method stub
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(3);
					}
				}});
		}
		
		final Handler appDownloadHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==1){
					int progress = Integer.parseInt(msg.obj.toString());
					detail.setDlProgress(progress);
					statusText.setText(msg.obj+"%");
//					if(progressBar!=null){
//						progressBar.setVisibility(View.VISIBLE);
//						progressBar.setProgress(detail.getDlProgress());
//					}
					if(type==1){
						setButtonDownloadStatus(context, button, progress,detail.getDownloadStatus());
					}else{
						setButtonDownloadStatus2(context, button, progress);
					}
				}else if(msg.what==2){
//					if(progressBar!=null){
//						progressBar.setVisibility(View.GONE);
//					}
					detail.setDownloadStatus(STATUS_COMPLETED);
					setButtonDownloadStr(context,statusText,detail.getDownloadStatus());
					File dlFile = new File(getDownloadFilePath(detail).replace("tmp", "apk"));
					if(dlFile.exists()){
						AppDownloadUtil.installApp(context, dlFile);
					}
					showInstallNotification(context, detail);
				}else if(msg.what==3){
//					if(progressBar!=null){
//						progressBar.setVisibility(View.GONE);
//					}
					detail.setDownloadStatus(STATUS_FAILED);
					setButtonDownloadStr(context,statusText,detail.getDownloadStatus());
				}
			}
		};
		File dlFile = new File(AppDownloadUtil.getDownloadFilePath(detail).replace("tmp", "apk"));
		if(dlFile.exists()){
			detail.setApkFilePath(getDownloadFilePath(detail));
			detail.setDownloadStatus(STATUS_COMPLETED);
			saveOrUpdateMyAppInDb(context, detail);
			setButtonDownloadStr(context,statusText,detail.getDownloadStatus());
		}
		if(startDown && detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEW){
			detail.setDownloadStatus(STATUS_DOWLOADING);
			//add by zhengguang.yang@20170227 start for push user behavior SDOWNLOAD only one time
			AppDownloadUtil.sendPushBehavior(context, YPushConfig.SDOWNLOAD, detail.getMsgId(),detail.getAppId(), detail.getPackageName());
			//add by zhengguang.yang end
			startDownload(context, detail, new AppDownloadListener(){
				@Override
				public void onDownloading(int progress) {
					// TODO Auto-generated method stub
		//								System.out.println("-----dlBtn1-----"+dlBtn.getTag().toString());
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						Message msg = new Message();
						msg.what=1;
						msg.obj = progress;
						appDownloadHandler.sendMessage(msg);
					}
				}

				@Override
				public void onDownloadSuccess() {
					// TODO Auto-generated method stub
					System.out.println("----------onDownloadSuccess----------");
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(2);
					}
				}

				@Override
				public void onDownloadFailed() {
					// TODO Auto-generated method stub
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(3);
					}
			}});
		}
		
		setButtonDownloadStr(context,statusText, detail.getDownloadStatus());
		if(type==1){
			setButtonDownloadStatus(context, button, detail.getDlProgress(),detail.getDownloadStatus());
		}else{
			setButtonDownloadStatus2(context, button, detail.getDlProgress());
		}
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View dlBtn) {
//				System.out.println("----status----"+detail.getDownloadStatus());
//				if (SharedPreferencesUtil.isDownloadOnlyWifi(context) && !PhoneInfoUtil.getNetType(context).equalsIgnoreCase("wifi")) {
//					Toast.makeText(context, R.string.download_only_wifi_tips, Toast.LENGTH_LONG).show();
//					return;
//				}
				
				if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEW || detail.getDownloadStatus()==AppDownloadUtil.STATUS_PAUSED 
						|| detail.getDownloadStatus()==AppDownloadUtil.STATUS_FAILED || detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEED_UPDATE){
//					System.out.println("------createDownloadListner-------"+detail.getDownloadUrl());
					File dlFile = new File(AppDownloadUtil.getDownloadFilePath(detail).replace("tmp", "apk"));
					if(dlFile.exists()){
//						MyApp myApp = (MyApp) GsonUtil.convertObject(detail, AppDetail.class, MyApp.class);
						detail.setApkFilePath(getDownloadFilePath(detail));
						detail.setDownloadStatus(STATUS_COMPLETED);
						saveOrUpdateMyAppInDb(context, detail);
						setButtonDownloadStr(context,statusText,detail.getDownloadStatus());
						AppDownloadUtil.installApp(context, dlFile);
					}else{
						//add by zhengguang.yang@20170227 start for push user behavior SDOWNLOAD only one time
						if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEW||detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEED_UPDATE){
							AppDownloadUtil.sendPushBehavior(context, YPushConfig.SDOWNLOAD, detail.getMsgId(),detail.getAppId(), detail.getPackageName());
						}
						//add by zhengguang.yang end
						detail.setDownloadStatus(STATUS_DOWLOADING);
						startDownload(context, detail, new AppDownloadListener(){
							@Override
							public void onDownloading(int progress) {
								// TODO Auto-generated method stub
//								System.out.println("-----dlBtn1-----"+dlBtn.getTag().toString());
								if(dlBtn.getTag()!=null && downloadTasks.containsKey(dlBtn.getTag().toString())){
									Message msg = new Message();
									msg.what=1;
									msg.obj = progress;
									appDownloadHandler.sendMessage(msg);
								}
							}

							@Override
							public void onDownloadSuccess() {
								// TODO Auto-generated method stub
								System.out.println("----------onDownloadSuccess----------");
								if(dlBtn.getTag()!=null && downloadTasks.containsKey(dlBtn.getTag().toString())){
									appDownloadHandler.sendEmptyMessage(2);
								}
							}

							@Override
							public void onDownloadFailed() {
								// TODO Auto-generated method stub
								if(dlBtn.getTag()!=null && downloadTasks.containsKey(dlBtn.getTag().toString())){
									appDownloadHandler.sendEmptyMessage(3);
								}
							}});
					}
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_DOWLOADING){
					detail.setDownloadStatus(STATUS_PAUSED);
					AppDownloadAsyncTask task = downloadTasks.get(detail.getDownloadUrl());
					if(task!=null){
						task.isStop = true;
					}
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_COMPLETED){
					installApp(context, new File(getDownloadFilePath(detail).replace("tmp", "apk")));
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_INSTALLED){
//					MyApp myApp = (MyApp) GsonUtil.convertObject(detail, AppDetail.class, MyApp.class);
					launchApp(context, detail);
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEED_UNINSTALL){
					uninstallApp(context, detail.getPackageName());
				}
				setButtonDownloadStr(context,statusText,detail.getDownloadStatus());
				if(type==1){
					setButtonDownloadStatus(context, button, detail.getDlProgress(),detail.getDownloadStatus());
				}else{
					setButtonDownloadStatus2(context, button, detail.getDlProgress());
				}
			}
		});
	}
	//add by zhengguang.yang@20160301 start for text can't show in theme material
	public static void createDownloadListner(final Context context, final Button button, final AppData detail,final int type, final boolean startDown) {
		Log.d(TAG, "createDownloadListner enter , status is " + detail.getDownloadStatus());
		Log.d(TAG, "createDownloadListner enter , startDown is " + startDown);
		if(downloadTasks.containsKey(detail.getDownloadUrl())){
			detail.setDownloadStatus(STATUS_DOWLOADING);
			final Handler appDownloadHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					int progress = 0;
					if(msg.obj!=null){
						progress = Integer.parseInt(msg.obj.toString());
						detail.setDlProgress(progress);
					}
					if(msg.what==1){
						button.setText(msg.obj+"%");
//						if(progressBar!=null){
//							progressBar.setVisibility(View.VISIBLE);
//							progressBar.setProgress(detail.getDlProgress());
//						}
						if(type==1){
							setButtonDownloadStatus(context, button, progress,detail.getDownloadStatus());
						}else{
							setButtonDownloadStatus2(context, button, progress);
						}
					}else if(msg.what==2){
//						if(progressBar!=null){
//							progressBar.setVisibility(View.GONE);
//						}
						detail.setDownloadStatus(STATUS_COMPLETED);
						setButtonDownloadStr(context,button,detail.getDownloadStatus());
						//add by zhengguang.yang@20160919 start for push user behavior
						AppDownloadUtil.sendPushBehavior(context, YPushConfig.FDOWNLOAD, detail.getMsgId(), detail.getAppId(), detail.getPackageName());
						//add by zhengguang.yang end
						File dlFile = new File(getDownloadFilePath(detail).replace("tmp", "apk"));
						if(dlFile.exists()){
							AppDownloadUtil.installApp(context, dlFile);
						}
						showInstallNotification(context, detail);
					}else if(msg.what==3){
//						if(progressBar!=null){
//							progressBar.setVisibility(View.GONE);
//						}
						detail.setDownloadStatus(STATUS_FAILED);
						setButtonDownloadStr(context,button,detail.getDownloadStatus());
					}
				}
			};
			downloadTasks.get(detail.getDownloadUrl()).setListener(new AppDownloadListener(){
				@Override
				public void onDownloading(int progress) {
					// TODO Auto-generated method stub
//					System.out.println("-----dlBtn2-----"+button.getTag().toString());
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						Message msg = new Message();
						msg.what=1;
						msg.obj = progress;
						appDownloadHandler.sendMessage(msg);
					}
				}

				@Override
				public void onDownloadSuccess() {
					// TODO Auto-generated method stub
					System.out.println("----------onDownloadSuccess----------");
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(2);
					}
				}

				@Override
				public void onDownloadFailed() {
					// TODO Auto-generated method stub
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(3);
					}
				}});
		}
		
		final Handler appDownloadHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==1){
					int progress = Integer.parseInt(msg.obj.toString());
					detail.setDlProgress(progress);
					button.setText(msg.obj+"%");
//					if(progressBar!=null){
//						progressBar.setVisibility(View.VISIBLE);
//						progressBar.setProgress(detail.getDlProgress());
//					}
					if(type==1){
						setButtonDownloadStatus(context, button, progress,detail.getDownloadStatus());
					}else{
						setButtonDownloadStatus2(context, button, progress);
					}
				}else if(msg.what==2){
//					if(progressBar!=null){
//						progressBar.setVisibility(View.GONE);
//					}
					detail.setDownloadStatus(STATUS_COMPLETED);
					setButtonDownloadStr(context,button,detail.getDownloadStatus());
					//add by zhengguang.yang@20160919 start for push user behavior
					AppDownloadUtil.sendPushBehavior(context, YPushConfig.FDOWNLOAD, detail.getMsgId(), detail.getAppId(), detail.getPackageName());
					//add by zhengguang.yang end
					File dlFile = new File(getDownloadFilePath(detail).replace("tmp", "apk"));
					if(dlFile.exists()){
						AppDownloadUtil.installApp(context, dlFile);
					}
					showInstallNotification(context, detail);
				}else if(msg.what==3){
//					if(progressBar!=null){
//						progressBar.setVisibility(View.GONE);
//					}
					detail.setDownloadStatus(STATUS_FAILED);
					setButtonDownloadStr(context,button,detail.getDownloadStatus());
				}
			}
		};
		File dlFile = new File(AppDownloadUtil.getDownloadFilePath(detail).replace("tmp", "apk"));
		if(dlFile.exists()){
			//add by zhengguang.yang@20160406 start for versioncode compare
			int localAppVersion = getInstalledAppVersionCode(context,detail.getPackageName());
			int remoteAppVersion = Integer.parseInt(detail.getVersionCode());
			//installedAppVersion == 0 :未安装此应用
			if(localAppVersion == 0 || localAppVersion < remoteAppVersion){
				detail.setApkFilePath(getDownloadFilePath(detail));
				detail.setDownloadStatus(STATUS_COMPLETED);
				saveOrUpdateMyAppInDb(context, detail);
				setButtonDownloadStr(context,button,detail.getDownloadStatus());
			}
			//add by zhengguang.yang@20160406 start for versioncode compare
			
			
		}
		if(startDown && detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEW){
			detail.setDownloadStatus(STATUS_DOWLOADING);
			//add by zhengguang.yang@20170227 start for push user behavior SDOWNLOAD only one time
			AppDownloadUtil.sendPushBehavior(context, YPushConfig.SDOWNLOAD, detail.getMsgId(),detail.getAppId(), detail.getPackageName());
			//add by zhengguang.yang end
			startDownload(context, detail, new AppDownloadListener(){
				@Override
				public void onDownloading(int progress) {
					// TODO Auto-generated method stub
		//								System.out.println("-----dlBtn1-----"+dlBtn.getTag().toString());
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						Message msg = new Message();
						msg.what=1;
						msg.obj = progress;
						appDownloadHandler.sendMessage(msg);
					}
				}

				@Override
				public void onDownloadSuccess() {
					// TODO Auto-generated method stub
					System.out.println("----------onDownloadSuccess----------");
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(2);
					}
				}

				@Override
				public void onDownloadFailed() {
					// TODO Auto-generated method stub
					if(button.getTag()!=null && downloadTasks.containsKey(button.getTag().toString())){
						appDownloadHandler.sendEmptyMessage(3);
					}
			}});
		}
		
		setButtonDownloadStr(context,button, detail.getDownloadStatus());
		if(type==1){
			setButtonDownloadStatus(context, button, detail.getDlProgress(),detail.getDownloadStatus());
		}else{
			setButtonDownloadStatus2(context, button, detail.getDlProgress());
		}
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View dlBtn) {
//				System.out.println("----status----"+detail.getDownloadStatus());
//				if (SharedPreferencesUtil.isDownloadOnlyWifi(context) && !PhoneInfoUtil.getNetType(context).equalsIgnoreCase("wifi")) {
//					Toast.makeText(context, R.string.download_only_wifi_tips, Toast.LENGTH_LONG).show();
//					return;
//				}
				
				if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEW || detail.getDownloadStatus()==AppDownloadUtil.STATUS_PAUSED 
						|| detail.getDownloadStatus()==AppDownloadUtil.STATUS_FAILED || detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEED_UPDATE){
//					System.out.println("------createDownloadListner-------"+detail.getDownloadUrl());
					File dlFile = new File(AppDownloadUtil.getDownloadFilePath(detail).replace("tmp", "apk"));
					if(dlFile.exists()){
//						MyApp myApp = (MyApp) GsonUtil.convertObject(detail, AppDetail.class, MyApp.class);
						detail.setApkFilePath(getDownloadFilePath(detail));
						detail.setDownloadStatus(STATUS_COMPLETED);
						saveOrUpdateMyAppInDb(context, detail);
						setButtonDownloadStr(context,button,detail.getDownloadStatus());
						AppDownloadUtil.installApp(context, dlFile);
					}else{
						//add by zhengguang.yang@20170227 start for push user behavior SDOWNLOAD only one time
						if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEW||detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEED_UPDATE){
							AppDownloadUtil.sendPushBehavior(context, YPushConfig.SDOWNLOAD, detail.getMsgId(),detail.getAppId(), detail.getPackageName());
						}
						//add by zhengguang.yang end
						detail.setDownloadStatus(STATUS_DOWLOADING);
						startDownload(context, detail, new AppDownloadListener(){
							@Override
							public void onDownloading(int progress) {
								// TODO Auto-generated method stub
//								System.out.println("-----dlBtn1-----"+dlBtn.getTag().toString());
								if(dlBtn.getTag()!=null && downloadTasks.containsKey(dlBtn.getTag().toString())){
									Message msg = new Message();
									msg.what=1;
									msg.obj = progress;
									appDownloadHandler.sendMessage(msg);
								}
							}

							@Override
							public void onDownloadSuccess() {
								// TODO Auto-generated method stub
								System.out.println("----------onDownloadSuccess----------");
								if(dlBtn.getTag()!=null && downloadTasks.containsKey(dlBtn.getTag().toString())){
									appDownloadHandler.sendEmptyMessage(2);
								}
							}

							@Override
							public void onDownloadFailed() {
								// TODO Auto-generated method stub
								if(dlBtn.getTag()!=null && downloadTasks.containsKey(dlBtn.getTag().toString())){
									appDownloadHandler.sendEmptyMessage(3);
								}
							}});
					}
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_DOWLOADING){
					detail.setDownloadStatus(STATUS_PAUSED);
					AppDownloadAsyncTask task = downloadTasks.get(detail.getDownloadUrl());
					if(task!=null){
						task.isStop = true;
					}
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_COMPLETED){
					installApp(context, new File(getDownloadFilePath(detail).replace("tmp", "apk")));
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_INSTALLED){
//					MyApp myApp = (MyApp) GsonUtil.convertObject(detail, AppDetail.class, MyApp.class);
					launchApp(context, detail);
				}else if(detail.getDownloadStatus()==AppDownloadUtil.STATUS_NEED_UNINSTALL){
					uninstallApp(context, detail.getPackageName());
				}
				setButtonDownloadStr(context,button,detail.getDownloadStatus());
				if(type==1){
					setButtonDownloadStatus(context, button, detail.getDlProgress(),detail.getDownloadStatus());
				}else{
					setButtonDownloadStatus2(context, button, detail.getDlProgress());
				}
			}
		});
	}
	//add by zhengguang.yang end
	
	public static void setButtonDownloadStatus(Context context,Button button,int progress,int status){
//		System.out.println("-----------setButtonDownloadStatus--------");
		if(progress>0){
			if(progress<=8){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_1);
				}
			}else if(progress<=20){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_1);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_2);
				}
			}else if(progress<=32){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_2);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_3);
				}
			}else if(progress<=44){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_3);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_4);
				}
			}else if(progress<=56){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_4);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_5);
				}
			}else if(progress<=68){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_5);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_6);
				}
			}else if(progress<=80){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_6);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_7);
				}
			}else if(progress<=92){
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_7);
				}else{
					button.setBackgroundResource(R.drawable.continue_btn_selected_8);
				}
			}else{
				if(status==AppDownloadUtil.STATUS_DOWLOADING){
					button.setBackgroundResource(R.drawable.pause_btn_selected_8);
				}else if(status==AppDownloadUtil.STATUS_COMPLETED){
					button.setBackgroundResource(R.drawable.install_btn_selected);
				}else if(status==AppDownloadUtil.STATUS_INSTALLED){
					button.setBackgroundResource(R.drawable.open_btn_selected);
				}else if(status==AppDownloadUtil.STATUS_NEED_UPDATE){
					button.setBackgroundResource(R.drawable.update_btn_selected);
				}else{
					button.setBackgroundResource(R.drawable.download_btn_selected);
				}
			}
		}else{
			if(status==AppDownloadUtil.STATUS_NEED_UNINSTALL){
				button.setBackgroundResource(R.drawable.uninstall_btn_selected);
			}else if(status==AppDownloadUtil.STATUS_COMPLETED){
				button.setBackgroundResource(R.drawable.install_btn_selected);
			}else if(status==AppDownloadUtil.STATUS_INSTALLED){
				button.setBackgroundResource(R.drawable.open_btn_selected);
			}else if(status==AppDownloadUtil.STATUS_NEED_UPDATE){
				button.setBackgroundResource(R.drawable.update_btn_selected);
			}else{
				button.setBackgroundResource(R.drawable.download_btn_selected);
			}
		}
	}
	
	public static void setButtonDownloadStatus2(Context context,Button button,int progress){
//		System.out.println("-------------setButtonDownloadStatus2----------"+progress);
		if(progress>0){
			if(progress<=10){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_1);
			}else if(progress<=19){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_2);
			}else if(progress<=28){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_3);
			}else if(progress<=37){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_4);
			}else if(progress<=46){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_5);
			}else if(progress<=55){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_6);
			}else if(progress<=64){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_7);
			}else if(progress<=73){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_8);
			}else if(progress<=82){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_9);
			}else if(progress<=91){
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_10);
			}else{
				button.setBackgroundResource(R.drawable.detail_download_btn_selected_11);
			}
		}
	}
	
	public static void setButtonDownloadStr(Context context,TextView statusText,int status){
		if(status==AppDownloadUtil.STATUS_NEW){
			statusText.setText(context.getString(R.string.btn_download));
		}else if(status==AppDownloadUtil.STATUS_DOWLOADING){
			statusText.setText(context.getString(R.string.btn_downloading));
		}else if(status==AppDownloadUtil.STATUS_PAUSED){
			statusText.setText(context.getString(R.string.btn_continue));
		}else if(status==AppDownloadUtil.STATUS_COMPLETED){
			statusText.setText(context.getString(R.string.btn_install));
		}else if(status==AppDownloadUtil.STATUS_FAILED){
			statusText.setText(context.getString(R.string.btn_retry));
		}else if(status==AppDownloadUtil.STATUS_INSTALLED){
			statusText.setText(context.getString(R.string.btn_launch));
		}else if(status==AppDownloadUtil.STATUS_NEED_UPDATE){
			statusText.setText(context.getString(R.string.btn_update));
		}else if(status==AppDownloadUtil.STATUS_NEED_UNINSTALL){
			statusText.setText(context.getString(R.string.btn_uninstall));
		}
	}
	
	//add by zhengguang.yang@20160301 start for text can't show in theme material
	public static void setButtonDownloadStr(Context context,Button button,int status){
		if(status==AppDownloadUtil.STATUS_NEW){
			button.setText(context.getString(R.string.btn_download));
		}else if(status==AppDownloadUtil.STATUS_DOWLOADING){
			button.setText(context.getString(R.string.btn_downloading));
		}else if(status==AppDownloadUtil.STATUS_PAUSED){
			button.setText(context.getString(R.string.btn_continue));
		}else if(status==AppDownloadUtil.STATUS_COMPLETED){
			button.setText(context.getString(R.string.btn_install));
		}else if(status==AppDownloadUtil.STATUS_FAILED){
			button.setText(context.getString(R.string.btn_retry));
		}else if(status==AppDownloadUtil.STATUS_INSTALLED){
			button.setText(context.getString(R.string.btn_launch));
		}else if(status==AppDownloadUtil.STATUS_NEED_UPDATE){
			button.setText(context.getString(R.string.btn_update));
		}else if(status==AppDownloadUtil.STATUS_NEED_UNINSTALL){
			button.setText(context.getString(R.string.btn_uninstall));
		}
	}
	//add by zhengguang.yang end
	
	public static void saveOrUpdateMyAppInDb(Context context, AppData myApp) {
		DBUtil.saveOrUpdate(context, myApp, AppData.class);
	}
	
	public static void deleteMyAppInDb(Context context, AppData myApp) {
		DBUtil.delete(context, myApp, AppData.class);
	}
	
	public static String getDownloadFilePath(AppData data) {
		File externalStoragePublicDirectory = new File(YPushConfig.APK_DOWNLOAD_DIR);
		if(!externalStoragePublicDirectory.exists()){
			externalStoragePublicDirectory.mkdirs();
		}
		return externalStoragePublicDirectory.getAbsolutePath()+"/"+data.getName()+".tmp";
	}
	
	public static List<AppData> listDBApps(Context context,int status) {
		List<AppData> list = (List<AppData>)DBUtil.queryForAll(context, AppData.class);
		if(status<0){
			return list;
		}else{
			List<AppData> downloadingList = new ArrayList<AppData>();
			if (list != null) {
				for (AppData app : list) {
					if (app.getDownloadStatus()==status) {
						downloadingList.add(app);
					}
				}
			}
			return downloadingList;
		}
	}
	
//	public static List<MyApp> listMyInstalledApps(Context context) {
//		List<MyApp>list = (List<MyApp>) DBUtil.queryForAll(context, MyApp.class);
//		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
//		List<MyApp> downloadingList = new ArrayList<MyApp>();
//		if (list != null) {
//			for (MyApp app : list) {
//				for (PackageInfo info : packages) {
//					 ApplicationInfo itemInfo = info.applicationInfo; 
//		             if((itemInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0){ 
//		                 continue; 
//		      		 }
//					 if(info.packageName.equals(app.getPackageName())) {
////						 app.setDownloadStatus(STATUS_INSTALLED);
//						 downloadingList.add(app);
//						 break;
//					 }
//				}
//			}
//		}
//		return downloadingList;
//	}
	
	public static ArrayList<AppInstalledData> getInstalledApps(Context context,boolean getSysPackages) {   
	      ArrayList<AppInstalledData> res = new ArrayList<AppInstalledData>();           
	      List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);   
	      for(int i=0;i<packs.size();i++) {   
	             PackageInfo p = packs.get(i);   
	             if ((!getSysPackages) && (p.versionName == null)) {   
	                    continue ;   
	             }     
	             ApplicationInfo itemInfo = p.applicationInfo; 
	             if((itemInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0){ 
	                 continue; 
	      		 }
//	             String appName = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
//	             if(appName.equals(context.getString(R.string.app_name))){
//	            	 continue;
//	             }
	             AppInstalledData app = new AppInstalledData();  
	             app.setName(p.applicationInfo.loadLabel(context.getPackageManager()).toString());
	             app.setPackageName(p.packageName);
	             app.setVersionCode(""+p.versionCode);
	             app.setVersionName(p.versionName);
	             app.setDownloadStatus(STATUS_NEED_UNINSTALL);
	             app.setAppIcon(p.applicationInfo.loadIcon(context.getPackageManager()));
	             res.add(app);   
	      }   
	      return res;    
	}
	
//	public static boolean checkUpdateMyApps(Context context) {
//		List<String> packageNames = getInstalledPackages(context);
//		Request req = new PackageCheckRequest(packageNames);
//		try {
//			RequestParams params = new RequestParams();
//			params.put("json_p", req.toString());
//			String result = HttpUtil.doStringHttpPost(context, new Gson().toJson(r), Config.API_APP_VERSION_CHECK_URL);
//			MyAppList list = new Gson().fromJson(result, MyAppList.class);
//			if (list != null && list.getData() != null && !list.getData().isEmpty()) {
//				List<AppData> installApps = getInstalledApps(context, true);
//				for (AppData app:list.getData()) {
//					for(AppData app2:installApps){
//						if (app.getPackageName().equals(app2.getPackageName())) {
//							if(Integer.parseInt(app.getVersionCode())>Integer.parseInt(app2.getVersionCode())){
//								app.setDownloadStatus(STATUS_NEED_UPDATE);
//								app.setNeedUpdated(true);
//								saveOrUpdateMyAppInDb(context, app);
//								break;
//							}
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
	
	public static List<AppData> listMyNeedUpdateApps(Context context) {
		List<AppData> results = new ArrayList<AppData>();
		List<AppData> list = (List<AppData>)DBUtil.queryForAll(context, AppData.class);
		for(AppData myapp:list){
			if(myapp.isNeedUpdated()){
				results.add(myapp);
			}
		}
//		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
//		Map<String, Integer> installedVersionCodeMap = new HashMap<String, Integer>();
//		for (PackageInfo info : packages) {
//			installedVersionCodeMap.put(info.packageName, info.versionCode);
//		}
//		List<MyApp> updateList = new ArrayList<MyApp>();
//		if (list != null) {
//			for (MyApp app : list) {
//				if (installedVersionCodeMap.get(app.getPackageName()) != null && app.getVersionCode()!=null) {
//					if (Integer.valueOf(app.getVersionCode()) > installedVersionCodeMap.get(app.getPackageName())) {
//						if (!downloadTasks.containsKey(app.getDownloadUrl())) {
//							updateList.add(app);
//						}
//					}					
//				}
//			}
//		}
		return results;
	}
	
	public static List<String> getInstalledPackages(Context context) {
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		List<String> packageNames = new ArrayList<String>();
		for (PackageInfo info : packages) {
			packageNames.add(info.packageName);
		}
		return packageNames;
	}
	//add by zhengguang.yang@20160406 start for versioncode compare
	public static int getInstalledAppVersionCode(Context context, String packageName)
	{
	    int versionCode = 0;
	    try
	    {
	        // 获取软件版本号，
	        versionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
	    } catch (NameNotFoundException e)
	    {
	        e.printStackTrace();
	    }
	    return versionCode;
	}
	//add by zhengguang.yang@20160406 end for versioncode compare

	
	
	public static List<AppData> listMyInstalledApps(Context context) {
		List<AppData> list = (List<AppData>) DBUtil.queryForAll(context, AppData.class);
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		List<AppData> downloadingList = new ArrayList<AppData>();
		if (list != null) {
			for (AppData app : list) {
				for (PackageInfo info : packages) {
					if (info.packageName.equals(app.getPackageName())) {
						downloadingList.add(app);
						break;
					}
				}
			}
		}
		return downloadingList;
	}
	
	public static boolean isFoundNewClient(PackageInfo info, AppData client) {
		return client != null &&  client.getVersionCode() != null && Integer.valueOf(client.getVersionCode()) > info.versionCode;
	}
	
//	private static int getApkDownloadProgress(AppData app) {
//		int apkLength = getAppContentLength(app.getDownloadUrl());
//		String fileName = getDownloadFilePath(app.getDownloadUrl());
//		File apkFile = new File(fileName);
//		int progress = 0;
//		if (apkFile.exists() && apkLength > 0) {
//			progress = (int) (((float) apkFile.length() / apkLength) * 100);
//		}
//		return progress;
//	}
	
	public static int getAppContentLength(String url) {
		int apkLength = -1;
		HttpURLConnection checkConn = null;
		try {
			URL downloadURL = new URL(url);
			checkConn = (HttpURLConnection) downloadURL.openConnection();
			apkLength = checkConn.getContentLength();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (checkConn != null) {
				try {
					checkConn.disconnect();
				} catch (Exception e) {
					// do nothing
				}
			}
		}
		return apkLength;
	}
	
	public static void completeInstalledApp(Context context, String packageName) {
		System.out.println("--------completeInstalledApp---------");
		updateMyAppInDb(context, packageName, STATUS_INSTALLED);
	}

//	public static void removeApp(Context context, String packageName) {
//		ArrayList<AppInstalledData> installedList = AppDownloadUtil.getInstalledApps(context, true);
//		boolean isFound = false;
//		for(AppData installedApp:installedList){
//			if(installedApp.getPackageName().equals(packageName)){
//				isFound = true;
//				break;
//			}
//		}
//		if(!isFound){
//			Map<String, Object> query = new HashMap<String, Object>();
//			query.put("packageName", packageName);
//			List<AppData>results = (List<AppData>)DBUtil.queryForFieldValues(context, AppData.class, query);
////			System.out.println("-----removeMyAppInDb---"+results.size());
//			if(results!=null && results.size()>0){
//				AppData app = results.get(0);
//				DBUtil.delete(context, app, AppData.class);
//			}
//		}else{
//			updateMyAppInDb(context, packageName, STATUS_INSTALLED);
//		}
//	}
	
	public static void updateMyAppInDb(Context context, String packageName, int status) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("packageName", packageName);
		List<AppData>results = (List<AppData>)DBUtil.queryForFieldValues(context, AppData.class, query);
//		System.out.println("-----updateMyAppInDb---"+results.size());
		if(results!=null && results.size()>0){
			AppData app = results.get(0);
			app.setDownloadStatus(status);
			app.setNeedUpdated(false);
			DBUtil.saveOrUpdate(context, app, AppData.class);
			//add by zhengguang.yang@20160919 start for push user behavior
			int localAppVersion = getInstalledAppVersionCode(context,app.getPackageName());
			int remoteAppVersion = Integer.parseInt(app.getVersionCode());
			if(localAppVersion == remoteAppVersion){
				sendPushBehavior(context, YPushConfig.INSTALLEDV, app.getMsgId(), app.getAppId(), app.getPackageName());
			}else{
				sendPushBehavior(context, YPushConfig.INSTALLEDNV,app.getMsgId(), app.getAppId(), app.getPackageName());
			}
			//add by zhengguang.yang end
		}
	}
	
	public static void deleteMyAppInDb(Context context, String downloadUrl) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("downloadUrl", downloadUrl);
		List<AppData>results = (List<AppData>)DBUtil.queryForFieldValues(context, AppData.class, query);
		System.out.println("-----deleteMyAppInDb---"+results.size());
		if(results!=null && results.size()>0){
			AppData app = results.get(0);
			DBUtil.delete(context, app, AppData.class);
		}
	}
	
	public static void deletePackageNameInDb(Context context, String packageName) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("packageName", packageName);
		List<AppData>results = (List<AppData>)DBUtil.queryForFieldValues(context, AppData.class, query);
		System.out.println("-----deletePackageNameInDb---"+results.size());
		if(results!=null && results.size()>0){
			AppData app = results.get(0);
			DBUtil.delete(context, app, AppData.class);
			
			if(!TextUtils.isEmpty(app.getApkFilePath())){
				File dlFile = new File(app.getApkFilePath().replace("tmp", "apk"));
				if(dlFile.exists()){
					dlFile.delete();
				}
			}
		}
	}
	
	public static File getDownloadDir() {
		File externalStoragePublicDirectory = new File(YPushConfig.APK_DOWNLOAD_DIR);
		externalStoragePublicDirectory.mkdirs();
		return externalStoragePublicDirectory;
	}

	/**
	 * 安装APK的代码
	 * 
	 * @param context
	 * @param apkFile
	 * pakFile APK文件存储路径
	 */
	public static void installApp(Context context, File apkFile) {
		Intent installintent = new Intent(Intent.ACTION_VIEW);
		installintent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		installintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PackageManager manager = context.getPackageManager();
		List<ResolveInfo> list = manager.queryIntentActivities(installintent, Intent.FLAG_ACTIVITY_NEW_TASK);
		Log.i(TAG, "list = null or empty ? "+ ((list==null)||(list.isEmpty())));
		if (list == null || list.isEmpty()) {
			Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
			intent.setData(Uri.fromFile(apkFile));
			context.startActivity(intent);
		} else {
			context.startActivity(installintent);
		}
	}
	
	/**
	 * 
	 * 根据APK package name按启动应用程序
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void launchApp(Context context, AppData myapp) {
		try {
			PackageManager pm = context.getPackageManager();
			Intent localIntent = pm.getLaunchIntentForPackage(myapp.getPackageName());
			context.startActivity(localIntent);
//			uploadUserBehavior(myapp, "CGAME_APPLAUNCH", context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 根据APK package name卸载
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void uninstallApp(Context context, String packageName) {
		try {
			Uri packageURI = Uri.parse("package:" + packageName);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(uninstallIntent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showInstallNotification(Context context, AppData appData) {
		File apkFile = new File(appData.getApkFilePath().replace(".tmp", ".apk"));
		if (!apkFile.exists()) {
			return;
		}
		if (TextUtils.isEmpty(appData.getPackageName())) {
			return;
		}
		if (appData.getNotificationId() != appData.getPackageName().hashCode()) {
			appData.setNotificationId(appData.getPackageName().hashCode());
		}
		Intent installintent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
		installintent.setData(Uri.fromFile(apkFile));
		installintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(context, appData.getNotificationId(), installintent, PendingIntent.FLAG_ONE_SHOT);
		Log.i(TAG, "showInstallNotification");

		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentIntent(contentIntent)// 设置intent
				.setSmallIcon(R.drawable.go_push_icon)// 设置状态栏里面的图标（小图标）24pixel
				.setTicker(appData.getName() + appData.getVersionName() + context.getString(R.string.push_msg_download_file_complete_title)) // 设置状态栏的显示的信息
				.setWhen(System.currentTimeMillis())// 设置时间发生时间
				.setAutoCancel(true)// 设置可以清除
				.setContentTitle(appData.getName() + " " + appData.getVersionName())// 设置下拉列表里的标题
				.setContentText(context.getString(R.string.push_msg_download_file_complete_title)+" , "+context.getString(R.string.push_msg_download_file_complete_summary));// 设置上下文内容
		Notification notification = builder.getNotification();// 获取一个Notification
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(appData.getNotificationId(), notification);
	}
	
	public static class AppBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = intent.getDataString();
			String action = intent.getAction();
			if (!TextUtils.isEmpty(packageName)) {
				packageName = packageName.replace("package:", "");
				Log.i(TAG + ".AppBroadcastReceiver", packageName + packageName.hashCode());
				((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(packageName.hashCode());
				//add by zhengguang.yang@20160919 start for push user behavior
				if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					Log.i(TAG, "AppBroadcastReceiver ACTION_PACKAGE_ADDED packageName="+packageName);
					completeInstalledApp(context, packageName);
				}else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
					Log.i(TAG, "AppBroadcastReceiver ACTION_PACKAGE_REMOVED packageName="+packageName);
					deletePackageNameInDb(context, packageName);
				}
				//add by zhengguang.yang end
			
			}
		}

	}
	
	//add by zhengguang.yang@20160919 start for push user behaviors
    public static void sendPushBehavior(Context context,String behavior,String msgId,String appId,String packageName){
    	Log.i(TAG,"sendPushBehavior-->behavior="+behavior+",msgId="+msgId+",appId="+appId+",packageName="+packageName);
    	Intent intent = new Intent(context,NewPushService.class);
    	intent.putExtra("from", "push_behavior");
    	Bundle bundle = new Bundle();
    	PushBehaviorData pushBehaviorData = new PushBehaviorData();
    	pushBehaviorData.setMsgId(msgId);
    	pushBehaviorData.setBehavior(behavior);
    	if(TextUtils.isEmpty(appId)){
    		pushBehaviorData.setAppId("");
    	}else{
    		pushBehaviorData.setAppId(appId);
    	}
    	if(TextUtils.isEmpty(packageName)){
    		pushBehaviorData.setPackageName("");
    	}else{
    		pushBehaviorData.setPackageName(packageName);
    	}
    	bundle.putSerializable("PushBehaviorData", pushBehaviorData);
    	intent.putExtras(bundle);
    	
    	context.startService(intent);
    }
	//add by zhengguang.yang end

	//add by sherry for auto install
	/**
	 * 静默安装
	 * @param context
	 * @File apkFile 文件
	 * @return 0表示安装成功，1表示文件不存在，2表示其他错误。
	 */
	public static int autoInstallApp(Context context, File apkFile) {
		Log.i(TAG, "autoInstallApp apkFile:" + apkFile);
		if (apkFile == null || apkFile.getPath() == null || apkFile.getPath().length() == 0 || apkFile.length() <= 0
				|| !apkFile.exists() || !apkFile.isFile()) {
			return 1;
		}
//		return 0;
		String[] args = {"pm", "install", "-r", "-i", "com.ragentek.ypush.service", "--user", "0", apkFile.getPath()};
		ProcessBuilder processBuilder = new ProcessBuilder(args);

		Process process = null;
		BufferedReader successResult = null;
		BufferedReader errorResult = null;
		StringBuilder successMsg = new StringBuilder();
		StringBuilder errorMsg = new StringBuilder();
		int result;
		try {
			process = processBuilder.start();
			successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
			errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String s;

			while ((s = successResult.readLine()) != null) {
				successMsg.append(s);
			}

			while ((s = errorResult.readLine()) != null) {
				errorMsg.append(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG, "IOException e" + e.getMessage());
			result = 2;
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "IOException e" + e.getMessage());
			result = 2;
		} finally {
			try {
				if (successResult != null) {
					successResult.close();
				}
				if (errorResult != null) {
					errorResult.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}

		// TODO should add memory is not enough here
		if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
			result = 0;
		} else {
			result = 2;
		}
		Log.i(TAG, "installSlient-->successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
		return result;
	}

	//add by sherry for auto download and install
	public static void createDownloadListner(final Context context, final AppData detail) {
		Log.i(TAG, "createDownloadListner");
		final Handler appDownloadHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					//int progress = Integer.parseInt(msg.obj.toString());
				} else if (msg.what == 2) {
					detail.setDownloadStatus(STATUS_COMPLETED);
					AppDownloadUtil.sendPushBehavior(context, YPushConfig.FDOWNLOAD, detail.getMsgId(), detail.getAppId(), detail.getPackageName());
					File dlFile = new File(getDownloadFilePath(detail).replace("tmp", "apk"));
					if (dlFile.exists()) {
						int flagInstall = AppDownloadUtil.autoInstallApp(context, dlFile);
						if (flagInstall == 0) {                                    //  0
							Log.i(TAG, "installed success");
						} else if (flagInstall == 1) {                            // 1
							Log.i(TAG, "file not found " + dlFile.getName());
						} else {                                                 // 2
							Log.i(TAG, "install failure " + dlFile.getName());
						}
					}
				} else if (msg.what == 3) {
					detail.setDownloadStatus(STATUS_FAILED);
				}
			}
		};
		int localAppVersion = getInstalledAppVersionCode(context, detail.getPackageName());
		int remoteAppVersion = Integer.parseInt(detail.getVersionCode());
		//installedAppVersion == 0 :未安装此应用
		if (localAppVersion != 0 && (localAppVersion > remoteAppVersion || localAppVersion == remoteAppVersion)) {
			Log.i(TAG, "the apk version is the newnest");
			return;
		}
		if (detail.getDownloadStatus() == AppDownloadUtil.STATUS_NEW || detail.getDownloadStatus() == AppDownloadUtil.STATUS_PAUSED
				|| detail.getDownloadStatus() == AppDownloadUtil.STATUS_FAILED || detail.getDownloadStatus() == AppDownloadUtil.STATUS_NEED_UPDATE) {
			System.out.println("------createDownloadListner-------" + detail.getDownloadUrl());
			File dlFile = new File(AppDownloadUtil.getDownloadFilePath(detail).replace("tmp", "apk"));
			if (dlFile.exists()) {
				Log.i(TAG, "file exists install");
				detail.setApkFilePath(getDownloadFilePath(detail));
				detail.setDownloadStatus(STATUS_COMPLETED);
				saveOrUpdateMyAppInDb(context, detail);
				AppDownloadUtil.autoInstallApp(context, dlFile);
			} else {
				if (detail.getDownloadStatus() == AppDownloadUtil.STATUS_NEW || detail.getDownloadStatus() == AppDownloadUtil.STATUS_NEED_UPDATE) {
					AppDownloadUtil.sendPushBehavior(context, YPushConfig.SDOWNLOAD, detail.getMsgId(), detail.getAppId(), detail.getPackageName());
				}
				detail.setDownloadStatus(STATUS_DOWLOADING);
				startAutoDownload(context, detail, new AppDownloadListener() {
					@Override
					public void onDownloading(int progress) {
						// TODO Auto-generated method stub
//								System.out.println("-----dlBtn1-----"+dlBtn.getTag().toString());
						if (detail.getDownloadUrl() != null && downloadTasks.containsKey(detail.getDownloadUrl())) {
							Message msg = new Message();
							msg.what = 1;
							//msg.obj = progress;
							appDownloadHandler.sendMessage(msg);
						}
					}

					@Override
					public void onDownloadSuccess() {
						// TODO Auto-generated method stub
						System.out.println("----------onDownloadSuccess----------");
						if (detail.getDownloadUrl() != null && downloadTasks.containsKey(detail.getDownloadUrl())) {
							appDownloadHandler.sendEmptyMessage(2);
						}
					}

					@Override
					public void onDownloadFailed() {
						// TODO Auto-generated method stub
						if (detail.getDownloadUrl() != null && downloadTasks.containsKey(detail.getDownloadUrl())) {
							appDownloadHandler.sendEmptyMessage(3);
						}
					}
				});
			}
		} else if (detail.getDownloadStatus() == AppDownloadUtil.STATUS_DOWLOADING) {
			detail.setDownloadStatus(STATUS_PAUSED);
			AppDownloadAsyncTask task = downloadTasks.get(detail.getDownloadUrl());
			if (task != null) {
				task.isStop = true;
			}
		} else if (detail.getDownloadStatus() == AppDownloadUtil.STATUS_COMPLETED) {
			autoInstallApp(context, new File(getDownloadFilePath(detail).replace("tmp", "apk")));
		} else if (detail.getDownloadStatus() == AppDownloadUtil.STATUS_INSTALLED) {
//					MyApp myApp = (MyApp) GsonUtil.convertObject(detail, AppDetail.class, MyApp.class);
			launchApp(context, detail);
		} else if (detail.getDownloadStatus() == AppDownloadUtil.STATUS_NEED_UNINSTALL) {
			uninstallApp(context, detail.getPackageName());
		}
	}

	//test for silent install
	public static class PullBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "receiver pull broadcast");
			Intent intent4 = new Intent(context, NewPushService.class);
			intent4.putExtra("from", "alarm_pull");
			context.startService(intent4);
		}
	}

    //add by sherrry start for push user EventStatistics
    public static class EventStatisticsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.e("AppDownloadUtil",intent.getAction());
            Intent intent2 = new Intent(context, NewPushService.class);
            Bundle bundle = new Bundle();
            String appData = intent.getStringExtra("appData");
            if (!TextUtils.isEmpty(appData)) {
				EventStatisticsData eventStatisticsData = new EventStatisticsData(appData);
				Log.e("appData",eventStatisticsData.toString());
/*                SetStatisticsData(appData, eventStatisticsData);*/
                bundle.putSerializable("EventStatisticsData", eventStatisticsData);
                intent2.putExtra("from", "event_statistics");
                intent2.putExtras(bundle);
                context.startService(intent2);
            }
        }
    }
}
