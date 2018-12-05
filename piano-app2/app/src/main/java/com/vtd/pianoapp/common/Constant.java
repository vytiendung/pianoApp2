package com.vtd.pianoapp.common;

public interface Constant {
	
	
	final float HEIGHT_BLACK_RATIO=0.56f;
	final float WIDTH_BLACK_RATIO=0.58333f;//0.5f;
	final float DELTA_BLACK_POS=0.058333f;
	float originWhiteKeyWidth = 100;

	public static final float DEFAULT_VOLUME=1f;
	public static final float DEFAULT_VOLUME_VALUE=127;

	public static final String NOTE_LABEL_STYLE="NOTE_NAME_STYLE";
	public static final String NOTE_LABEL_TYPE="NOTE_NAME_TYPE";

	public static final String SOUND_VOLUME_APP="sound_volume_app";
	public static final String SOUND_TIME_OF_SUSTAIN="sound_time_of_sustain";

	public static final String PLAY_ASSIST="PLAY_ASSIST";
	public static final String MAGIC_MODE ="MAGIC_MODE";
	public static final String NOTE_NAMING="NOTE_NAMING";
	public static final String NOTE_NAMING_UP="NOTE_NAMING_UP";
	public static final String NOTE_NAMING_DOWN="NOTE_NAMING_DOWN";
	public static final String VIBRATE="VIBRATE";
	public static final String VIBRATE_TIME="VIBRATE_TIME";
	public static final String DECAY_TIME="DECAY_TIME";
	public static final String OTHER_HAND="OTHER_HAND";
	public static final String KEY_PER_SCREEN="KEY_PER_SCREEN";
	public static final String KEY_PER_SCREEN_UP="KEY_PER_SCREEN_UP";
	public static final String KEY_PER_SCREEN_DOWN="KEY_PER_SCREEN_DOWN";
	public static final String PLAY_SPEED="PLAY_SPEED";
	public static final String WIDE_TOUCH_AREA="WIDE_TOUCH_AREA";
	public static final String EXTERNAL_KEYBOARD ="EXTERNAL_KEYBOARD";

	public static final String KEY_HELP_ON="HELP_ON";
	public static final boolean DEFAULT_VALUE_HELP_ON = false;
	public static final String KEY_VOLUME_PERSENT="VOLUME_PERSENT";
	
	public static final float PERSENT_OF_ONE_VOLUME_LEVEL=6.25f;
	public static final String SHOW_ANIM_GFX="SHOW_ANIM_GFX";
	public static final String IS_PREVIEW_SONG_DISABLE="IS_PREVIEW_SONG_DISABLE";

	//Graphics UI
	String EFFECT_KEY_PRESSED_SETTING = "effect_key_pressed";
	String COLOR_PICKER_GUIDE_WHITE_NOTE = "guide_white_color";
	String COLOR_PICKER_GUIDE_BLACK_NOTE = "guide_black_color";
	String LOCK_ROTATE_SCREEN = "lock_rotate_screen";

	
	
	
	
	
	
	public static final int DECAY_TIME_LONG=50;
	public static final int DECAY_TIME_NONE=0;
	
	
	/*
	 * hand
	 */
	
	public static final int LEFT_HAND=0;
	public static final int RIGHT_HAND=1;

	/*
	 * Device type
	 */
	
	public static final int SMALL=1;
	public static final int LARGE=3;

	public static final String MIDI_SHEET_DATA="MIDI_SHEET_DATA";
	public static final String MIDI_SHEET_TRACK_INDEX="MIDI_SHEET_TRACK_INDEX";
	

	final String QUICK_MENU_SELECTED = "QUICK_MENU_SELECTED";



	public static final String KEY_SCALE_X="KEY_SCALE_X";
	public static final String KEY_SCALE_Y="KEY_SCALE_Y";
	public static final String KEY_SCALE_Y_TMP="KEY_SCALE_Y_TMP";
	public static final int MAX_KEY_NUM = 20;
	public static final int MIN_KEY_NUM = 8;
	public static final int EXTRA_KEY_NUM = 8;
	public static final float MAX_KEY_HEIGHT_RATIO = 1.2f; // 1/1.2
	public static final float MIN_KEY_HEIGHT_RATIO = 2.5f;
	public static final float DEFAULT_KEY_HEIGHT_RATIO = 2;

	public static final String FORCE_LOAD_NEW_SOUND = "FORCE_LOAD_NEW_SOUND";



	public static final String IS_KEEP_SCREEN = "IS_KEEP_SCREEN";
	public static final String FINISH_ACTIVITY = "FINISH_ACTIVITY";

	public static final int PIANO_INTRUMENT_ID = 0;

	public static final int ACCURACY_EFFECT_LAYER_TAG = 16;
	public static final int SCORE_EFFECT_LAYER_TAG = 17;
	public static final int ANIM_LAYER_TAG = 18;



}
