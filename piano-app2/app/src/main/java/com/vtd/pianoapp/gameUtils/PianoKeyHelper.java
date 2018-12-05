package com.vtd.pianoapp.gameUtils;

import com.vtd.pianoapp.common.Constant;

public class PianoKeyHelper {
	public static boolean checkIsKeyboardC(int keyIndex){
		return (keyIndex == 4 || keyIndex == 16 || keyIndex == 28 || keyIndex == 40 || keyIndex == 52 ||
				keyIndex == 64 || keyIndex == 76 || keyIndex == 88 || keyIndex == 100);
	}

	public static boolean checkIsKeyboardC(String name) {
		return (name.contains("c") && name.length() == 2);
	}

	public static boolean isPinkLabel(String name) {
		return name.contains("0") || name.contains("3") || name.contains("6");
	}

	public static boolean isGreenLabel(String name) {
		return name.contains("1") || name.contains("4") || name.contains("7");
	}

	public static float getBlackKeyWidthFromWhiteKeyWidth(float whiteKeyWidth) {
		return whiteKeyWidth * Constant.WIDTH_BLACK_RATIO;// + (Config.getInstance().rows == Constant.PERFORM_MODE ? whiteKeyWidth / 9 : 0);
	}
}
