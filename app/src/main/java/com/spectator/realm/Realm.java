package com.spectator.realm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.spectator.data.Voter;

import org.bson.types.ObjectId;

import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class Realm {
    public static String appID = "counter-guimd";
    public static App app = new App(new AppConfiguration.Builder(appID)
            .build());
    public static Credentials credentials = Credentials.anonymous();

    public Realm(String appID) {
        Realm.appID = appID;
        login(credentials);
    }

    public void login(Credentials credentials) {
        app.loginAsync(credentials, result -> {
            if (result.isSuccess()) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.");
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: " + result.getError());
            }
        });
    }

    public static void insertVoter(Voter voter) {
        User user = app.currentUser();
        SyncConfiguration config = new SyncConfiguration.Builder(user, "counter")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build();
        io.realm.Realm.getInstanceAsync(config, new io.realm.Realm.Callback() {
            @Override
            public void onSuccess(@NonNull io.realm.Realm realm) {
                Log.v("EXAMPLE", "Successfully opened a realm.");
                // Read all tasks in the realm. No special syntax required for synced realms.
                RealmResults<Voter> voters = realm.where(Voter.class).findAll();
                // Write to the realm. No special syntax required for synced realms.
                realm.executeTransaction(r -> {
                    r.insertOrUpdate(voter);
                });

                Log.v("READ", voters.asJSON());
                // Don't forget to close your realm!
                realm.close();
            }
        });
    }

}
