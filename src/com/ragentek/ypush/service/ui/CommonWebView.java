package com.ragentek.ypush.service.ui;

import java.io.File;

import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.util.CommonUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.ZoomDensity;
import android.net.Uri;

public class CommonWebView extends BaseActivity {
    private WebView webView;
    private final static String CACHE_DIR = "QcWebView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_webview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window Mywindow = this.getWindow();
            Mywindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            Mywindow.setStatusBarColor(0x10000000);
        }
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");

        if (TextUtils.isEmpty(title)) {
            Log.e("CommonWebView", "actionbar gone");
            mActionBar.hide();
        } else {
            mActionBarTitle.setText(title);
        }

        webView = (WebView) findViewById(R.id.common_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);//显示放大缩小的control
        webSettings.setSupportZoom(true);//可以缩放
        webSettings.setDefaultZoom(ZoomDensity.CLOSE);//默认缩放模式
        webSettings.setUseWideViewPort(true);//为图片添加放大缩小功能
        webSettings.setJavaScriptEnabled(true);//使其支持javascript
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        webSettings.setAppCacheEnabled(true);//enable cache
        String path = getFilesDir().getAbsolutePath() + File.separator + CACHE_DIR;
        webSettings.setAppCachePath(path);
        //database in location
        webSettings.setDatabaseEnabled(true);
        webSettings.setDatabasePath(path);

        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLoadWithOverviewMode(true);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 16) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView,true);
        }
        webView.loadUrl(url);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView sview, String surl) {
				//add by lining , start
				if(surl == null) return false;
		        try {
		            if(surl.startsWith("tel:")//电话
		            ) {
		                Intent mmIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(surl));
		                startActivity(mmIntent);
		                return true;
		            }
		        } catch (Exception e) {
					e.printStackTrace();
					return true;
		        }
		        //处理http和https开头的url
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
		            cookieManager.setAcceptThirdPartyCookies(sview,true);
		        }
		        sview.loadUrl(surl);
		        return true;
				//add by lining , end
			}
			
		});
    }

    public class MyWebviewCient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            WebResourceResponse response = null;
            response = super.shouldInterceptRequest(view, url);
            return response;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

    }

}
