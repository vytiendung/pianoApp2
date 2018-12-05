package com.vtd.pianoapp.keyboard;

public class SurfaceViewConfig {
	public static final int CHALLENGE_MODE = -1;
	public static final int PRACTICE_MODE = -2;

	private int mode;

	public SurfaceViewConfig setMode(int mode) {
		this.mode = mode;
		return this;
	}

	public int getMode() {
		return mode;
	}
}
