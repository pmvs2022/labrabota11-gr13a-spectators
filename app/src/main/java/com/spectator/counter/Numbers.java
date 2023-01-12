package com.spectator.counter;

import android.widget.TextView;

public class Numbers {

    private int totally = 0;
    private int daily = 0;
    private int hourly = 0;

    private TextView totalText;
    private TextView dailyText;
    private TextView hourlyText;

    public Numbers() {
    }

    public void setTotalTextView(TextView totalText) {
        this.totalText = totalText;
        updateLabels();
    }
    public void setDailyTextView(TextView dailyText) {
        this.dailyText = dailyText;
        updateLabels();
    }
    public void setHourlyTextView(TextView hourlyText) {
        this.hourlyText = hourlyText;
        updateLabels();
    }

    public int getTotally() {
        return totally;
    }

    public int getDaily() {
        return daily;
    }

    public int getHourly() {
        return hourly;
    }

    public void changeAll(int amount) {
        setTotal(totally + amount);
        setDaily(daily + amount);
        setHourly(hourly + amount);
    }

    private void updateLabels() {
        if (totalText != null)
            totalText.setText(String.valueOf(totally));
        if (dailyText != null)
            dailyText.setText(String.valueOf(daily));
        if (hourlyText != null)
            hourlyText.setText(String.valueOf(hourly));
    }


    public void setTotal(int amount) {
        totally = clamp(amount, 0, Integer.MAX_VALUE);
        updateLabels();
    }

    public void setHourly(int amount) {
        hourly = clamp(amount, 0, Integer.MAX_VALUE);
        updateLabels();
    }

    public void setDaily(int amount) {
        daily = clamp(amount, 0, Integer.MAX_VALUE);
        updateLabels();
    }

    private static int clamp(int val, int min, int max) {
        if (val < min) return min;
        else return Math.min(val, max);
    }
}
