package com.spectator.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import org.bson.types.ObjectId;

public enum TaskStatus {
    Open("Open"),
    InProgress("In Progress"),
    Complete("Complete");
    String displayName;
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
}
