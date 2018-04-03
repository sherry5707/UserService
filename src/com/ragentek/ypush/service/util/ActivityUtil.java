package com.ragentek.ypush.service.util;

import android.content.Context;
import android.content.Intent;
import android.webkit.URLUtil;

import com.ragentek.ypush.service.ui.CommonWebView;

public class ActivityUtil {
	private static final String KEY_TITLE = "title";
	private static final String KEY_URL = "url";
	
	public static void startWebActivity(Context context,String title,String url){
		if(URLUtil.isNetworkUrl(url)){
			Intent intent = new Intent(context,
					CommonWebView.class);
			intent.putExtra(KEY_TITLE, title);
			intent.putExtra(KEY_URL, url);
			context.startActivity(intent);
		}
	}
}
