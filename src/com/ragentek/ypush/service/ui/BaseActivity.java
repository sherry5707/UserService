package com.ragentek.ypush.service.ui;

import com.ragentek.ypush.service.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class BaseActivity extends Activity{

	public static final int DIALOG_USER_FEEDBACK_CONFIRM = 1001;
	
	public static final int TYPE_USER_SERVICE_AGREEMENT = 2001;
	public static final int TYPE_USER_SECRECY_AGREEMENT = 2002;
	protected boolean isChecked = false;
	
	public ActionBar mActionBar = null;
	public TextView mActionBarTitle = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActionBar();
	}
	
	private void initActionBar() {
		mActionBar = getActionBar();
		mActionBar.setIcon(R.drawable.back);//设置整个系统的默认返回按钮
//		mActionBar.setHomeButtonEnabled(true);//使能Home按钮actionbar_title
//		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setHomeAsUpIndicator(R.drawable.back);
		View mCustomView = getLayoutInflater().inflate(R.layout.actionbar_layout, null);
		mActionBarTitle = (TextView) mCustomView.findViewById(R.id.actionbar_title);
		mActionBar.setCustomView(mCustomView, new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ActionBar.LayoutParams mP = (ActionBar.LayoutParams) mCustomView.getLayoutParams();
		mP.gravity = mP.gravity & ~Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.CENTER_HORIZONTAL;
		mActionBar.setCustomView(mCustomView, mP);		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home://按下返回按钮时，默认返回上一级Activity
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	@Override
	protected Dialog onCreateDialog(int dialogId) {
		if(dialogId == DIALOG_USER_FEEDBACK_CONFIRM){
			View view = getLayoutInflater().inflate(R.layout.dialog_user_feedback, null);
			TextView textView1 = (TextView)view.findViewById(R.id.user_service_agreement);
			TextView textView2 = (TextView)view.findViewById(R.id.user_secrecy_agreement);
			textView1.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
			textView2.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
			CheckBox checkBox = (CheckBox)view.findViewById(R.id.showCheck);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					isChecked = arg0.isChecked();
				}
			});
			Dialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.user_feedback_advice_title)
				.setView(view)
				.setNegativeButton(R.string.exit, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						finish();
					}
				})
				.setPositiveButton(R.string.use_as_well, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						dismissDialog(DIALOG_USER_FEEDBACK_CONFIRM);
						saveToPreference(!isChecked);
					}
				})
				.create();
			dialog.setCancelable(false);
			dialog.show();
			return dialog;
		}
		
		return null;
	}
	
	public void saveToPreference(Boolean show){
        SharedPreferences pref = this.getSharedPreferences(getPackageName(), Context.MODE_WORLD_WRITEABLE);
        Editor editor = pref.edit();
    	editor.putBoolean("show", show);
        editor.commit();
	}
	
	public void onUserServiceAgreementClick(View v){
		startUserAgreementActivity(TYPE_USER_SERVICE_AGREEMENT);
	}
	
	public void onUserSecrecyAgreementClick(View v){
		startUserAgreementActivity(TYPE_USER_SECRECY_AGREEMENT);
	}
	
	public void startUserAgreementActivity(int type){
		Intent intent = new Intent(this, UserAgreementActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("type", type);
		startActivity(intent);
	}
	
}
