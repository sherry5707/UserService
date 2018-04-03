package com.ragentek.ypush.service.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.YPushService;
import com.ragentek.ypush.service.util.CommonUtils;
import com.ragentek.ypush.service.util.SIMCardInfo;
import com.ragentek.ypush.service.util.YPushConfig;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class UserFeedback extends BaseActivity{
	
	private static final String TAG = "UserRequest";
	
	public static final int PHOTO_PICKED_WITH_DATA = 3001;
    private static final int PICTURE_MAX_WIDTH = 480;
    private static final int PICTURE_MAX_HEIGHT = 800;
    
    private Bitmap mSelectedBitmap;
    private HttpClient mHttpClient = null;
    private String mFilePath;

	private ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialogSuccess;
	private AlertDialog mAlertDialogFailed;
	//add by sherry
/*	private int clickId;
	private ArrayList<Bitmap> localMaps=new ArrayList<>();
	private ImageButton img1,img2,img3;
	private ImageButton imgDelete1,imgDelete2,imgDelete3;
	private ArrayList<ImageButton> imgBtns=new ArrayList<>();
	private ArrayList<ImageButton> imgDeleteBtns=new ArrayList<>();
	private ArrayList<String> mFilePaths=new ArrayList<>();*/
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0://success
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				try {
					mAlertDialogSuccess.show();
				} catch (BadTokenException e) {
					e.printStackTrace();
				} finally {
					Toast.makeText(getApplicationContext(), R.string.user_feedback_commit_success, Toast.LENGTH_LONG).show();
				}
				break;
			case 1://error
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				try {
					mAlertDialogFailed.show();
				} catch (BadTokenException e) {
					e.printStackTrace();
				} finally {
					Toast.makeText(getApplicationContext(), R.string.user_feedback_net_error, Toast.LENGTH_LONG).show();
				}
				break;
			case 2://finish
				try {
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
					if (mAlertDialogFailed.isShowing()) {
						mAlertDialogFailed.dismiss();
					}
					if (mAlertDialogSuccess.isShowing()) {
						mAlertDialogSuccess.dismiss();
					}
				} catch (java.lang.IllegalArgumentException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				for (File file : getFilesDir().listFiles()) {
					if (file.exists()) {
						file.delete();
					}
				}
				UserFeedback.this.finish();
				break;
			case 3://background
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
//				Toast.makeText(getApplicationContext(), R.string.commit_in_background_tips, Toast.LENGTH_SHORT).show();
				UserFeedback.this.finish();
				break;
			}
		}
		
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.user_feedback);
		setContentView(R.layout.user_feedback_lollipop);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window Mywindow = this.getWindow();
			Mywindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			Mywindow.setStatusBarColor(0x10000000);
		}
		init();
		CommonUtils.getMyPhoneAppList(getApplicationContext());
	}
	
	private void init(){
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		mActionBarTitle.setText(title);
//		TextView textView = (TextView)findViewById(R.id.feedback_type);
//		textView.setText(title);
//		actionBar.setTitle(getResources().getString(R.string.app_name) + " ( "+title+" )");
		
//		ImageButton backButton = (ImageButton)findViewById(R.id.user_feedback_back);
//		backButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
////				finish();
//				mHandler.sendEmptyMessage(2);
//			}
//		});

		TextView commitButton = (TextView) findViewById(R.id.user_feedback_commit);
		commitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				commitUserRequest();
			}
		});
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(R.string.app_name);
		mProgressDialog.setMessage(getString(R.string.submitting));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.commit_in_background), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mHandler.sendEmptyMessage(3);
			}
		});
		mProgressDialog.setOnCancelListener(null);
		
		//modified by zhengguang.yang@20160302 for dialog content can't show 
//		mAlertDialogSuccess = new AlertDialog.Builder(UserFeedback.this)//
		mAlertDialogSuccess = new AlertDialog.Builder(UserFeedback.this,R.style.myAlertDialogText)//
				.setTitle(R.string.app_name)//
				.setMessage(R.string.user_feedback_commit_success)//
				.setCancelable(false)//
				.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						UserFeedback.this.finish();
					}
				}).create();
		
		//modified by zhengguang.yang@20160302 for dialog content can't show 
//		mAlertDialogFailed = new AlertDialog.Builder(UserFeedback.this)//
		mAlertDialogFailed = new AlertDialog.Builder(UserFeedback.this,R.style.myAlertDialogText)//
				.setTitle(R.string.app_name)//
				.setMessage(R.string.user_feedback_net_error)//
				.setCancelable(false)//
				.setNegativeButton(R.string.btn_cancel, null)//
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						commitUserRequest();
					}
				}).create();
		//add by sherry
