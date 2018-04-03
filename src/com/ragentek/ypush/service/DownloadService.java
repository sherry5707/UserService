package com.ragentek.ypush.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ragentek.ypush.service.util.YPushConfig;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;



public class DownloadService extends Service {
	private static final String TAG = "YPushService.DownloadService";

	DownloadThread mdownloadThread;
	
	private final static int UPDATECLIENT_COMPLETE = 2;

	private final static int UPDATECLIENT_FAIL = 3;
	
	private final static int DOWNLOAD_COMPLETE = 4;

	private final static int DOWNLOAD_FAIL = 5;
	
	private File mUpdateClientFile = null;
	
	//is update flag
	private boolean mIsUpdateClient = false;
	
	//updateClient version info
	private String mUpdateClientVersion = "0";
	
	//download url and file name
	private String mDownloadUrl = null;
	private String mDownloadFileName = null;

	
	@Override
	public IBinder onBind(Intent arg0) {		
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate enter");

	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart intent=" + intent);
		
		if (intent != null) {
			Bundle bundle = new Bundle();
	        bundle = intent.getExtras();
	        mDownloadUrl = bundle.getString("url");
	        mDownloadFileName = bundle.getString("fileName");
	        mUpdateClientVersion = bundle.getString("version", "0");
	        mIsUpdateClient = bundle.getBoolean("isUpdateClient", false);
	        Log.d(TAG, "mDownloadUrl=" + mDownloadUrl + " ,mDownloadFileName=" + mDownloadFileName
	        		+ " ,mUpdateClientVersion=" + mUpdateClientVersion + " ,mIsUpdateClient=" + mIsUpdateClient);
			
			mdownloadThread = new DownloadThread();
			mdownloadThread.start();
		}	
		
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(TAG, "onDestroy");
	}
	
