package com.vtd.pianoapp.keyboard;

public interface PerformActionListener {

	void onKeyPerformed(int keyIndex, int pointerId);

	void onKeyReleased(int keyIndex, int pointerId);
}
