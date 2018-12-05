package com.vtd.pianoapp.common;

import android.app.ActivityManager;
import android.content.Context;
import com.vtd.pianoapp.MyApplication;
import com.vtd.pianoapp.util.SettingConstant;

import java.util.HashMap;
import java.util.Map;


public class SettingsContants {
    public static final String APP_SETTINGS_PREFERENCE = "appSettingsPrefrences";

    public static final Map DEFAULT_SETTINGS = new HashMap();

    static {
        Context applicationContext = MyApplication.getInstance().getApplicationContext();
        ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);

        DEFAULT_SETTINGS.put(Constant.NOTE_LABEL_TYPE, 0);
        DEFAULT_SETTINGS.put(Constant.NOTE_LABEL_STYLE, 0);
        DEFAULT_SETTINGS.put(Constant.PLAY_ASSIST, true);
        DEFAULT_SETTINGS.put(Constant.EXTERNAL_KEYBOARD, false);
        DEFAULT_SETTINGS.put(Constant.WIDE_TOUCH_AREA, false);
        DEFAULT_SETTINGS.put(Constant.NOTE_NAMING, true);
        DEFAULT_SETTINGS.put(Constant.NOTE_NAMING_UP, true);
        DEFAULT_SETTINGS.put(Constant.NOTE_NAMING_DOWN, true);
        DEFAULT_SETTINGS.put(Constant.SHOW_ANIM_GFX, activityManager.getMemoryClass() >= 48);
        DEFAULT_SETTINGS.put(Constant.MAGIC_MODE, false);
        DEFAULT_SETTINGS.put(Constant.VIBRATE, true);
        DEFAULT_SETTINGS.put(Constant.VIBRATE_TIME, 30);
        DEFAULT_SETTINGS.put(Constant.OTHER_HAND, true);
        DEFAULT_SETTINGS.put(Constant.IS_PREVIEW_SONG_DISABLE, false);
        DEFAULT_SETTINGS.put(Constant.QUICK_MENU_SELECTED, 0);
        DEFAULT_SETTINGS.put(Constant.PLAY_SPEED, 50);
        DEFAULT_SETTINGS.put(Constant.KEY_SCALE_X, 1.0f);
        DEFAULT_SETTINGS.put(Constant.KEY_SCALE_Y, 1.0f);
        DEFAULT_SETTINGS.put(Constant.IS_KEEP_SCREEN, true);
        DEFAULT_SETTINGS.put(Constant.SOUND_VOLUME_APP, 10);
        DEFAULT_SETTINGS.put(Constant.SOUND_TIME_OF_SUSTAIN, 1000);
        DEFAULT_SETTINGS.put(SettingConstant.SCREEN_POLYPHONY_NUMBER, 128);
        DEFAULT_SETTINGS.put(Constant.EFFECT_KEY_PRESSED_SETTING, true);
        DEFAULT_SETTINGS.put(Constant.LOCK_ROTATE_SCREEN, false);
        DEFAULT_SETTINGS.put(Constant.COLOR_PICKER_GUIDE_WHITE_NOTE, 0xff4adbff);
        DEFAULT_SETTINGS.put(Constant.COLOR_PICKER_GUIDE_BLACK_NOTE, 0xff00ff00);
    }

}
