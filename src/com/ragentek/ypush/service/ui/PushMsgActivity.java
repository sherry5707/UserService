package com.ragentek.ypush.service.ui;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ragentek.ypush.service.NewPushService;
import com.ragentek.ypush.service.PushBehaviorData;
import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.YPushService;
import com.ragentek.ypush.service.download.AppDownloadUtil;
import com.ragentek.ypush.service.network.AppDetailResponse;
import com.ragentek.ypush.service.network.HttpClient;
import com.ragentek.ypush.service.network.MyResponseHandler;
import com.ragentek.ypush.service.network.PushBehaviorResponseHandler;
import com.ragentek.ypush.service.util.AppDetailData;
import com.ragentek.ypush.service.util.ImageLoaderUtil;
import com.ragentek.ypush.service.util.PhoneInfoUtil;
import com.ragentek.ypush.service.util.SIMCardInfo;
import com.ragentek.ypush.service.util.YPushConfig;

import cz.msebera.android.httpclient.Header;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Broadcaster;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.content.Context;
import android.provider.Settings;
import android.graphics.Bitmap;

import android.content.pm.PackageManager.NameNotFoundException;//add by zhengguang.yang@20160406 for versioncode compare


public class PushMsgActivity  extends Activity {
	
	private static final String TAG = "YPushService.PushMsgActivity";
    public static final String INTENT_DOWNLOAD_COMPLETED = "com.ragentek.ypush.download.completed";
	
	private TextView mMsgTitleView;
	private TextView mMsgBodyView;
	private Intent mMsgIntent;

	private Button mDownloadButton;
	private LinearLayout detailPicLayout;
	private boolean mIsDetailOpen = false;
	private ImageLoader imageLoader;
	private AppDetailResponse res;
	private String pushType;
	private boolean mStartDown;
	private WebView mWebView;
	//add by zhengguang.yang@20160919 start for push user behavior
    String appId;
    String msgId;
    //add by zhengguang.yang end
	
//	private final String SYSTEM_DIALOG_REASON_KEY = "reason";  
//	private final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";  
//    private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";  
//    private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"; 

	@Override
    public void onCreate(Bundle savedInstanceState) {
		    Log.d(TAG, "onCreate enter");	
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_msg_activity);
        
        setTitle(getString(R.string.push_msg_ticker));
        imageLoader = ImageLoaderUtil.getInstance(getApplicationContext(),R.drawable.default_img_game);
        
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//		this.registerReceiver(mHomeKeyReceiver, intentFilter);
        
        init();
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.d(TAG, "onCreate exit");	
    }    
    
