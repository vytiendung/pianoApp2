package com.vtd.pianoapp.keyboard;

class SingleKeyboardData {
	private static final String STATE_RELEASED = "KEY_RELEASED";
	private static final String STATE_PRESSED = "KEY_PRESSED";
	private static final String STATE_HINTED = "KEY_HINTED";
	private final String name;
	private final int index;
	private String state = STATE_RELEASED;
	private int repeatTimes = 0;

	SingleKeyboardData(int index, String name) {
		this.index = index;
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	boolean isPressed() {
		return state.equals(STATE_PRESSED);
	}

	boolean isHinted() {
		return state.equals(STATE_HINTED);
	}

	String getPressedSpriteName() {
		return isBlackKey() ? "black_down.png" : "white_down.png";
	}

	boolean isBlackKey() {
		return name.contains("m");
	}

	String getHintedSpriteName() {
		return isBlackKey() ? "black_hint.png" : "white_hint.png";
	}

	String getReleasedSpriteName() {
		return isBlackKey() ? "black_up.png" : "white_up.png";
	}

	int getRepeatTimes() {
		return repeatTimes;
	}

	void setRepeatTimes(int repeatTimes) {
		this.repeatTimes = repeatTimes;
	}

	void setPressed() {
		state = STATE_PRESSED;
	}

	void setReleased() {
		state = STATE_RELEASED;
	}

	void setHinted() {
		state = STATE_HINTED;
	}

	void resetRepeatTimes() {
		repeatTimes = 0;
	}
}
