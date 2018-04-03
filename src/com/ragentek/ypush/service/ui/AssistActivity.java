package com.ragentek.ypush.service.ui;

import java.util.ArrayList;
import java.util.List;
import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.ui.fragment.FragmentQuestionFeedback;
import com.ragentek.ypush.service.ui.fragment.FragmentUserService;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AssistActivity extends FragmentActivity {
	public static final int DIALOG_USER_FEEDBACK_CONFIRM = 1001;
	protected boolean isChecked = false;
	
	private ViewPager mPager;
	private ArrayList<Fragment> fragmentsList;
	private Fragment mFragment01;
	private Fragment mFragment02;
	private TextView tvUserService;
	private ImageView ivUserService;
	private TextView tvQuestionFeedback;
	private ImageView ivQuestionFeedback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window Mywindow = this.getWindow();
			Mywindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			Mywindow.setStatusBarColor(0x10000000);
		}
		setStatusBarLightMode(this,true);
		initViews();
	}
	public boolean setStatusBarLightMode(Activity activity, boolean isFontColorDark) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (isFontColorDark) {
				//activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			} else {
				activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
			return true;
		}
		return false;
	}
	private void initViews() {
		initActionBar();
		initViewPager();
		showUserDialog();
	}

	private void initViewPager() {
		mPager = (ViewPager) findViewById(R.id.main_vp);
		fragmentsList = new ArrayList<Fragment>();

		mFragment01 = new FragmentUserService();
		mFragment02 = new FragmentQuestionFeedback();

		fragmentsList.add(mFragment02);
		fragmentsList.add(mFragment01);

		mPager.setAdapter(new MyFragmentPagerAdapter(
				getSupportFragmentManager(), fragmentsList));
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mPager.setCurrentItem(0);
	}

	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setCustomView(R.layout.main_tab_title);

		tvUserService = (TextView) actionBar.getCustomView().findViewById(
				R.id.tv_user_service);
		ivUserService = (ImageView) actionBar.getCustomView().findViewById(
				R.id.indicator2);
		tvQuestionFeedback = (TextView) actionBar.getCustomView().findViewById(
				R.id.tv_question_feedback);
		ivQuestionFeedback = (ImageView) actionBar.getCustomView()
				.findViewById(R.id.indicator1);

		tvQuestionFeedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setTabSelection(0);
				mPager.setCurrentItem(0);
			}
		});
		tvUserService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setTabSelection(1);
				mPager.setCurrentItem(1);
			}
		});

		setTabSelection(0);
	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		private ArrayList<Fragment> fragmentsList;

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public MyFragmentPagerAdapter(FragmentManager fm,
				ArrayList<Fragment> fragments) {
			super(fm);
			this.fragmentsList = fragments;
		}

		@Override
		public int getCount() {
			return fragmentsList.size();
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragmentsList.get(arg0);
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			setTabSelection(arg0);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	private void setTabSelection(int index) {
		resetStatus();
		switch (index) {
		case 0:
			tvQuestionFeedback.setTextColor(getResources().getColor(R.color.main_tab_text_color));
			ivQuestionFeedback.setVisibility(View.VISIBLE);
			ivUserService.setVisibility(View.INVISIBLE);
			break;
		case 1:
			tvUserService.setTextColor(getResources().getColor(R.color.main_tab_text_color));
			ivQuestionFeedback.setVisibility(View.INVISIBLE);
			ivUserService.setVisibility(View.VISIBLE);
			break;
		}
	}

	private void resetStatus() {
		tvUserService.setTextColor(getResources().getColor(R.color.main_tabtitle_normal2));
//		ivUserService.setBackgroundColor(getResources().getColor(
//				R.color.main_tabtitle_normal));

		tvQuestionFeedback.setTextColor(getResources().getColor(R.color.main_tabtitle_normal2));
//		ivQuestionFeedback.setBackgroundColor(getResources().getColor(
//				R.color.main_tabtitle_normal));
	}

	public void showUserDialog(){
		SharedPreferences pref = this.getSharedPreferences(getPackageName(), Context.MODE_WORLD_WRITEABLE);
		Boolean showFlag = pref.getBoolean("show", true);
		if(showFlag){
			showDialog(DIALOG_USER_FEEDBACK_CONFIRM);
		}
	}
	
	@Override
	@Deprecated
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
				.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setPositiveButton(R.string.use_as_well, new DialogInterface.OnClickListener() {
					
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
	///////////////////////////////////////////////
	public static final int TYPE_USER_SERVICE_AGREEMENT = 2001;
	public static final int TYPE_USER_SECRECY_AGREEMENT = 2002;
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
