package com.ragentek.ypush.service.ui.view;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.ui.data.AdsInfo;
import com.ragentek.ypush.service.util.ActivityUtil;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageCycleView extends LinearLayout{
	/**
	 * 上下文
	 */
	private Context mContext;
	
	/**
	 * 图片轮播视图
	 */
	private ViewPager mAdvPager = null;

	/**
	 * 滚动图片视图适配器
	 */
	private ImageCycleAdapter mAdvAdapter;

	/**
	 * 图片轮播指示器控件
	 */
	private ViewGroup mGroup;
	
	/**
	 * 图片轮播指示器-个图
	 */
	private ImageView mImageView = null;
	
	/**
	 * 滚动图片指示器-视图列表
	 */
	private ImageView[] mImageViews = null;
	
	/**
	 * 图片滚动当前图片下标
	 */
	private int mImageIndex = 0;
	/**
	 * 广告数据列表，是外部的拷贝
	 */
	private ArrayList<AdsInfo> mAdsInfos;
	
	/**
	 * 视图是否可见
	 */
	private boolean isShow = false;
	
	/**
	 * 图片轮播间隔
	 */
	private static final int IMAGE_CYCLE_DELAY = 3000;

	private String defaultImageUrl="http://www.qingcheng.com";
	public ImageCycleView(Context context) {
		super(context);
	}

	public ImageCycleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.ad_cycle_view, this);
		mAdvPager = (ViewPager)findViewById(R.id.adv_pager);
		mAdvPager.setOnPageChangeListener(new GuidePageChangeListener());
		mAdvPager.setAdapter(mAdvAdapter = new ImageCycleAdapter(mContext, mAdsInfos));
		mGroup = (ViewGroup)findViewById(R.id.viewGroup);
	}
	
	

	/**
	 * 装填数据
	 * @param adsInfos
	 */
	public void setData(ArrayList<AdsInfo> adsInfos) {
		if(adsInfos != null){
			if(mAdsInfos == null){
				mAdsInfos = new ArrayList<AdsInfo>();
				mAdsInfos.addAll(adsInfos);
			}else{
				if(mAdsInfos.equals(adsInfos)){
					return;
				}else{
					mAdsInfos.clear();
					mAdsInfos.addAll(adsInfos);
				}
			}
			
			updateAllView();
		}
	}
	
	public void setDefaultData(){
		mImageView = new ImageView(mContext);
		mImageView.setImageResource(R.drawable.qc_homeurl);
		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityUtil.startWebActivity(mContext,"",defaultImageUrl);
			}
		});
		mGroup.addView(mImageView);
	}

	private void updateAllView() {
		// 初始化指示器
		mGroup.removeAllViews();
		int imageCount = mAdsInfos.size();
		mImageViews = new ImageView[imageCount];
		for (int i = 0; i < imageCount; i++) {
			mImageView = new ImageView(mContext);
			mImageViews[i] = mImageView;
			if (i == 0) {
				mImageViews[i].setBackgroundResource(R.drawable.dot_focus);
			} else {
				mImageViews[i].setBackgroundResource(R.drawable.dot_normal);
			}
			mGroup.addView(mImageViews[i]);
		}
		
		//初始化轮播图片
		mAdvAdapter.setData(mAdsInfos);
		
		//开始轮播循环
		startImageTimerTask();
	}



	/**
	 * 轮播图片状态监听器
	 * @author zhengguang.yang
	 *
	 */
	private class GuidePageChangeListener implements OnPageChangeListener {
		boolean isScrolled = false;
		private static final int PAGER_MOVE = 1;
		private static final int PAGER_SWITCH = 2;
		private static final int PAGER_MOVE_OVER = 0;

		public void onPageScrollStateChanged(int arg0) {
			switch (arg0) {
			case PAGER_MOVE:// 手势滑动
				isScrolled = false;
				break;
			case PAGER_SWITCH:// 界面切换
				isScrolled = true;
				break;
			case PAGER_MOVE_OVER:// 滑动结束
				// 当前为最后一张，此时从右向左滑，则切换到第一张
				if (mAdvPager.getCurrentItem() == mAdvPager.getAdapter()
						.getCount() - 1 && !isScrolled) {
					mAdvPager.setCurrentItem(0);
				}
				// 当前为第一张，此时从左向右滑，则切换到最后一张
				else if (mAdvPager.getCurrentItem() == 0 && !isScrolled) {
					mAdvPager.setCurrentItem(mAdvPager.getAdapter().getCount() - 1);
				}
				break;
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int index) {
			// 设置当前显示的图片下标
			mImageIndex = index;
			// 设置图片滚动指示器背景
			mImageViews[index].setBackgroundResource(R.drawable.dot_focus);
			for (int i = 0; i < mImageViews.length; i++) {
				if (index != i) {
					mImageViews[i].setBackgroundResource(R.drawable.dot_normal);
				}
			}
		}

	}
	/**
	 * 轮播图片适配器
	 * @author zhengguang.yang
	 *
	 */
	private class ImageCycleAdapter extends PagerAdapter {

		/**
		 * 图片视图缓存列表
		 */
		private ArrayList<ImageView> mImageViewCacheList;

		/**
		 * 图片资源列表
		 */
		private ArrayList<AdsInfo> mAdList = new ArrayList<AdsInfo>();

		private Context mContext;

		public ImageCycleAdapter(Context context, ArrayList<AdsInfo> adList) {
			mContext = context;
			mAdList = adList;
			mImageViewCacheList = new ArrayList<ImageView>();
		}

		
		public void setData(ArrayList<AdsInfo> adsInfos){
			mAdList = adsInfos;
			if(adsInfos != null){
				notifyDataSetChanged();
			}
		}
		
		
		
		@Override
		public int getItemPosition(Object object) {
			//当下拉列表刷新数据时，图片个数为0，能实时刷新，不会残留
			return POSITION_NONE;
		}


		@Override
		public int getCount() {
			if(mAdList == null){
				return 0;
			}else{
				return mAdList.size();
			}
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			if(mAdList != null&&mAdList.size() > 0){
				String imageUrl = mAdList.get(position).getBannerImgUrl();
				ImageView imageView = null;
				if (mImageViewCacheList.isEmpty()) {
					imageView = new ImageView(mContext);
					imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				} else {
					imageView = mImageViewCacheList.remove(0);
				}
				// 设置图片点击监听
				imageView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(mAdList != null){
							String title = mAdList.get(position).getAdTitle();
							String url = (String) mAdList.get(position).getAdDetailUrl();
							ActivityUtil.startWebActivity(mContext,title,url);
						}else {
							ActivityUtil.startWebActivity(mContext,"",defaultImageUrl);
						}
					}
				});
				container.addView(imageView);
				imageView.setImageResource(R.drawable.qc_homeurl);
				//ImageLoader.getInstance().displayImage(imageUrl, imageView);
				return imageView;
			}else{
				return null;
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			ImageView view = (ImageView) object;
			container.removeView(view);
			mImageViewCacheList.add(view);
		}

	}
	

	/**
	 * 开始图片滚动任务
	 */
	private void startImageTimerTask() {
		stopImageTimerTask();
		// 图片每3秒滚动一次
		mHandler.postDelayed(mImageTimerTask, IMAGE_CYCLE_DELAY);
	}

	/**
	 * 停止图片滚动任务
	 */
	private void stopImageTimerTask() {
		mHandler.removeCallbacksAndMessages(null);
	}

	private Handler mHandler = new Handler();

	/**
	 * 图片自动轮播Task
	 */
	private Runnable mImageTimerTask = new Runnable() {

		@Override
		public void run() {
			if (mImageViews != null&&mImageViews.length > 1) {
				// 下标等于图片列表长度说明已滚动到最后一张图片,重置下标
				if ((++mImageIndex) >= mImageViews.length) {
					mImageIndex = 0;
				}
				mAdvPager.setCurrentItem(mImageIndex);
				if(isShow){
					startImageTimerTask();
				}else{
					stopImageTimerTask();
				}
			}
		}
	};
	
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		isShow = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		isShow = false;
		stopImageTimerTask();
	}
	
}
