package com.ragentek.ypush.service.util;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ImageLoaderUtil {
	
	public static final String PIC_CACHE_DIR = Environment.getExternalStorageDirectory().getPath() + "/go_userservice/cache/";
	
	public static ImageLoader getInstance(Context context,int defaultImg){
		return init(context,defaultImg);
	}
	
	private static ImageLoader init(Context context,int defaultImg){
		ImageLoader imageLoader = ImageLoader.getInstance();
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showStubImage(defaultImg)
        .showImageOnFail(defaultImg)
        .bitmapConfig(Bitmap.Config.ARGB_8888)
        .cacheInMemory()
        .cacheOnDisc()
        .displayer(new RoundedBitmapDisplayer(12))
        .build();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
//        .discCacheExtraOptions(640, 960, CompressFormat.JPEG, 85)
        .threadPoolSize(4)
        .threadPriority(Thread.NORM_PRIORITY - 1)
        .denyCacheImageMultipleSizesInMemory()
        .discCache(new LimitedAgeDiscCache(new File(PIC_CACHE_DIR),7*24*3600*1000))
        .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
        .defaultDisplayImageOptions(options)
        .build();
		
		imageLoader.init(config);
		return imageLoader;
	}
	
}
