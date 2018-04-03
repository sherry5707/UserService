package com.ragentek.ypush.service.ui.fragment;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.ui.UseGuideActivity;
import com.ragentek.ypush.service.ui.data.ActivityInfo;
import com.ragentek.ypush.service.ui.data.Response.BaseResponse;
import com.ragentek.ypush.service.ui.data.Response.ResponseMessage;
import com.ragentek.ypush.service.ui.view.ImageCycleView;
import com.ragentek.ypush.service.util.ACache;
import com.ragentek.ypush.service.util.ActivityUtil;
import com.ragentek.ypush.service.util.CommonUtils;
import com.ragentek.ypush.service.util.MD5;
import com.ragentek.ypush.service.widget.pulltorefresh.library.PullToRefreshBase;
import com.ragentek.ypush.service.widget.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.ragentek.ypush.service.widget.pulltorefresh.library.PullToRefreshListView;

import cz.msebera.android.httpclient.Header;

@SuppressWarnings({ "deprecation", "deprecation" })
public class FragmentUserService extends Fragment {
	private static final String TAG = "FragmentUserService";
	private static final String CUSTOMTER_MOBILE = "4008215000";
	private static final String CUSTOMER_SERVICE_URL = "http://www.qingcheng.com/m/myui/cspolicy.html";
	private static final String SERVICE_SITE_URL = "http://www.qingcheng.com/m/myui/cssites";
	private GridView mGridView;
	private FrameLayout userServiceNonet;
	private ImageView ivUserServiceNonet;
	private TextView tvUserServiceNonet;
	private ProgressBar progressBar;
	private ImageCycleView cycleView;
	private boolean isRun = false;
	
/*	private void onSuccessShow(ResponseMessage messageResponse){
		updateViewsData(messageResponse);
	}*/
	
	/*private void onFailShow(boolean isListRefresh){
		if(!isListRefresh){
			hideProgress();
		}
		showToast(R.string.load_fail);
	}*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_user_service, container,
				false);
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		isRun = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isRun = false;
//		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
		//getDataAndShow();
		cycleView.setDefaultData();
	}
	
/*	private void getDataAndShow() {
		ResponseMessage messageResponse = (ResponseMessage) read();
		Log.i(TAG,"onViewCreated-->messageResponse="+messageResponse);
		if (messageResponse == null) {
			mPullRefreshListView.setVisibility(View.GONE);
			if (CommonUtils.hasNetWork(getActivity())) {
				showProgress();
				initDatas();
			} else {
				hideProgress();
			}
		} else {
			updateViewsData(messageResponse);
			initDatas();
		}
	}*/

	private void initViews() {
		initNetFailView();
		initListView();
		initCommonViews();
		initImageCycleView();
	}

	private void initImageCycleView() {
		cycleView = (ImageCycleView) getActivity().findViewById(R.id.ad_view);
	}

	private void initNetFailView() {
		userServiceNonet = (FrameLayout) getActivity().findViewById(
				R.id.user_service_nonet);
		ivUserServiceNonet = (ImageView) getActivity().findViewById(
				R.id.iv_user_service_nonet);
		tvUserServiceNonet = (TextView)getActivity().findViewById(R.id.tv_user_service_nonet);
		progressBar = (ProgressBar)getActivity().findViewById(R.id.user_service_progressbar);
		ivUserServiceNonet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(CommonUtils.hasNetWork(getActivity())){
					showProgress();
					initDatas();
				}else{
					showToast(R.string.net_weak);
				}
			}
		});		
	}

	private void init() {
		//初始化cache
		mCache = ACache.get(getActivity());
		//初始化ImageLoader
		//configImageLoader();
		//初始化各视图
		initViews();
	}

	
	private void showProgress(){
		userServiceNonet.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		tvUserServiceNonet.setVisibility(View.GONE);
		ivUserServiceNonet.setVisibility(View.GONE);
	}
	
	private void hideProgress(){
		userServiceNonet.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		tvUserServiceNonet.setVisibility(View.VISIBLE);
		ivUserServiceNonet.setVisibility(View.VISIBLE);
	}

	private void showToast(int resId){
		Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
	}
