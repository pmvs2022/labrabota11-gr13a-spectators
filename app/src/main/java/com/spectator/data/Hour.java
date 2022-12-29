package com.spectator.data;

import com.spectator.utils.DateFormatter;

import org.json.JSONException;
import org.json.JSONObject;

public class Hour implements JsonObjectConvertable {

    public static final String ARRAY_KEY = "hours";
    public static final String hourKey = "hour";
    public static final String countKey = "count";
    public static String[] jsonKeys = new String[] {hourKey, countKey};
    public static Class[] constructorArgs = new Class[] {String.class, int.class};

    private String hour;
    private int count;

    public Hour(String hour, int count) {
        this.hour = hour;
        this.count = count;
    }
    public Hour(long timestamp, int count) {
        this.hour = extractHourFromTimestamp(timestamp);
        this.count = count;
    }

    public String getHour() {
        return hour;
    }

    public int getCount() {
        return count;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject JSONObjectHour = new JSONObject();
        try {
            JSONObjectHour.put("hour", this.getHour());
            JSONObjectHour.put("count", this.getCount());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSONObjectHour;
    }

    public static String extractHourFromTime(String formattedTime) {
        String hourString = formattedTime.substring(0,2);
        if (hourString.charAt(1) == ':' || hourString.charAt(1) == '.' ||  hourString.charAt(1) == '/')
            hourString = hourString.substring(0,1);
        else if (hourString.charAt(0) == '0') {
            hourString = hourString.substring(1,2);
        }
        return hourString;
    }

    public static String extractHourFromTimestamp(long timestamp) {
        String hourString = DateFormatter.formatTimeDefaultPattern(timestamp);
        return extractHourFromTime(hourString);
    }

}
