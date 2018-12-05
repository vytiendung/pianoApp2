package com.vtd.pianoapp.util;


public interface SettingConstant {

	//sound font setting
	String REVERB_ON_OFF = "pref_reverb_on_off";
	String REVERB_ZOOM_SIZE = "pref_reverb_zoom_size";
	String REVERB_DAMPING = "pref_reverb_damping";
	String REVERB_WIDTH = "pref_reverb_with";
	String REVERB_LEVEL = "pref_reverb_level";
	String CHORUS_ON_OFF = "pref_chorus_on_off";
	String CHORUS_VOICE_COUNT = "pref_chorus_voice_count";
	String CHORUS_LEVEL = "pref_chorus_level";
	String CHORUS_SPEED = "pref_chorus_speed";
	String CHORUS_DEPTH = "pref_chorus_depth";
	String SCREEN_REVERB = "pref_reverb";
	String SCREEN_CHORUS = "pref_chorus";
	String SCREEN_POLYPHONY_NUMBER = "pref_polyphony_number";

	//reverb constants
	float MAX_REVERB_ZOOM_SIZE = 1.2f;
	float MIN_REVERB_ZOOM_SIZE = 0;

	float MIN_REVERB_DAMPING = 0;
	float MAX_REVERB_DAMPING = 1;

	float MIN_REVERB_WIDTH = 0;
	float MAX_REVERB_WIDTH = 100;

	float MIN_REVERB_LEVEL = 0;
	float MAX_REVERB_LEVEL = 1;

	//chorus constants
	int MIN_CHORUS_VOICE_COUNT = 0;
	int MAX_CHORUS_VOICE_COUNT = 99;

	float MIN_CHORUS_LEVEL = 0;
	float MAX_CHORUS_LEVEL = 10;

	float MIN_CHORUS_SPEED = 0.29f;
	float MAX_CHORUS_SPEED = 5.0f;

	float MIN_CHORUS_DEPTH = 0.0f;
	float MAX_CHORUS_DEPTH = 21.0f;

	//UI Settings
	String VIEW_AS_TABLET = "view_as_tablet";
	String SET_DEFAULT_FOR_VIEW_AS_TABLET = "set_default_for_view_as_tablet";
}
