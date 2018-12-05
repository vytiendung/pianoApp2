package com.vtd.pianoapp.game;

import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.util.CommonUtils;

import static com.vtd.pianoapp.common.Constant.PIANO_INTRUMENT_ID;

public class GameplaySetting {
	private static final String GAME_MODE_CHALLENGE = "Challenge";

	private static boolean guideNoteShowing = true;

	public static boolean isAutoPlay() {
		return false;
	}

	public static boolean hasPlayingAssistance() {
		return false;
	}


	public static int getInstrumentId() {
		return PIANO_INTRUMENT_ID;
	}

	public static boolean isNoteWaitingEnabled() {
		return !Config.getInstance().isLockScreen;
	}

	public static boolean isGuideNotesEnabled() {
		return guideNoteShowing;
	}

	public static void disableGuideNotes() {
		guideNoteShowing = false;
	}

	public static void enableGuideNotes() {
		guideNoteShowing = true;
	}

	static void increasePlayCount() {
		String keyPlayCount = "PLAY_NUMBER";
		long number = CommonUtils.getLongSetting(keyPlayCount, 0);
		number++;
		CommonUtils.saveLongSetting(keyPlayCount, number);
	}

	public static float getAnimSpeed() {
		if (isChallengeMode()) {
			Config config = Config.getInstance();
			config.speed = (config.winHeight * 0.5f) / 3.5f;
			return config.speed;
		} else {
			Config config = Config.getInstance();
			config.speed = (config.winHeight * 0.5f) / 3.5f;
			float speedRate = (config.speedRate + 50f) / 100f;
			return config.speed * speedRate;
		}
	}

	public static boolean isChallengeMode() {
		return true;
	}
}
