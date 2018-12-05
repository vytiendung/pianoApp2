package com.vtd.pianoapp.ui;

import android.graphics.Bitmap;

import java.util.HashMap;

public class BitmapFromPlist {

	public final Bitmap image;
	public final HashMap<String, BitmapInfoProperty> properties;

	public BitmapFromPlist(Bitmap image, HashMap<String, BitmapInfoProperty> properties) {
		this.image = image;
		this.properties = properties;
	}

	public void cleanUp() {
		if (image != null && !image.isRecycled()) {
			image.recycle();
		}
	}
}
