package com.spectator.data;

import com.spectator.utils.DateFormatter;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Comment extends RealmObject implements JsonObjectConvertable {

    public static final String COMMENTS_PATH = "comments.json";
    public static final String ARRAY_KEY = "comments";
    public static final String timeKey = "formattedTime";
    public static final String dateKey = "formattedDate";
    public static final String commentKey = "comment";
    public static final String[] jsonKeys = {timeKey, dateKey, commentKey};
    public static final Class[] constructorArgs = {String.class, String.class, String.class};

    @PrimaryKey
    @Required
    private ObjectId _id = new ObjectId();
    private String formattedTime;
    private String formattedDate;
    private String commentText;

    public void setId(ObjectId id) {
        this._id = id;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Comment(long timestamp, String text) {
        this.formattedTime = DateFormatter.formatTimeDefaultPattern(timestamp);
        this.formattedDate = DateFormatter.formatDateDefaultPattern(timestamp);
        this.commentText = text;
    }

    public Comment(String formattedTime, String formattedDate, String text) {
        this.formattedTime = formattedTime;
        this.formattedDate = formattedDate;
        this.commentText = text;
    }

    public Comment() {}

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put(timeKey, this.formattedTime);
            object.put(dateKey, this.formattedDate);
            object.put(commentKey, this.commentText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getCommentText() {
        return commentText;
    }

    public Comment getCommentWithChanged(String commentText) {
        return new Comment(this.formattedTime, this.formattedDate, commentText);
    }
}
