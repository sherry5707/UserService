
package com.ragentek.ypush.service;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.provider.Settings;

import com.ragentek.ypush.service.util.CommonUtils;
import com.ragentek.ypush.service.ui.EditDialog;
import com.ragentek.ypush.service.ui.NumberPickerDialog;

import java.util.Calendar;
//add by zhengguang.yang@20170914 start for add query active status
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import android.text.TextUtils;
import com.ragentek.ypush.service.util.SIMCardInfo;
import android.os.Build;
//end by zhengguang.yang

public class MainActivity extends PreferenceActivity {
    private static final String TAG = "MainActivity";
    private Preference mFirstUploadTimeLimitPref;
    private Preference mBusinessServerSetting;
    private Preference mDataUploadStatusPref;
    private Preference mClearDataUploadStatusPref;
    private Preference mQueryActiveStatusPref;//add by zhengguang.yang@20170914 for add query active status
    private int mTimeSettingCounter = 120;//240;//modified by zhengguang.yang
    private static final int MAX_TIME_SETTING_COUNTER = 240;
    private static final int MIN_TIME_SETTING_COUNTER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_setting);
        initPrefs();
    }

    private void initPrefs() {
        mFirstUploadTimeLimitPref = findPreference("pref_key_reg_once_limit");
        mBusinessServerSetting = findPreference("pref_key_set_business_server");
        mDataUploadStatusPref = findPreference("pref_key_reg_once_status");
        mClearDataUploadStatusPref = findPreference("pref_key_clear_reg_once_status");
	//add by zhengguang.yang@20170914 start for add query active status
	 mQueryActiveStatusPref = findPreference("pref_key_query_active_status");
	//end by zhengguang.yang

        setRegOnceStatus();
        boolean isBusinessServer = getBusinessServerSetting(this);
        ((CheckBoxPreference) mBusinessServerSetting).setChecked(isBusinessServer);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mFirstUploadTimeLimitPref) {
            // mRegOnceCounter = mConfigData.getRegOnceLimit();
			//modified by zhengguang.yang@20160517 start for active
//            new NumberPickerDialog(this,
//                    mTimeSettingListener,
//                    mTimeSettingCounter, // current counter
//                    MIN_TIME_SETTING_COUNTER, // min limit
//                    MAX_TIME_SETTING_COUNTER, // max limit
//                    R.string.pref_title_reg_once_limit).show();
        	mTimeSettingCounter = CommonUtils.getDataUploadTime(getApplicationContext());
        	new EditDialog(this,
                    mTimeSettingListener,
                    mTimeSettingCounter, // current counter
                    R.string.pref_title_reg_once_limit).show();
			//modified by zhengguang.yang@20160517 end for active
        } else if (preference == mBusinessServerSetting) {
            boolean checkStatus = ((CheckBoxPreference) mBusinessServerSetting).isChecked();
            Log.i(TAG, "onPreferenceTreeClick checkStatus= " + checkStatus);
            setAsBusinessServerSetting(this, checkStatus);
        } else if (preference == mClearDataUploadStatusPref) {
            CommonUtils.clearIntervalUploadStatus(getApplicationContext());
            //add by zhengguang.yang@20160517 start for active
            CommonUtils.clearAddTime(getApplicationContext());
            //add by zhengguang.yang@20160517 end for active
            mClearDataUploadStatusPref.setSummary(R.string.reg_once_flag_cleared);
            mClearDataUploadStatusPref.setEnabled(false);
            mDataUploadStatusPref.setSummary(R.string.unregistered);
        }//add by zhengguagn.yang@20170914 start for add query active status
        else if(preference == mQueryActiveStatusPref){
			QueryActiveStatusTask queryTask = new QueryActiveStatusTask(MainActivity.this);
			queryTask.execute();
	}
        //end by zhengguang.yang
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
	//add by zhengguang.yang@20170914 start for add query active status
	private static final int DEFAULT_SOCKET_TIME = 90*1000;//90s
	private static final String QUERY_ACTIVE_STATUS_URL_ = "https://www.kindui.com/admin/devicePushLog/checkActivatedState";
	private static final String QUERY_ACTIVE_STATUS_URL_TEST = "http://test.apiv1.kindui.com/admin/devicePushLog/checkActivatedState";
	private static final String KEY_IMEI = "imei";
	private static final String KEY_MODEL = "productModel";
	public static String getQueryActiveStatusServiceUrl(){
		boolean isBusiServer = CommonUtils.getBusinessServerSetting(MyApplication.getInstance());
		return isBusiServer?QUERY_ACTIVE_STATUS_URL_:QUERY_ACTIVE_STATUS_URL_TEST;
    }
	
	class QueryActiveStatusTask extends AsyncTask<String, Integer, String> {
		ProgressDialog progressDialog;
		OkHttpClient client;
		public QueryActiveStatusTask(Context context) {
			progressDialog = new ProgressDialog(context, 0);
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		@Override
		protected void onPreExecute() {
			if (progressDialog != null) {
				progressDialog.show();
			}
		}

		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground");
			String result = null;
			if(client == null){
				client = new OkHttpClient();
				client.setReadTimeout(90, TimeUnit.SECONDS);
			}
			String serviceUrl = getQueryActiveStatusServiceUrl();
			String imei = getDeviceId(getApplicationContext());
			String model = getProductModel();
			Log.i(TAG,"queryActiveStatus-->serviceUrl="+serviceUrl+",imei="+imei+",model="+model);
			
			RequestBody body = new FormEncodingBuilder()
									.add(KEY_IMEI, imei)
									.add(KEY_MODEL, model).build();
			
			Request request = new Request.Builder().url(serviceUrl).post(body).build();
			try {
				Response response = client.newCall(request).execute();
				if (response.isSuccessful()) {
					result = response.body().string();
					Log.i(TAG, "result=" + result);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			if(TextUtils.isEmpty(result)){
				mQueryActiveStatusPref.setSummary("no active");	
			}else{
				mQueryActiveStatusPref.setSummary(result);	
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			
		}
	}

	private String getDeviceId(Context c){
		SIMCardInfo si = new SIMCardInfo(c);
		String imei = si.getIMEI();
		if(imei == null){
			imei = "";
		}
		return imei;
	}
	public static String getProductModel() {
		String productName = "";
		productName = Build.MODEL;
		return productName.trim();
	}
	//end by zhengguang.yang



    /*
     * isBusinessServer, true is set as business server, false test server
     */
    public void setAsBusinessServerSetting(Context context, boolean isBusinessServer) {
        Log.i(TAG, "setAsBusinessServerSetting isBusinessServer=" + isBusinessServer);
        boolean setResult = Settings.System
                .putString(
                        context.getContentResolver(),
                        /* Settings.System.QINGCHENG_BUSINESS_SERVER_CONFIG */"set_qingcheng_server_config",
                        String.valueOf(isBusinessServer));
        Log.i(TAG, "setAsBusinessServerSetting setResult=" + setResult);
    }

    /**
     * return true: business server, false: test server
     */
    public boolean getBusinessServerSetting(Context context) {
        String isBusinessServer = Settings.System
                .getString(
                        context.getContentResolver(),
                        /* Settings.System.QINGCHENG_BUSINESS_SERVER_CONFIG */"set_qingcheng_server_config");
        boolean result = (Boolean.parseBoolean(isBusinessServer) || (isBusinessServer == null));
        Log.i(TAG, "getBusinessServerSetting isBusinessServer=" +
                isBusinessServer + ",result=" + result);
        return result;
    }

    private void setRegOnceStatus() {
        boolean registered = CommonUtils.isRegOnceSuccess(getApplicationContext());
        Log.i(TAG, "setRegOnceStatus registered= " + registered);
        mDataUploadStatusPref.setTitle(R.string.pref_title_reg_once_status);

        String uploadStatusSummary;
        if (registered) {
            mClearDataUploadStatusPref.setEnabled(true);
            ((CheckBoxPreference) mClearDataUploadStatusPref).setChecked(false);
            mClearDataUploadStatusPref.setSummary(R.string.clear_reg_once_flag_summary);
            uploadStatusSummary = getResources().getString(R.string.registered_success,
                    CommonUtils.getUploadedIntervalDate(getApplicationContext()));
        } else {
            mClearDataUploadStatusPref.setEnabled(false);
            mClearDataUploadStatusPref.setSummary(R.string.clear_reg_once_flag_summary);
            uploadStatusSummary = getResources().getString(R.string.registered_fail);
        }

        mDataUploadStatusPref.setSummary(uploadStatusSummary);
    }

    //modified by zhengguang.yang@20160517 start for active
    /*private NumberPickerDialog.OnNumberSetListener mTimeSettingListener =
            new NumberPickerDialog.OnNumberSetListener() {
                public void onNumberSet(int limit) {
                    Log.i(TAG, "mRegOnceLimitListener -- limit =  " + limit);
                    if (mTimeSettingCounter != limit) {
                        mTimeSettingCounter = limit;
                        CommonUtils.setDataUploadTime(getApplicationContext(), limit);
                    }
                }
            };*/
    private EditDialog.OnNumberSetListener mTimeSettingListener =
            new EditDialog.OnNumberSetListener() {
                public void onNumberSet(int limit) {
                    Log.i(TAG, "mRegOnceLimitListener -- limit =  " + limit);
                    if (mTimeSettingCounter != limit) {
                        mTimeSettingCounter = limit;
                        CommonUtils.setDataUploadTime(getApplicationContext(), limit);
                    }
                }
            };
     //modified by zhengguang.yang@20160517 end for active
}
