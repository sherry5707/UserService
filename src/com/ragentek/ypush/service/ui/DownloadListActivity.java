package com.ragentek.ypush.service.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.download.AppDownloadUtil;
import com.ragentek.ypush.service.network.HttpClient;
import com.ragentek.ypush.service.util.AppData;
import com.ragentek.ypush.service.util.ImageLoaderUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadListActivity extends Activity {
	private ViewPager viewPager;
	private List<View> pagerViewList = new ArrayList<View>();
	private List<AppData> results = new ArrayList<AppData>();
	private DownloadedGamesAdapter adapter;
	private ImageLoader imageLoader;

	
	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.activity_download_center);
        imageLoader = ImageLoaderUtil.getInstance(getApplicationContext(),R.drawable.default_img_game);

		init();
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
	
	private void init(){
		
		viewPager = (ViewPager)findViewById(R.id.viewpager);
		
		pagerViewList.add(initSubPage(getLayoutInflater().inflate(R.layout.include_download_center_sub_page_view, null)));
		
		viewPager.setAdapter(new ViewPagerAdapter());

		findViewById(R.id.progressBar).setVisibility(View.GONE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		results.clear();
		results.addAll(getDownloadedGames());
		adapter.notifyDataSetChanged();
	}
	
	private View initSubPage(View view){
		ListView listView = (ListView)view.findViewById(R.id.listview);
		results.addAll(getDownloadedGames());
		adapter = new DownloadedGamesAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
//				Intent intent = new Intent(getApplicationContext(),GameDetailActivity.class);
//				intent.putExtra("id", results2.get(arg2).get_id());
//				intent.putExtra("sourceFlag", results2.get(arg2).getSourceFlag());
//				startActivity(intent);
			}});
		listView.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				// TODO Auto-generated method stub
				//modified by zhengguang.yang@20160302 for dialog content can't show
//				new AlertDialog.Builder(DownloadListActivity.this)
				new AlertDialog.Builder(DownloadListActivity.this,R.style.myAlertDialogText)
				.setTitle(R.string.push_download_warning)
				.setMessage(R.string.push_download_confirm_delete)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(results.get(arg2).getDownloadStatus()!=AppDownloadUtil.STATUS_DOWLOADING){
							AppDownloadUtil.deleteMyAppInDb(getApplicationContext(), results.get(arg2).getDownloadUrl());
							File dlFile = new File(results.get(arg2).getApkFilePath().replace("tmp", "apk"));
//							System.out.println("-----dlFile----"+dlFile.getPath());
							if(dlFile.exists()){
								dlFile.delete();
							}
							results.clear();
							results.addAll(getDownloadedGames());
							adapter.notifyDataSetChanged();
						}else{
							Toast.makeText(getApplicationContext(), R.string.push_download_pause_first, Toast.LENGTH_SHORT).show();
						}
					}
				}).show();
				return false;
			}});
		return view;
	}
	
	private List<AppData> getDownloadedGames(){
		List<AppData> apps = new ArrayList<AppData>();
		List<AppData> tmpList = AppDownloadUtil.listDBApps(getApplicationContext(),-1);
		for (AppData app : tmpList) {
			if(!app.isNeedUpdated()){
				apps.add(app);
			}
		}
		return apps;
	}
	
	private class DownloadedGamesAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return results.size();
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
			if(convertView==null){
				convertView = getLayoutInflater().inflate(R.layout.include_download_center_list_view, null);
			}
			ImageView itemImage = (ImageView)convertView.findViewById(R.id.item_image);
			if(!HttpClient.isNetSpeedSlow){
				imageLoader.displayImage(results.get(position).getLogoUrl(), itemImage);
			}
			TextView appName = (TextView)convertView.findViewById(R.id.app_name);
			appName.setText(results.get(position).getName());
			TextView tipText = (TextView)convertView.findViewById(R.id.app_download_status_tips);
			if(results.get(position).getDownloadStatus()==AppDownloadUtil.STATUS_COMPLETED){
				tipText.setText("下载完成，长按删除记录");
			}else if(results.get(position).getDownloadStatus()==AppDownloadUtil.STATUS_INSTALLED){
				tipText.setText("已安装，长按删除记录");
			}else{
				tipText.setText("下载中，长按删除记录");
			}
			TextView statusText = (TextView)convertView.findViewById(R.id.download_status_text);
			Button downloadBtn = (Button)convertView.findViewById(R.id.btn_download);
			AppDownloadUtil.createDownloadListner(getApplicationContext(), downloadBtn,statusText, results.get(position),1, false);
			
			return convertView;
		}
	}
	
	private class ViewPagerAdapter extends PagerAdapter {
		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager)container).removeView(pagerViewList.get(position));
		}

		@Override
		public int getCount() {
			return pagerViewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (View)arg1;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager)container).addView(pagerViewList.get(position), 0);
			return pagerViewList.get(position);
		}
	}
	
}  