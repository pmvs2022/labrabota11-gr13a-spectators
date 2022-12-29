package com.spectator.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesIO {

    public static final String MY_SETTINGS = "Settings";
    public static final String IS_FIRST_TIME = "Is first time";
    public static final String LANG_RADIOBUTTON_INDEX = "Saved language button index";
    public static final String VIBE_RADIOBUTTON_INDEX = "Saved vibration button index";
    public static final String IS_NIGHT_MODE = "Night mode";
    public static final String IS_BANDS_AND_VOTERS_CONNECTED = "Is bands increase also increases voters";
    public final static String TEXT_RADIOBUTTON_INDEX = "Text size button index";

    private SharedPreferences sp;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public PreferencesIO(Context context) {
        sp = context.getSharedPreferences(MY_SETTINGS, MODE_PRIVATE);
    }

    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**be careful, now it supports only one listener!**/
    public void setOnChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        //Needed because listener can be garbage collected
        this.listener = listener;
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public void deleteOnChangeListener() {
        sp.unregisterOnSharedPreferenceChangeListener(listener);
        this.listener = null;
    }

}
