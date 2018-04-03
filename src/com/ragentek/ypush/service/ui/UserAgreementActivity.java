package com.ragentek.ypush.service.ui;

import com.ragentek.ypush.service.R;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class UserAgreementActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_agreement_text);
		initText();
		mActionBarTitle.setText(getTitle());
	}

	public void initText() {
		Intent intent = getIntent();
		int type = intent.getIntExtra("type", TYPE_USER_SERVICE_AGREEMENT);
		System.out.println("initText type is " + type);
		switch (type) {
		case TYPE_USER_SERVICE_AGREEMENT:
			updateWebView("file:///android_asset/user_service.html");
			break;

		case TYPE_USER_SECRECY_AGREEMENT:
			updateWebView("file:///android_asset/user_secrecy.html");
			break;
		default:
			break;
		}
	}
	
	public void updateWebView(String url){
		WebView webView = (WebView)findViewById(R.id.myui_user_agreement);
		webView = (WebView) findViewById(R.id.webview);
		webView = new WebView(this); 
		webView.getSettings().setJavaScriptEnabled(true); 
		webView.loadUrl(url); 
		setContentView(webView);
        webView.setWebViewClient(new MyWebViewClient());
	}
	
    //Web视图 
    private class MyWebViewClient extends WebViewClient { 
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
            view.loadUrl(url); 
            return true; 
        } 
    } 
}
