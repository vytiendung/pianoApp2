package com.vtd.pianoapp.keyboard;

import com.vtd.pianoapp.common.Config;

public class KeyboardWidth {
	public static final int MIN_NUM_KEY = 7;
	public static final int MAX_NUM_KEY = 20;

	public static int getActualNumKeysPerScreen() {
		return (int) (Config.getInstance().keyPerScreen / Config.getInstance().keyScaleX);
	}

	public static void saveNumKeysPerScreen(int numKeysPerScreen) {
		Config.getInstance().keyPerScreen = numKeysPerScreen;
		Config.getInstance().keyScaleX = 1f;
	}

	public static float currentScaleFactor() {
		return Config.getInstance().keyScaleX;
	}

	public static boolean equalMinWidth() {
		return (float) Config.getInstance().keyPerScreen / Config.getInstance().keyScaleX >= MAX_NUM_KEY;
	}

	public static boolean equalMaxWidth() {
		return (float) Config.getInstance().keyPerScreen / Config.getInstance().keyScaleX <= MIN_NUM_KEY;
	}

	public static float whiteKeyWidthPixels() {
		Config config = Config.getInstance();
		return config.keyScaleX * config.winWidth / config.keyPerScreen;
	}

	public static void saveByScaleFactor(float scaleFactor) {
		Config.getInstance().keyScaleX = scaleFactor;
		if (equalMinWidth()) {
			saveNumKeysPerScreen(MAX_NUM_KEY);
		} else if (equalMaxWidth()) {
			saveNumKeysPerScreen(MIN_NUM_KEY);
		}
	}

	public static int exchangeSeekBarValueToActualValue(int seekBarValue) {
		return MIN_NUM_KEY + MAX_NUM_KEY - seekBarValue;
	}
}
