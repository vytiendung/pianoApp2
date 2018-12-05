package com.vtd.pianoapp.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.view.Display;
import com.vtd.pianoapp.MyApplication;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;

import java.util.Arrays;

import static com.vtd.pianoapp.common.Constant.*;


public class UserConfig {

	private static final String TAG = "UserConfig";
	private static UserConfig instance;
	private Config config;
	private Context context;

	public static UserConfig getInstance() {
		if (instance == null) {
			instance = new UserConfig(MyApplication.getInstance());
		}
		return instance;
	}

	private UserConfig(Context context) {
		this.context = context;
		config = Config.getInstance();
	}

	public void initConfig(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			config.appVersionCode = info.versionCode;

			SettingsManager settingsManager = SettingsManager.getInstance();


			config.noteList = Arrays.asList("a0", "a0m", "b0", "c1", "c1m", "d1", "d1m", "e1", "f1", "f1m", "g1", "g1m", "a1", "a1m", "b1", "c2",
					"c2m", "d2", "d2m", "e2", "f2", "f2m", "g2", "g2m", "a2", "a2m", "b2", "c3", "c3m", "d3", "d3m", "e3", "f3", "f3m", "g3", "g3m",
					"a3", "a3m", "b3", "c4", "c4m", "d4", "d4m", "e4", "f4", "f4m", "g4", "g4m", "a4", "a4m", "b4", "c5", "c5m", "d5", "d5m", "e5",
					"f5", "f5m", "g5", "g5m", "a5", "a5m", "b5", "c6", "c6m", "d6", "d6m", "e6", "f6", "f6m", "g6", "g6m", "a6", "a6m", "b6", "c7",
					"c7m", "d7", "d7m", "e7", "f7", "f7m", "g7", "g7m", "a7", "a7m", "b7", "c8");

			config.noteLabelStyle = settingsManager.getInt(NOTE_LABEL_STYLE, 0);
			NoteLabelManager.getInstance().initLabelList();

			config.keyHeightRatio = 1f / Constant.DEFAULT_KEY_HEIGHT_RATIO;
			config.maxLevel = 20;

			config.scaleX = (float) config.winWidth / config.originalWidht;
			config.scaleY = (float) config.winHeight / config.originalHeight;
			config.winSize = CGSize.make(config.winWidth, config.winHeight);


			config.isWideTouchArea = settingsManager.getBoolean(WIDE_TOUCH_AREA, false);


			config.isShowAnimGfx = settingsManager.getBoolean(SHOW_ANIM_GFX, config.memClass < 48 ? false : true);

			config.isMagicMode = settingsManager.getBoolean(MAGIC_MODE, false);

			config.isVibrate = settingsManager.getBoolean(VIBRATE, true);

			config.vibrateTime = settingsManager.getInt(VIBRATE_TIME, 30);

			config.decayTime = settingsManager.getInt(DECAY_TIME, DECAY_TIME_LONG);

			config.isPreviewDisable = settingsManager.getBoolean(IS_PREVIEW_SONG_DISABLE, false);
			config.isHelpOn = settingsManager.getBoolean(KEY_HELP_ON, DEFAULT_VALUE_HELP_ON);

			config.guideWhiteNoteColor = settingsManager.getInt(Constant.COLOR_PICKER_GUIDE_WHITE_NOTE, 0);
			config.guideBlackNoteColor = settingsManager.getInt(Constant.COLOR_PICKER_GUIDE_BLACK_NOTE, 0);
			config.reverbOn = true;
			config.chorusOn = true;
			config.enableKeyPressedEffect = settingsManager.getBoolean(Constant.EFFECT_KEY_PRESSED_SETTING, true);
			AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			config.volumePersent = settingsManager.getInt(KEY_VOLUME_PERSENT,
					(int) (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * PERSENT_OF_ONE_VOLUME_LEVEL));
//			DeviceResolution mDeviceResolution = new DeviceResolution(context);
//			config.deviceResolution = mDeviceResolution.getRelution();
//			config.deviceWidth = mDeviceResolution.getWidth();
//			config.deviceHeight = mDeviceResolution.getHeight();

			int defaultNumKey = 10;
			if (config.deviceType == LARGE)
				defaultNumKey = 12;
			config.keyPerScreen = settingsManager.getInt(KEY_PER_SCREEN, defaultNumKey);
			config.keyPerScreenUp = settingsManager.getInt(KEY_PER_SCREEN_UP, defaultNumKey);
			config.keyPerScreenDown = settingsManager.getInt(KEY_PER_SCREEN_DOWN, defaultNumKey);
			config.keyScaleX = settingsManager.getFloat(KEY_SCALE_X, 1f);
			config.keyScaleY = settingsManager.getFloat(KEY_SCALE_Y, 1f);

			config.speedRate = settingsManager.getInt(PLAY_SPEED, 50);

			config.keyWidthWhite = config.winSize.width / config.keyPerScreen;

			config.keyHeightWhite = config.winSize.height * config.keyHeightRatio;

			config.keyWidthBlack = config.keyWidthWhite * WIDTH_BLACK_RATIO;

			config.keyHeightBlack = config.keyHeightWhite * HEIGHT_BLACK_RATIO;

			config.fullPianoSize = config.keyWidthWhite * 52;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void configWindowSize(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight();
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
			Point realSize = new Point();
			display.getRealSize(realSize);
			displayWidth = realSize.x;
			displayHeight = realSize.y;
		}
		if (displayWidth > displayHeight) {
			config.winWidth = displayWidth;
			config.winHeight = displayHeight;
		} else {
			config.winWidth = displayHeight;
			config.winHeight = displayWidth;

		}
	}

	public ccColor3B getWhiteNoteGuideColor() {
		int r = (config.guideWhiteNoteColor & 0x00ff0000) >> 16;
		int g = (config.guideWhiteNoteColor & 0x0000ff00) >> 8;
		int b = (config.guideWhiteNoteColor & 0x000000ff);
		return ccColor3B.ccc3(r, g, b);
	}

	public ccColor3B getBlackNoteGuideColor() {
		int r = (config.guideBlackNoteColor & 0x00ff0000) >> 16;
		int g = (config.guideBlackNoteColor & 0x0000ff00) >> 8;
		int b = (config.guideBlackNoteColor & 0x000000ff);
		return ccColor3B.ccc3(r, g, b);
	}

}
