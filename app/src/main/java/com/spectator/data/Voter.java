package com.spectator.data;

import com.spectator.utils.DateFormatter;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Voter extends RealmObject implements JsonObjectConvertable {

    public static final String ARRAY_KEY = "voters";
    public static final String timestampKey = "timestamp";
    public static final String dateKey = "formattedDate";
    public static final String timeKey = "formattedTime";
    public static final String countKey = "count";
    public static final String[] jsonKeys = new String[] {timestampKey, countKey};
    public static final Class[] constructorArgs = new Class[] {long.class, int.class};

    @PrimaryKey
    @Required
    private ObjectId _id = new ObjectId();
    private long timestamp;
    private String formattedDate;
    private String formattedTime;
    private int count;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public void setCount(int count) {
        this.count = count;
    }

    //class for storing info (timestamp, number and comment) about a voter
    public Voter(long timestamp, int count) {
        this.timestamp = timestamp;
        this.formattedDate = DateFormatter.formatDateDefaultPattern(timestamp);
        this.formattedTime = DateFormatter.formatTimeDefaultPattern(timestamp);
        this.count = count;
    }

    public Voter(ObjectId id, long timestamp, int count) {
        this._id = id;
        this.timestamp = timestamp;
        this.count = count;
    }

    public Voter(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getLong("timestamp"), jsonObject.getInt("count"));
    }

    public Voter(){}

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public int getCount() {
        return count;
    }

    //Creating a Json Object from Voter
    @Override
    public JSONObject toJSONObject() {
        JSONObject JSONObjectVoter = new JSONObject();
        try {
            JSONObjectVoter.put(timestampKey, this.getTimestamp());
            JSONObjectVoter.put(countKey, this.getCount());
            JSONObjectVoter.put(dateKey, this.getFormattedDate());
            JSONObjectVoter.put(timeKey, this.getFormattedTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONObjectVoter;
    }

    public String toString() {
        String str = this.getFormattedDate() + this.getCount();
        return str;
    }
}
