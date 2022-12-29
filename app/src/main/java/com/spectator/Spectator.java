package com.spectator;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.spectator.menu.Start;
import com.spectator.utils.PreferencesIO;

import java.util.Random;

public class Spectator extends Application {

    private PreferencesIO preferencesIO;
    private SharedPreferences.OnSharedPreferenceChangeListener nightListener;

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesIO = new PreferencesIO(this);

        boolean isNightMode = preferencesIO.getBoolean(PreferencesIO.IS_NIGHT_MODE, true);
        setNightTheme(isNightMode);

        /*final Context context = this;
        nightListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                Log.e("OnSharedChangeBaseNight", s);
                Log.e(getPackageName(), "package");
                if (s.equals(PreferencesIO.IS_NIGHT_MODE)) {
                    boolean isNightMode = sharedPreferences.getBoolean(s, true);
                    setNightTheme(isNightMode);

                }
            }
        };
        preferencesIO.setOnChangeListener(nightListener);*/

    }

    private void setNightTheme(boolean isNightMode) {
        Log.e("Night", String.valueOf(isNightMode));
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
