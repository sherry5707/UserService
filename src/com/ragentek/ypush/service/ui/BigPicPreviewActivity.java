package com.ragentek.ypush.service.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ragentek.ypush.service.R;
import com.ragentek.ypush.service.util.ImageLoaderUtil;


public class BigPicPreviewActivity extends Activity {
	private String[] picArray;
	private Gallery gallery;
	private GalleryAdapter adapter;
	private ImageLoader imageLoader;
	
	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.activity_big_pic_preview);
		imageLoader = ImageLoaderUtil.getInstance(getApplicationContext(),R.drawable.default_img_game);
		init();
	}
	
	private void init(){
		Bundle bundle = getIntent().getExtras();
		if(bundle!=null){
			picArray = bundle.getStringArray("picArray");
			if(picArray!=null && picArray.length>0){
				gallery = (Gallery)findViewById(R.id.gallery);
				adapter = new GalleryAdapter();
				gallery.setAdapter(adapter);
				gallery.setOnItemClickListener(new Gallery.OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						finish();
					}});
			}
		}
	}
	
	private class GalleryAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return picArray.length;
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
				convertView = getLayoutInflater().inflate(R.layout.include_big_pic_preview_view_item, null);
			}
			ImageView itemImage = (ImageView)convertView.findViewById(R.id.item_image);
			imageLoader.displayImage(picArray[position], itemImage);
			
			return convertView;
		}
	}
	
}  
