package com.spectator.menu;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.spectator.BaseActivity;
import com.spectator.R;
import com.spectator.data.Voter;
import com.spectator.map.MapActivity;
import com.spectator.utils.PreferencesIO;

import org.bson.types.ObjectId;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class Start extends BaseActivity {

    public static App app;
    private TextView start;
    private TextView map;
    private TextView settings;
    private TextView aboutUs;
    private TextView appName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Start", "onCreate");
        setContentView(R.layout.start);

        Realm.init(this);

        start = (TextView) findViewById(R.id.start);
        map = (TextView) findViewById(R.id.map);
        settings = (TextView) findViewById(R.id.settings);
        aboutUs = (TextView) findViewById(R.id.about_us);
        appName = (TextView) findViewById(R.id.app_name);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.spectator.realm.Realm realm = new com.spectator.realm.Realm(com.spectator.realm.Realm.appID);
                realm.login(com.spectator.realm.Realm.credentials);

                Intent intentStart = new Intent(getApplicationContext(), Menu.class);
                startActivity(intentStart);
            }

        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMap = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intentMap);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentSettings = new Intent(getApplicationContext(), Settings.class);
                startActivity(intentSettings);
            }
        });

        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentAbout = new Intent(getApplicationContext(), AboutUs.class);
                startActivity(intentAbout);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("Start", "onStart");
        appName.setText(R.string.app_name);
        start.setText(R.string.start_counting);
        settings.setText(R.string.settings);
        aboutUs.setText(R.string.about_us);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Start", "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("Start", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Start", "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Start", "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("Start", "onRestart");
        //Bodge, but otherwise sometimes doesn't change language; and on old version doesn't change night mode
        recreate();
    }
}