/*	private void updateViewsData(ResponseMessage messageResponse) {
		userServiceNonet.setVisibility(View.GONE);
		mPullRefreshListView.setVisibility(View.VISIBLE);
		
		cycleView.setData(messageResponse.Ads);
		setListViewData(messageResponse.Activities);
	}*/


	private void setListViewData(ArrayList<ActivityInfo> activityInfos) {
		if(activityInfos != null){
			if(mActivityInfos == null){
				mActivityInfos = new ArrayList<ActivityInfo>();
				mActivityInfos.addAll(activityInfos);
			}else{
				if(mActivityInfos.equals(activityInfos)){
					return;
				}else{
					mActivityInfos.clear();
					mActivityInfos.addAll(activityInfos);
				}
			}	
			mCustomBaseAdapter.setData(mActivityInfos);
		}
	}

	//////////////////////////
	private int SOCKET_TIMEOUT = 2000;
	private void initDatas() {
		//requestData(false);
	}
	////////////////////////////////////////////
	private ACache mCache;
	private static final String KEY_NET_STRING = "KEY_NET_STRING";
	private static final int CACHE_SAVE_TIME = 60*60;//单位秒,默认一小时
	private void save(Serializable messageStr,int saveTime) {
		mCache.put(KEY_NET_STRING, messageStr,saveTime);
	}

	private Serializable read() {
		return (Serializable) mCache.getAsObject(KEY_NET_STRING);
	}

	///////////////////////////////////////////////////////
	private static final int COMMON_ITEM_COUNT = 4;
	public class CustomGridViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return COMMON_ITEM_COUNT;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getActivity().getLayoutInflater().inflate(
					R.layout.common_grid_item, null);
			ImageView itemIcon = (ImageView) convertView
					.findViewById(R.id.itemIcon);
			TextView itemTitle = (TextView) convertView
					.findViewById(R.id.itemTitle);
			itemIcon.setImageResource(R.drawable.common_item00 + position);
			itemTitle.setText(R.string.title_common_00 + position);
			return convertView;
		}

	}
	
	private static final int TITLE_COMMON_00 = 0;
	private static final int TITLE_COMMON_01 = 1;
	private static final int TITLE_COMMON_02 = 2;
	private static final int TITLE_COMMON_03 = 3;
	/**
	 * 使用指南，售后政策，服务网点，人工服务
	 */
	private void initCommonViews() {
		mGridView = (GridView) getActivity()
				.findViewById(R.id.common_grid_view);
		mGridView.setAdapter(new CustomGridViewAdapter());
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case TITLE_COMMON_00:
					Intent useGuideIntent = new Intent(getActivity(),
							UseGuideActivity.class);
					startActivity(useGuideIntent);
					break;
				case TITLE_COMMON_01:
					ActivityUtil.startWebActivity(getActivity(), getResources()
							.getString(R.string.title_common_01), CUSTOMER_SERVICE_URL);
					break;
				case TITLE_COMMON_02:
					ActivityUtil.startWebActivity(getActivity(), getResources()
							.getString(R.string.title_common_02), SERVICE_SITE_URL);
					break;
				case TITLE_COMMON_03:
					callToCustomer();
					break;
				}
			}
		});

	}

	/////////////////////////////////////
	static class ViewHolder {
		public ImageView img;
		public TextView title;
		public TextView content;
	}
	
	public class CustomBaseAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;
		private ArrayList<ActivityInfo> mActivityInfos;

		public CustomBaseAdapter(Context context,ArrayList<ActivityInfo> activityInfos) {
			super();
			this.mInflater = LayoutInflater.from(context);
			this.mActivityInfos = activityInfos;
		}
		
		public void setData(ArrayList<ActivityInfo> activityInfos){
			mActivityInfos = activityInfos;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if(mActivityInfos == null){
				return 0;
			}else{
				return mActivityInfos.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.img_list_item, null);
				viewHolder.img = (ImageView) convertView.findViewById(R.id.img);
				viewHolder.title = (TextView) convertView.findViewById(R.id.title);
				viewHolder.content = (TextView) convertView.findViewById(R.id.content);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			if(mActivityInfos != null){
				//ImageLoader.getInstance().displayImage((String)mActivityInfos.get(position).getThumbnailUrl(), viewHolder.img,getImageDisplayOptions(R.drawable.activity_default_icon));
				viewHolder.title.setText((String) mActivityInfos.get(position).getActivityTitle());
				viewHolder.content.setText((String) mActivityInfos.get(position).getActivityDesc());
			}
			return convertView;
		}

	}
	
	private DisplayImageOptions imageOptions;
	private DisplayImageOptions getImageDisplayOptions(int resId){
		if(imageOptions == null){
			imageOptions = new DisplayImageOptions.Builder()
			.showStubImage(resId) // 设置图片下载期间显示的图片
			.showImageForEmptyUri(resId) // 设置图片Uri为空或是错误的时候显示的图片
			.showImageOnFail(resId) // 设置图片加载或解码过程中发生错误显示的图片
			.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
			.cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
			.build(); // 创建配置过得DisplayImageOption对象
		}
		return imageOptions;
	}
	private PullToRefreshListView mPullRefreshListView;
	private CustomBaseAdapter mCustomBaseAdapter;
	private ArrayList<ActivityInfo> mActivityInfos;
	private void initListView() {
		mPullRefreshListView = (PullToRefreshListView) getActivity().findViewById(R.id.pull_refresh_list);
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				/*String label = DateUtils.formatDateTime(getActivity().getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);*/
				//requestData(true);
				mPullRefreshListView.onRefreshComplete();
			}
		});
		//获取listView
		ListView listView = mPullRefreshListView.getRefreshableView();
		mCustomBaseAdapter = new CustomBaseAdapter(getActivity(),mActivityInfos);
		listView.setAdapter(mCustomBaseAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Log.i(TAG,"onItemClick-->position="+position+",id="+id);
				if(id == -1){
					//点击的是headerview或者footerview
					return;
				}
				int realPosition = (int)id;
				if(mActivityInfos != null&&mActivityInfos.size() > 0){
					String title = (String) mActivityInfos.get(realPosition).getActivityTitle();
					String url = (String) mActivityInfos.get(realPosition).getActivityDetailUrl();
					ActivityUtil.startWebActivity(getActivity(), title, url);
				}
			}
		});
		//添加header
		LayoutInflater	mInflater = LayoutInflater.from(getActivity());
		View view = mInflater.inflate(R.layout.list_view_header, null);
		listView.addHeaderView(view);
			
	}
	/////////////////////////////////
