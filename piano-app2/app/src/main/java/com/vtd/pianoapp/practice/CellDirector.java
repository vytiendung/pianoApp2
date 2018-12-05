package com.vtd.pianoapp.practice;


import com.vtd.pianoapp.common.Config;

public class CellDirector {

	public static float getHeightByDuration(float durationMillis) {
		return durationMillis* Config.getInstance().speed/1000f;
	}
}
