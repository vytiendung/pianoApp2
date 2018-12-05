package com.vtd.pianoapp.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vtd.pianoapp.MyApplication;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class SettingsManager {

	private static final String TAG = "SettingsManager";

    private static SettingsManager instance;
    private Context context;
    private SharedPreferences settingPreferences;

    private SettingsManager(Context context) {
        this.context = context;
        settingPreferences = context.getSharedPreferences(SettingsContants.APP_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        loadDefaultSetting();
    }

    public static void createWith(Context context) {
        instance = new SettingsManager(context);
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager(MyApplication.getInstance().getApplicationContext());
        }
        return instance;
    }

    private Map loadDefaultSetting() {
        Map newSettings = settingPreferences.getAll();
        Map legacySettings = loadLegacySettings();
        Set<Map.Entry> set = SettingsContants.DEFAULT_SETTINGS.entrySet();
        boolean needSaveNewSetting = false;
        boolean needSaveOldSetting = false;
        for (Map.Entry entry : set) {
            Object key = entry.getKey();
            Object defaultValue = entry.getValue();
            if (!newSettings.containsKey(key)) {
                newSettings.put(key, defaultValue);
                needSaveNewSetting = true;
            }
            if (key instanceof String) {
                if (legacySettings.containsKey(key)) {
                    Object legacyValue = legacySettings.get(key);
                    legacySettings.remove(key);
                    newSettings.put(key, legacyValue);
                    needSaveOldSetting = true;
                    needSaveNewSetting = true;
                }
            }
        }
        if (needSaveOldSetting) {
            savePreferences(PreferenceManager.getDefaultSharedPreferences(context), legacySettings);
        }
        if (needSaveNewSetting) {
            savePreferences(settingPreferences, newSettings);
        }
        return newSettings;
    }

    public Map loadLegacySettings() {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map settings = defaultPreferences.getAll();
        return settings;
    }

    private void savePreferences(SharedPreferences sharedPreferences, Map preferences) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit().clear();
            Set<Map.Entry> set = preferences.entrySet();
            for (Map.Entry entry : set) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (String.class.isInstance(key)) {
                    this.put(editor, (String) key, value);
                }
            }
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getInt(String key, int defaultValue) {
        return settingPreferences.getInt(key, defaultValue);
    }

    public void putInt(String key, int intValue) {
        settingPreferences.edit().putInt(key, intValue).commit();
    }

    public long getLong(String key, long defaultValue) {
        return settingPreferences.getLong(key, defaultValue);
    }

    public void putLong(String key, long longValue) {
        settingPreferences.edit().putLong(key, longValue).commit();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return settingPreferences.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean booleanValue) {
        settingPreferences.edit().putBoolean(key, booleanValue).commit();
    }

    public String getString(String key, String defaultValue) {
        return settingPreferences.getString(key, defaultValue);
    }

    public void putString(String key, String stringValue) {
        settingPreferences.edit().putString(key, stringValue).commit();
    }

    public float getFloat(String key, float defaultValue) {
        try {
            return settingPreferences.getFloat(key, defaultValue);
        } catch (ClassCastException ex) {
            if ( settingPreferences.contains(key) ) {
                try {
                    int value = settingPreferences.getInt(key, 0);
                    return Float.valueOf(value);
                } catch (ClassCastException ex1) {
                    long value = settingPreferences.getLong(key, 0);
                    return Float.valueOf(value);
                }
            }
        }
        return defaultValue;
    }

    public SettingsManager putFloat(String key, float floatValue) {
        settingPreferences.edit().putFloat(key, floatValue).commit();
        return this;
    }

    public void setHistorySearch(List<String> data) {
        Gson gson = new Gson();
        String jsonData = gson.toJson(data);
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putString("historySearch", jsonData);
        editor.commit();
    }

    public List<String> getHistorySearch() {
        List<String> result = new ArrayList<>();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        Gson gson = new Gson();
        String value = settingPreferences.getString("historySearch", "");
        if (value.equalsIgnoreCase("")) {
            return result;
        }
        return gson.fromJson(value, type);
    }

    private void put(SharedPreferences.Editor editor, String key, Object value) {
        if (String.class.isInstance(value)) {
            editor.putString(key, (String) value);
        } else if (Boolean.class.isInstance(value)) {
            editor.putBoolean(key, ((Boolean) value).booleanValue());
        } else if (Integer.class.isInstance(value)) {
            editor.putInt(key, ((Integer) value).intValue());
        } else if (Long.class.isInstance(value)) {
            editor.putLong(key, ((Long) value).longValue());
        } else if (Float.class.isInstance(value)) {
            editor.putFloat(key, ((Float) value).floatValue());
        } else if (Double.class.isInstance(value)) {
            editor.putFloat(key, ((Double) value).floatValue());
        }
    }

    public boolean contains(String key) {
        return settingPreferences.contains(key);
    }

    public String settingsToJSONString() {
        JSONObject settings = settingsToJSONObject();
        return settings.toString();
    }

    @NonNull
    public JSONObject settingsToJSONObject() {
        Map allEntries = settingPreferences.getAll();
        JSONObject settings = new JSONObject();
        if (allEntries != null) {
            Set<Map.Entry> set = allEntries.entrySet();
            for (Map.Entry entry : set) {
                try {
                    if (!"tree_uri".equals(entry.getKey())) {
                        settings.put((String) entry.getKey(), entry.getValue());
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return settings;
    }

    public void changePreferenceName(PreferenceManager preferenceManager) {
        preferenceManager.setSharedPreferencesName(SettingsContants.APP_SETTINGS_PREFERENCE);
        preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);
    }
}
