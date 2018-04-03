package com.ragentek.ypush.service.ui.fragment;

import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.db.MyDatabaseUtil;
import com.ragentek.ypush.service.ui.UserFeedback;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentQuestionFeedback extends Fragment{
	private static final String TAG = "YPushService.AssistActivity";
	MyDatabaseUtil myDBUtil;
	private int clickTmp=-1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_question_feedback, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		getDisplayParameters();
		
//		showUserDialog();
		
		initViews();

        ((Activity) getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        Log.d(TAG, "onCreate exit");
		
	}
private GridView mGridView;
private GridAdapter mAdapter;
	
	/** add by @author xiaolin.he . here use grid view instead of 12 views*/
	private void initViews() {
		mGridView = (GridView) getActivity().findViewById(R.id.grid_view);
		mAdapter=new GridAdapter();
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.e(TAG,"position:"+position);
				mAdapter.setSelection(position);
				mAdapter.notifyDataSetChanged();
				startUserRequestActivity(getResources().getString(R.string.title_00_bug_call + position));
			}
		});
	}
	
	private class GridAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return 8;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getActivity().getLayoutInflater().inflate(R.layout.assist_activity_lollipop_grid_item, null);
			LinearLayout item_layout=(LinearLayout)convertView.findViewById(R.id.item_layout);
			/*if(clickTmp==position){
				item_layout.setBackgroundResource(R.drawable.item_bg_click);
			}else {
				item_layout.setBackgroundResource(R.drawable.item_bg);
			}*/
			ImageView itemIcon = (ImageView) convertView.findViewById(R.id.itemIcon);
			TextView itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
			itemIcon.setImageResource(R.drawable.item00 + position);
			itemTitle.setText(R.string.title_00_bug_call + position);
			return convertView;
		}
		//add by sherry for item background selector
		public void setSelection(int position){
			clickTmp=position;
		}
	}

	public void showUserDialog(){
		SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_WORLD_WRITEABLE);
		Boolean showFlag = pref.getBoolean("show", true);
		if(showFlag){
//			showDialog(DIALOG_USER_FEEDBACK_CONFIRM);
		}
	}
	
	private void getDisplayParameters(){
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int resoWidth = dm.widthPixels;
		int resoHeight = dm.heightPixels;
		Log.d(TAG, "resoWidth=" + resoWidth +" ,resoHeight=" + resoHeight);
		float curDensity = dm.density;
		int curDensityDpi = dm.densityDpi;
		Log.d(TAG, "curDensity=" + curDensity +" ,curDensityDpi=" + curDensityDpi);
		
		double diagonalPixels = Math.sqrt(Math.pow(resoWidth, 2) + Math.pow(resoHeight, 2));
		double screenSize = diagonalPixels/(160*curDensity);
		Log.d(TAG, "diagonalPixels=" + diagonalPixels +" ,screenSize=" + screenSize);
		
		WindowManager manager = getActivity().getWindowManager();
		int srcWidth = manager.getDefaultDisplay().getWidth();
		int srcHeight = manager.getDefaultDisplay().getHeight();
		Log.d(TAG, "srcWidth=" + srcWidth +" ,srcHeight=" + srcHeight);
		
		DisplayMetrics dm2 = new DisplayMetrics();
		dm2 = getActivity().getApplicationContext().getResources().getDisplayMetrics();
		int conresoWidth = dm2.widthPixels;
		int conresoHeight = dm2.heightPixels;
		Log.d(TAG, "conresoWidth=" + conresoWidth +" ,conresoHeight=" + conresoHeight);

		myDBUtil = new MyDatabaseUtil(getActivity().getBaseContext());

		if ( !("1".equals(myDBUtil.myDBQuery("haveResolution"))) ) {
			Log.v(TAG, "to update resolution to DB!");
			
			myDBUtil.myDBUpdate("haveResolution", "1");
			myDBUtil.myDBUpdate("resoWidth", resoWidth+"");
			myDBUtil.myDBUpdate("resoHeight", resoHeight+"");
		}
		
		if ( "1".equals(myDBUtil.myDBQuery("haveResolution")) ) {
			String w3 = myDBUtil.myDBQuery("resoWidth");
			String h3 = myDBUtil.myDBQuery("resoHeight");
			Log.d(TAG, "DB 2 w3=" + w3 +" h3=" + h3);
		}
	}
	
	private void startUserRequestActivity(String title){
		Intent intent = new Intent(getActivity(), UserFeedback.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("title", title);
		this.startActivity(intent);
	}
	
	
	

}
