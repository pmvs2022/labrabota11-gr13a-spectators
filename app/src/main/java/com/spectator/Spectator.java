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
import com.spectator.realm.Task;
import com.spectator.realm.TaskStatus;
import com.spectator.utils.PreferencesIO;

import org.bson.types.ObjectId;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

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
