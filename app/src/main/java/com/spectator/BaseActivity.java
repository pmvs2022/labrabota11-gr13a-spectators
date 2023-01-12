package com.spectator;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.spectator.realm.Task;
import com.spectator.realm.TaskStatus;
import com.spectator.utils.PreferencesIO;

import org.bson.types.ObjectId;

import java.util.Locale;
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

public class BaseActivity extends AppCompatActivity {

    private PreferencesIO preferencesIO;
    private SharedPreferences.OnSharedPreferenceChangeListener nightListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Realm.init(this);
        App app = new App(new AppConfiguration.Builder("counter-guimd")
                .build());
        Credentials credentials = Credentials.anonymous();

        app.loginAsync(credentials, result -> {
            if (result.isSuccess()) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.");
                User user = app.currentUser();
                String partitionValue = "Counter";
                SyncConfiguration config = new SyncConfiguration.Builder(
                        user,
                        partitionValue)
                        .build();
                Realm uiThreadRealm = Realm.getInstance(config);
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: " + result.getError());
            }
        });

        /*SyncConfiguration config = new SyncConfiguration.Builder(app.currentUser(), parti)
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build();
        Realm.getInstanceAsync(config, new Realm.Callback() {
            @Override
            public void onSuccess(@NonNull Realm realm) {
                Log.v(
                        "EXAMPLE",
                        "Successfully opened a realm with reads and writes allowed on the UI thread."
                );
            }
        });*/

        preferencesIO = new PreferencesIO(this);

        /*boolean isNightMode = preferencesIO.getBoolean(PreferencesIO.IS_NIGHT_MODE, true);
        setNightTheme(isNightMode);*/

        int localeIndex = preferencesIO.getInt(PreferencesIO.LANG_RADIOBUTTON_INDEX, 1);
        setLocale(localeIndex);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void setLocale(int localeIndex) {
        Locale locale;
        //Log.e("setLocale", String.valueOf(localeIndex));
        switch (localeIndex) {
            case 0:
                locale = new Locale("en");
                break;
            case 1:
                locale = new Locale("ru");
                break;
            case 2:
                locale = new Locale("be");
                break;
            default:
                locale = new Locale("en");
        }
        Locale.setDefault(locale);
        Configuration config = this.getResources().getConfiguration();
        config.locale = locale;
        this.getResources().updateConfiguration(config,
                this.getResources().getDisplayMetrics());
        Log.e(getLocalClassName(), getLocale().toString());
    }

    private Locale getLocale() {
        return this.getResources().getConfiguration().locale;
    }
}
