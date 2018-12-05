package com.vtd.pianoapp;

import android.app.Application;

public class MyApplication extends Application {
	private static MyApplication instance;
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
	}
	public static synchronized MyApplication getInstance() {
		return instance;
	}
}