/*	private static final String CS_ACTIVITY_BASE_URL = "http://www.qingcheng.com/api/myui.aspx?";
	private static final String CS_ACTIVITY_METHOD = "get_myui_activities";
	private static final String CS_ACTIVITY_TOKEN = "hm90GxlpKadyxu5jm89!XLiRP";
	private static final int REFRESH_LIST_DELAY = 500;
	private Gson gson = new Gson();
	private AsyncHttpClient client = null;
	private Handler mHandler = new Handler();
	private Runnable refreshRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(mPullRefreshListView != null){
				mPullRefreshListView.onRefreshComplete();
			}
		}
	};
	
	private void requestData(final boolean isListRefresh){
		if(client == null){
			client = new AsyncHttpClient();
			client.setTimeout(SOCKET_TIMEOUT);
		}
		String timestamp = CommonUtils.getCurrentTimeStamp();
		String token = MD5.md5(CS_ACTIVITY_METHOD + timestamp+ CS_ACTIVITY_TOKEN);
		RequestParams params = new RequestParams();
		params.put("method", CS_ACTIVITY_METHOD);
		params.put("timestamp", timestamp);
		params.put("token", token);
		Log.i(TAG,"requestData() start url="+CS_ACTIVITY_BASE_URL+params.toString());
		client.get(CS_ACTIVITY_BASE_URL, params, new AsyncHttpResponseHandler(){
			private BaseResponse baseResponse = null;
			private ResponseMessage messageResponse = null;
			//modified by zhengguang.yang 2015.11.20 start. for android6.0 make asynchttpclient use. 
//			@Override
//			public void onFailure(Throwable arg0, String arg1) {
//				super.onFailure(arg0, arg1);
//				Log.i(TAG,"onFailure-->arg0="+arg0+",arg1="+arg1);
//			}
//
//			@Override
//			public void onSuccess(String arg1) {
//				super.onSuccess(arg1);
//					Log.i(TAG,"onSuccess-->arg1="+arg1);
//					try{
//						if(isRun){
//							baseResponse = gson.fromJson(arg1, BaseResponse.class);
//							if(baseResponse != null&&baseResponse.isSuccess()){
//								messageResponse = gson.fromJson(URLDecoder.decode(baseResponse.message), ResponseMessage.class);
//							}
//						}
//					}catch(Throwable e){
//						e.printStackTrace();
//					}
//			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//				super.onFailure(arg0, arg1);
				Log.i(TAG,"onFailure-->arg0="+arg0+",arg1="+arg1+",arg2="+arg2+",arg3="+arg3);
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
//				super.onSuccess(arg1);
				if(arg2 != null){
					String result = new String(arg2);
					Log.i(TAG,"onSuccess-->result="+result);
					try{
						if(isRun){
							baseResponse = gson.fromJson(result, BaseResponse.class);
							if(baseResponse != null&&baseResponse.isSuccess()){
								messageResponse = gson.fromJson(URLDecoder.decode(baseResponse.message), ResponseMessage.class);
							}
						}
					}catch(Throwable e){
						e.printStackTrace();
					}
				}
			}
			//modified by zhengguang.yang 2015.11.20 end. for android6.0 make asynchttpclient use. 

			@Override
			public void onFinish() {
				super.onFinish();
				Log.i(TAG,"onFinish");
				try{
					if(isRun){
						if(messageResponse == null){
							onFailShow(isListRefresh);
						}else{
							messageResponse.sort();
							save(messageResponse, CACHE_SAVE_TIME);
							onSuccessShow(messageResponse);
						}
					}
				}catch(Throwable e){
					e.printStackTrace();
					onFailShow(isListRefresh);
				}finally{
					if(isListRefresh){
						mHandler.postDelayed(refreshRunnable,REFRESH_LIST_DELAY);
					}
				}
			}
		});
	}
	///////////////////////////////////////////////////
	*//**
	 * 配置ImageLoder
	 *//*
	private void configImageLoader() {
		// 初始化ImageLoader
		@SuppressWarnings("deprecation")
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.ads_default_icon) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.ads_default_icon) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.ads_default_icon) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
				.build(); // 创建配置过得DisplayImageOption对象

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getActivity().getApplicationContext())
				.defaultDisplayImageOptions(options)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		ImageLoader.getInstance().init(config);
	}
	*/
	private void callToCustomer() {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ CUSTOMTER_MOBILE));
		this.startActivity(intent);
	}

}
