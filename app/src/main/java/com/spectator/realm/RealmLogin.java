package com.spectator.realm;

import static com.spectator.realm.Realm.app;

import android.util.Log;

import io.realm.mongodb.Credentials;

public class RealmLogin {

    public RealmLogin(Credentials credentials) {
        app.loginAsync(credentials, result -> {
            if (result.isSuccess()) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.");
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: " + result.getError());
            }
        });
    }
}
