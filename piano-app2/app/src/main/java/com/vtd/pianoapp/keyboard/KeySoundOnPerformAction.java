package com.vtd.pianoapp.keyboard;

import android.content.Context;
import android.os.Vibrator;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.SoundManager;
import com.vtd.pianoapp.common.Config;
import com.vtd.pianoapp.common.Constant;

public class KeySoundOnPerformAction implements PerformActionListener {
	private static KeySoundOnPerformAction instance;
	private Vibrator vibrator;
	private SoundManager soundManager = SoundManager.getInstance();
	private Config config = Config.getInstance();

	public static KeySoundOnPerformAction ref() {
		if (instance == null) {
			instance = new KeySoundOnPerformAction();
		}
		return instance;
	}

	private KeySoundOnPerformAction() {
		if (vibrator == null)
			vibrator = (Vibrator) MyApplication.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public void onKeyPerformed(int keyIndex, int pointerId) {
		soundManager.playSound(Constant.PIANO_INTRUMENT_ID, keyIndex + 21, pointerId, getVolume());
		vibrate();
	}

	private float getVolume() {
		float volume = Constant.DEFAULT_VOLUME;
		return volume;
	}

	@Override
	public void onKeyReleased(int keyIndex, int pointerId) {
		soundManager.stopSound(pointerId, Constant.DEFAULT_VOLUME);
	}

	public void vibrate() {
		if (Config.getInstance().isVibrate)
			vibrator.vibrate(Config.getInstance().vibrateTime);
	}
}
