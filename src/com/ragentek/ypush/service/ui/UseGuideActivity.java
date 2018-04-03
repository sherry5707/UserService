package com.ragentek.ypush.service.ui;

import java.util.ArrayList;
import java.util.List;
import com.ragentek.ypush.service.R;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import android.os.SystemProperties;

public class UseGuideActivity extends BaseActivity {
//	private static final String MACHINE_SKILL = "玩机技巧";

	private final static String default_QCMODE = "n2";
	private final static String default_NETWORK_URL = "http://www.myuios.com/docs/manuals/go/n2/index.php";
	private final static String NETWORK_URL = "http://www.myuios.com/docs/manuals/[QCMODE]/index.php";

	private static final String COMMON_QUESTION_URL = "http://www.qingcheng.com/m/myui/csfaq.html";

	private List<String> list = new ArrayList<String>();
	private String networkMode;
	private String networkUrl;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.use_guide);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window Mywindow = this.getWindow();
			Mywindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			Mywindow.setStatusBarColor(0x10000000);
		}
		initViews();
		mActionBarTitle.setText(getTitle());
	}

	private void initViews() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		networkMode = this.getResources().getString(R.string.user_guide);
		networkUrl = getNetWorkURL();

		ListView lv = (ListView) findViewById(R.id.use_guide_lv);
		list.add(networkMode);
		list.add(this.getResources().getString(R.string.service_common_question));

		lv.setAdapter(new CustomArrayAdapter(this, R.layout.list_item, list));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String title = list.get(arg2);
				if (title.equals(networkMode)) {
					url = networkUrl;
				} else if (title.equals(UseGuideActivity.this.getResources().getString(R.string.service_common_question))) {
					url = COMMON_QUESTION_URL;
				}
				Intent intent = new Intent(UseGuideActivity.this,
						CommonWebView.class);
				intent.putExtra("title", title);
				intent.putExtra("url", url);
				startActivity(intent);
			}
		});

	}
	
	public class CustomArrayAdapter extends ArrayAdapter<String>{
		private int mLayoutId;
		private List<String> mList;
		private LayoutInflater mInflater;
		public CustomArrayAdapter(Context context, int layoutId,
				List<String> list) {
			super(context, layoutId, list);
			mInflater = LayoutInflater.from(context);
			mLayoutId = layoutId;
			mList = list;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = mInflater.inflate(mLayoutId, null);
			TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(mList.get(position));
			return view;
		}
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private String getNetWorkMode() {
		//modified by zhengguang.yang@20170328 start for use ro.product.model replace ro.product.model, ro.product.name = "C6";
		//String mode = SystemProperties.get("ro.product.model");
		//if (TextUtils.isEmpty(mode)||!mode.contains(" ")) {
		//	return default_QCMODE;
		//}
		//String[] result = mode.split(" ");
		//return result[1];
		
		String mode = SystemProperties.get("ro.product.name");
		if (TextUtils.isEmpty(mode)) {
			return default_QCMODE;
		}
		return mode;
		//end by zhengguang.yang

	}

	private String getNetWorkURL() {
		//modified by zhengguang.yang@20170328 start for use ro.product.model replace ro.product.model, ro.product.name = "C6";ro.product.brand = "go";
		//String mode = SystemProperties.get("ro.product.model");
		//if (TextUtils.isEmpty(mode)||!mode.contains(" ")) {
		//	return default_NETWORK_URL;
		//}
		//return NETWORK_URL.replace("[QCMODE]",
		//		mode.toLowerCase().replace(" ", "/"));
				
		String brand = 	SystemProperties.get("ro.product.brand");	
		String name = SystemProperties.get("ro.product.name");
		if (TextUtils.isEmpty(brand)||TextUtils.isEmpty(name)) {
			return default_NETWORK_URL;
		}
		String mode = brand+" "+name;
		return NETWORK_URL.replace("[QCMODE]",
				mode.toLowerCase().replace(" ", "/"));
				
		//end by zhengguang.yang
	}

}