	Handler	mHandler = new Handler() {
        @SuppressWarnings("deprecation")
		@Override
        public void handleMessage(Message msg) {
            // process incoming messages here
            switch (msg.what) {
                case 0:
                	//tView.append((String) msg.obj);
                	Log.v(TAG, "handleMessage do some thing"+(String) msg.obj);		
                	break;

                case UPDATECLIENT_COMPLETE:
                	Log.v(TAG, "UPDATECLIENT_COMPLETE");
                	
                	//zhaoshh,for if external data is null
                	if ((mUpdateClientVersion == null) || !mUpdateClientFile.exists()) {
                		Log.v(TAG, "external data is null");
                		
                		break;
                	}
                	
                	try {
						int verCode = getVerCode(getBaseContext());
						String verName = getVerName(getBaseContext());
						Log.d(TAG, "verCode=" + verCode + " ,verName=" + verName);	
						
						StringBuffer buffer= new StringBuffer();
						String valueVer = "";
						String[] ver = verName.split("\\.");
						Log.v(TAG, "ver.length:" + ver.length);
						for (int i=0; i<ver.length; i++) {
							buffer.append(ver[i]);
							Log.v(TAG, "str buffer:" + buffer.toString());
						}
						valueVer = buffer.toString();
						
						String[] currentVer = new String[10];
						StringBuffer currentBuffer= new StringBuffer();
						String currentValueVer = "";
						currentVer = mUpdateClientVersion.split("\\.");
						Log.v(TAG, "currentVer.length:" + currentVer.length);
						for (int i=0; i<currentVer.length; i++) {
							currentBuffer.append(currentVer[i]);
							Log.v(TAG, "str currentBuffer:" + currentBuffer.toString());
						}
						currentValueVer = currentBuffer.toString();
	
						int oldver = Integer.parseInt(valueVer);
						int currentver = Integer.parseInt(currentValueVer);
						Log.d(TAG, "oldver=" + oldver + " ,currentver=" + currentver);
						
						//if (verName.equals(updateClientVersionName)) {
						if (oldver <= currentver) {
							boolean USE_ANDROID_INSTALL = true;
							if (USE_ANDROID_INSTALL) {
								//zhaoshh0313,for install-self start		
			            		Intent installIntent = new Intent();
			            		installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			            		installIntent.setAction(Intent.ACTION_VIEW);
			            		//installIntent.setDataAndType(Uri.fromFile(appFile), "application/vnd.android.package-archive");
			            		installIntent.setDataAndType(Uri.fromFile(mUpdateClientFile), "application/vnd.android.package-archive");
			            		Log.d(TAG, "installIntent=" + installIntent);
			            		getBaseContext().startActivity(installIntent);
			            		//zhaoshh0313,for install-self end
							} else {
								
							}
							
						}
		            } catch(Exception ex){
		            	Log.d(TAG, "install have exception");
		            	
		                ex.printStackTrace();		                
		            }
            		
                	break;
                case UPDATECLIENT_FAIL:
                	Log.v(TAG, "UPDATECLIENT_FAIL");	

                	break;
                case DOWNLOAD_COMPLETE:
                	Log.v(TAG, "DOWNLOAD_COMPLETE");
                	
                	{
	                	//open file manager path Intent
	                	Intent filePathIntent = new Intent();
	                	filePathIntent.setClassName("com.mediatek.filemanager", "com.mediatek.filemanager.FileManagerOperationActivity");
	                	File tempdownloadDir = new File(Environment.getExternalStorageDirectory(), YPushConfig.downloadDir);
	                	filePathIntent.putExtra("select_path", tempdownloadDir.getPath());
	                	
	                	//zhaoshh0322
	                	//notification bar prompt user download complete fun
	                	NotificationManager promptNotificationManager = null;
	
						Notification promptNotification = null;
						promptNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
						
						//modified by yangzg 2015.11.27 start. for use new notification method
//						promptNotification = new Notification();
						PendingIntent contentPendingIntent = PendingIntent.getActivity(DownloadService.this, 0, filePathIntent, 0);
	                    promptNotification = new Notification.Builder(DownloadService.this)
													.setContentTitle(getString(R.string.push_msg_download_file_complete_title))
													.setContentText(getString(R.string.push_msg_download_file_complete_summary))
													.setContentIntent(contentPendingIntent)
													.build();
						promptNotification.icon = R.drawable.ic_launcher;
						promptNotification.tickerText = getString(R.string.push_msg_download_file_complete_ticker);
						
						promptNotification.flags = Notification.FLAG_AUTO_CANCEL; //zhaoshh for after click auto cancel
	                    
	                    promptNotification.defaults = Notification.DEFAULT_SOUND;
//	                    promptNotification.setLatestEventInfo(DownloadService.this, getString(R.string.push_msg_download_file_complete_title), 
//		                		getString(R.string.push_msg_download_file_complete_summary), contentPendingIntent);
	                    //modified by yangzg 2015.11.27 end. for use new notification method
		                Log.v(TAG, "download complete: promptNotification=" + promptNotification);
		                
		                promptNotificationManager.notify(0, promptNotification);
                	}

                	break;
                case DOWNLOAD_FAIL:
                	Log.v(TAG, "DOWNLOAD_FAIL");
                	
                	{
	                	//zhaoshh0322
	                	//notification bar prompt user download fail fun
	                	NotificationManager promptNotificationManager = null;
	
						Notification promptNotification = null;
						promptNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
						//modified by yangzg 2015.11.27 start. for use new notification method
//						promptNotification = new Notification();
						promptNotification = new Notification.Builder(DownloadService.this)
												.setContentTitle(getString(R.string.push_msg_download_file_fail_title))
												.setContentText(getString(R.string.push_msg_download_file_fail_summary))
												.setContentIntent(null)
												.build();
						promptNotification.icon = R.drawable.ic_launcher;
						promptNotification.tickerText = getString(R.string.push_msg_download_file_fail_ticker);
						promptNotification.flags = Notification.FLAG_AUTO_CANCEL; //zhaoshh for after click auto cancel
	                    //PendingIntent contentPendingIntent = PendingIntent.getActivity(DownloadService.this, 0, null, 0);
	                    
	                    promptNotification.defaults = Notification.DEFAULT_SOUND;
//	                    promptNotification.setLatestEventInfo(DownloadService.this, getString(R.string.push_msg_download_file_fail_title), 
//		                		getString(R.string.push_msg_download_file_fail_summary), null);
	                    //modified by yangzg 2015.11.27 end. for use new notification method
		                Log.v(TAG, "download complete: promptNotification=" + promptNotification);
		                
		                promptNotificationManager.notify(0, promptNotification);
                	}

                	break;
            }
            super.handleMessage(msg);
        }
    };

	
	class DownloadThread extends Thread {
    	
    	public DownloadThread() {  
            
        }

