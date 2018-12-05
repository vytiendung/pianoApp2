package com.vtd.pianoapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.vtd.pianoapp.R;
import com.vtd.pianoapp.SoundManager;
import com.vtd.pianoapp.common.Constant;
import com.vtd.pianoapp.common.UserConfig;

public class StartupActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.startup_activity_layout);
		UserConfig.getInstance().configWindowSize(StartupActivity.this);
		UserConfig.getInstance().initConfig(StartupActivity.this);
		SoundManager.getInstance().loadDefaultSound(0, Constant.PIANO_INTRUMENT_ID, new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(StartupActivity.this, GamePlayActivity.class));
				finish();
			}
		});

	}
}
