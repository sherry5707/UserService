package com.ragentek.ypush.service.util;

import android.graphics.drawable.Drawable;


public class MainItemParam {
	private String name;
	private boolean isDefault;
	private int itemIdx;
	private int imageResource;
	private Drawable imageDrawable;
	
	public int getImageResource() {
		return imageResource;
	}
	public void setImageResource(int imageResource) {
		this.imageResource = imageResource;
	}
	public Drawable getImageDrawable() {
		return imageDrawable;
	}
	public void setImageDrawable(Drawable imageDrawable) {
		this.imageDrawable = imageDrawable;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isDefault() {
		return isDefault;
	}
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	public int getItemIdx() {
		return itemIdx;
	}
	public void setItemIdx(int itemIdx) {
		this.itemIdx = itemIdx;
	}
}