        public void run() {
        	Message message = mHandler.obtainMessage();

            if (mIsUpdateClient) {
            	File updateClientDir = null;
            	
            	mIsUpdateClient = false;
            	
            	//zhaoshh,for if external data is null
            	if ((mDownloadFileName == null) || mDownloadUrl == null) {
                	Log.d(TAG, "updateClient Url is null,can not download");
                	message.what = UPDATECLIENT_FAIL;
	                mHandler.sendMessage(message);
	                
	                return ;
                }
            	
            	updateClientDir = new File(Environment.getExternalStorageDirectory(), YPushConfig.updateClientDir);
            	mUpdateClientFile = new File(updateClientDir.getPath(), mDownloadFileName);
            	Log.d(TAG, "update client start: mDownloadUrl=" + mDownloadUrl + " ,mDownloadFileName=" + mDownloadFileName);
            	
            	
	            try {
	                if (!updateClientDir.exists()){
	                	Log.d(TAG, "updateClientDir is not exist");	
	                    updateClientDir.mkdirs();
	                }
	
	                if (!mUpdateClientFile.exists()){
	                	Log.d(TAG, "updateClientFile is not exist");	
	                	mUpdateClientFile.createNewFile();
	                }
	
	                long downloadSize = downloadFile(mDownloadUrl, mUpdateClientFile);
	                if (downloadSize > 0){
	                	Log.d(TAG, "downloadSize <=0 ,download complete");
	                	message.what = UPDATECLIENT_COMPLETE;
	                	mHandler.sendMessage(message);
	                }
	
	            } catch(Exception ex){
	                ex.printStackTrace();
	                message.what = UPDATECLIENT_FAIL;
	                Log.d(TAG, "Exception download fail");
	                mHandler.sendMessage(message);
	            }
	            
            } else {
            	File downloadDir = null;
            	File downloadFile = null;
            	
            	/*
			    if(android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())){
			        downloadDir = new File(Environment.getExternalStorageDirectory(), YPushConfig.downloadDir);
			        downloadFile = new File(downloadDir.getPath(), mDownloadFileName);
			        Log.d(TAG, "download start: mDownloadUrl=" + mDownloadUrl + " ,mDownloadFileName=" + mDownloadFileName);
			    }
			    */
            	
            	//zhaoshh,for if external data is null
            	if ((mDownloadFileName == null) || mDownloadUrl == null) {
                	Log.d(TAG, "download Url is null,can not download");
                	message.what = DOWNLOAD_FAIL;
	                mHandler.sendMessage(message);
	                
	                return ;
                }
            	
            	downloadDir = new File(Environment.getExternalStorageDirectory(), YPushConfig.downloadDir);
		        downloadFile = new File(downloadDir.getPath(), mDownloadFileName);
		        Log.d(TAG, "download start: mDownloadUrl=" + mDownloadUrl + " ,mDownloadFileName=" + mDownloadFileName);
            	
            	try {
	                if (!downloadDir.exists()){
	                	Log.d(TAG, "downloadDir is not exist");	
	                    downloadDir.mkdirs();
	                }
	
	                if (!downloadFile.exists()){
	                	Log.d(TAG, "downloadFile is not exist");	
	                    downloadFile.createNewFile();
	                } else {
	                	Log.d(TAG, "downloadFile is exist");
	                	String fileExtension = "";
	                	String fileprefix = "";
	                	String strDataFormat = "";
					    int beginIndex = mDownloadFileName.lastIndexOf('.');
					    fileExtension = mDownloadFileName.substring(beginIndex);
					    fileprefix = mDownloadFileName.substring(0, beginIndex);
					    
					    Date date = new Date(System.currentTimeMillis());
					    SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmss");
					    strDataFormat = dateFormat.format(date);
					    
					    mDownloadFileName = fileprefix + strDataFormat + fileExtension;
					    Log.d(TAG, "get fileName: beginIndex=" + beginIndex + " ,fileExtension=" + fileExtension + " ,fileprefix" + fileprefix);
					    Log.d(TAG, "get fileName: strDataFormat=" + strDataFormat + " ,mDownloadFileName=" + mDownloadFileName);
					    
	                	downloadFile = new File(downloadDir.getPath(), mDownloadFileName);
	                	downloadFile.createNewFile();
	                }     
	
	                long downloadSize = downloadFile(mDownloadUrl, downloadFile);
	                if (downloadSize > 0){
	                	Log.d(TAG, "download complete");
	                	message.what = DOWNLOAD_COMPLETE;
	                	mHandler.sendMessage(message);
	                }
	
	            } catch(Exception ex){
	                ex.printStackTrace();
	                message.what = DOWNLOAD_FAIL;
	                Log.d(TAG, "download fail");
	                mHandler.sendMessage(message);
	            }
            }
        }
    }
	
	public long downloadFile(String downloadUrl, File saveFile) throws Exception {
        int downloadCount = 0;
        int currentSize = 0;
        long totalSize = 0;
        int updateTotalSize = 0;

        HttpURLConnection httpConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            URL url = new URL(downloadUrl);
            Log.d(TAG, "url=" + url);
            httpConnection = (HttpURLConnection)url.openConnection();

            httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");

            if(currentSize > 0) {
                httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
            }

            httpConnection.setConnectTimeout(10000);
            httpConnection.setReadTimeout(20000);
            updateTotalSize = httpConnection.getContentLength();

            if (httpConnection.getResponseCode() == 404) {
            	Log.d(TAG, "getResponseCode==404");	
                throw new Exception("fail!");
            }

            is = httpConnection.getInputStream();                   
            fos = new FileOutputStream(saveFile, false);
            byte buffer[] = new byte[4096];
            int readsize = 0;

            while((readsize = is.read(buffer)) > 0){
                fos.write(buffer, 0, readsize);
                totalSize += readsize;

                if((downloadCount == 0) || (((int) (totalSize*100/updateTotalSize))-10) > downloadCount){ 
                    downloadCount += 10;
                }                        
            }
        } finally {
            if(httpConnection != null) {
                httpConnection.disconnect();
            }

            if(is != null) {
                is.close();
            }

            if(fos != null) {
                fos.close();
            }
        }

        Log.d(TAG, "totalSize=" + totalSize);
        return totalSize;
    }
	
    private int getVerCode(Context context){
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            Log.d(TAG, "getVerCode verCode=" + verCode);
        } catch (NameNotFoundException e) {
        }
        return verCode;
    }
    
    private String getVerName(Context context){
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Log.d(TAG, "getVerName verName=" + verName);
        } catch (NameNotFoundException e) {
        }
        return verName;
    }

}