/*		img1 = (ImageButton)findViewById(R.id.edit_add_picture);
		img2 = (ImageButton)findViewById(R.id.edit_add_picture2);
		img3 = (ImageButton)findViewById(R.id.edit_add_picture3);
		imgDelete1 = (ImageButton)findViewById(R.id.edit_delete_picture);
		imgDelete2 = (ImageButton)findViewById(R.id.edit_delete_picture2);
		imgDelete3 = (ImageButton)findViewById(R.id.edit_delete_picture3);
		imgBtns.add(0,img1);
		imgBtns.add(1,img2);
		imgBtns.add(2,img3);
		imgDeleteBtns.add(0,imgDelete1);
		imgDeleteBtns.add(1,imgDelete2);
		imgDeleteBtns.add(2,imgDelete3);*/
	}

	//add by sherry
/*	private void UpdateImageButton(){
		Log.e(TAG,"UpdateImageButton");
		int i=0;
		for(;i<localMaps.size();i++){
			Log.e(TAG,"localMaps.size():"+localMaps.size());
			if(localMaps.size()<3){
				imgBtns.get(i+1).setVisibility(View.VISIBLE);
			}
			imgBtns.get(i).setImageBitmap(localMaps.get(i));
			imgDeleteBtns.get(i).setVisibility(View.VISIBLE);
		}
		if(i<imgBtns.size()) {
			imgDeleteBtns.get(i).setVisibility(View.GONE);
			imgBtns.get(i).setImageResource(R.drawable.add_img);
			i++;
			for (; i < imgBtns.size(); i++) {
				imgBtns.get(i).setVisibility(View.GONE);
				imgDeleteBtns.get(i).setVisibility(View.GONE);
			}
		}
	}*/

	public void onAddPictureButtonClick(View v){
		System.out.println("yangyang enter onAddPictureButtonClick");
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	intent.setType("image/*");
    	startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
	}
	
	public void onDeletePictureButtonClick(View v){
		if(mFilePath!= null || mSelectedBitmap != null){
			mFilePath = null;
			mSelectedBitmap = null;
		}
		ImageButton imageView = (ImageButton)findViewById(R.id.edit_add_picture);
		imageView.setImageResource(R.drawable.add_img);
		ImageButton deleteButton = (ImageButton)findViewById(R.id.edit_delete_picture);
		deleteButton.setVisibility(View.GONE);
		//Log.e(TAG,"localMaps.size():"+localMaps.size());
		/*switch (v.getId()){
			case R.id.edit_delete_picture:{
				localMaps.remove(0);
				mFilePaths.remove(0);
				UpdateImageButton();
				break;
			}
			case R.id.edit_delete_picture2:{
				localMaps.remove(1);
				mFilePaths.remove(1);
				UpdateImageButton();
				break;
			}
			case R.id.edit_delete_picture3:{
				localMaps.remove(2);
				mFilePaths.remove(2);
				UpdateImageButton();
				break;
			}
		}*/
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode != RESULT_OK){
    		System.out.println("yangyang enter createpersonalappguidance onActivityResult result not ok");
    		return; 
        }
		switch (requestCode) {
			case PHOTO_PICKED_WITH_DATA:
			    if (data == null) {
			        return;
			    }
			    Uri selectedImage = data.getData();
		        if (selectedImage == null) {
		            return;
		        }
			    mFilePath = selectedImage.getPath();
		        String selectedUriString = selectedImage.toString();
		        if (selectedUriString.startsWith("content://")) {
		            String[] filePathColumn = { MediaStore.Images.Media.DATA };
		            Cursor cursor = getContentResolver().query(selectedImage,
		                    filePathColumn, null, null, null);
		            cursor.moveToFirst();
		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		            mFilePath = cursor.getString(columnIndex);
		            cursor.close();
		        }
		
		        BitmapFactory.Options options = new BitmapFactory.Options();
		        options.inJustDecodeBounds = true; //just get the photo's width&height
		        BitmapFactory.decodeFile(mFilePath,options);
		        int photoHeight = options.outHeight;
		        int photoWidth = options.outWidth;
		        
		        double ratioThumb = 2D;
		        double widthRatioThumb = (double)photoWidth / (double) PICTURE_MAX_WIDTH; 
		        double heightRatioThumb = (double) photoHeight / (double) PICTURE_MAX_HEIGHT; 
		        if(widthRatioThumb < heightRatioThumb)
		            widthRatioThumb = heightRatioThumb;
		        if(widthRatioThumb > ratioThumb)
		            ratioThumb = widthRatioThumb;
		        if(widthRatioThumb <= 1 && heightRatioThumb <= 1){
		        	ratioThumb = 1D;
		        }
		
		        options.inJustDecodeBounds = false;
		        options.inSampleSize = (int)ratioThumb;
		    	options.inPreferredConfig = Bitmap.Config.RGB_565;
		               
		    	mSelectedBitmap = BitmapFactory.decodeFile(mFilePath,options);
		        
				ImageButton imageView = (ImageButton)findViewById(R.id.edit_add_picture);
				if(mSelectedBitmap != null){
					imageView.setImageBitmap(mSelectedBitmap);
					imageView.invalidate();
					ImageButton deleteButton = (ImageButton)findViewById(R.id.edit_delete_picture);
					deleteButton.setVisibility(View.VISIBLE);
					/*switch (clickId){
						case R.id.edit_add_picture: {
							if(localMaps.size()<3){
								localMaps.add(0,mSelectedBitmap);
								mFilePaths.add(0,mFilePath);
							}else {
								localMaps.set(0,mSelectedBitmap);
								mFilePaths.set(0,mFilePath);
							}
							UpdateImageButton();
							break;
						}
						case R.id.edit_add_picture2: {
							if(localMaps.size()<3){
								localMaps.add(1,mSelectedBitmap);
								mFilePaths.add(1,mFilePath);
							}else {
								localMaps.set(1,mSelectedBitmap);
								mFilePaths.set(0,mFilePath);
							}
							UpdateImageButton();
							break;
						}
						case R.id.edit_add_picture3: {
							if(localMaps.size()<3){
								localMaps.add(2,mSelectedBitmap);
								mFilePaths.add(2,mFilePath);
							}else {
								localMaps.set(2,mSelectedBitmap);
								mFilePaths.set(2,mFilePath);
							}
							UpdateImageButton();
							break;
						}
					}*/
				}
				break;
			default:
				break;
		}
    }

	private String getSuitableWidthBitmapFile(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		if (file.length() < 309000) {//if the image size less than 300K , upload origin image , or compress image .
			return path;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int width = options.outWidth;
		int rate = 1;
		int baseSize = 1080;
		while (width / baseSize > 1) {
			rate = rate * 2;
			baseSize = baseSize * 2;
		}
		options.inSampleSize = rate;
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		if (bitmap != null) {
			Log.i(TAG, "bitmap width=" + bitmap.getWidth() + "\nbitmap height=" + bitmap.getHeight());
		}
		
		String mFileCopyPath = this.getFilesDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
		Log.i(TAG, mFileCopyPath+"\r\nmFileCopyPath.length == "+new File(mFileCopyPath).length());
		try {
			FileOutputStream fos = new FileOutputStream(mFileCopyPath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 62, fos);
			fos.flush();
			fos.close();
			return mFileCopyPath;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void commitUserRequest(){
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		
		EditText description = (EditText)findViewById(R.id.edit_question_description);
		EditText email = (EditText)findViewById(R.id.edit_add_email);
		String descripString = description.getText().toString().trim();
		String emailString = email.getText().toString().trim();
		
		Log.i(TAG, "commitUserRequest, title is " + title + ", descripString is " + descripString + 
				", emailString is " + emailString);
		if((!descripString.equals("")) && (!emailString.equals(""))){
			if(isEmail(emailString)){
				commitUserFeedback(title, descripString, emailString);
			} else if(isNumeric(emailString)){
				commitUserFeedback(title, descripString, emailString);
			} else {
				Toast.makeText(this, R.string.user_feedback_email_error, Toast.LENGTH_LONG).show();
			}			
		} else {
			Toast.makeText(this, R.string.user_feedback_input_error, Toast.LENGTH_LONG).show();
		}
 	}
	
	private void makeToast(final int id){
		Looper.prepare();
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void commitUserFeedback(final String title, final String description, final String email){
		if(!isWifiConnected() && !isNetworkConnected()){
			Toast.makeText(getApplicationContext(), R.string.user_feedback_net_error, Toast.LENGTH_LONG).show();
			return;
		}
//		mProgressDialog = ProgressDialog.show(UserFeedback.this, getString(R.string.app_name), getString(R.string.submitting), true, false);
		if (mProgressDialog.isShowing()) {
			//
		}else {
			mProgressDialog.show();
		}
		new Thread(){
			@Override
			public void run(){
				commitUserFeedbackToService(title, description, email);
			}
		}.start();
	}
	//removed by zhengguang.yang 2015.12.01 start. for api23 use okhttp replace httpclient
//	private void commitUserFeedbackToService(final String title, String description, String email){
//		String mFilePath = getSuitableWidthBitmapFile(this.mFilePath);
//		SIMCardInfo si = new SIMCardInfo(this);
//		final String imei = si.getIMEI();
//		
//        HttpParams httpParams = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpParams,10000);
//        HttpConnectionParams.setSoTimeout(httpParams,20000);
//        mHttpClient = new DefaultHttpClient(httpParams);		
//        
//        String baseUrl = YPushConfig.HTTP_HEAD + YPushConfig.API_USER_FEEDBACK_URL;
//        
//        try {
//        	
//    		List<NameValuePair> params = new ArrayList<NameValuePair>();
//    		params.add(new BasicNameValuePair("deviceId", imei));
//    		params.add(new BasicNameValuePair("feedbackPushType", title));
//            params.add(new BasicNameValuePair("feedbackPushDescription", description));   
//            params.add(new BasicNameValuePair("feedbackPushEmail", email));
//            if(mFilePath == null){
//            	params.add(new BasicNameValuePair("feedbackPushPicture", ""));
//            }
//            
//            String urlPara = baseUrl + "?" + URLEncodedUtils.format(params, HTTP.UTF_8);
////			Log.i(TAG, "urlPara\r\n" + urlPara);
//        	HttpPost postMethod = new HttpPost(urlPara);
//        	
//            if(mFilePath != null){
//            	Log.i(TAG, "add file , filepath is " + mFilePath);
//            	File file = new File(mFilePath);
////				Log.i(TAG, "add file , filepath is " + mFilePath+file.length());
//            	MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//            	multipartEntity.addPart("feedbackPushPicture", new FileBody(file));
//            	postMethod.setEntity(multipartEntity);
//            }
//
//            HttpResponse response = mHttpClient.execute(postMethod);
//			
//			Log.i(TAG, "resCode = " + response.getStatusLine().getStatusCode()); //鑾峰彇鍝嶅簲鐮� 
//			Log.i(TAG, "result = " + EntityUtils.toString(response.getEntity(), "utf-8"));
//			
//			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
//				Log.i(TAG, "success!");
//				mHandler.sendEmptyMessage(0);
////				makeToast(R.string.user_feedback_commit_success);
//			}else{
//				Log.i(TAG, "fail!");
//				mHandler.sendEmptyMessage(1);
////				makeToast(R.string.user_feedback_net_error);
//			}
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			mHandler.sendEmptyMessage(2);
////			finish();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (ConnectTimeoutException e) {
//			mHandler.sendEmptyMessage(1);
//			Log.i(TAG, "ConnectTimeoutException"+ "\r\n" +e.getLocalizedMessage());
//			e.printStackTrace();
//		} catch (IOException e) {
//			mHandler.sendEmptyMessage(1);
//			e.printStackTrace();
//		}
//		        
//		//return result;
//	}
	//removed by zhengguang.yang 2015.12.01 end. for api23 use okhttp replace httpclient
	
	public boolean isNetworkConnected() {  
		ConnectivityManager mConnectivityManager = (ConnectivityManager) this 
		.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
		if (mNetworkInfo != null) { 
			return mNetworkInfo.isAvailable(); 
		}
		return false; 
	}
	
	public boolean isWifiConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) this 
				.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager .getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
		if (mWiFiNetworkInfo != null) { 
			return mWiFiNetworkInfo.isAvailable(); 
		} 
		return false; 
	}
	
	public boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);

		return m.matches();
	}
	
	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		// add BUG:standardization contacts. by:xiaolin.he 2015-3-2(start)
		if (TextUtils.isEmpty(str) || str.length() < 7) {
			return false;
		}
		// add BUG:standardization contacts. by:xiaolin.he 2015-3-2(end)
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
	
	//add by zhengguang.yang 2015.12.01 start. for api23 use okhttp replace httpclient
	private OkHttpClient client = null;
	private static final String KEY_deviceId = "deviceId";
	private static final String KEY_feedbackPushType = "feedbackPushType";
	private static final String KEY_feedbackPushDescription = "feedbackPushDescription";
	private static final String KEY_feedbackPushEmail = "feedbackPushEmail";
	private static final String KEY_feedbackPushPicture = "feedbackPushPicture";
	//add by zhengguang.yang@20160505 start for add new push param
	private static final String KEY_versionCode = "versionCode";
	private static final String KEY_romVersion = "romVersion";
	private static final String KEY_productModel = "productModel";
	private static final String KEY_aliRomVersion = "aliRomVersion";
	private static final String KEY_ipAddress = "ipAddress";
	//add by zhengguang.yang@20160505 end for add new push param
	private void commitUserFeedbackToService(final String title, String description, String email){
		String mFilePath = getSuitableWidthBitmapFile(this.mFilePath);
		SIMCardInfo si = new SIMCardInfo(this);
		String imei;
		if(si.getIMEI() == null){
			imei = "";
		}else{
			imei = si.getIMEI();;
		}
		//add by zhengguang.yang@20160505 start for add new feedback param
		String versionCode = YPushService.PUSH_CLIENT_VERSION_CODE;
		String romVersion = YPushService.getMyUIVersion();
		String productModel = YPushService.getProductModel();
		String aliRomVersion = YPushService.getAliRomVersion();
		String ipAddress = YPushService.getIpAddress();
		Log.i(TAG,"versionCode="+versionCode+",romVersion="+romVersion+",productModel="+productModel+",aliRomVersion="+aliRomVersion+",ipAddress="+ipAddress);
		//add by zhengguang.yang@20160505 end for add new feedback param
		
		configureTimeout();
		String baseUrl = YPushConfig.HTTP_HEAD + YPushConfig.API_USER_FEEDBACK_URL;
		
		Log.i(TAG,"imei="+imei+",title="+title+",description="+description+",email="+email);
		RequestBody body;
		if(mFilePath == null){
		body = new FormEncodingBuilder()
					.add(KEY_deviceId, imei)
					.add(KEY_feedbackPushType, title)
					.add(KEY_feedbackPushDescription, description)
					.add(KEY_feedbackPushEmail, email)
					.add(KEY_versionCode, versionCode)
					.add(KEY_romVersion, romVersion)
					.add(KEY_productModel, productModel)
					.add(KEY_aliRomVersion, aliRomVersion)
					.add(KEY_ipAddress, ipAddress)
					.build();
		}else{
		File file = new File(mFilePath);
		RequestBody fileBody = RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file);
		body = new MultipartBuilder()
					.type(MultipartBuilder.FORM)
					.addFormDataPart(KEY_deviceId, imei)
					.addFormDataPart(KEY_feedbackPushType, title)
					.addFormDataPart(KEY_feedbackPushDescription, description)
					.addFormDataPart(KEY_feedbackPushEmail, email)
					.addFormDataPart(KEY_versionCode, versionCode)
					.addFormDataPart(KEY_romVersion, romVersion)
					.addFormDataPart(KEY_productModel, productModel)
					.addFormDataPart(KEY_aliRomVersion, aliRomVersion)
					.addFormDataPart(KEY_ipAddress, ipAddress)
					.addFormDataPart(KEY_feedbackPushPicture, file.getName(), fileBody)
					.build();
		}
		
		Request request = new Request.Builder()
							.url(baseUrl)
							.post(body)
							.build();
		
		try {
		Response response = client.newCall(request).execute();
		if(response.isSuccessful()){
			Log.i(TAG,"result="+response.body().string());
			mHandler.sendEmptyMessage(0);
		}else{
			mHandler.sendEmptyMessage(1);
		}
		Thread.sleep(3000);
		mHandler.sendEmptyMessage(2);
		} catch (IOException e) {
			e.printStackTrace();
			mHandler.sendEmptyMessage(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
}

	private void configureTimeout() {
		if(client == null){
		client = new OkHttpClient();
		client.setConnectTimeout(10, TimeUnit.SECONDS);
		client.setWriteTimeout(20, TimeUnit.SECONDS);
		client.setReadTimeout(20, TimeUnit.SECONDS);
		}
	}

	private String guessMimeType(String path){
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor = fileNameMap.getContentTypeFor(path);
		if (contentTypeFor == null)
		{
			contentTypeFor = "application/octet-stream";
		}
		return contentTypeFor;
	}
	//add by zhengguang.yang 2015.12.01 end. for api23 use okhttp replace httpclient
}
