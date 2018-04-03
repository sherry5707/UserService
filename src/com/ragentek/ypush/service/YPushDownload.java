package com.ragentek.ypush.service;

import java.io.File;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class YPushDownload {
    private static final String TAG = "DownloadActivity";
    
    public static final int MSG_DOWNLOAD_REQUEST = 0;
    public static final int MSG_DOWNLOAD_COMPLETED = 1;
    public static final int MSG_DOWNLOAD_STOP = 2;

    public static final String INTENT_DOWNLOAD_COMPLETED = "com.ragentek.ypush.download.completed";
    
    public Context mContext;
    public Uri mUri;
    private DownloadManager mDownloadMgr;
    private DownloadCompletedReceiver mDownloadCompletedReceiver;
    private boolean mIsDownload;
    private long mDownloadId;
    
    private final class DownloadCompletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                long download_id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Message message = new Message();
                message.what = MSG_DOWNLOAD_COMPLETED;
                message.obj = download_id;
                mHandler.sendMessage(message);
                context.unregisterReceiver(mDownloadCompletedReceiver);
            }
        }
    }
    
    public YPushDownload(Context context) {        
        mContext = context;

        mDownloadMgr = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mDownloadCompletedReceiver = new DownloadCompletedReceiver();
        mContext.registerReceiver(mDownloadCompletedReceiver, filter);
    }
    
    public void startDownload(String url) {
        Message message = new Message();
        message.what = MSG_DOWNLOAD_REQUEST;
        message.obj = url;
        mHandler.sendMessage(message);
    }
    
    public long getDownloadId(){
        return mDownloadId;
    }

    public void stopDownload(){
        Message message = new Message();
        message.what = MSG_DOWNLOAD_STOP;
        message.obj = mDownloadId;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_DOWNLOAD_REQUEST:
                mIsDownload = true;
                String url = (String) msg.obj;
                Uri uri = Uri.parse(url);
                Request request = new DownloadManager.Request(uri);
                String nameString = getDownloadNameString(url);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameString);
                request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                mDownloadId = mDownloadMgr.enqueue(request);
                break;
            case MSG_DOWNLOAD_COMPLETED:
                long id = (Long) msg.obj;
                Uri downLoadedUri = mDownloadMgr.getUriForDownloadedFile(id);
                if (downLoadedUri != null && downLoadedUri.toString() != null && downLoadedUri.toString().startsWith("content://")) {
                    Cursor cursor = mDownloadMgr.query(new DownloadManager.Query().setFilterById(id));
                    try {
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                            String localPath = cursor.getString(columnIndex);
                            downLoadedUri = Uri.fromFile(new File(localPath));
                        } else {
                        }
                    } finally {
                        cursor.close();
                    }
                }
                
                Intent downloadIntent = new Intent(INTENT_DOWNLOAD_COMPLETED);
                mContext.sendBroadcast(downloadIntent);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(downLoadedUri, "application/vnd.android.package-archive");  
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    // Toast.makeText(mContext, R.string.update_fail,
                    //         Toast.LENGTH_SHORT).show();
                }
                break;
            case MSG_DOWNLOAD_STOP:
                long downloadId = (Long)msg.obj;
                mDownloadMgr.remove(downloadId);
                break;
            default:
                break;
            }
        }
    };

    public boolean isDownloading(){
        return mIsDownload;
    }

    public String getDownloadNameString(String url){
        int pos = url.lastIndexOf("/");
        return url.substring(pos+1);
    }
}