//	private BroadcastReceiver mHomeKeyReceiver = new BroadcastReceiver(){
//		public void onReceive(Context context, Intent intent) {
//			String intentString = intent.getAction();
//			if(intentString.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
//				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
//				Log.d(TAG, "mHomeKeyReceiver enter reason is " + reason);
//				if(reason != null){
//					if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {  
//						Log.d(TAG, "SYSTEM_DIALOG_REASON_HOME_KEY enter finish");
//                        finish();
//                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {  
//                    	Log.d(TAG, "SYSTEM_DIALOG_REASON_RECENT_APPS enter finish");
//                    	finish();
//                    }
//				}
//			}
//		};
//	};
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume enter");
		if(res!=null && res.getData()!=null){
			Log.d(TAG, "onResume enter res != null");
			refreshDetailView(res);
			
			//modified by zhengguang.yang@20160301 start for text can't show in theme material
//			TextView statusText = (TextView)findViewById(R.id.download_status_text);
//			mDownloadButton.setTag(res.getData().getDownloadUrl());
//			AppDownloadUtil.createDownloadListner(getApplicationContext(), 
//				mDownloadButton,statusText, res.getData(),2,false);
			
			mDownloadButton.setTag(res.getData().getDownloadUrl());
			AppDownloadUtil.createDownloadListner(getApplicationContext(), 
				mDownloadButton, res.getData(),2,false);
			//modified by zhengguang.yang@20160301 end
		}
	};
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
	
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		this.unregisterReceiver(mHomeKeyReceiver);
//	}
	
	public void init(){
		//modified by zhengguang.yang@20160302 start for new push
//        String msgData;
//		String strContent;
		String url;
		//modified by zhengguang.yang end
		String title;
		String msg;
		String visitFlag; 
		final String downloadType;
		
        mMsgTitleView = (TextView) findViewById(R.id.text_msg_title);
        mMsgBodyView = (TextView) findViewById(R.id.text_msg);
        mDownloadButton = (Button)findViewById(R.id.btn_download);
        
        mMsgIntent = getIntent();
        Bundle bundle = new Bundle();
        bundle = mMsgIntent.getExtras();
        
        pushType = bundle.getString("pushType");
        //modified by zhengguang.yang@20160302 start for new push 
//        msgData = bundle.getString("msgData");
//        strContent = bundle.getString("content");
        url = bundle.getString("url");
        //modified by zhengguang.yang end
        title = bundle.getString("title");
        msg = bundle.getString("msg");
        downloadType = bundle.getString("downloadType");
        //add by zhengguang.yang@20160919 start for push user behavior
        appId = bundle.getString("appId");
        msgId = bundle.getString("msgId");
        //add by zhengguang.yang end
        visitFlag = bundle.getString("wifiVisitFlag");
        
        ScrollView appMsgView = (ScrollView)findViewById(R.id.app_msg_view);
        ScrollView urlMsgView = (ScrollView)findViewById(R.id.url_msg_scroll);
        
        //according to the push msg data, set the biew display
		if (title != null) {
			String displayTitle;
	        displayTitle = String.format(getString(R.string.push_msg_title), title);
	        mMsgTitleView.setText(displayTitle);
		} else {
			mMsgTitleView.setVisibility(View.GONE);
		}
		
		if (msg != null) {
			if(pushType.equals("2")){
				mMsgBodyView.setVisibility(View.VISIBLE);
				mMsgBodyView.setText(msg);
			}
		} else {
			mMsgBodyView.setVisibility(View.GONE);
		}        
        //pushType 1:apk   2:text  3:url
        if(pushType.equals("1")){
        	urlMsgView.setVisibility(View.GONE);
        	appMsgView.setVisibility(View.VISIBLE);
            
            if(downloadType.equals("3")){
            	mStartDown = false;
            }else{
            	mStartDown = true;
            }
            
            detailPicLayout = (LinearLayout)findViewById(R.id.detail_pic_layout);
            
			RequestParams params = new RequestParams();
			//modified by zhengguang.yang@20160919 start for push user behavior
			params.put("_id", appId);
			HttpClient.getClient(getApplicationContext()).get(YPushConfig.HTTP_HEAD + 
					YPushConfig.API_APP_DETAIL_URL,params, getHandler(AppDetailResponse.class,msgId,appId));
			//add by zhengguang.yang end
        }else if(pushType.equals("3")){
        	//add by zhengguang.yang@20160919 start for push user behavior
        	AppDownloadUtil.sendPushBehavior(PushMsgActivity.this,YPushConfig.VIEWMSG,msgId, appId, null);
        	//add by zhengguang.yang end
        	urlMsgView.setVisibility(View.VISIBLE);
        	appMsgView.setVisibility(View.GONE);
        	try {	
        		//modified by zhengguang.yang@20160302 start for new push
        		String correctUrl = url;
        		boolean isBusiServer = getBusinessServerSetting(getApplicationContext());
				Log.v(TAG, "correct url: isBusiServer=" + isBusiServer);
				//if business server need correct
				if (isBusiServer) {
					//double Domain Name
					String useNetType = PhoneInfoUtil.getUseNetType(getApplicationContext());
					Log.v(TAG, "str useNetType:" + useNetType);
					if ("unknown".equals(useNetType)) {
						Log.v(TAG, "unknown network use default service!");
					
					} else if ("WIFI".equals(useNetType)) {
						Log.v(TAG, "WIFI network use fastest service!");
					
						correctUrl = correctWifiNetworkService(url, visitFlag);
					} else if ("MOBILE".equals(useNetType)) {
						Log.v(TAG, "MOBILE network use correct service!");
					
						SIMCardInfo si = new SIMCardInfo(getApplicationContext());
						String providersType = si.getProvidersType();
					
						if (providersType != null) {
							correctUrl = correctNetworkService(url, providersType);
							Log.v(TAG, "get correctUrl from correctNetworkService.");
						}

					}
				}
					
				final String apkurl = correctUrl;
		        //实例化WebView对象 
				mWebView = (WebView) findViewById(R.id.webview);
		        mWebView = new WebView(this); 
		        //设置WebView属性，能够执行Javascript脚本 
		        mWebView.getSettings().setJavaScriptEnabled(true); 
		        //加载需要显示的网页 
		        mWebView.loadUrl(apkurl); 
		        //设置Web视图 
		        setContentView(mWebView);
		        mWebView.setWebViewClient(new MyWebViewClient());
		        
//				Map<String,Object> tempMsgMap = parseData(msgData);
//				Map<String,Object> tempContent = (Map)tempMsgMap.get("content");
//				List<Map<String, Object>> urlList = new ArrayList<Map<String,Object>>();
//				urlList = (ArrayList)tempContent.get("urlList");
//				Log.v(TAG, "urlList=" + urlList + " ,tempMsgMap=" + tempMsgMap + " ,tempContent=" + tempContent);
//				
//				if (urlList != null) {
//					int listSize = urlList.size();
//					Log.v(TAG, "listSize=" + listSize + " ,urlList=" + urlList);
//					for (int i=0; i<listSize; i++) {
//						Map<String,Object> temp = urlList.get(i);
//						//modified by zhengguang.yang@20160301 start for key is wrong
//						//String Name = (String)temp.get("Name");
//						//String url = (String)temp.get("url");	
//						//String correctUrl = url;
//						//Log.v(TAG, "temp=" + temp + " ,Name=" + Name + " ,url=" + url);
//						
//						String name = (String)temp.get("name");
//						String url = (String)temp.get("url");	
//						String correctUrl = url;
//						Log.v(TAG, "temp=" + temp + " ,name=" + name + " ,url=" + url);
//						
//						//modified by zhengguang.yang end
//						
//						boolean isBusiServer = getBusinessServerSetting(getApplicationContext());
//						Log.v(TAG, "correct url: isBusiServer=" + isBusiServer);
//						//if business server need correct
//						if (isBusiServer) {
//							//double Domain Name
//							String useNetType = PhoneInfoUtil.getUseNetType(getApplicationContext());
//							Log.v(TAG, "str useNetType:" + useNetType);
//							if ("unknown".equals(useNetType)) {
//								Log.v(TAG, "unknown network use default service!");
//							
//							} else if ("WIFI".equals(useNetType)) {
//								Log.v(TAG, "WIFI network use fastest service!");
//							
//								correctUrl = correctWifiNetworkService(url, visitFlag);
//							} else if ("MOBILE".equals(useNetType)) {
//								Log.v(TAG, "MOBILE network use correct service!");
//							
//								SIMCardInfo si = new SIMCardInfo(getApplicationContext());
//								String providersType = si.getProvidersType();
//							
//								if (providersType != null) {
//									correctUrl = correctNetworkService(url, providersType);
//									Log.v(TAG, "get correctUrl from correctNetworkService.");
//								}
//
//							}
//						}
//							
//						final String apkurl = correctUrl;
//				        //实例化WebView对象 
//						mWebView = (WebView) findViewById(R.id.webview);
//				        mWebView = new WebView(this); 
//				        //设置WebView属性，能够执行Javascript脚本 
//				        mWebView.getSettings().setJavaScriptEnabled(true); 
//				        //加载需要显示的网页 
//				        mWebView.loadUrl(apkurl); 
//				        //设置Web视图 
//				        setContentView(mWebView);
//				        mWebView.setWebViewClient(new MyWebViewClient());
//					}
//				}
        		//modified by zhengguang.yang end
			} catch (Exception e) {
				Log.e(TAG, "has Exception");
				e.printStackTrace();
				
				//return ; //can not return, because if return the view "%1$S" 
			}
        }else{
        	//add by zhengguang.yang@20160919 start for push user behavior
        	AppDownloadUtil.sendPushBehavior(PushMsgActivity.this, YPushConfig.VIEWMSG, msgId, appId,null);
        	//add by zhengguang.yang end
        	appMsgView.setVisibility(View.GONE);
        	urlMsgView.setVisibility(View.GONE);
        }
		
	}
	  
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { 
        	Log.v(TAG, "pushType = " + pushType);
        	if(pushType.equals("3") && mWebView.canGoBack()){
        		mWebView.goBack(); //goBack()表示返回WebView的上一页面 
        		Log.v(TAG, "pushType return true ");
        		return true; 
            }else{
            	super.onKeyDown(keyCode, event);
            	return true;
            }
        } 
        Log.v(TAG, "pushType return false ");
        return false; 
	}
	
    private static Map<String, Object> parseData(String data){
		Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(data, new TypeToken<Map<String, Object>>() {}.getType()); 
        Log.v(TAG, "parseData map:" + map);
        
        return map;
    }
	
    
    public void openInternetLinkage(String stringUrl) {
	    Intent intent = new Intent();
	    intent.setData(Uri.parse(stringUrl));
	    intent.setAction("android.intent.action.VIEW");
	    //if have muti browser,can choise by user. But permise: xml file must have property "android:autoLink="web""
	    //intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
	    
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    startActivity(intent);
    }
    
    //this func for get the correct network service. networkType: 1:Mobile, 2:Unicom, 3:Telecom
  	private String correctNetworkService(String sourceUrl, String networkType) {
  		
  		if ( sourceUrl == null || ("".equals(sourceUrl)) || networkType == null || ("".equals(networkType)) ) {
  			Log.v(TAG, "url or net type error!");
  			
  			return null;
  		}
  		
  		String correctUrl = null;
  		
  		try {
  			int index = sourceUrl.indexOf("download");
  			int len = sourceUrl.length();
  			Log.v(TAG, "index=" + index + " ,len=" + len);
  			
  			if ((index == -1) || (index < 0) || (index > len)) {
  				Log.v(TAG, "url not need correct!");
  				
  				return sourceUrl;
  			}
  			
  			String temp1 = sourceUrl.substring(0, index);
  			String temp2 = sourceUrl.substring(index, index+8);
  			String temp3 = sourceUrl.substring(index+8+1);					
  			Log.v(TAG, "temp1=" + temp1 + " ,temp2=" + temp2 + " ,temp3=" + temp3);
  			
  			if ("1".equals(networkType)) {
  				Log.v(TAG, "China Mobile network use Telecom service!");
  				
  				correctUrl = temp1 + temp2 + "1" + temp3;				
  			} else if ("2".equals(networkType)) {
  				Log.v(TAG, "China Unicom network use Unicom service!");
  				
  				correctUrl = temp1 + temp2 + "2" + temp3;				
  			} else if ("3".equals(networkType)) {
  				Log.v(TAG, "China Telecom network use Telecom service!");		
  				
  				correctUrl = temp1 + temp2 + "1" + temp3;
  			}
  			
  		} catch (Exception e) {
  			// TODO Auto-generated catch block
  			Log.v(TAG,"Exception e=" + e);
  			e.printStackTrace();
  		}	
  		
  		Log.v(TAG, " sourceUrl=" + sourceUrl);
  		Log.v(TAG, "correctUrl=" + correctUrl);
  		
  		return correctUrl;		
  	}
  	
  	public String correctWifiNetworkService(String oldUrl, String visitFlag) {
  		Log.v(TAG,"notifyCorrectNetworkService: oldUrl=" + oldUrl);
  		String correctUrl = oldUrl;
  		
  		if ("0".equals(visitFlag)) {
			Log.v(TAG, "same speed use set service!");
			
			correctUrl = oldUrl;				
		} else if ("1".equals(visitFlag)) {
			Log.v(TAG, "Telecom fast use Telecom service!");
			
			correctUrl = correctNetworkService(oldUrl, "1");			
		} else if ("2".equals(visitFlag)) {
			Log.v(TAG, "Unicom fast use Unicom service!");		
			
			correctUrl = correctNetworkService(oldUrl, "2");
		}
		
		return correctUrl;
	}

        //return. true: business server. false: test server
	public boolean getBusinessServerSetting(Context context){
		boolean isBusinessServer = true;
		try{
		    String strBusinessServer = Settings.System.getString(context.getContentResolver(),
					/*Settings.System.QINGCHENG_BUSINESS_SERVER_CONFIG*/"set_qingcheng_server_config");
		    isBusinessServer = Boolean.parseBoolean(strBusinessServer) || (strBusinessServer == null);

		}catch(Exception e){
		    e.printStackTrace();
		    Log.v(TAG, "getBusinessServerSetting have exception e=" + e);
		}
		return isBusinessServer;
	}
  	
    private void asyncSetAppIcon(final String iconUrl, final ImageView imageView) {
        final AsyncTask<Void, Void, Void> updateTask = new AsyncTask<Void, Void, Void>() {
        	private Bitmap bitmap;
            @Override
            protected Void doInBackground(Void... aVoid) {
                bitmap = YPushService.returnBitMap(iconUrl);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                imageView.setImageBitmap(bitmap);
            }
        };
        updateTask.execute();
    }
    //modified by zhengguang.yang@20160919 start for push user behavior
    /*
	private MyResponseHandler getHandler(Class<?> classzz){
		MyResponseHandler handler = new MyResponseHandler(PushMsgActivity.this,classzz){
			//modified by zhengguang.yang 2015.11.20 start. for android6.0 make asynchttpclient use. 
//			@Override
//			public void onSuccess(String result) {
//				super.onSuccess(result);
//				
//				if(getDataSuccess){
//					System.out.println("----result----"+result);
//					if(response instanceof AppDetailResponse){
//						res = (AppDetailResponse)response;
//						if(res.getData()!=null){
//							refreshDetailView(res);
//							
//							TextView statusText = (TextView)findViewById(R.id.download_status_text);
//							mDownloadButton.setTag(res.getData().getDownloadUrl());
//							AppDownloadUtil.createDownloadListner(getApplicationContext(), 
//									mDownloadButton,statusText, res.getData(),2,mStartDown);
//						}else{
//							Toast.makeText(getApplicationContext(), R.string.push_msg_res_error, Toast.LENGTH_SHORT).show();
//							finish();
//						}
//					}
//				}
//			}
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				super.onSuccess(arg0, arg1, arg2);
				
				if(getDataSuccess){
					if(arg2 != null){
						String result = new String(arg2);
						System.out.println("----result----"+result);
						if(response instanceof AppDetailResponse){
							res = (AppDetailResponse)response;
							if(res.getData()!=null){
								refreshDetailView(res);
								
							//modified by zhengguang.yang@20160301 start for text can't show in theme material
//							TextView statusText = (TextView)findViewById(R.id.download_status_text);
//							mDownloadButton.setTag(res.getData().getDownloadUrl());
//							AppDownloadUtil.createDownloadListner(getApplicationContext(), 
//									mDownloadButton,statusText, res.getData(),2,mStartDown);
							
							mDownloadButton.setTag(res.getData().getDownloadUrl());
							AppDownloadUtil.createDownloadListner(getApplicationContext(), 
									mDownloadButton, res.getData(),2,mStartDown);
							//modified by zhengguang.yang end
							
							}else{
								Toast.makeText(getApplicationContext(), R.string.push_msg_res_error, Toast.LENGTH_SHORT).show();
								finish();
							}
						}
					}
				}
			}
			//modified by zhengguang.yang 2015.11.20 end. for android6.0 make asynchttpclient use. 
		};
		return handler;
	}
	*/
	private PushBehaviorResponseHandler getHandler(Class<?> classzz,final String msgId,final String appId){
		PushBehaviorResponseHandler handler = new PushBehaviorResponseHandler(PushMsgActivity.this,classzz,msgId,appId){
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				super.onSuccess(arg0, arg1, arg2);
				
				if(getDataSuccess){
					if(arg2 != null){
						String result = new String(arg2);
						System.out.println("----result----"+result);
						if(response instanceof AppDetailResponse){
							res = (AppDetailResponse)response;
							if(res.getData()!=null){
								refreshDetailView(res);
							mDownloadButton.setTag(res.getData().getDownloadUrl());
							//modified by zhengguang.yang@20160919 start for push user behavior
							res.getData().setAppId(appId);
							res.getData().setMsgId(msgId);
							AppDownloadUtil.sendPushBehavior(PushMsgActivity.this,YPushConfig.VIEWMSG,msgId, appId, res.getData().getPackageName());
							AppDownloadUtil.createDownloadListner(getApplicationContext(), 
									mDownloadButton, res.getData(),2,mStartDown);
				        	//add by zhengguang.yang end
							}else{
								Toast.makeText(getApplicationContext(), R.string.push_msg_res_error, Toast.LENGTH_SHORT).show();
								finish();
							}
						}
					}
				}
			}
			//modified by zhengguang.yang 2015.11.20 end. for android6.0 make asynchttpclient use. 
		};
		return handler;
	}
	//add by zhengguang.yang end
    
	
	private void refreshDetailView(final AppDetailResponse res){
		final AppDetailData data = res.getData();
		List<String> installedPacekagesList = AppDownloadUtil.getInstalledPackages(getApplicationContext());
		for(String packageName:installedPacekagesList){
			if(data.getPackageName().equals(packageName)){
				//add by zhengguang.yang@20160406 start for versioncode compare
				int localAppVersion = AppDownloadUtil.getInstalledAppVersionCode(getApplicationContext(),packageName);
				int remoteAppVersion = Integer.parseInt(data.getVersionCode());
				if(localAppVersion >= remoteAppVersion){
					data.setDownloadStatus(AppDownloadUtil.STATUS_INSTALLED);
				}
				break;
				//add by zhengguang.yang@20160406 start for versioncode compare
			}
		}
		
        ImageView appIcon = (ImageView)findViewById(R.id.item_image);
        if(data.getLogoUrl() != null) {
        	asyncSetAppIcon(data.getLogoUrl(), appIcon);
        }

        TextView appSize = (TextView)findViewById(R.id.app_size);
        if(data.getSize() != null) appSize.setText(data.getSize());
        TextView appVersion = (TextView)findViewById(R.id.app_amount);
        if(data.getVersionName() != null) appVersion.setText(getResources().getString(R.string.push_app_version) + data.getVersionName());
        TextView appName = (TextView)findViewById(R.id.app_name);
        if(data.getName() != null) appName.setText(data.getName());
        
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.detail_default_pic)
        .showImageOnFail(R.drawable.detail_default_pic)
        .bitmapConfig(Bitmap.Config.ARGB_8888)
        .cacheInMemory()
        .cacheOnDisc()
        .build();
		
		if(data.getScreenShotMobile() != null && data.getScreenShotMobile().length >0){
			detailPicLayout.removeAllViews();
			for(String url:data.getScreenShotMobile()){
				View subView = getLayoutInflater().inflate(R.layout.include_game_detail_pic_view, null);
				ImageView subImage = (ImageView)subView.findViewById(R.id.item_image);
				subImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
//						Intent intent = new Intent(getApplicationContext(),BigPicPreviewActivity.class);
//						intent.putExtra("picArray", data.getScreenShotMobile());
//						startActivity(intent);
					}
				});
				imageLoader.displayImage(url, subImage,options);
				detailPicLayout.addView(subView);
			}
		}
		
		((TextView)findViewById(R.id.app_detail_text)).setText(Html.fromHtml(res.getData().getDescription()));
		findViewById(R.id.btn_app_detail_more_layout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				TextView detailText = (TextView)findViewById(R.id.app_detail_text);
				ImageView detailImg = (ImageView)findViewById(R.id.btn_app_detail_more);
				TextView detailBtnText = (TextView)findViewById(R.id.app_detail_more_text);
				if(!mIsDetailOpen){
					mIsDetailOpen = true;
					detailText.setMaxLines(999);
					detailImg.setBackgroundResource(R.drawable.detail_more_btn_selected_2);
					detailBtnText.setText(R.string.push_msg_description_retract);
				}else{
					mIsDetailOpen = false;
					detailText.setMaxLines(4);
					detailImg.setBackgroundResource(R.drawable.detail_more_btn_selected_1);
					detailBtnText.setText(R.string.push_msg_description_more);
				}
			}
		});
	}
	
    //Web视图 
    public class MyWebViewClient extends WebViewClient { 
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
            view.loadUrl(url); 
            return true; 
        } 
    } 
    
    private boolean checkWifiConnected(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
}
