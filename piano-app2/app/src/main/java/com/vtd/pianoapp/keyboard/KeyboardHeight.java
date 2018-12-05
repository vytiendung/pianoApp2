package com.vtd.pianoapp.keyboard;


import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.common.Config;

public class KeyboardHeight {
	public static final float MIN_PERCENT = 20;
	public static final float MAX_PERCENT = 100;
	private static float minScaleFactor = -1; // depend on MIN_PERCENT
	private static float maxScaleFactor = -1; // depend on MAX_PERCENT
	private static float initHeight = -1;

	public static boolean equalMaxCurrentHeight() {
		defineScaleFactorLimit();
		return Config.getInstance().keyScaleY >= maxScaleFactor;
	}

	private static void defineScaleFactorLimit() {
		if (initHeight == -1) {
			calculateScaleFactorLimit();
		}
	}

	public static boolean equalMaxPossibleHeight() {
		defineScaleFactorLimit();
		float maxPossibleScaleFactor = 1 / Config.getInstance().keyHeightRatio;
		return Config.getInstance().keyScaleY >= maxPossibleScaleFactor;
	}

	private static void calculateScaleFactorLimit() {
		float menuHeight = 0;
		Config config = Config.getInstance();
		float maxHeight = config.winHeight - menuHeight;
		initHeight = maxHeight * config.keyHeightRatio;
		minScaleFactor = (MIN_PERCENT /100f) * maxHeight / initHeight;
		maxScaleFactor = (MAX_PERCENT /100f) * maxHeight / initHeight;
	}

	public static boolean equalMinCurrentHeight() {
		defineScaleFactorLimit();
		return Config.getInstance().keyScaleY <= minScaleFactor;
	}

	public static float currentHeightPercent() {
		float maxPossibleScaleFactor = 1 / Config.getInstance().keyHeightRatio;
		return Config.getInstance().keyScaleY * 100f / maxPossibleScaleFactor;
	}

	public static int currentHeightPixels() {
		return exchangeScaleFactorToHeightPixels(Config.getInstance().keyScaleY);
	}

	private static int exchangeScaleFactorToHeightPixels(float scaleFactor) {
		defineScaleFactorLimit();
		if (scaleFactor <= minScaleFactor) {
			return (int) (minScaleFactor * initHeight);
		} else if (scaleFactor >= maxScaleFactor) {
			return (int) (maxScaleFactor * initHeight);
		} else {
			return (int) (scaleFactor * initHeight);
		}
	}

	public static void saveByHeightPercent(float heightPercent) {
		saveByScaleFactor(exchangeHeightPercentToScaleFactor(heightPercent));
	}

	private static float exchangeHeightPercentToScaleFactor(float heightPercent) {
		float maxHeight = initHeight / Config.getInstance().keyHeightRatio;
		return (heightPercent / 100f) * maxHeight / initHeight;
	}

	public static void saveByScaleFactor(float scaleFactor) {
		defineScaleFactorLimit();
		if (scaleFactor < minScaleFactor) {
			scaleFactor = minScaleFactor;
		} else if (scaleFactor > maxScaleFactor) {
			scaleFactor = maxScaleFactor;
		}
		Config.getInstance().keyScaleY = scaleFactor;
	}

	public static float currentScaleFactor() {
		return Config.getInstance().keyScaleY;
	}

	public static void setMaxCurrentHeight(float heightPixels) {
		maxScaleFactor = heightPixels / initHeight;
	}
}
